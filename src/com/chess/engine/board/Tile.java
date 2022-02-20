package com.chess.engine.board;
import com.chess.engine.piece.Piece;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a single tile on a chess board
 */
public abstract class Tile {
    // The coordinate of the chess field
    protected final int tileCoordinate;

    // Creating all the possible empty tiles
    private static final Map<Integer, EmptyTile> EMPTY_TILES_CACHE = createAllPossibleEmptyTiles();

    /**
     * Constructor method for the Tile
     * @param tileCoordinate is the given tile coordinate
     */
    private Tile(int tileCoordinate) {
        this.tileCoordinate = tileCoordinate;
    }

    /**
     * @return an immutable copy of all the possible empty tiles
     */
    private static Map<Integer, EmptyTile> createAllPossibleEmptyTiles() {
        final Map<Integer, EmptyTile> emptyTileMap = new HashMap<>();

        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            emptyTileMap.put(i, new EmptyTile(i));
        }

        return ImmutableMap.copyOf(emptyTileMap);
    }

    /**
     * This method will be used to create a tile on the chess board
     * If the piece is null, it will take the empty tile, from the empty tiles hashmap
     * Otherwise it will create an occupied tile with the given piece on it
     * @param tileCoordinate is the given coordinate of the tile
     * @param piece is the given piece
     * @return the created tile
     */
    public static Tile createTile(final int tileCoordinate, final Piece piece) {
        return piece != null ? new OccupiedTile(tileCoordinate, piece) : EMPTY_TILES_CACHE.get(tileCoordinate);
    }

    /**
     * @return TRUE if this tile is occupied by a piece, FALSE if not occupied
     */
    public abstract boolean isOccupied();

    /**
     * @return the Piece that occupies the tile, null if the tile is empty
     */
    public abstract Piece getPiece();

    public int getTileCoordinate() {
        return this.tileCoordinate;
    }


    /**
     * Child class, representing an empty chess field
     * @author David
     */
    public static final class EmptyTile extends Tile {
        /**
         * Constructor method
         * @param tileCoordinate is the given coordinate of the empty tile
         */
        private EmptyTile(int tileCoordinate) {
            super(tileCoordinate);
        }

        @Override
        public String toString() {
            return "-";
        }

        @Override
        public boolean isOccupied() {
            return false;
        }

        @Override
        public Piece getPiece() {
            return null;
        }
    }

    /**
     * Child class, representing an occupied Chess field
     * @author David
     */
    public static final class OccupiedTile extends Tile {
        // This variable represents the piece that occupies the given tile
        private final Piece pieceOnTile;

        /**
         * Constructor method
         * @param tileCoordinate is the given coordinate of the occupied tile
         * @param pieceOnTile is the given piece on the tile
         */
        private OccupiedTile(int tileCoordinate, Piece pieceOnTile) {
            super(tileCoordinate);
            this.pieceOnTile = pieceOnTile;
        }

        /**
         * Black pieces are lowercase, white pieces are uppercase
         */
        @Override
        public String toString() {
            return getPiece().getPieceAlliance().isBlack() ? getPiece().toString().toLowerCase() : getPiece().toString();
        }

        @Override
        public boolean isOccupied() {
            return true;
        }

        @Override
        public Piece getPiece() {
            return this.pieceOnTile;
        }
    }
}