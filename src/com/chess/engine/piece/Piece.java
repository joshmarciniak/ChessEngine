package com.chess.engine.piece;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.Collection;

/**
 * This class represents a chess piece
 */
public abstract class Piece {
    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    /**
     *
     * @param piecePosition is the coordinate of the piece
     * @param pieceAlliance is the color of the piece (White/Black)
     */
    public Piece(final PieceType pieceType, final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.pieceAlliance = pieceAlliance;
        this.piecePosition = piecePosition;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

    public int computeHashCode() {
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    /**
     * @return the color of the piece (White/Black)
     */
    public Alliance getPieceAlliance() {
        return this.pieceAlliance;
    }

    /**
     * @param board is the current state of the board
     * @return a Collection of Legal Moves the piece can make
     */
    public abstract Collection<Move> calculateLegalMoves(final Board board);

    public abstract Piece movePiece(Move move);

    /**
     * This method moves the given piece
     * @param  is the move
     * @return a new piece that is exactly the same as the piece that made the move, but
     * with an updated position
     */

    public int getPiecePosition() {
        return this.piecePosition;
    }
    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return piecePosition == piece.piecePosition && isFirstMove == piece.isFirstMove && pieceType == piece.pieceType && pieceAlliance == piece.pieceAlliance;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    public enum PieceType {

        PAWN("P") {
            @Override
            public boolean isKing() {
                return false;
            }
            public boolean isRook() {
                return false;
            }

            @Override
            public int getPieceValue() {
                return 100;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        KNIGHT("N") {
            @Override
            public boolean isKing() {
                return false;
            }
            public boolean isRook() {
                return false;
            }

            @Override
            public int getPieceValue() {
                return 300;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        BISHOP("B") {
            @Override
            public boolean isKing() {
                return false;
            }
            public boolean isRook() {
                return false;
            }

            @Override
            public int getPieceValue() {
                return 350;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        ROOK("R") {
            @Override
            public boolean isKing() {
                return false;
            }
            public boolean isRook() {
                return true;
            }

            @Override
            public int getPieceValue() {
                return 500;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        QUEEN("Q") {
            @Override
            public boolean isKing() {
                return false;
            }
            public boolean isRook() {
                return false;
            }

            @Override
            public int getPieceValue() {
                return 1100;
            }

            @Override
            public boolean isQueen() {
                return true;
            }
        },
        KING("K") {
            @Override
            public boolean isKing() {
                return true;
            }
            public boolean isRook() {
                return false;
            }

            @Override
            public int getPieceValue() {
                return 10000;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        };

        private final String pieceName;

        PieceType(final String pieceName) {
            this.pieceName = pieceName;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        public abstract boolean isKing();
        public abstract boolean isRook();
        public abstract int getPieceValue();

        public abstract boolean isQueen();
    }
}