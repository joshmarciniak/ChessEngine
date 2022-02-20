package com.chess.pgn;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.Player;

public class MySqlGamePersistence implements PGNPersistence {

    private final Connection dbConnection;

    private static MySqlGamePersistence INSTANCE = new MySqlGamePersistence();

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/chess";
    private static final String USER = "postgres";
    private static final String PASS = "andersen23";
    //private static final String NEXT_BEST_MOVE_QUERY =
    //        "SELECT SUBSTR(g1.moves, LENGTH('%s') + %d, SUBSTRING(STRPOS(g1.moves, LENGTH('%s') + %d, LENGTH(g1.moves)), ',') - 1), " +
    //                "COUNT(*) FROM game g1 WHERE g1.moves LIKE '%s%%' AND (outcome = '%s') GROUP BY substr(g1.moves, LENGTH('%s') + %d, " +
    //                "INSTR(substr(g1.moves, LENGTH('%s') + %d, LENGTH(g1.moves)), ',') - 1) ORDER BY 2 DESC";
    private static final String NEXT_BEST_MOVE_QUERY =
            "SELECT (regexp_split_to_array(moves, ',\\s*'))[?] as first_moves, count(*)\n" +
                    "    FROM game\n" +
                    "    WHERE (regexp_split_to_array(moves, ',\\s*'))[:?] = cast(? as text[]) AND ((outcome = ?) OR (outcome = 'Tie'))\n" +
                    "    GROUP by first_moves ORDER BY count DESC;";


    private MySqlGamePersistence() {
        this.dbConnection = createDBConnection();
        createGameTable();
        //createIndex("outcome", "OutcomeIndex");
        //createIndex("moves", "MoveIndex");
//        createOutcomeIndex();
//        createMovesIndex();
    }

    private static Connection createDBConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASS);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MySqlGamePersistence get() {
        return INSTANCE;
    }

    @Override
    public void persistGame(final Game game) {
        executePersist(game);
    }

    @Override
    public Move getNextBestMove(final Board board,
                                final Player player,
                                final String gameText) {
        return queryBestMove(board, player, gameText);
    }

    private Move queryBestMove(final Board board,
                               final Player player,
                               final String gameText) {

        String bestMove = "";
        String count = "0";
        try {

            List<String> items = Arrays.asList(gameText.split("\\s*,\\s*"));
            final PreparedStatement gameStatement = dbConnection.prepareStatement(NEXT_BEST_MOVE_QUERY);
            gameStatement.setInt(1, items.size() + 1);
            gameStatement.setInt(2, items.size());

            Array array = dbConnection.createArrayOf("VARCHAR", items.toArray());
            gameStatement.setArray(3, array);


            String plyr = player.getAlliance().name().toLowerCase();
            plyr = plyr.substring(0,1).toUpperCase() + plyr.substring(1).toLowerCase();

            gameStatement.setString(4, plyr);

            System.out.println(gameStatement);
            final ResultSet rs2 = gameStatement.executeQuery();
            // TODO change the move picking algorithm
            if(rs2.next()) {
                bestMove = rs2.getString(1);
                count = rs2.getString(2);
            }
            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
        System.out.println("\tselected book move = " +bestMove+ " with " +count+ " hits");

        if (bestMove == null) {
            return Move.NULL_MOVE;
        }

        return PGNUtilities.createMove(board, bestMove);
    }

    private void createGameTable() {
        try {
            final Statement statement = this.dbConnection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Game(id int primary key, outcome varchar(10), moves varchar(3072));");
            statement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    private void createIndex(final String columnName,
                             final String indexName) {
        try {
            final String sqlString = "SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_CATALOG = 'def' AND " +
                    "                 TABLE_SCHEMA = DATABASE() AND TABLE_NAME = \"game\" AND INDEX_NAME = \"" +indexName+"\"";
            final Statement gameStatement = this.dbConnection.createStatement();
            gameStatement.execute(sqlString);
            final ResultSet resultSet = gameStatement.getResultSet();
            if(!resultSet.isBeforeFirst() ) {
                final Statement indexStatement = this.dbConnection.createStatement();
                indexStatement.execute("CREATE INDEX " +indexName+ " on Game(" +columnName+ ");\n");
                indexStatement.close();
            }
            gameStatement.close();
        }
        catch (final SQLException e) {
            System.out.println("CREATE INDEX " +indexName+ " on Game(" +columnName+ ");\n");
            e.printStackTrace();
        }

    }

    public int getMaxGameRow() {
        int maxId = 0;
        try {
            final String sqlString = "SELECT MAX(ID) FROM Game";
            final Statement gameStatement = this.dbConnection.createStatement();
            gameStatement.execute(sqlString);
            final ResultSet rs2 = gameStatement.getResultSet();
            if(rs2.next()) {
                maxId = rs2.getInt(1);
            }
            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
        return maxId;
    }

    private void executePersist(final Game game) {
        try {
            final String gameSqlString = "INSERT INTO Game(id, outcome, moves) VALUES(?, ?, ?);";
            final PreparedStatement gameStatement = this.dbConnection.prepareStatement(gameSqlString);
            gameStatement.setInt(1, getMaxGameRow() + 1);
            gameStatement.setString(2, game.getWinner());
            gameStatement.setString(3, game.getMoves().toString().replaceAll("\\[", "").replaceAll("\\]", ""));
            gameStatement.executeUpdate();
            gameStatement.close();
        }
        catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}