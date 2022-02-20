package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.MoveStatus;
import com.chess.engine.board.MoveTransition;
import com.chess.engine.piece.King;
import com.chess.engine.piece.Piece;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a chess player
 */
public abstract class Player {

    protected Board board;
    protected final King playerKing;
    protected Collection<Move> legalMoves;
    private final boolean isInCheck;

    /**
     * Constructor for the player class
     * @param board is the board state
     * @param legalMoves is a list of all the current legal moves of the current player
     * @param opponentMoves is a list of all the current legal moves of the opponent of the current player
     */
    public Player(final Board board,
                  final Collection<Move> legalMoves,
                  final Collection<Move> opponentMoves) {
        this.board = board;
        this.playerKing = establishKing();
        this.legalMoves = ImmutableList.copyOf(Iterables.concat(legalMoves, calculateKingCastles(legalMoves, opponentMoves)));
        this.isInCheck = !Player.calculateAttackOnTile(this.playerKing.getPiecePosition(), opponentMoves).isEmpty();
    }

    /**
     * @param piecePosition is the coordinate of the current piece
     * @param opponentMoves is a list of all the opponent moves
     * @return a list of all the attack moves on the given piece position
     */
    public static Collection<Move> calculateAttackOnTile(int piecePosition, Collection<Move> opponentMoves) {
        final List<Move> attackMoves = new ArrayList<>();
        // Iterate over all the possible opponent moves
        for (final Move move : opponentMoves) {
            if (piecePosition == move.getDestinationCoordinate()) {
                // add an attack move if any of the possible moves have have a destination that coincides with the given piece position
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }

    /**
     * @param piecePosition is the coordinate of the current piece
     * @param opponentMoves is a list of all the opponent moves
     * @return a list of all the attack moves on the given piece position
     */
    public static Collection<Move> calculateQueenAttackOnTile(int piecePosition, Collection<Move> opponentMoves) {
        final List<Move> attackMoves = new ArrayList<>();
        // Iterate over all the possible opponent moves
        for (final Move move : opponentMoves) {
            if (piecePosition == move.getDestinationCoordinate() && move.getMovedPiece().getPieceType().isQueen()) {
                // add an attack move if any of the possible moves have have a destination that coincides with the given piece position
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }


    /**
     * @return the King of the current player
     */
    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        throw new RuntimeException("This Board is Invalid");
    }

    /**
     * @return the King
     */
    public King getPlayerKing() {
        return this.playerKing;
    }

    /**
     * @return a list of legal moves
     */
    public Collection<Move> getLegalMoves() {
        return this.legalMoves;
    }

    public void setLegal(Collection<Move> newLegalMoves) {
        this.legalMoves = newLegalMoves;
    }

    /**
     * @param move is the current move
     * @return true if the move is legal, false if illegal
     */
    public boolean isMoveLegal(Move move) {
        return this.legalMoves.contains(move);
    }

    /**
     * @return true if king is under check
     */
    public boolean isInCheck() {
        return this.isInCheck;
    }

    /**
     * @return true if king is checkmated
     */
    public boolean isInCheckMate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    /**
     * @return true if the king has an escape move, false if the king doesn't have any moves
     */
    protected boolean hasEscapeMoves() {
        for (final Move move : this.legalMoves) {
            final MoveTransition transition = makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the king is in stalemate
     */
    public boolean isInStalemate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    /**
     * @return true if the king has already castled
     */
    public boolean isCastled() {
        return false;
    }

    public boolean isKingSideCastleCapable() {
        return this.playerKing.isKingSideCastleCapable();
    }

    public boolean isQueenSideCastleCapable() {
        return this.playerKing.isQueenSideCastleCapable();
    }

    /**
     * Make a move and return a transition
     * @param move is the move being made
     * @return a move transition
     */
    public MoveTransition makeMove(final Move move) {
        if (!this.legalMoves.contains(move)) {
            return new MoveTransition(this.board, this.board, move, MoveStatus.ILLEGAL_MOVE);
        }
        final Board transitionedBoard = move.execute();
        return transitionedBoard.getCurrentPlayer().getOpponent().isInCheck() ?
                new MoveTransition(this.board, this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK) :
                new MoveTransition(this.board, transitionedBoard, move, MoveStatus.DONE);
    }

    public MoveTransition unMakeMove(final Move move) {
        return new MoveTransition(this.board, move.undo(), move, MoveStatus.DONE);
    }

    /**
     * @return the active pieces of the current player
     */
    public abstract Collection<Piece> getActivePieces();

    /**
     * @return the color of the player (WHITE/BLACK)
     */
    public abstract Alliance getAlliance();

    /**
     * @return the color of the opponent of the current player (WHITE/BLACK)
     */
    public abstract Player getOpponent();

    public abstract Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentLegals);
}