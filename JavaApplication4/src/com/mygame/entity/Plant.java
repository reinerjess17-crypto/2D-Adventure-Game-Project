package com.mygame.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// Base class for everything that grows: berries, trees, cacti.
// A plant never moves and has no health, so it extends Entity directly, NOT Creature.
// Its life is a simple cycle: ready -> used -> cooldown -> ready again.
public abstract class Plant extends Entity {

    protected BufferedImage image;          // the picture currently shown
    protected final int cooldownTicks;      // how long it takes to become ready again
    protected int cooldownTimer = 0;        // ticks waited so far
    protected boolean ready = true;         // true = can be used right now

    protected Plant(double x, double y, int width, int height,
                    BufferedImage image, int cooldownTicks) {
        super(x, y, width, height);
        this.image = image;
        this.cooldownTicks = cooldownTicks;
    }

    // ----- Update: only the cooldown needs time to pass -----

    @Override
    public void update(EntityManager manager) {
        if (ready) return;                  // nothing to wait for

        cooldownTimer++;
        if (cooldownTimer >= cooldownTicks) {
            ready = true;                   // grown back
            cooldownTimer = 0;
        }
    }

    // ----- Draw -----

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int screenX = (int) Math.round(x) - camX;
        int screenY = (int) Math.round(y) - camY;

        if (image != null) {
            // Same rule as the player: centered horizontally, bottom of the picture
            // on the bottom of the entity. Tall pictures (trees) grow UPWARD.
            int drawX = screenX + (width - image.getWidth()) / 2;
            int drawY = screenY + height - image.getHeight();
            g2.drawImage(image, drawX, drawY, null);
        } else {
            // Image missing: a bright box so the problem is obvious (like the tiles).
            // Darker while the plant is on cooldown, so you can still see its state.
            g2.setColor(ready ? Color.MAGENTA : new Color(90, 0, 90));
            g2.fillRect(screenX, screenY, width, height);
        }
    }

    // ----- Using the plant -----

    // The shared rule: it only works when ready, then it goes on cooldown.
    // The CHILD decides what the effect actually is, in applyEffect().
    protected void use(Player player) {
        if (!ready) return;
        applyEffect(player);
        ready = false;
        cooldownTimer = 0;
    }

    // A berry heals, a tree gives wood, a cactus hurts. Each plant writes its own.
    protected abstract void applyEffect(Player player);

    public boolean isReady() { return ready; }
}