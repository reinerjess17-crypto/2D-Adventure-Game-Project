package com.mygame.main;

// Holds the numbers that the whole game shares.
// "static final" means the value never changes and belongs to the class itself,
// so any file can read it with GameSettings.NAME.
public class GameSettings {

    // Size of the HIDDEN CANVAS (in pixels). All game coordinates use this size.
    // We draw the game small, then enlarge it, so pixel art stays crisp.
    public static final int BASE_WIDTH = 640;
    public static final int BASE_HEIGHT = 360;

    // How many times the canvas is enlarged on the real window (2 = twice as big)
    public static final int SCALE = 2;

    // Size of the REAL window panel: the canvas times the scale (1280x720)
    // Both come from BASE and SCALE, so changing SCALE updates everything.
    public static final int SCREEN_WIDTH = BASE_WIDTH * SCALE;
    public static final int SCREEN_HEIGHT = BASE_HEIGHT * SCALE;

    // One tile is 32x32 pixels ON THE CANVAS.
    // The enlarging happens once, when the canvas is shown, not here.
    public static final int TILE_SIZE = 32;

    // How many times per second the game updates and draws
    public static final int FPS = 60;
}