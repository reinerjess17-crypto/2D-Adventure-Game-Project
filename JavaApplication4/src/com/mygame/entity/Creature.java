package com.mygame.entity;

import com.mygame.world.TileMap;
import java.awt.Rectangle;
import java.util.List;

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

    // Moves one axis at a time, so the creature slides along walls.
    // It now receives the EntityManager (not just the map), because to check solid
    // entities like trees it needs to see them. The map is reachable through the manager.
    protected void moveWithCollision(double dx, double dy, EntityManager manager) {
        if (!collides(x + dx, y, manager)) {
            x += dx;
        }
        if (!collides(x, y + dy, manager)) {
            y += dy;
        }
    }

    // Is the creature blocked if it stood at (newX, newY)? Tiles first, then solid entities.
    private boolean collides(double newX, double newY, EntityManager manager) {
        TileMap map = manager.getMap();
        double left = newX + hitbox.x;
        double top = newY + hitbox.y;
        double right = left + hitbox.width - 1;      // -1 = the last pixel INSIDE the box
        double bottom = top + hitbox.height - 1;

        boolean blockedByTile = map.isSolidAtPixel(left, top)
            || map.isSolidAtPixel(right, top)
            || map.isSolidAtPixel(left, bottom)
            || map.isSolidAtPixel(right, bottom);

        return blockedByTile || hitsSolidEntity(newX, newY, manager); 
    }

    // Would our hitbox, standing at (newX, newY), overlap a solid entity?
    private boolean hitsSolidEntity(double newX, double newY, EntityManager manager) {
        Rectangle myBox = getHitboxAt(newX, newY);   // from Entity

        // Only look at entities close to us. 64 pixels is two tiles: plenty for now.
        List<Entity> nearby = manager.getEntitiesNear(newX + width / 2.0, newY + height / 2.0, 64);
        for (Entity other : nearby) {
            if (other == this) continue;             // never block yourself
            if (!other.isSolid()) continue;          // berries and creatures don't block
            if (myBox.intersects(other.getHitboxAt(other.getX(), other.getY()))) {
                return true;
            }
        }
        return false;
    }
}