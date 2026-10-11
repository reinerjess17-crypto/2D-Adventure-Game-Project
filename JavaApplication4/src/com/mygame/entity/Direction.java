package com.mygame.entity;

// The 8 directions the player can face.
// The text in brackets is the word used in the image file names.
public enum Direction {
    DOWN("down"), DOWN_LEFT("down_left"), LEFT("left"), UP_LEFT("up_left"),
    UP("up"), UP_RIGHT("up_right"), RIGHT("right"), DOWN_RIGHT("down_right");

    private final String fileName;

    Direction(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    // Turns the key direction (-1, 0 or 1 on each axis) into a Direction.
    // Remember: on the screen, Y grows DOWNWARD, so dirY > 0 means down.
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
}
