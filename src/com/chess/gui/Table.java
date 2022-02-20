package com.chess.gui;

import com.chess.engine.board.*;
import com.chess.engine.openings.OpeningsDatabase;
import com.chess.engine.piece.Pawn;
import com.chess.engine.piece.Piece;
import com.chess.engine.player.Player;
import com.chess.engine.player.ai.Minimax;
import com.chess.engine.player.ai.MoveStrategy;
import com.chess.engine.player.ai.StockAlphaBeta;
import com.chess.pgn.FenUtilities;
import com.chess.pgn.MySqlGamePersistence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

//import static com.chess.pgn.PGNUtilities.persistPGNFile;
import static com.chess.pgn.PGNUtilities.persistPGNFile;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

/**
 * This is the main GUI class
 */
public class Table extends Observable {

    private final JFrame gameFrame;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    private Board chessBoard;

    private boolean highlightLegalMoves = true;

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;

    private Move computerMove;

    // Sets up screen dimension
    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(800, 650);

    // Sets up board dimension
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(600, 550);

    public static int TILE_SIZE = 50;
    // Sets up board tile dimension
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(TILE_SIZE, TILE_SIZE);
    private final static String defaultPieceImagePath = "art/pieces/";

    private static final Table INSTANCE = new Table();
    private boolean useBook;

    /**
     * Constructor for the GUI
     */
    public Table() {
        // Sets the name of the window
        this.gameFrame = new JFrame("Chess");

        // This layout sets the borders
        this.gameFrame.setLayout(new BorderLayout());

        // Creating a menu bar to place in borders
        final JMenuBar tableMenuBar = createTableMenuBar();

        // Setting the menu bard on the screen
        this.gameFrame.setJMenuBar(tableMenuBar);

        this.useBook = false;

        // Setting the screen size
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);

        // Setting this to true, so that the window would appear
        this.gameFrame.setVisible(true);

        // Creating the chessboard
        this.chessBoard = Board.createStandardBoard();
        this.boardDirection = BoardDirection.NORMAL;

        // Initialize additional panels
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();

        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.addObserver(new TableGameAIWatcher());

