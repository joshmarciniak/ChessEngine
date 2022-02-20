package com.chess.engine.board;

import com.chess.engine.piece.King;
import com.chess.engine.piece.Pawn;
import com.chess.engine.piece.Piece;
import com.chess.engine.piece.Rook;

import java.util.Objects;

/**
 * This class represents chess moves
 */
public abstract class Move {

    // This variable represents the given state of the board
    protected final Board board;
    protected final Piece movedPiece;
    protected final int destinationCoordinate;
    protected final boolean isFirstMove;

    public static final Move NULL_MOVE = new NullMove();

    /**
     * Constructor for Move
     * @param board The board during this move
     * @param movedPiece The piece that makes this move
     * @param destinationCoordinate The board field coordinate where it lands
     */
    private Move(final Board board, final Piece movedPiece, final int destinationCoordinate) {
        boolean isFirstMove1;
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;

        // If there no moved piece, set the isFirstMove to false
        try {
            isFirstMove1 = movedPiece.isFirstMove();
        }
        catch (NullPointerException e) {
            isFirstMove1 = false;
        }

        this.isFirstMove = isFirstMove1;
    }

    private Move(final Board board, final int destinationCoordinate) {
        this.board = board;
        this.movedPiece = null;
        this.destinationCoordinate = destinationCoordinate;
        this.isFirstMove = false;
    }

    public Board getBoard() {
        return this.board;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + this.destinationCoordinate;

        try {
            result = prime * result + this.movedPiece.hashCode();
            result = prime * result + this.movedPiece.getPiecePosition();
        }
        catch (Exception ignore) {

        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return destinationCoordinate == move.destinationCoordinate && Objects.equals(board, move.board) && Objects.equals(movedPiece, move.movedPiece) && ((Move) o).getCurrentCoordinate() == getCurrentCoordinate();
    }

    /**
     * @return the current coordinate of the moved piece
     */
    public int getCurrentCoordinate() {
        return this.movedPiece.getPiecePosition();
    }

    public int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public Piece getMovedPiece() {
        return this.movedPiece;
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() {
        return false;
    }

    public Piece getAttackedPiece() {
        return null;
    }

    /**
     * Undo the last move
     * @return a new board
     */
    public Board undo() {
        final Board.Builder builder = new Board.Builder();
        this.board.getAllPieces().forEach(builder::setPiece);
        builder.setMoveMaker(this.board.getCurrentPlayer().getAlliance());
        return builder.build();
    }

    /**
     * This method executes an actual move on the board
     * @return a freshly built board with the updated move on it
     */
    public Board execute() {
        final Board.Builder builder = new Board.Builder();
        for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
            if (!this.movedPiece.equals(piece)) {
                builder.setPiece(piece);
            }
        }
        for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }

        // move the moved piece
        builder.setPiece(this.movedPiece.movePiece(this));
        builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
        builder.setMoveTransition(this);

        // This call rebuilds the board in the updated state and returns it
        // The two updates are the new .setPiece and .setMoveMaker calls
        return builder.build();
    }

    /**
     * This class represents a normal move, one where no captures happen
     */
    public static final class NormalMove extends Move {
        public NormalMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof NormalMove && super.equals(other);
        }

        @Override
        public String toString() {
            return movedPiece.getPieceType().toString() + BoardUtils.addPieceOffsetFile(board, this) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate)
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    /**
     * This class represents a capture move
     */
    public static class CaptureMove extends Move {
        // The piece which was captured by this move
        final Piece attackedPiece;

        public CaptureMove(Board board, Piece movedPiece, int destinationCoordinate, Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public int hashCode() {
            return this.attackedPiece.hashCode() + super.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            CaptureMove that = (CaptureMove) o;
            return super.equals(that) && Objects.equals(attackedPiece, that.attackedPiece);
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public Piece getAttackedPiece() {
            return this.attackedPiece;
        }

        @Override
        public String toString() {
            return movedPiece.getPieceType().toString() + BoardUtils.addPieceOffsetFile(board, this) + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate)
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }

    }

    /**
     * This class represents a pawn move
     * @author David
     */
    public static final class PawnMove extends Move {

        /**
         * Constructor for Move
         *
         * @param board                 The board during this move
         * @param movedPiece            The piece that makes this move
         * @param destinationCoordinate The board field coordinate where it lands
         */
        public PawnMove(Board board, Piece movedPiece, int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        public boolean equals(Object other) {
            return this == other || other instanceof PawnMove && super.equals(other);
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.getDestinationCoordinate())
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    /**
     * This class represents a pawn capture move
     * @author David
     */
    public static class PawnCaptureMove extends CaptureMove {

        public PawnCaptureMove(Board board, Piece movedPiece, int destinationCoordinate, Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnCaptureMove && super.equals(other);
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.movedPiece.getPiecePosition()).charAt(0) + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate)
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    /**
     * This class represents an en passant move
     * @author David
     */
    public static final class PawnEnPassantCaptureMove extends PawnCaptureMove {

        public PawnEnPassantCaptureMove(Board board, Piece movedPiece, int destinationCoordinate, Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        public boolean equals(final Object other) {
            return this == other || other instanceof PawnEnPassantCaptureMove && super.equals(other);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                if (!piece.equals(this.getAttackedPiece())) {
                    builder.setPiece(piece);
                }
            }

            // move the moved piece
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);

            // This call rebuilds the board in the updated state and returns it
            // The two updates are the new .setPiece and .setMoveMaker calls
            return builder.build();
        }
    }

