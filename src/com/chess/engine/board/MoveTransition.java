package com.chess.engine.board;

/**
 *
 */
public class MoveTransition {

    private final Board fromBoard;
    private final Board toBoard;
    private final Move move;
    private final MoveStatus moveStatus;

    /**
     *
     * @param fromBoard
     * @param toBoard
     * @param move
     * @param moveStatus
     */
    public MoveTransition(final Board fromBoard,
                          final Board toBoard,
                          final Move move,
                          final MoveStatus moveStatus) {
        this.fromBoard = fromBoard;
        this.toBoard = toBoard;
        this.move = move;
        this.moveStatus = moveStatus;
    }

    public MoveStatus getMoveStatus() {
        return this.moveStatus;
    }

    public Board getFromBoard() {
        return this.fromBoard;
    }

    public Board getToBoard() {
        return this.toBoard;
    }

    public Board getTransitionBoard() {
        return this.toBoard;
    }
}