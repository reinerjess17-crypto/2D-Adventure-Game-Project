package com.mygame.world;

public class Camera {
    // World position (in pixels) of the canvas's top-left corner
    private int x = 0;
    private int y = 0;

    public int getX() {return x;}
    public int getY() {return y;}

    //Shift the camera by the given amount
    public void move(int dx, int dy){
        x += dx;
        y += dy;
    }
    // Forces the camera to stay inside the world.
    // worldWidth/worldHeight = size of the map in pixels
    // viewWidth/viewHeight   = size of the visible canvas
    public void clamp(int worldWidth, int worldHeight, int viewWidth, int viewHeight){
        // The farthest the top-left corner may go.
        // Math.max(0, ...) protects us if the world is smaller than the screen.
        int maxX = Math.max(0, worldWidth - viewWidth);
        int maxY = Math.max(0, worldHeight - viewHeight);

        // Push the position back inside the range [0, max]
        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));
    }
    // Places the camera so the target point is in the middle of the view
    public void follow(double targetX, double targetY, int viewWidth, int viewHeight){
         x = (int) Math.round(targetX - viewWidth / 2.0);
    y = (int) Math.round(targetY - viewHeight / 2.0);
    }
}
