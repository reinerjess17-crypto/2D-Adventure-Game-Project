package com.mygame.main;

public class GameSettings {
    // The size of the hidden canvas. All game coordinates use THIS size.
    public static final int BASE_WIDTH = 640;
    public static final int BASE_HEIGHT = 360;

    // How much the canvas is enlarged on the real window
    public static final int SCALE = 2;// 2x multiplier

    // The size of the real window panel (1280x720)
    public static final int SCREEN_WIDTH = BASE_WIDTH * SCALE;
    public static final int SCREEN_HEIGHT = BASE_HEIGHT * SCALE;

    // A tile is 32x32 on the canvas. The scaling happens later, once.
    public static final int TILE_SIZE = 32;

    public static final int FPS = 60;
}