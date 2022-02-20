package com.chess;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.gui.Table;
import tests.com.chess.tests.BoardTest;

public class Chess {
    public static void main(String[] args) {
        Board board = Board.createStandardBoard();
        System.out.println(board);

        Table.get().show();
        BoardTest test = new BoardTest();
    }
}
