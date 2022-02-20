package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.piece.*;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a chess board
 */
public class Board {

    // This list of tiles constitutes a chessboard
    private final List<Tile> gameBoard;
    private final Map<Integer, Piece> boardConfig;

    // This list of pieces constitutes the active pieces that
    // are white and black respectively
    private Collection<Piece> whitePieces;
    private Collection<Piece> blackPieces;

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private Player currentPlayer;

    // The move that was made during the given board setup
    private final Move transitionMove;

    // En Passant Pawn is set to null, unless a pawn jump was made
    private final Pawn enPassantPawn;

    /**
     * This constructor uses the board builder pattern to build a board
     * @param builder is the baord builder
     */
    Board(final Builder builder) {

        this.boardConfig = Collections.unmodifiableMap(builder.boardConfig);

        // Create a game board
        this.gameBoard = createGameBoard(builder);

        // List the active pieces
        this.whitePieces = calculateActivePieces(this.gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, Alliance.BLACK);
        this.enPassantPawn = builder.enPassantPawn;

        // List the legal moves
        final Collection<Move> whiteStandardLegalMoves = calculateLegalMoves(whitePieces);
        final Collection<Move> blackStandardLegalMoves = calculateLegalMoves(blackPieces);

        // Create instances of white and black players
        this.whitePlayer = new WhitePlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.blackPlayer = new BlackPlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);

        // Use a polymorphism trick
        // Where White player chooses the white player to be the next move maker and vice versa
        this.currentPlayer = builder.nextMoveMaker.choosePlayer(this.whitePlayer, this.blackPlayer);

