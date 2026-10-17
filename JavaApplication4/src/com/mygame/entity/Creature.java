package com.mygame.entity;

import com.mygame.world.TileMap;
import java.awt.Rectangle;

// Anything that moves and can be hurt: the player, villagers, enemies, animals.
public abstract class Creature extends Entity {

    protected int health;
    protected int maxHealth;
    protected double speed;                       // pixels per tick
    protected Direction facing = Direction.DOWN;
    protected int invulnerableTicks = 0;          // while above 0, damage is ignored

    // All the images of this creature (idle + walking, 8 directions).
    // "final": a creature keeps the same set for its whole life.
    protected final SpriteSet sprites;

    protected Creature(double x, double y, int width, int height, int maxHealth, double speed, SpriteSet sprites) {
        super(x, y, width, height);               // let Entity set up the position and size
        this.maxHealth = maxHealth;
        this.health = maxHealth;                  // creatures start at full health
        this.speed = speed;
        this.sprites = sprites;
        // Creatures use a smaller box than their picture: only the "feet" collide
        this.hitbox = new Rectangle(4, 4, width - 8, height - 8);
    }

    // ----- Timers -----
 
    // Counts the invulnerability down by one tick. The child calls this ONCE at the
    // start of its update(). We can't do it here automatically, because update() is
    // still abstract in Creature: each child writes its own.
    protected void tickTimers() {
        if (invulnerableTicks > 0) {
            invulnerableTicks--;
        }
    }

    // ----- Health: every change goes through these methods -----

    public void takeDamage(int amount) {
        if (!isAlive() || invulnerableTicks > 0) return;   // can't hurt the dead or the protected
        health = Math.max(0, health - amount);             // never below 0
        if (health == 0) {
            onDeath();
        }
    }

    public void heal(int amount) {
        if (!isAlive()) return;
        health = Math.min(maxHealth, health + amount);     // never above the maximum
    }

    public boolean isAlive() { return health > 0; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    // Each kind of creature dies differently: the player fades and respawns,
    // an enemy drops loot and disappears. So the PARENT only says "something must happen".
    protected abstract void onDeath();

    // ----- Movement with collision (the code you already wrote in Player) -----

    // Moves one axis at a time, so the creature slides along walls
    protected void moveWithCollision(double dx, double dy, TileMap map) {
        if (!collides(x + dx, y, map)) {
            x += dx;
        }
        if (!collides(x, y + dy, map)) {
            y += dy;
        }
    }

    // "private": only Creature itself needs this, so the children can't misuse it
    private boolean collides(double newX, double newY, TileMap map) {
        double left = newX + hitbox.x;
        double top = newY + hitbox.y;
        double right = left + hitbox.width - 1;      // -1 = the last pixel INSIDE the box
        double bottom = top + hitbox.height - 1;

        return map.isSolidAtPixel(left, top)
            || map.isSolidAtPixel(right, top)
            || map.isSolidAtPixel(left, bottom)
            || map.isSolidAtPixel(right, bottom);
    }
}