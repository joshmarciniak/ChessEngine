package com.chess.engine.openings;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * This class contains helper functions that set up the connection with the database
 */
public class OpeningsDatabase {

    /**
     * @param boardsetup is a given boardsetup
     * @return the candidate moves according to the openings database
     */
    public static Set<Move> getBestMove(final Board boardsetup) {
        String boardToString = boardsetup.toString().replaceAll("\\s","");

        // This is the list of the best moves that are kept in the database
        Set<Move> listOfBestMoves = new HashSet<>();
        PreparedStatement statement;
        Connection conn;

        try {
            conn = DriverManager.getConnection
                    ("jdbc:postgresql://localhost:5432/chess","postgres", "andersen23");

            statement = conn.prepareStatement(
                    "SELECT boardsetup, bestmove from openings");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String x = rs.getString(1).replaceAll("\\s","");;
                String y = rs.getString(2);

                if (x.equals(boardToString)) {
                    for (final Move move : boardsetup.getCurrentPlayer().getLegalMoves()) {
                        if (move.toString().equals(y)) {
                            listOfBestMoves.add(move);
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return listOfBestMoves;
    }

    /**
     * This method gets the best single move randomly
     */
    public static Move getBestSingleMove(final Board boardsetup) {
        String boardToString = boardsetup.toString().replaceAll("\\s","");
        ArrayList<Move> listOfBestMoves = new ArrayList<>();
        PreparedStatement statement;
        Connection conn;

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/chess","postgres", "andersen23");

            statement = conn.prepareStatement(
                    "SELECT boardsetup, bestmove from openings");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String x = rs.getString(1).replaceAll("\\s","");;
                String y = rs.getString(2);

                if (x.equals(boardToString)) {
                    for (final Move move : boardsetup.getCurrentPlayer().getLegalMoves()) {
                        if (move.toString().equals(y)) {
                            listOfBestMoves.add(move);
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        // If the list is not empty return a random move in the given list
        return !listOfBestMoves.isEmpty() ? listOfBestMoves.get(new Random().nextInt(listOfBestMoves.size())) : null;
    }

    /**
     * Insert the baord and the move into the database
     * @param board is the given board
     * @param move is the given move
     */
    public static void SqlInsert(String board, String move) {

        PreparedStatement statement;
        Connection conn;

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/chess","postgres", "andersen23");

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO openings(boardsetup, bestmove) VALUES (?, ?)");

            stmt.setString(1, board);
            stmt.setString(2, move);
            System.out.println("System updated");
            stmt.executeUpdate();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
