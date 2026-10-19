package com.mygame.entity;

// The 8 directions a creature can face.
// The text in brackets is the word used in the image file names.
// The two numbers are the direction as a STEP: -1, 0 or 1 on each axis.
// Remember: on the screen, Y grows DOWNWARD, so dirY = 1 means down.
public enum Direction {
    DOWN("down", 0, 1), DOWN_LEFT("down_left", -1, 1), LEFT("left", -1, 0), UP_LEFT("up_left", -1, -1),
    UP("up", 0, -1), UP_RIGHT("up_right", 1, -1), RIGHT("right", 1, 0), DOWN_RIGHT("down_right", 1, 1);

    private final String fileName;
    private final int dirX;
    private final int dirY;

    Direction(String fileName, int dirX, int dirY) {
        this.fileName = fileName;
        this.dirX = dirX;
        this.dirY = dirY;
    }

    public String getFileName() { return fileName; }
    public int getDx() { return dirX; }
    public int getDy() { return dirY; }

    // Turns the key direction (-1, 0 or 1 on each axis) into a Direction.
    // Only call this when at least one of them is not 0.
    public static Direction fromInput(int dirX, int dirY) {
        if (dirY > 0) {
            if (dirX < 0) return DOWN_LEFT;
            if (dirX > 0) return DOWN_RIGHT;
            return DOWN;
        }
        if (dirY < 0) {
            if (dirX < 0) return UP_LEFT;
            if (dirX > 0) return UP_RIGHT;
            return UP;
        }
        return dirX < 0 ? LEFT : RIGHT;
    }

    // The 8 directions in order of ANGLE, starting at "right" and turning clockwise on the
    // screen (because Y grows downward). Index 0 = angle 0, index 1 = 45 degrees, and so on.
    private static final Direction[] BY_OCTANT = {
        RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT, UP, UP_RIGHT
    };

    // Turns ANY vector (for example "from me to the player") into the closest of the 8 directions.
    // fromInput only understands -1, 0, 1. This one understands (37.2, -80.5).
    public static Direction fromVector(double dx, double dy) {
        double angle = Math.atan2(dy, dx);                       // from -PI to PI, 0 = pointing right
        int octant = (int) Math.round(angle / (Math.PI / 4));    // 45-degree slices: -4 to 4
        return BY_OCTANT[Math.floorMod(octant, 8)];              // floorMod turns -3 into 5, etc.
    }
}