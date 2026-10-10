package com.mygame.world;

// The camera decides which part of the world is visible on the canvas.
// It stores the WORLD position of the canvas's top-left corner.
// Drawing something on the screen = its world position minus the camera position.
public class Camera {

    // World position (in pixels) of the canvas's top-left corner
    private int x = 0;
    private int y = 0;

    // Getters: other classes read the position through these
    public int getX() { return x; }
    public int getY() { return y; }

    // Shifts the camera by a given amount.
    // (Not used now that the camera follows the player, but handy for debugging.)
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    // Places the camera so the target point sits in the MIDDLE of the view.
    // The camera position is the top-left corner, so we subtract half the view size.
    // targetX/targetY      = world position to look at (the player's center)
    // viewWidth/viewHeight = size of the visible canvas
    public void follow(double targetX, double targetY, int viewWidth, int viewHeight) {
        x = (int) Math.round(targetX - viewWidth / 2.0);
        y = (int) Math.round(targetY - viewHeight / 2.0);
    }

    // Forces the camera to stay inside the world, so we never see black outside the map.
    // worldWidth/worldHeight = size of the map in pixels
    // viewWidth/viewHeight   = size of the visible canvas
    public void clamp(int worldWidth, int worldHeight, int viewWidth, int viewHeight) {
        // The farthest the top-left corner may go: the world size minus the view size.
        // Math.max(0, ...) protects us if the world is smaller than the screen.
        int maxX = Math.max(0, worldWidth - viewWidth);
        int maxY = Math.max(0, worldHeight - viewHeight);

        // Read from the inside out:
        // Math.min(x, maxX) stops x from being too big,
        // Math.max(0, ...) then stops it from being negative.
        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));
    }
}