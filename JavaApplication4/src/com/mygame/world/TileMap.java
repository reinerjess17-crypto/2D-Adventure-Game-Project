package com.mygame.world;

import com.mygame.main.GameSettings;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TileMap {

    // ---------------------------------------------------------------
    // TILE TYPES: names for the numbers used in the map file.
    // "byte" because the map is stored as bytes. Changing a number
    // later only needs to happen here.
    // ---------------------------------------------------------------
    public static final byte WATER = 0;
    public static final byte SAND = 1;
    public static final byte LAND = 2;

    // ---------------------------------------------------------------
    // FIELDS
    // ---------------------------------------------------------------

    // The world: one byte per tile. map[row][col]
    private final byte[][] map;

    // Size of the map measured in tiles. These now come FROM the file,
    // so the map can be any size without changing the code.
    private final int mapCols;
    private final int mapRows;

    // Tile pictures. The index matches the tile number:
    // tileImages[0] = water, [1] = sand, [2] = land
    private final BufferedImage[] tileImages = new BufferedImage[3];

    // Describes where numbers are split in a line: by commas and/or spaces.
    // Created once here (instead of for every line) because it is reused.
    private static final Pattern SEPARATOR = Pattern.compile("[,;\\s]+");

    // ---------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------

    // mapPath = where the map file is, for example "/res/maps/world.csv"
    public TileMap(String mapPath) {
        this.map = loadMapFile(mapPath);// read the file into the grid
        this.mapRows = map.length;// number of lines = number of rows
        this.mapCols = map[0].length;// length of the first line = number of columns
        loadTileImages();// load the pictures once
    }

    // ---------------------------------------------------------------
    // SIZE IN PIXELS (used by the camera and the player)
    // ---------------------------------------------------------------
    public int getWidthInPixels() { return mapCols * GameSettings.TILE_SIZE; }
    public int getHeightInPixels() { return mapRows * GameSettings.TILE_SIZE; }

    // Map size in TILES (the pixel sizes above are these times TILE_SIZE)
    public int getCols() { return mapCols; }
    public int getRows() { return mapRows; }

    // Which tile type is at this column and row? (WATER, SAND or LAND)
    // The caller must give a column and row inside the map.
    public byte getTile(int col, int row) { return map[row][col]; }

    // ---------------------------------------------------------------
    // MAP LOADING
    // ---------------------------------------------------------------

    // Reads the map file and turns it into a grid of bytes.
    private byte[][] loadMapFile(String path) {
        // We don't know the number of rows in advance, so we collect each
        // row in a list first and convert it to an array at the end.
        List<byte[]> rows = new ArrayList<>();
        
        // "try (...)" closes the file automatically when we are done
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Map file not found: " + path);
            }

            // BufferedReader lets us read the file one line at a time
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            int lineNumber = 0;  // used in error messages

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Some programs put an invisible marker at the very start of the file.
                // Remove it BEFORE trim(), because trim() does not remove it.
                if (lineNumber == 1 && !line.isEmpty() && line.charAt(0) == '\uFEFF'){
                    line = line.substring(1);
                }
                line = line.trim(); // remove spaces around the line
                if (line.isEmpty()) continue; // skip blank lines

                // Split "0,1,2,2" into the pieces "0", "1", "2", "2"
                String[] pieces = SEPARATOR.split(line);
                byte[] row = new byte[pieces.length];

                for (int col = 0; col < pieces.length; col++) {
                    byte tile = Byte.parseByte(pieces[col]); // text -> number

                    // Catch bad data NOW, with a clear message, instead of
                    // crashing later while drawing.
                    if (tile < 0 || tile >= tileImages.length) {
                        throw new IOException("Unknown tile number " + tile
                                + " at line " + lineNumber + ", column " + (col + 1));
                    }
                    row[col] = tile;
                }

                // Every row must be as long as the first one
                if (!rows.isEmpty() && row.length != rows.get(0).length) {
                    throw new IOException("Line " + lineNumber + " has " + row.length
                            + " tiles but the first line has " + rows.get(0).length);
                }
                rows.add(row);
            }
        } catch (IOException | NumberFormatException e) {
            // Without a map there is no game, so we stop with a clear message
            // instead of continuing in a broken state.
            throw new IllegalStateException("Could not load map: " + e.getMessage(), e);
        }

        if (rows.isEmpty()) {
            throw new IllegalStateException("Map file is empty: " + path);
        }
        // Convert the list of rows into the final grid
        return rows.toArray(new byte[0][]);
    }

    // ---------------------------------------------------------------
    // IMAGE LOADING (once at startup, never inside draw)
    // ---------------------------------------------------------------
 
    // Loads all tile pictures from res/tiles into memory.
    // The file names must match your real files exactly
    private void loadTileImages() {
        tileImages[WATER] = loadImage("/res/tiles/TileMap_Water.png");
        tileImages[SAND] = loadImage("/res/tiles/TileMap_sand.png");
        tileImages[LAND] = loadImage("/res/tiles/TileMap_Grass.png");
    }

    // Loads ONE image. Returns null on failure so the game keeps running
    // and draw() can show a magenta warning tile.
    private BufferedImage loadImage(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Image not found: " + path);
            }
            return ImageIO.read(stream); // reads the PNG into a BufferedImage
        } catch (IOException e) {
            e.printStackTrace(); // shows the problem in the console
            return null;
        }
    }

    // ---------------------------------------------------------------
    // DRAWING (only the part the camera can see)
    // ---------------------------------------------------------------

    // g2              = the drawing tool for the hidden canvas
    // cameraX/cameraY = WORLD position of the canvas's top-left corner
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        // Step 1: which tiles are visible? Pixel position / tile size = tile number.
        // Math.max / Math.min keep the numbers inside the map so we never read
        // outside the array (which would crash).
        int firstCol = Math.max(0, cameraX / GameSettings.TILE_SIZE);
        int firstRow = Math.max(0, cameraY / GameSettings.TILE_SIZE);
        int lastCol = Math.min(mapCols - 1,
                (cameraX + GameSettings.BASE_WIDTH) / GameSettings.TILE_SIZE);
        int lastRow = Math.min(mapRows - 1,
                (cameraY + GameSettings.BASE_HEIGHT) / GameSettings.TILE_SIZE);
        
        // Step 2: draw only those tiles (about 240 per frame, however big the world is).
        // "<=" so the partly visible last row and column are drawn too.
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {

                // WORLD position of the tile minus the camera = SCREEN position
                int screenX = col * GameSettings.TILE_SIZE - cameraX;
                int screenY = row * GameSettings.TILE_SIZE - cameraY;

                BufferedImage image = tileImages[map[row][col]];// Find the picture that belongs to this tile's number

                if (image != null) {
                    g2.drawImage(image, screenX, screenY, null);
                } else {
                    // Image failed to load: bright magenta so the problem is obvious
                    g2.setColor(Color.MAGENTA);
                    g2.fillRect(screenX, screenY,
                            GameSettings.TILE_SIZE, GameSettings.TILE_SIZE);
                }
            }
        }
    }
    //----------------------------------------------------------------
    //COLLISION
    //----------------------------------------------------------------

    // Is the point at this WORLD position (in pixels) blocked?
    public boolean isSolidAtPixel(double worldX, double worldY) {
    // pixel -> tile. Math.floor rounds DOWN, which is correct for negatives too
    // negative numbers. (Plain integer division would turn -5/32 into 0, not -1.)
        int col = (int) Math.floor(worldX / GameSettings.TILE_SIZE);
        int row = (int) Math.floor(worldY / GameSettings.TILE_SIZE);

        // Outside the map counts as solid,  so nobody can leave the world
        if (col < 0 || row < 0 || col >= mapCols || row >= mapRows){
            return true;
        }
        return isSolidTile(map[row][col]);
    }
    // The ONE place that decides which tile types block movement.
    // To make sand solid later, you change only this line.
    private boolean isSolidTile(byte tile) {
        return tile == WATER;
    }
    
}