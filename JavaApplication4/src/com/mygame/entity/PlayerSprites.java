package com.mygame.entity;

import com.mygame.util.ImageLoader;

import java.awt.image.BufferedImage;

// Holds every image of the player, loaded once.
public class PlayerSprites {

    // How many walking frames each direction has. Change this to match your art.
    public static final int WALK_FRAMES = 4;

    // idle[direction]         -> the standing image
    // walk[direction][frame]  -> the walking images
    // Direction.ordinal() turns a name into its position number (DOWN = 0, DOWN_LEFT = 1, ...)
    private final BufferedImage[] idle = new BufferedImage[Direction.values().length];
    private final BufferedImage[][] walk = new BufferedImage[Direction.values().length][WALK_FRAMES];

    public PlayerSprites() {
        // Direction.values() gives all 8 directions, so one loop loads everything
        for (Direction d : Direction.values()) {
            String name = d.getFileName();
            idle[d.ordinal()] = ImageLoader.load("/res/player/idle_" + name + ".png");

            for (int frame = 0; frame < WALK_FRAMES; frame++) {
                walk[d.ordinal()][frame] =
                        ImageLoader.load("/res/player/walk_" + name + "_" + frame + ".png");
            }
        }
    }

    public BufferedImage getIdle(Direction d) {
        return idle[d.ordinal()];
    }

    public BufferedImage getWalk(Direction d, int frame) {
        return walk[d.ordinal()][frame];
    }
}