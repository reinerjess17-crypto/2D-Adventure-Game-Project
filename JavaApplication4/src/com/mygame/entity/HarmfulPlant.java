package com.mygame.entity;

import java.awt.image.BufferedImage;

// A plant that hurts the player when touched: cacti, thorn bushes...
// It is a Plant (so it has the ready/cooldown cycle) AND a DamageSource.
// Compare with UsefulPlant: that one waits for the player to press E,
// this one acts by itself when the player touches it.
public abstract class HarmfulPlant extends Plant implements DamageSource {

    protected final int contactDamage;      // damage per touch

    protected HarmfulPlant(double x, double y, int width, int height,
                           BufferedImage image, int cooldownTicks, int contactDamage) {
        super(x, y, width, height, image, cooldownTicks);
        this.contactDamage = contactDamage;
    }

    // Called by the EntityManager every tick the player overlaps this plant.
    // use() (from Plant) only works when the plant is ready, so a player standing
    // on a cactus is hurt once per cooldown, not 60 times per second.
    public void onTouch(Player player) {
        use(player);
    }

    // The effect of this kind of plant is always the same: hurt the player.
    // Creature.takeDamage() still decides if it counts (dead or protected players ignore it).
    @Override
    protected void applyEffect(Player player) {
        player.takeDamage(contactDamage);
    }

    @Override
    public int getContactDamage() {
        return contactDamage;
    }
}