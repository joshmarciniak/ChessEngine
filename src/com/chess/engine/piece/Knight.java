package com.chess.engine.piece;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Knight extends Piece{
    // The numbers here implement the logic of knight moves
    private final static int[] CANDIDATE_MOVE_COORDINATES = { -17, -15, -10, -6, 6, 10, 15, 17 };

    /**
     *
     * @param piecePosition is the position of the knight
     * @param pieceAlliance is the color of the knight (White/Black)
     */
    public Knight(Alliance pieceAlliance, int piecePosition) {
        super(PieceType.KNIGHT, pieceAlliance, piecePosition, true);
    }

    public Knight(Alliance pieceAlliance, int piecePosition, boolean isFirstMove) {
        super(PieceType.KNIGHT, pieceAlliance, piecePosition, isFirstMove);
    }

    /**
     *
     * @param board is the state of the board
     * @return a list of all the possible knight moves
     */
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        // List of Legal Moves
        final List<Move> legalMoves = new ArrayList<>();

        // Iterate over all the candidates
        for (final int currentCandidate: CANDIDATE_MOVE_COORDINATES) {
            // The destination is coordinate is the current position + the candidate move
            int candidateDestinationCoordinate = this.piecePosition + currentCandidate;

            // Checking for exclusions
            if (isFirstColumnExclusion(this.piecePosition, currentCandidate) || isSecondColumnExclusion(this.piecePosition, currentCandidate) ||
                    isSeventhColumnExclusion(this.piecePosition, currentCandidate) || isEighthColumnExclusion(this.piecePosition, currentCandidate)) {
                continue;
            }

            // Check if the offspring coordinate is valid
            if (BoardUtils.isValidCoordinate(candidateDestinationCoordinate)) {
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                // If no other pieces exist on this tile, just add this to the set of legal moves
                if (!candidateDestinationTile.isOccupied()) {
                    legalMoves.add(new Move.NormalMove(board, this, candidateDestinationCoordinate));
                }

                // See if the tile is occupied by a piece of the same color
                else {
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();

                    // If it is a piece of an opposite color, create a capture move
                    if (this.pieceAlliance != pieceAlliance) {
                        legalMoves.add(new Move.CaptureMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Knight movePiece(Move move) {
        return new Knight(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
    }

    /**
     *
     */
    public String toString() {
        return PieceType.KNIGHT.toString();
    }

    /**
     *
     * @param currentPosition
     * @param candidateOffset
     * @return
     */
    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.INSTANCE.FIRST_COLUMN.get(currentPosition) && (candidateOffset == -17 || candidateOffset == -10 ||
                candidateOffset == 6 || candidateOffset == 15);
    }

    /**
     *
     * @param currentPosition
     * @param candidateOffset
     * @return
     */
    private static boolean isSecondColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.INSTANCE.SECOND_COLUMN.get(currentPosition) && (candidateOffset == -10 || candidateOffset == 6);

    }

    /**
     *
     * @param currentPosition
     * @param candidateOffset
     * @return
     */
    private static boolean isSeventhColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.INSTANCE.SEVENTH_COLUMN.get(currentPosition) && (candidateOffset == 10 || candidateOffset == -6);
    }

    /**
     *
     * @param currentPosition
     * @param candidateOffset
     * @return
     */
    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.INSTANCE.EIGHTH_COLUMN.get(currentPosition) && (candidateOffset == 17 || candidateOffset == 10 ||
                candidateOffset == -6 || candidateOffset == -15);
    }
}