    /**
     * This class represents a pawn jump move
     * For white pieces from e2-e4
     * For black pieces from e7-e5
     * @author David
     */
    public static final class PawnJump extends Move {
        /**
         * Constructor for Move
         *
         * @param board                 The board during this move
         * @param movedPiece            The piece that makes this move
         * @param destinationCoordinate The board field coordinate where it lands
         */
        public PawnJump(Board board, Piece movedPiece, int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            final Pawn movedPawn = (Pawn)this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.getDestinationCoordinate())
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    /**
     * This class represents a Pawn Promotion move
     * @author David
     */
    public static class PawnPromotion extends Move {
        final Move decoratedMove;
        final Pawn promotedPawn;

        public PawnPromotion(final Move decoratedMove) {
            super(decoratedMove.getBoard(), decoratedMove.getMovedPiece(), decoratedMove.getDestinationCoordinate());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn) decoratedMove.getMovedPiece();
        }

        @Override
        public int hashCode() {
            return decoratedMove.hashCode() + (31 * promotedPawn.hashCode());
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnPromotion && super.equals(other);
        }

        @Override
        public Board execute() {
            final Board pawnMovedBoard = this.decoratedMove.execute();

            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getActivePieces()) {
                if (!this.promotedPawn.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            builder.setPiece(this.promotedPawn.getPromotionPiece().movePiece(this));

            builder.setMoveMaker(pawnMovedBoard.getCurrentPlayer().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }

        public boolean isAttack() {
            return this.decoratedMove.isAttack();
        }

        @Override
        public Piece getAttackedPiece() {
            return this.decoratedMove.getAttackedPiece();
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "=Q"
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    public static class PawnPromotionCapture extends PawnPromotion {

        public PawnPromotionCapture(Move decoratedMove) {
            super(decoratedMove);
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.movedPiece.getPiecePosition()).charAt(0) + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "=Q"
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    /**
     * This class represents a castle move
     * @author David
     */
    static abstract class CastleMove extends Move {

        protected final Rook castleRook;
        protected final int castleRookStart;
        protected final int castleRookDestination;

        public CastleMove(final Board board,
                          final Piece movedPiece,
                          final int destinationCoordinate,
                          final Rook castleRook,
                          final int castleRookStart,
                          final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        public Rook getCastleRook() {
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    if (!this.castleRook.equals(piece)) {
                        builder.setPiece(piece);
                    }
                }
            }
            for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setPiece(new Rook(this.castleRook.getPieceAlliance(), this.castleRookDestination));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            builder.setMoveTransition(this);
            return builder.build();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.castleRook.hashCode();
            result = prime * result + this.castleRookDestination;
            return result;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof CastleMove)) {
                return false;
            }

            final CastleMove otherCastleMove = (CastleMove) other;
            return super.equals(otherCastleMove) && this.castleRook.equals(otherCastleMove.getCastleRook());
        }
    }

    /**
     * This class represents castling with the H1 rook
     * @author David
     */
    public static final class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(final Board board,
                                  final Piece movedPiece,
                                  int destinationCoordinate,
                                  final Rook castleRook,
                                  final int castleRookStart,
                                  final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof KingSideCastleMove && super.equals(other);
        }

        @Override
        public String toString() {
            return "O-O"
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    /**
     * This class represents castling with the A8 rook
     * @author David
     */
    public static final class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(final Board board,
                                   final Piece movedPiece,
                                   int destinationCoordinate,
                                   final Rook castleRook,
                                   final int castleRookStart,
                                   final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof QueenSideCastleMove && super.equals(other);
        }

        @Override
        public String toString() {
            return "O-O-O"
                    + BoardUtils.addCheckSign(board, getCurrentCoordinate(), destinationCoordinate);
        }
    }

    /**
     * This class represents a null move
     */
    public static final class NullMove extends Move {

        /**
         * Constructor for Move
         *
         */
        private NullMove() {
            super(null, null, -1);
        }

        @Override
        public Board execute() {
            throw new RuntimeException("Cannot Execute The Null Move");
        }

        @Override
        public int getCurrentCoordinate() {
            return -1;
        }
    }

    /**
     * This class represents move fabrication
     * @author David
     */
    public static class MoveFactory {
        private MoveFactory() {
            throw new RuntimeException("Not Instantiable");
        }

        /**
         *
         * @param board
         * @param currentCoordinate
         * @param destinationCoordinate
         * @return
         */
        public static Move createMove(final Board board, final int currentCoordinate,final int destinationCoordinate) {
            for (final Move move : board.getAllLegalMoves()) {
                if (move.getCurrentCoordinate() == currentCoordinate &&
                        move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }
            return NULL_MOVE;
        }

        public static Move getNullMove() {
            return NULL_MOVE;
        }
    }
}