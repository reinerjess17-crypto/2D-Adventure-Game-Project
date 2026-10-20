package com.mygame.entity;

import com.mygame.util.ImageLoader;

import java.awt.image.BufferedImage;

// Holds every image of ONE creature (idle + walking, in all 8 directions), loaded once.
// The player, a villager or a boar each get their own SpriteSet.
// There are TWO ways to build one, depending on how your art is saved:
//   load(...)       -> one PNG per picture   (40 files for 4 walk frames)
//   loadSheets(...) -> one PNG per direction, with all the frames side by side (8 files)
public class SpriteSet {

    // How many walking frames each direction has in THIS set.
    private final int walkFrames;

    // idle[direction]         -> the standing image
    // walk[direction][frame]  -> the walking images
    // Direction.ordinal() turns a name into its position number (DOWN = 0, DOWN_LEFT = 1, ...)
    private final BufferedImage[] idle = new BufferedImage[Direction.values().length];
    private final BufferedImage[][] walk;

    // "private": nobody writes new SpriteSet(...) directly.
    // They call SpriteSet.load(...) or SpriteSet.loadSheets(...) below.
    private SpriteSet(int walkFrames) {
        this.walkFrames = walkFrames;
        this.walk = new BufferedImage[Direction.values().length][walkFrames];
    }

    // ----- Way 1: separate files (what the player uses) -----
    //
    // folder     = where the images are, for example "/res/player"
    // walkFrames = how many walking frames each direction has (the player has 4)
    // File names:
    //   idle_<direction>.png          e.g. /res/player/idle_down_left.png
    //   walk_<direction>_<frame>.png  e.g. /res/player/walk_down_left_2.png
    public static SpriteSet load(String folder, int walkFrames) {
        SpriteSet set = new SpriteSet(walkFrames);

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

    // ----- Way 2: one "sheet" per direction -----
    //
    // Each direction is ONE picture with all its frames in a row, left to right:
    //   frame 1 = idle (standing), frames 2 and on = the walk cycle.
    // A sheet with 4 walk frames therefore holds 5 pictures side by side.
    //
    // folder = e.g. "/res/carabao"      prefix = e.g. "carabao"
    // File names: <folder>/<prefix>_<direction>.png   e.g. /res/carabao/carabao_down_left.png
    //
    // All the frames of a sheet must have the SAME width, with no gaps between them,
    // so the sheet's width must divide exactly by (walkFrames + 1).
    public static SpriteSet loadSheets(String folder, String prefix, int walkFrames) {
        SpriteSet set = new SpriteSet(walkFrames);
        int framesInSheet = 1 + walkFrames;          // the idle picture + the walk pictures

        for (Direction d : Direction.values()) {
            String path = folder + "/" + prefix + "_" + d.getFileName() + ".png";
            BufferedImage sheet = ImageLoader.load(path);
            if (sheet == null) continue;             // ImageLoader already printed the problem;
                                                     // this direction stays empty (a magenta box in game)

            if (sheet.getWidth() % framesInSheet != 0) {
                System.err.println("Sprite sheet " + path + " is " + sheet.getWidth()
                        + " pixels wide, which does not divide into " + framesInSheet
                        + " equal frames. The frames will be cut wrongly.");
            }

            int frameWidth = sheet.getWidth() / framesInSheet;
            int frameHeight = sheet.getHeight();

            // getSubimage(x, y, w, h) cuts a rectangle out of the sheet (it does not copy the pixels)
            set.idle[d.ordinal()] = sheet.getSubimage(0, 0, frameWidth, frameHeight);
            for (int frame = 0; frame < walkFrames; frame++) {
                set.walk[d.ordinal()][frame] =
                        sheet.getSubimage((frame + 1) * frameWidth, 0, frameWidth, frameHeight);
            }
        }
        return set;
    }

    // ----- Reading -----

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