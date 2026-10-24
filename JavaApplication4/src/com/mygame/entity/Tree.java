package com.mygame.entity;

import com.mygame.item.Item;
import com.mygame.main.GameSettings;
import com.mygame.util.ImageLoader;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// A tree: it blocks movement (it is SOLID) and the player can gather wood from it.
// Comes in several looks ("variants") with different sizes and trunks.
public class Tree extends UsefulPlant {

    // One collision box per variant: the TRUNK only, relative to the 32x32 footprint tile
    // (x offset, y offset, width, height). A thin tree needs a thin box.
    // Variant 0 is the first box, variant 1 the second, and so on.
    // TO ADD A VARIANT: add one box here AND one picture (tree_N.png). Nothing else.
    private static final Rectangle[] TRUNK_BOXES = {
        new Rectangle(4, 8, 24, 24),     // variant 0 -> tree_0.png
        new Rectangle(6, 10, 20, 22),    // variant 1 -> tree_1.png
        new Rectangle(8, 12, 16, 20),    // variant 2 -> tree_2.png
        new Rectangle(6, 10, 20, 22),    // variant 3 -> tree_3.png  (placeholder: adjust)
        new Rectangle(6, 10, 20, 22)     // variant 4 -> tree_4.png  (placeholder: adjust)
    };

    // The number of variants comes FROM the boxes, so the two can never disagree.
    // (This line must stay below TRUNK_BOXES, because Java sets static fields top to bottom.)
    private static final int VARIANT_COUNT = TRUNK_BOXES.length;

    // One picture per variant, loaded ONCE and shared by every tree.
    // Files: /res/plants/tree_0.png, tree_1.png, tree_2.png ...
    // Pictures can be taller than 32 pixels: Plant.draw() grows them UPWARD from the base.
    private static final BufferedImage[] IMAGES = new BufferedImage[VARIANT_COUNT];

    static {
        for (int i = 0; i < VARIANT_COUNT; i++) {
            IMAGES[i] = ImageLoader.load("/res/plants/forest_tree_" + i + ".png");
        }
    }

    private static final int COOLDOWN_TICKS = 1800;     // 30 seconds before it gives wood again
    private static final double INTERACT_RANGE = 48;    // the trunk blocks us, so we can't get very close

    private final int woodAmount = 3;
    private final Item woodItem = new Item("Wood");

    // Random look: picks a variant and passes it to the next constructor
    public Tree(double x, double y) {
        this(x, y, (int) (Math.random() * VARIANT_COUNT));
    }

    // Chosen look
    public Tree(double x, double y, int variant) {
        // The entity is one tile big: that is its FOOTPRINT on the ground
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              IMAGES[wrap(variant)], COOLDOWN_TICKS, INTERACT_RANGE, "Gather wood");

        solid = true;   // the flag from Entity that Creature.moveWithCollision checks

        // Only the TRUNK blocks. "new Rectangle(box)" makes a COPY, so every tree
        // owns its box instead of sharing one object with all the others.
        hitbox = new Rectangle(TRUNK_BOXES[wrap(variant)]);
    }

    // Keeps any number inside 0..VARIANT_COUNT-1, so a wrong variant can never crash the game.
    // "static" because we need it INSIDE the super(...) call, before the tree exists.
    private static int wrap(int variant) {
        return Math.floorMod(variant, VARIANT_COUNT);
    }

    // What the tree does when used. For now it only prints a message.
    // TEMPORARY: replace with inventory.add(woodItem, woodAmount) when the inventory exists.
    @Override
    protected void applyEffect(Player player) {
        player.addWood(woodAmount);        // TEMPORARY wood counter (replace with the inventory later)
        System.out.println("Gathered " + woodAmount + " " + woodItem.getName()
            + " (total: " + player.getWood() + ")");
    }

    // Overrides the simple prompt of UsefulPlant to include the amount
    @Override
    public String getPrompt() {
        return "Gather " + woodAmount + " " + woodItem.getName();
    }
}