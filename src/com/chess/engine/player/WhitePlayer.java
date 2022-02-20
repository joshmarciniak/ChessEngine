package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.piece.Piece;
import com.chess.engine.piece.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents the White Player
 */
public class WhitePlayer extends Player {
    public WhitePlayer(final Board board, final Collection<Move> whiteStandardLegalMoves, final Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.board = board;
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    public Collection<Move> calculateKingCastles(final Collection<Move> playerLegals, final Collection<Move> opponentLegals) {
        final List<Move> kingCastles = new ArrayList<>();

        if (this.playerKing.isFirstMove() && !this.isInCheck()) {
            if (!this.board.getTile(61).isOccupied() && !this.board.getTile(62).isOccupied()) {
                final Tile rookTile = this.board.getTile(63);

                if (rookTile.isOccupied() && rookTile.getPiece().isFirstMove()) {
                    if (Player.calculateAttackOnTile(61, opponentLegals).isEmpty()
                            && Player.calculateAttackOnTile(62, opponentLegals).isEmpty()
                            && rookTile.getPiece().getPieceType().isRook()) {
                        kingCastles.add(new Move.KingSideCastleMove(this.board,
                                this.playerKing,
                                62,
                                (Rook)rookTile.getPiece(),
                                rookTile.getTileCoordinate(),
                                61));
                    }
                }
            }

            if (!this.board.getTile(59).isOccupied() &&
                    !this.board.getTile(58).isOccupied() &&
                    !this.board.getTile(57).isOccupied()) {

                if (Player.calculateAttackOnTile(59, opponentLegals).isEmpty() &&
                        Player.calculateAttackOnTile(58, opponentLegals).isEmpty()) {
                    final Tile rookTile = this.board.getTile(56);

                    if (rookTile.isOccupied() && rookTile.getPiece().isFirstMove()) {
                        if (rookTile.getPiece().getPieceType().isRook()) {
                            kingCastles.add(new Move.QueenSideCastleMove(this.board,
                                    this.playerKing,
                                    58,
                                    (Rook)rookTile.getPiece(),
                                    rookTile.getTileCoordinate(),
                                    59));
                        }
                    }
                }
            }
        }

        return ImmutableList.copyOf(kingCastles);
    }
}