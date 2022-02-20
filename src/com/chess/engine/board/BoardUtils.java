package com.chess.engine.board;

import com.chess.engine.piece.Piece;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.*;

/**
 * This class contains helper variables and functions for the board
 */
public enum BoardUtils {

    INSTANCE;

    // Creating an array of booleans to check for exception for the given move
    public final List<Boolean> FIRST_COLUMN = initColumn(0); // A file
    public final List<Boolean> SECOND_COLUMN = initColumn(1); // B file
    public final List<Boolean> THIRD_COLUMN = initColumn(2); // C file
    public final List<Boolean> FOURTH_COLUMN = initColumn(3); // D file
    public final List<Boolean> FIFTH_COLUMN = initColumn(4); // E file
    public final List<Boolean> SIXTH_COLUMN = initColumn(5); // F file
    public final List<Boolean> SEVENTH_COLUMN = initColumn(6); // G file
    public final List<Boolean> EIGHTH_COLUMN = initColumn(7); // H file

    public final List<Boolean> FIRST_ROW = initRow(0); // 8th rank
    public final List<Boolean> SECOND_ROW = initRow(8); // 7th rank
    public final List<Boolean> THIRD_ROW = initRow(16); // 6th rank
    public final List<Boolean> FOURTH_ROW = initRow(24); // 5th rank
    public final List<Boolean> FIFTH_ROW = initRow(32); // 4th rank
    public final List<Boolean> SIXTH_ROW = initRow(40); // 3rd rank
    public final List<Boolean> SEVENTH_ROW = initRow(48); // 2nd rank
    public final List<Boolean> EIGHTH_ROW = initRow(56); // 1st rank

    // Constants for the number of tiles
    public static final int START_TILE_INDEX = 0;
    public static final int NUM_TILES = 64;
    public static final int NUM_TILES_PER_ROW = 8;

    public static final List<String> ALGEBRAIC_NOTATION = initializeAlgebraicNotation();