        // Get the transition move
        // If no transition move could be created, get a null move
        this.transitionMove = builder.transitionMove != null ? builder.transitionMove : Move.MoveFactory.getNullMove();
    }

    /**
     * @return a formatted string that represents the board in ASCII
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            final String tileText = this.gameBoard.get(i).toString();
            builder.append(String.format("%3s", tileText));
            if ((i + 1) % BoardUtils.NUM_TILES_PER_ROW == 0) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * @param pieces is a list of all the active pieces
     * @return a list of all the legal moves
     */
    private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) {
        final List<Move> legalMoves = new ArrayList<>();

        // Iterate over every single piece
        for (final Piece piece : pieces) {
            // Calculate the legal moves for a given piece
            // It will return a collection of all the legal moves possible to make with the current piece
            // Then add all the collections to get a list of all the legal moves
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }
        // System.out.println("There are " + legalMoves.size());
        // return the list
        return ImmutableList.copyOf(legalMoves);
    }

    /**
     * @param gameBoard is a list of all the tiles in the board
     * @param alliance is a color (White/Black)
     * @return a collection of pieces that have the given alliance and are on the tiles.
     */
    private static Collection<Piece> calculateActivePieces(final List<Tile>gameBoard, final Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();

        // Iterate over all the tiles
        for (final Tile tile : gameBoard) {
            // Once the tile is occupied
            if (tile.isOccupied()) {
                final Piece piece = tile.getPiece();
                // Check if the piece on the current tile has the given alliance
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                }
            }
        }

        // System.out.println("There are " + activePieces.size() + " for " + alliance.name());
        return ImmutableList.copyOf(activePieces);
    }

    /**
     * @param tileCoordinate is a coordinate
     * @return the tile with the given coordinate
     */
    public Tile getTile(int tileCoordinate) {
        return gameBoard.get(tileCoordinate);
    }

    public Move getTransitionMove() {
        return this.transitionMove;
    }

    /**
     * @param builder is the builder pattern that created the board
     * @return a list of tiles with their associated piece
     */
    private static List<Tile> createGameBoard(final Builder builder) {
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];
        // Iterate over every tile
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            // Create the tile

            // 1) If in the board configuration (boardConfig), the i'th tile doesn't have a piece
            // an empty tile will be created
            // 2) If there is a piece, an occupied tile will be created.
            tiles[i] = Tile.createTile(i, builder.boardConfig.get(i));
        }
        return ImmutableList.copyOf(tiles);
    }

    /**
     * @return a board with all the pieces on it
     */
    public static Board createStandardBoard() {
        final Builder builder = new Builder();
        // Black Layout

        builder.setPiece(new Rook(Alliance.BLACK, 0));
        builder.setPiece(new Knight(Alliance.BLACK, 1));
        builder.setPiece(new Bishop(Alliance.BLACK, 2));
        builder.setPiece(new Queen(Alliance.BLACK, 3));
        builder.setPiece(new King(Alliance.BLACK, 4, true, true));
        builder.setPiece(new Bishop(Alliance.BLACK, 5));
        builder.setPiece(new Knight(Alliance.BLACK, 6));
        builder.setPiece(new Rook(Alliance.BLACK, 7));
        builder.setPiece(new Pawn(Alliance.BLACK, 8));
        builder.setPiece(new Pawn(Alliance.BLACK, 9));
        builder.setPiece(new Pawn(Alliance.BLACK, 10));
        builder.setPiece(new Pawn(Alliance.BLACK, 11));
        builder.setPiece(new Pawn(Alliance.BLACK, 12));
        builder.setPiece(new Pawn(Alliance.BLACK, 13));
        builder.setPiece(new Pawn(Alliance.BLACK, 14));
        builder.setPiece(new Pawn(Alliance.BLACK, 15));

        // White Layout
        builder.setPiece(new Pawn(Alliance.WHITE, 48));
        builder.setPiece(new Pawn(Alliance.WHITE, 49));
        builder.setPiece(new Pawn(Alliance.WHITE, 50));
        builder.setPiece(new Pawn(Alliance.WHITE, 51));
        builder.setPiece(new Pawn(Alliance.WHITE, 52));
        builder.setPiece(new Pawn(Alliance.WHITE, 53));
        builder.setPiece(new Pawn(Alliance.WHITE, 54));
        builder.setPiece(new Pawn(Alliance.WHITE, 55));
        builder.setPiece(new Rook(Alliance.WHITE, 56));
        builder.setPiece(new Knight(Alliance.WHITE, 57));
        builder.setPiece(new Bishop(Alliance.WHITE, 58));
        builder.setPiece(new Queen(Alliance.WHITE, 59));
        builder.setPiece(new King(Alliance.WHITE, 60, true, true));
        builder.setPiece(new Bishop(Alliance.WHITE, 61));
        builder.setPiece(new Knight(Alliance.WHITE, 62));
        builder.setPiece(new Rook(Alliance.WHITE, 63));

        //white to move
        builder.setMoveMaker(Alliance.WHITE);
        //build the board
        return builder.build();
    }

    /**
     * @return a combined list of all the legal move (WHITE + BLACK)
     */
    public Iterable<Move> getAllLegalMoves() {
        return Iterables.unmodifiableIterable(Iterables.concat(this.whitePlayer.getLegalMoves(), this.blackPlayer.getLegalMoves()));
    }

    /**
     * @return all the active white pieces
     */
    public Collection<Piece> getWhitePieces() {
        return this.whitePieces;
    }

    /**
     * @return all the active black pieces
     */
    public Collection<Piece> getBlackPieces() {
        return this.blackPieces;
    }

    public Collection<Piece> getAllPieces() {
        return Stream.concat(this.whitePieces.stream(),
                this.blackPieces.stream()).collect(Collectors.toList());
    }

    /**
     * @return the black player
     */
    public Player blackPlayer() {
        return this.blackPlayer;
    }

    /**
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    // public void changeCurrentPlayer() { this.currentPlayer = currentPlayer.getOpponent(); }

    public Piece getPiece(final int coordinate) {
        return this.boardConfig.get(coordinate);
    }

    /**
     * @return the white player
     */
    public Player whitePlayer() {
        return this.whitePlayer;
    }

    public void setCurrentPlayer(Player opponent) {
        this.currentPlayer = opponent;
    }

    public Pawn getEnPassantPawn() {
        return this.enPassantPawn;
    }

    /**
     * A builder design pattern that creates and returns a board.
     */
    public static class Builder {

        public Move transitionMove;
        // This map represents the board
        Map<Integer, Piece> boardConfig;
        Alliance nextMoveMaker;
        Pawn enPassantPawn;

        /**
         * Constructor that initializes the board configuration hashmap
         */
        public Builder() {
            this.boardConfig = new HashMap<>();
        }

        /**
         * @param piece is a given piece
         * @return a board after putting the pieces on the board
         */
        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        /**
         *
         * @param nextMoveMaker is the color of the player that makes the next move in this board
         */
        public Builder setMoveMaker(Alliance nextMoveMaker) {
            this.nextMoveMaker = nextMoveMaker;
            return this;
        }

        /**
         *
         * @return the board
         */
        public Board build() {
            return new Board(this);
        }

        public void setEnPassantPawn(Pawn movedPawn) {
            this.enPassantPawn = movedPawn;
        }

        public void setMoveTransition(final Move transitionMove) {
            this.transitionMove = transitionMove;
        }
    }
}
