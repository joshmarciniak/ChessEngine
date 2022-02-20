package com.chess.engine.board;

/**
 *
 */
public enum MoveStatus {
    DONE {
        /**
         *
         * @return
         */
        @Override
        public boolean isDone() {
            return true;
        }
    },
    ILLEGAL_MOVE {
        @Override
        public boolean isDone() {
            return false;
        }
    },
    LEAVES_PLAYER_IN_CHECK {
        @Override
        public boolean isDone() {
            return false;
        }
    };

    /**
     *
     * @return
     */
    public abstract boolean isDone();
}