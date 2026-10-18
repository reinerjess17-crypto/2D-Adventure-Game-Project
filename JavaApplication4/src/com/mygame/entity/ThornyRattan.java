package com.mygame.entity;

import com.mygame.main.GameSettings;
import com.mygame.util.ImageLoader;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// A cactus: hurts the player when touched. Comes in several looks ("variants")
// so a desert of identical cacti doesn't look repetitive.
public class ThornyRattan extends HarmfulPlant {

    // How many different cactus pictures you drew. Change this number to match your art.
    // Files: /res/plants/sharp_cactus_0.png, sharp_cactus_1.png, ...
    private static final int VARIANT_COUNT = 2;

    // One picture per variant, loaded ONCE and shared by every cactus
    private static final BufferedImage[] IMAGES = new BufferedImage[VARIANT_COUNT];

    // A "static block" runs one time, when the class is first used.
    // We need it because filling the array takes a loop, which can't be written
    // on the same line as the declaration.
    static {
        for (int i = 0; i < VARIANT_COUNT; i++) {
            IMAGES[i] = ImageLoader.load("/res/plants/thorny_rattan_" + i + ".png");
        }
    }

    private static final int CONTACT_DAMAGE = 10;
    private static final int COOLDOWN_TICKS = 60;    // 1 second between hits while the player stays on it

    // Random look: this constructor picks a variant and passes it to the next one
    public ThornyRattan(double x, double y) {
        this(x, y, (int) (Math.random() * VARIANT_COUNT));
    }

    // Chosen look. floorMod keeps the number inside 0..VARIANT_COUNT-1,
    // so a wrong variant number can never crash the game.
    public ThornyRattan(double x, double y, int variant) {
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              IMAGES[Math.floorMod(variant, VARIANT_COUNT)], COOLDOWN_TICKS, CONTACT_DAMAGE);

        // NOT solid: the player must be able to walk INTO the cactus to get hurt.
        // (A solid entity keeps the player's box outside of its own, so they would never overlap.)
        // The hitbox is a bit smaller than the tile, so grazing the edge doesn't hurt.
        hitbox = new Rectangle(4, 4, 24, 24);
    }
}