    /**
     * @return a list of strings that set the addresses of a given chess board
     */
    private static List<String> initializeAlgebraicNotation() {
        return Collections.unmodifiableList(Arrays.asList(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"));
    }

    public static final Map<String, Integer> POSITION_TO_COORDINATE = initializePositionToCoordinate();

    /**
     * This method uses the Algebraic notation method
     * to map an array element to its algebraic notation
     * EX: (A8 - 0), (B8 - 1), (H1 - 63)
     * @return a Map containing Strings to Integers
     */
    private static Map<String, Integer> initializePositionToCoordinate() {
        final Map<String, Integer> positionToCoordinate = new HashMap<>();
        for (int i = START_TILE_INDEX; i < NUM_TILES; i++) {
            positionToCoordinate.put(ALGEBRAIC_NOTATION.get(i), i);
        }
        return Collections.unmodifiableMap(positionToCoordinate);
    }

    /**
     * @param board is a given board
     * @return true if any of the players are in check
     */
    public static boolean isThreatenedBoardImmediate(final Board board) {
        return board.whitePlayer().isInCheck() || board.blackPlayer().isInCheck();
    }

    /**
     * @param columnNumber is the given column number
     * @return a list of boolean numbers, where true matches with the given list
     * element only if the element is in the given columnNumber
     */
    private static List<Boolean> initColumn(int columnNumber) {
        final Boolean[] column = new Boolean[NUM_TILES];
        for(int i = 0; i < column.length; i++) {
            column[i] = false;
        }
        do {
            column[columnNumber] = true;
            columnNumber += NUM_TILES_PER_ROW;
        } while(columnNumber < NUM_TILES);

        // Return an immutable list
        return Collections.unmodifiableList(Arrays.asList((column)));
    }

    /**
     * Same as the columns except for rows
     * @param rowNumber
     * @return
     */
    private static List<Boolean> initRow(int rowNumber) {
        final Boolean[] row = new Boolean[NUM_TILES];
        for(int i = 0; i < row.length; i++) {
            row[i] = false;
        }
        do {
            row[rowNumber] = true;
            rowNumber++;
        } while(rowNumber % NUM_TILES_PER_ROW != 0);

        // Return an immutable list
        return Collections.unmodifiableList(Arrays.asList(row));
    }

    /**
     * This method is used by the StockAlphaBeta.MoveSorter to sort the moves by their priority
     * @param move is the given move
     * @return a number that sets the given hierarchy.
     * 1. capture (attack) moves
     * 2. pawn moves
     * 3. Knight moves
     * 4. Bishop Moves
     * 5. Rook moves
     * 6. Queen Moves
     */
    public static int mvvlva(final Move move) {
        final Piece movingPiece = move.getMovedPiece();
        if(move.isAttack()) {
            final Piece attackedPiece = move.getAttackedPiece();
            return (attackedPiece.getPieceType().getPieceValue() - movingPiece.getPieceType().getPieceValue() +  Piece.PieceType.KING.getPieceValue()) * 100;
        }
        return Piece.PieceType.KING.getPieceValue() - movingPiece.getPieceType().getPieceValue();
    }

    /**
     * @param board is the given state of the board
     * @param N is the number of moves you want to go back
     * @return a list of the past N moves in the given board
     */
    public static List<Move> lastNMoves(final Board board, int N) {
        final List<Move> moveHistory = new ArrayList<>();
        Move currentMove = board.getTransitionMove();
        int i = 0;
        while(currentMove != Move.MoveFactory.getNullMove() && i < N) {
            moveHistory.add(currentMove);
            currentMove = currentMove.getBoard().getTransitionMove();
            i++;
        }
        return Collections.unmodifiableList(moveHistory);
    }

    /**
     * @param board is the given state of the board
     * @return true if the game is finished
     */
    public static boolean isEndGame(final Board board) {
        return board.getCurrentPlayer().isInCheckMate() ||
                board.getCurrentPlayer().isInStalemate();
    }

    /**
     *
     * @param coordinate is a given coordinate
     * @return false if the coordinate is out of bounds, true otherwise
     */
    public static boolean isValidCoordinate(final int coordinate) {
        return coordinate >= 0 && coordinate < 64;
    }

    /**
     * @param position is a given int positions
     * @return 0 if position='a8'
     */
    public static int getCoordinateAtPosition(final String position) {
        return POSITION_TO_COORDINATE.get(position);
    }

    /**
     * @param coordinate is a given coordinate
     * @return 'a8' if coordinate is 0
     */
    public static String getPositionAtCoordinate(final int coordinate) {
        return ALGEBRAIC_NOTATION.get(coordinate);
    }

    /**
     * @param move is the given move
     * @return true if the given move threatens the opponent's king
     */
    public static boolean kingThreat(final Move move) {
        final Board board = move.getBoard();
        final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
        return transition.getToBoard().getCurrentPlayer().isInCheck();
    }

    /**
     * This method plays an audio
     * It is only called when a move is made on the board
     */
    public static void playAudio() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("Audio/move_sound.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch(Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }

    /**
     * Adds a check or a mate sign at the end of the move notation
     * @param board current state of the board
     * @param currentCoordinate offset of the move
     * @param destinationCoordinate destination of the move
     * @return "+" if the move is a check move, "#" if the move is a mate move
     */
    public static String addCheckSign(final Board board, int currentCoordinate, int destinationCoordinate) {
        final Move move = Move.MoveFactory.createMove(board, currentCoordinate, destinationCoordinate);
        final MoveTransition transition = board.getCurrentPlayer().makeMove(move);

        if (transition.getMoveStatus().isDone()) {
            final Board newBoard = move.execute();
            if (newBoard.getCurrentPlayer().isInCheckMate()) {
                return "#";
            } else if (newBoard.getCurrentPlayer().isInCheck()) {
                return "+";
            }
        }
        return "";
    }

    /**
     * This method returns the file name of the piece that made the given move
     * If another piece of the same type can make a move to the same destination
     * @param board is the current state of the board
     * @param move is a given move
     * @return the file name of the piece that made the move
     */
    public static String addPieceOffsetFile(final Board board, final Move move) {
        final Collection<Move> allMoves = board.getCurrentPlayer().getLegalMoves();

        for (final Move move1 : allMoves) {
            if ((move1.getMovedPiece().getPieceType() == move.getMovedPiece().getPieceType())
                    && (move.getDestinationCoordinate() == move1.getDestinationCoordinate())
                    &&  (!move.equals(move1))) {
                return getPositionAtCoordinate(move.getCurrentCoordinate()).substring(0, 1);
            }
        }
        return "";
    }
}