        // Adding the board panel and setting the board in center
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);

        this.gameSetup = new GameSetup(this.gameFrame, true);

        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
    }

    public static Table get() {
        return INSTANCE;
    }

    public void show() {
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    /**
     * This method creates a menu bar
     * And adds a file menu to it
     * @return the menu bar
     */
    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    /**
     * @return a file menu
     */
    private JMenu createFileMenu() {
        // Bar name is file
        final JMenu fileMenu = new JMenu("File");

        // First item is loading a PGN file
        final JMenuItem openPGN = new JMenuItem("Load PGN File", KeyEvent.VK_O);
        openPGN.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int option = chooser.showOpenDialog(Table.get().getGameFrame());
            if (option == JFileChooser.APPROVE_OPTION) {
                loadPGNFile(chooser.getSelectedFile());
            }
        });
        fileMenu.add(openPGN);

        final JMenuItem openFEN = new JMenuItem("Load FEN File", KeyEvent.VK_F);
        openFEN.addActionListener(e -> {
            String fenString = JOptionPane.showInputDialog("Input FEN");
            if(fenString != null) {
                undoAllMoves();
                chessBoard = FenUtilities.createGameFromFEN(fenString);
                Table.get().getBoardPanel().drawBoard(chessBoard);
            }
        });
        fileMenu.add(openFEN);

        // Second item is exiting the window
        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            // Status code is 0
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private void loadPGNFile(File pgnFile) {
        try {
            persistPGNFile(pgnFile);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private JFrame getGameFrame() {
        return this.gameFrame;
    }

    private JMenu createPreferencesMenu() {

        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);

        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight Legal Moves", true);

        legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected();
            }
        });

        preferencesMenu.add(legalMoveHighlighterCheckbox);

        return preferencesMenu;
    }

    private JMenu createOptionsMenu() {
        final JMenu optionsMenu = new JMenu("Options");

        final JMenuItem resetMenuItem = new JMenuItem("New Game", KeyEvent.VK_P);
        resetMenuItem.addActionListener(e -> undoAllMoves());
        optionsMenu.add(resetMenuItem);

        final JMenuItem undoMoveMenuItem = new JMenuItem("Undo last move", KeyEvent.VK_M);
        undoMoveMenuItem.addActionListener(e -> {
            if(Table.get().getMoveLog().size() > 0) {
                undoLastMove();
            }
        });
        optionsMenu.add(undoMoveMenuItem);

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });

        optionsMenu.add(setupGameMenuItem);

        return optionsMenu;
    }

    private void undoAllMoves() {
        for(int i = Table.get().getMoveLog().size() - 1; i >= 0; i--) {
            final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
            this.chessBoard = this.chessBoard.getCurrentPlayer().unMakeMove(lastMove).getToBoard();
        }
        this.computerMove = null;
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
    }

    /**
     * This function takes back the most recent move
     */
    private void undoLastMove() {
        final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
        this.chessBoard = this.chessBoard.getCurrentPlayer().unMakeMove(lastMove).getToBoard();
        this.computerMove = null;
        Table.get().getMoveLog().removeMove(lastMove);
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
    }

    private void setupUpdate(final GameSetup gameSetup) {
        setChanged();
        notifyObservers(gameSetup);
    }

    private static class TableGameAIWatcher implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().getCurrentPlayer()) &&
                    !Table.get().getGameBoard().getCurrentPlayer().isInCheckMate() &&
                    !Table.get().getGameBoard().getCurrentPlayer().isInStalemate()) {
                System.out.println(Table.get().getGameBoard().getCurrentPlayer() + " is set to AI, thinking....");
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            if (Table.get().getGameBoard().getCurrentPlayer().isInCheckMate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().getCurrentPlayer() + " is in checkmate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            if (Table.get().getGameBoard().getCurrentPlayer().isInStalemate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().getCurrentPlayer() + " is in stalemate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void updateGameBoard(final Board board) {
        this.chessBoard = board;
    }

    public void updateComputerMove(final Move move) {
        this.computerMove = move;
    }

    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private void moveMadeUpdate(final PlayerType playerType) {
        setChanged();
        notifyObservers(playerType);
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {

        private AIThinkTank() {

        }

        @Override
        protected Move doInBackground() throws Exception {
            //final Collection<Move> bestBookMoveList;
            //Move bestBookMove = OpeningsDatabase.getBestSingleMove(Table.get().getGameBoard());

            //if (bestBookMove != null) {
            //    return bestBookMove;
            //}

            // Set the list of legal moves to the candidate moves found in the database
            // bestBookMoveList = OpeningsDatabase.getBestMove(Table.get().getGameBoard());
            //if (!bestBookMoveList.isEmpty()) {
            //    Table.get().getGameBoard().getCurrentPlayer().setLegal(bestBookMoveList);
            //}

            final Move bestMove;
            final Move bookMove = Table.get().getUseBook()
                    ? MySqlGamePersistence.get().getNextBestMove(Table.get().getGameBoard(),
                    Table.get().getGameBoard().getCurrentPlayer(),
                    Table.get().getMoveLog().getMoves().toString().replaceAll("\\[", "").replaceAll("]", ""))
                    : Move.MoveFactory.getNullMove();
            if (Table.get().getUseBook() && bookMove != Move.MoveFactory.getNullMove()) {
                bestMove = bookMove;
            }
            else {
                final StockAlphaBeta strategy = new StockAlphaBeta(Table.get().getGameSetup().getSearchDepth());

                bestMove = strategy.execute(Table.get().getGameBoard());
                System.out.println(bestMove.toString());
            }

            return bestMove;
        }

        @Override
        public void done() {
            try {
                final Move bestMove = get();

                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().getCurrentPlayer().makeMove(bestMove).getTransitionBoard());
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);


            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean getUseBook() {
        return this.useBook;
    }

    public enum BoardDirection {
        NORMAL {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    /**
     * This class is a board panel
     * It has a 8x8 layout
     * It adds 64 tiles to the list BoardTiles
     */
    private class BoardPanel extends JPanel {
        // List of tile panels
        final List<TilePanel> boardTiles;

        BoardPanel() {
            // Calling a grid layout class to create a chess board
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            int kingPosition = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {

                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }

            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board) {
            removeAll();
            for (final TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    public static class MoveLog {

        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        public void addMove(final Move move) {
            this.moves.add(move);
            BoardUtils.playAudio();
        }

        public int size() {
            return this.moves.size();
        }

        public void clear() {
            this.moves.clear();
        }

        public Move removeMove(int index) {
            return this.moves.remove(index);
        }

        public boolean removeMove(final Move move) {
            return this.moves.remove(move);
        }
    }

    enum PlayerType {
        HUMAN,
        COMPUTER
    }


    /**
     * This class represents a Tile Panel
     */
    private class TilePanel extends JPanel {
        // The id of a given tile
        private final int tileID;
        private int oldX, oldY, newMouseX, newMouseY;

        TilePanel (final BoardPanel boardPanel, final int tileID) {
            // GridBagLayout is a sophisticated layout manager
            // It aligns components by placing then within a grid of cells
            super(new GridBagLayout());
            this.tileID = tileID;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {

                    if (isRightMouseButton(e)) {
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    }
                    else if (isLeftMouseButton(e)) {
                        if (sourceTile == null) {
                            // first click
                            sourceTile = chessBoard.getTile(tileID);
                            humanMovedPiece = sourceTile.getPiece();
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            }

                        }
                        else {
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getPiece().getPiecePosition(), tileID);
                            final MoveTransition transition = chessBoard.getCurrentPlayer().makeMove(move);

                            //if (!(move instanceof Move.NullMove)) {
                            //   OpeningsDatabase.SqlInsert(chessBoard.toString(), move.toString());
                            //}
                            //System.out.println((chessBoard.toString() + " " + move.toString()));
                            if (transition.getMoveStatus().isDone()) {
                                chessBoard = transition.getToBoard();
                                moveLog.addMove(move);
                            }
                            sourceTile = null;
                            destinationTile = null;
                            humanMovedPiece = null;

                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                gameHistoryPanel.redo(chessBoard, moveLog);
                                takenPiecesPanel.redo(moveLog);

                                if (gameSetup.isAIPlayer(chessBoard.getCurrentPlayer())) {
                                    Table.get().moveMadeUpdate(PlayerType.HUMAN);
                                }

                                boardPanel.drawBoard(chessBoard);
                            }
                        });
                    }

                }

                @Override
                public void mousePressed(MouseEvent e) {
                    /**
                     * if (isLeftMouseButton(e)) {
                     *                         sourceTile = chessBoard.getTile(tileID);
                     *                         humanMovedPiece = sourceTile.getPiece();
                     *                         oldX = e.getX();
                     *                         oldY = e.getY();
                     *                     }
                     */

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    /**
                     * if (isLeftMouseButton(e)) {
                     *                         int x = e.getX();
                     *                         int y = e.getY();
                     *
                     *                         int finalDestination = sourceTile.getPiece().getPiecePosition() + (8 * (Math.round((float) (oldY - y) / 100))) + (1 * (Math.round( (float) (oldX - x)) / 100) );
                     *                         System.out.println("OLD: " + oldX + " " + oldY);
                     *                         System.out.println("NEW: " + x + " " + y);
                     *                         final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getPiece().getPiecePosition(), finalDestination);
                     *                         final MoveTransition transition = chessBoard.getCurrentPlayer().makeMove(move);
                     *                         if (transition.getMoveStatus().isDone()) {
                     *                             chessBoard = transition.getToBoard();
                     *                             moveLog.addMove(move);
                     *                         }
                     *                         sourceTile = null;
                     *                         destinationTile = null;
                     *                         humanMovedPiece = null;
                     *
                     *                         SwingUtilities.invokeLater(new Runnable() {
                     *                             @Override
                     *                             public void run() {
                     *                                 gameHistoryPanel.redo(chessBoard, moveLog);
                     *                                 takenPiecesPanel.redo(moveLog);
                     *                                 boardPanel.drawBoard(chessBoard);
                     *                             }
                     *                         });
                     *                     }
                     */

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            validate();
        }

        public void drawTile(final Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            validate();
            repaint();
            highlightLegals(board);

        }

        /**
         * This method assigns a chess piece picture to a given tile
         * @param board is the given board
         */
        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileID).isOccupied()) {
                try {
                    // Load the image of the piece that occupies this tile
                    final BufferedImage image = ImageIO.read(new File(defaultPieceImagePath + board.getTile(this.tileID).getPiece().getPieceAlliance().toString().substring(0, 1) +
                            board.getTile(this.tileID).getPiece().toString() + ".gif"));
                    // Add a new Image to the tile grid, which coincides with the i'th tile on a chess board
                    add(new JLabel(new ImageIcon(image)));
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to upload");
                }
            }
        }

        /**
         * This method highlights the legal moves for the piece that was clicked
         * @param board is the given board
         */
        private void highlightLegals(final Board board) {
            if (highlightLegalMoves) {
                for (final Move move : pieceLegalMoves(board)) {
                    if (move.getDestinationCoordinate() == this.tileID) {
                        if ( !sourceTile.getPiece().getPieceType().isKing() && move.isCastlingMove()) {
                            continue;
                        }
                        final MoveTransition transition = chessBoard.getCurrentPlayer().makeMove(move);
                        if (!transition.getMoveStatus().isDone()) {
                            // getKingTilePanel(board, this.boardPanel).setBackground(Color.red);
                            continue;
                        }

                        try {
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        /**
         * @param board is the given board
         * @return a list of all the legal moves
         */
        private Collection<Move> pieceLegalMoves(final Board board) {
            if (humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.getCurrentPlayer().getAlliance()) {
                // return a list of all the legal moves + the castle moves
                return ImmutableList.copyOf(Iterables.concat ( humanMovedPiece.calculateLegalMoves(board),
                        chessBoard.getCurrentPlayer().calculateKingCastles(chessBoard.getCurrentPlayer().getLegalMoves(),
                                chessBoard.getCurrentPlayer().getOpponent().getLegalMoves()) ));
            }
            return Collections.emptyList();
        }

        /**
         * This method assigns color to a given chess tile
         * Lower Left (A1 field is dark colored)
         */
        private void assignTileColor() {
            if (BoardUtils.INSTANCE.FIRST_ROW.get(this.tileID) ||
                    BoardUtils.INSTANCE.THIRD_ROW.get(this.tileID) ||
                    BoardUtils.INSTANCE.FIFTH_ROW.get(this.tileID) ||
                    BoardUtils.INSTANCE.SEVENTH_ROW.get(this.tileID) ) {

                setBackground(this.tileID % 2 == 0 ? Color.lightGray : new Color(68, 151, 179));
            }

            if (BoardUtils.INSTANCE.SECOND_ROW.get(this.tileID) ||
                    BoardUtils.INSTANCE.FOURTH_ROW.get(this.tileID) ||
                    BoardUtils.INSTANCE.SIXTH_ROW.get(this.tileID) ||
                    BoardUtils.INSTANCE.EIGHTH_ROW.get(this.tileID) ) {

                setBackground(this.tileID % 2 == 0 ? new Color(68, 151, 179) : Color.lightGray);
            }
        }
    }
}