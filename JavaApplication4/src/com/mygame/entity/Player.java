package com.mygame.entity;

import com.mygame.main.GameSettings;
import com.mygame.world.TileMap;
import com.mygame.input.KeyHandler;

import java.awt.Color;
import java.awt.Graphics2D;

public class Player {
    //Position in the WORLD (pixels)
    private double x;
    private double y;
    
    private final double speed = 2.0;// pixels per frame
    private final int size = GameSettings.TILE_SIZE;// the picture is 32x32
 
    // NEW: the hitbox is the invisible box used for collision.
    // It starts 4 pixels inside the picture and is 24x24.
    private final int hitboxOffset = 4;
    private final int hitboxSize = 24;
 
    public Player(double startX, double startY){
        this.x = startX;
        this.y = startY;
    }
    // NEW: asks "if the player stood at (newX, newY), would it touch a solid tile?"
    private boolean collides(TileMap map, double newX, double newY){
        // The four edges of the hitbox at that position
        double left = newX + hitboxOffset;
        double top = newY + hitboxOffset;
        double right = left + hitboxSize - 1;
        double bottom = top + hitboxSize - 1;
        
         // Check the four corners. If any corner is on a solid tile, it collides.
        return map.isSolidAtPixel(left, top)
            || map.isSolidAtPixel(right,top)
            || map.isSolidAtPixel(left, bottom)
            || map.isSolidAtPixel(right, bottom);
    }
     // CHANGED: it now receives the map, instead of the world width and height
    public void update(KeyHandler keys, TileMap map) {
        // 1. Read the keys into a direction
        int dirX = 0;
        int dirY = 0;
        if (keys.leftPressed)  dirX -= 1;
        if (keys.rightPressed) dirX += 1;
        if (keys.upPressed)    dirY -= 1;
        if (keys.downPressed)  dirY += 1;

        // 2. Normalize so diagonal speed equals straight speed
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        if (length == 0) return;   // no key pressed, so nothing to do

        double dx = dirX / length * speed;   // how far to move on X this frame
        double dy = dirY / length * speed;   // how far to move on Y this frame

        // 3. Move on X only if the new spot is free
        if (!collides(map, x + dx, y)) {
            x += dx;
        }
        // 4. Then move on Y only if the new spot is free
        if (!collides(map, x, y + dy)) {
            y += dy;
        }
    }

    // Same as before
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) Math.round(x) - cameraX;
        int screenY = (int) Math.round(y) - cameraY;
        g2.setColor(Color.GREEN);
        g2.fillRect(screenX, screenY, size, size);
    }

    // Same as before
    public double getCenterX() { return x + size / 2.0; }
    public double getCenterY() { return y + size / 2.0; }


}
