package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.MoveTransition;
import com.chess.engine.player.Player;

public class Minimax implements MoveStrategy {

    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;
    public Minimax(final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
    }

    @Override
    public long getNumBoardsEvaluated() {
        return 0;
    }

    @Override
    public Move execute(Board board) {

        final long startTime = System.currentTimeMillis();

        Move bestMove = null;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;

        int currentValue;

        System.out.println(board.getCurrentPlayer().toString() + "THINKING WITH DEPTH = " + this.searchDepth);

        int numMoves = board.getCurrentPlayer().getLegalMoves().size();

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                System.out.println(board.getCurrentPlayer().toString());
                if ((Player.calculateQueenAttackOnTile(move.getDestinationCoordinate(), board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty()) &&
                        (move.getMovedPiece().getPieceType().isQueen() &&
                                !Player.calculateAttackOnTile(move.getDestinationCoordinate(), board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty())) {
                    continue;
                }
                // if it is white, black player should minimize
                // else, white player should maximize
                currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min((Board) moveTransition.getTransitionBoard(), this.searchDepth - 1) :
                        max((Board) moveTransition.getTransitionBoard(), this.searchDepth - 1);

                if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                } else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;

        return bestMove;
    }

    private static boolean isMateScenario(Board board) {
        return board.getCurrentPlayer().isInCheck()
                || board.getCurrentPlayer().isInStalemate();
    }

    public int min(final Board board, int depth) {
        if (depth == 0 || isMateScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }

        int lowestSeenValue = Integer.MAX_VALUE;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max((Board) moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
            }
        }
        return lowestSeenValue;
    }

    public int max(final Board board, int depth) {
        if (depth == 0 || isMateScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }

        int highestSeenValue = Integer.MIN_VALUE;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min((Board) moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
        }
        return highestSeenValue;

    }

    @Override
    public String toString() {
        return "MiniMax";
    }


}