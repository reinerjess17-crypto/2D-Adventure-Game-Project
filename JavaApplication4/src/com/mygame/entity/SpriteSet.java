package com.mygame.entity;

import com.mygame.util.ImageLoader;

import java.awt.image.BufferedImage;

// Holds every image of ONE creature (idle + walking, in all 8 directions), loaded once.
// This is PlayerSprites made reusable: the player, a villager or a boar each get
// their own SpriteSet, built from their own folder.
public class SpriteSet {

    // How many walking frames each direction has in THIS set.
    // It is no longer a "static" constant, because different creatures
    // can have a different number of frames.
    private final int walkFrames;

    // idle[direction]         -> the standing image
    // walk[direction][frame]  -> the walking images
    // Direction.ordinal() turns a name into its position number (DOWN = 0, DOWN_LEFT = 1, ...)
    private final BufferedImage[] idle = new BufferedImage[Direction.values().length];
    private final BufferedImage[][] walk;

    // "private": nobody writes new SpriteSet(...) directly.
    // They call SpriteSet.load(...) below, which reads better and matches your UML.
    private SpriteSet(int walkFrames) {
        this.walkFrames = walkFrames;
        this.walk = new BufferedImage[Direction.values().length][walkFrames];
    }

    // Builds a SpriteSet by loading all the images from a folder.
    // folder     = where the images are, for example "/res/player" or "/res/boar"
    // walkFrames = how many walking frames each direction has (the player has 4)
    //
    // The file names must follow the pattern you already use:
    //   idle_<direction>.png          e.g. /res/player/idle_down_left.png
    //   walk_<direction>_<frame>.png  e.g. /res/player/walk_down_left_2.png
    public static SpriteSet load(String folder, int walkFrames) {
        SpriteSet set = new SpriteSet(walkFrames);

        // Direction.values() gives all 8 directions, so one loop loads everything
        for (Direction d : Direction.values()) {
            String name = d.getFileName();
            set.idle[d.ordinal()] = ImageLoader.load(folder + "/idle_" + name + ".png");

            for (int frame = 0; frame < walkFrames; frame++) {
                set.walk[d.ordinal()][frame] =
                        ImageLoader.load(folder + "/walk_" + name + "_" + frame + ".png");
            }
        }
        return set;
    }

    // The standing image for a direction (null if the file could not be loaded)
    public BufferedImage getIdle(Direction d) {
        return idle[d.ordinal()];
    }

    // One walking frame for a direction (null if the file could not be loaded)
    public BufferedImage getWalk(Direction d, int frame) {
        return walk[d.ordinal()][frame];
    }

    // The animation code needs this to know when to loop back to frame 0
    public int getWalkFrames() {
        return walkFrames;
    }
}