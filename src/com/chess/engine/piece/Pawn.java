package com.chess.engine.piece;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece {

    private final static int[] CANDIDATE_MOVE_COORDINATES = { 7, 8, 9, 16 };
    /**
     * This constructor is called when creating a new pawn
     * @param piecePosition is the coordinate of the piece
     * @param pieceAlliance is the color of the piece (White/Black)
     */
    public Pawn(Alliance pieceAlliance, int piecePosition) {
        super(PieceType.PAWN, pieceAlliance, piecePosition, true);
    }

    /**
     * This constructor is called when creating a new pawn move
     * @param pieceAlliance
     * @param piecePosition
     * @param isFirstMove
     */
    public Pawn(Alliance pieceAlliance, int piecePosition, boolean isFirstMove) {
        super(PieceType.PAWN, pieceAlliance, piecePosition, isFirstMove);
    }

    /**
     * This method returns a list of all the pawn moves
     * @param board is the current state of the board
     */
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            final int candidateDestinationCoordinate = this.piecePosition + (currentCandidateOffset * this.getPieceAlliance().getDirection());

            if (!BoardUtils.isValidCoordinate(candidateDestinationCoordinate)) {
                continue;
            }

            if (currentCandidateOffset == 8 && !board.getTile(candidateDestinationCoordinate).isOccupied()) {
                if (this.pieceAlliance.isPromotionSquare(candidateDestinationCoordinate)) {
                    legalMoves.add(new Move.PawnPromotion((new Move.PawnMove(board, this, candidateDestinationCoordinate))));
                }
                else {
                    legalMoves.add(new Move.PawnMove(board, this, candidateDestinationCoordinate));
                }
            }

            else if (currentCandidateOffset == 16 && this.isFirstMove() &&
                    ( (BoardUtils.INSTANCE.SECOND_ROW.get(this.piecePosition) && this.getPieceAlliance().isBlack()) ||
                            (BoardUtils.INSTANCE.SEVENTH_ROW.get(this.piecePosition) && this.getPieceAlliance().isWhite() ) ) ) {
                final int behindCandidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection() * 8);
                if (!board.getTile(behindCandidateDestinationCoordinate).isOccupied() &&
                        !board.getTile(candidateDestinationCoordinate).isOccupied()) {
                    legalMoves.add(new Move.PawnJump(board, this, candidateDestinationCoordinate));
                }
            }

            else if (currentCandidateOffset == 7 &&
                    !(((this.pieceAlliance.isWhite() && BoardUtils.INSTANCE.EIGHTH_COLUMN.get(this.piecePosition)) ||
                            (this.pieceAlliance.isBlack() && BoardUtils.INSTANCE.FIRST_COLUMN.get(this.piecePosition))))) {
                if (board.getTile(candidateDestinationCoordinate).isOccupied()) {
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()){

                        if (this.pieceAlliance.isPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new Move.PawnPromotionCapture(new Move.PawnCaptureMove(board, this, candidateDestinationCoordinate, pieceOnCandidate)));
                        }
                        else {
                            legalMoves.add(new Move.PawnCaptureMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                }
                else if (board.getEnPassantPawn() != null) {
                    // Get the en passant pawn that is next to it
                    if (board.getEnPassantPawn().getPiecePosition() == this.piecePosition + (this.pieceAlliance.getOppositeDirection())) {
                        final Piece pieceOnCandidate = board.getEnPassantPawn();
                        if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                            legalMoves.add(new Move.PawnEnPassantCaptureMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                }
            }

            else if (currentCandidateOffset == 9 &&
                    !(((this.pieceAlliance.isWhite() && BoardUtils.INSTANCE.FIRST_COLUMN.get(this.piecePosition)) ||
                            (this.pieceAlliance.isBlack() && BoardUtils.INSTANCE.EIGHTH_COLUMN.get(this.piecePosition))))) {
                if (board.getTile(candidateDestinationCoordinate).isOccupied()) {
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()){

                        if (this.pieceAlliance.isPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new Move.PawnPromotionCapture(new Move.PawnCaptureMove(board, this, candidateDestinationCoordinate, pieceOnCandidate)));
                        }
                        else {
                            legalMoves.add(new Move.PawnCaptureMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                }
                else if (board.getEnPassantPawn() != null) {
                    // Get the pawn that is next to the current pawn
                    if (board.getEnPassantPawn().getPiecePosition() == this.piecePosition - (this.pieceAlliance.getOppositeDirection())) {
                        final Piece pieceOnCandidate = board.getEnPassantPawn();
                        if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                            legalMoves.add(new Move.PawnEnPassantCaptureMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Piece movePiece(Move move) {
        return new Pawn(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    /**
     * The promotion piece is Queen by default
     */
    public Piece getPromotionPiece() {
        return new Queen(this.pieceAlliance, this.piecePosition, false);
    }

    public String toString() {
        return PieceType.PAWN.toString();
    }
}