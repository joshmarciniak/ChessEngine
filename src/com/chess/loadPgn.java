package com.chess;

import com.chess.engine.board.Board;
import com.chess.gui.Table;
import tests.com.chess.tests.BoardTest;

import java.io.File;
import java.io.IOException;

import static com.chess.pgn.PGNUtilities.persistPGNFile;

public class loadPgn {

    public static void main(String[] args) {

        String[] pathnames;
        File f = new File("C:\\Users\\Patron\\Desktop\\Java\\ChessOpenings\\openings and players");
        pathnames = f.list();

        assert pathnames != null;
        for (String pathname : pathnames) {
            loadPGNFile(new File("C:\\Users\\Patron\\Desktop\\Java\\ChessOpenings\\openings and players\\" + pathname));
        }
    }

    private static void loadPGNFile(File pgnFile) {
        try {
            persistPGNFile(pgnFile);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }


}
