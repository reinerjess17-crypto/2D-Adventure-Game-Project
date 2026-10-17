package com.mygame.entity;

import com.mygame.main.GameSettings;
import com.mygame.util.ImageLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// A berry bush: press E to pick it and heal. It grows back after a while.
public class LifeBerry extends UsefulPlant {

    // "static": loaded ONCE when the class is first used, and shared by every berry.
    // If these were normal fields, 50 berries would load the same file 50 times.
    // Change the paths to match your real files.
    private static final BufferedImage RIPE_IMAGE =
            ImageLoader.load("/res/plants/berry_bush_berries.png");
    private static final BufferedImage PICKED_IMAGE =
            ImageLoader.load("/res/plants/berry_bush_no_berries.png");

    private static final int COOLDOWN_TICKS = 600;      // 10 seconds to grow back (raise it later)
    private static final double INTERACT_RANGE = 40;    // a bit more than one tile

    private final int healAmount = 20;

    public LifeBerry(double x, double y) {
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              RIPE_IMAGE, COOLDOWN_TICKS, INTERACT_RANGE, "Pick berry");
    }

    // Before drawing, choose the picture that matches the state.
    // Then the normal Plant.draw() (the parent) does the actual drawing.
    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        image = ready ? RIPE_IMAGE : PICKED_IMAGE;
        super.draw(g2, camX, camY);
    }

    // The berry's own effect: heal the player
    @Override
    protected void applyEffect(Player player) {
        player.heal(healAmount);
    }
}
