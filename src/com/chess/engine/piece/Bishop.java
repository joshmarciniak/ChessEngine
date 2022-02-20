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

/**
 * This class represents a bishop on a chess board
 */
public class Bishop extends Piece{

    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = { -9, -7, 7, 9 };
    /**
     * @param piecePosition is the coordinate of the piece
     * @param pieceAlliance is the color of the piece (White/Black)
     */
    public Bishop(Alliance pieceAlliance, int piecePosition) {
        super(PieceType.BISHOP, pieceAlliance, piecePosition, true);
    }

    public Bishop(Alliance pieceAlliance, int piecePosition, boolean isFirstMove) {
        super(PieceType.BISHOP, pieceAlliance, piecePosition, isFirstMove);
    }

    /**
     * @param board is the current state of the board
     * @return a Collection of Legal Moves the bishop can make
     */
    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final int candidateCoordinateOffset: CANDIDATE_MOVE_VECTOR_COORDINATES) {
            int candidateDestinationCoordinate = this.piecePosition;

            while(BoardUtils.isValidCoordinate(candidateDestinationCoordinate)) {
                if (isFirstColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset) ||
                        isEighthColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset)) {
                    break;
                }

                candidateDestinationCoordinate += candidateCoordinateOffset;

                if(BoardUtils.isValidCoordinate(candidateDestinationCoordinate)) {
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
                        break;
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Bishop movePiece(Move move) {
        return new Bishop(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    /**
     *
     */
    public String toString() {
        return PieceType.BISHOP.toString();
    }

    /**
     * This method checks for edge cases for when the bishop is in the first column
     * @param currentPosition
     * @param candidateOffset
     * @return
     */
    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.INSTANCE.FIRST_COLUMN.get(currentPosition) && (candidateOffset == 7 || candidateOffset == -9);
    }

    /**
     *
     * @param currentPosition
     * @param candidateOffset
     * @return
     */
    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.INSTANCE.EIGHTH_COLUMN.get(currentPosition) && (candidateOffset == -7 || candidateOffset == 9);
    }
}