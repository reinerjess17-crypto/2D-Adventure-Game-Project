package com.mygame.entity;

import com.mygame.world.TileMap;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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

    // ----- Animation (shared by EVERY creature) -----
    protected boolean moving = false;             // true = show the walk frames, false = show the idle image
    protected int animTimer = 0;                  // counts ticks since the last frame change
    protected int frameIndex = 0;                 // which walking frame is showing (0 to walkFrames - 1)
    protected int walkTicksPerFrame = 8;          // 8 ticks = 7.5 frames per second. A child can change it
                                                  // (for example a boar could use a smaller number to look faster)

    // The last parameter is the creature's SpriteSet. Each child decides which one
    // it needs and passes it up, for example:
    //   super(x, y, 32, 32, 100, 2.0, SpriteSet.load("/res/player", 4));
    protected Creature(double x, double y, int width, int height,
                       int maxHealth, double speed, SpriteSet sprites) {
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

    // Normal damage (a bite, a hit): ignored while the creature is dead or protected
    public void takeDamage(int amount) {
        if (!isAlive() || invulnerableTicks > 0) return;   // can't hurt the dead or the protected
        drainHealth(amount);
    }

    // Health loss that IGNORES the protection timer: being drained, poison, bleeding...
    // These happen again and again, so they must not be blocked by the last hit's protection.
    // (A dead creature still can't lose more.)
    public void drainHealth(int amount) {
        if (!isAlive()) return;
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
    public Direction getFacing() { return facing; }
    public boolean isMoving() { return moving; }

    // Each kind of creature dies differently: the player fades and respawns,
    // an enemy drops loot and disappears. So the PARENT only says "something must happen".
    protected abstract void onDeath();

    // ----- Animation -----

    // The child calls this once per tick and says whether it is moving THIS tick.
    // It switches between walking and idle, and advances the walk frames.
    protected void updateAnimation(boolean isMoving) {
        // We just started walking or just stopped: restart the cycle from frame 0
        if (isMoving != moving) {
            moving = isMoving;
            animTimer = 0;
            frameIndex = 0;
        }
        if (!moving) return;      // the idle picture is a single image: nothing to advance

        animTimer++;
        if (animTimer >= walkTicksPerFrame) {
            animTimer = 0;
            frameIndex = (frameIndex + 1) % sprites.getWalkFrames();
        }
    }

    // Draws the creature's current picture, fully visible
    protected void drawSprite(Graphics2D g2, int camX, int camY) {
        drawSprite(g2, camX, camY, 1f);
    }

    // The same, with transparency: 1 = fully visible, 0 = invisible.
    // The player uses it to fade out while dying. Two methods with the same name but
    // different parameters is called "overloading".
    protected void drawSprite(Graphics2D g2, int camX, int camY, float alpha) {
        drawSprite(g2, camX, camY, alpha, 0);
    }

    // The full version. offsetY moves the picture on the screen: negative = UP.
    // A rider uses it to sit higher than the animal's feet.
    protected void drawSprite(Graphics2D g2, int camX, int camY, float alpha, int offsetY) {
        // Walking frame while moving, idle image when still
        BufferedImage image = moving ? sprites.getWalk(facing, frameIndex)
                                     : sprites.getIdle(facing);
        // WORLD position minus camera position = SCREEN position
        int screenX = (int) Math.round(x) - camX;
        int screenY = (int) Math.round(y) - camY + offsetY;

        if (image == null) {
            // The picture is missing: draw a bright box, so you can still SEE and test
            // the creature before its art is ready (same idea as the magenta tiles).
            g2.setColor(Color.MAGENTA);
            g2.fillRect(screenX, screenY, width, height);
            return;
        }

        // Center the picture horizontally and put its bottom on the bottom of the entity
        int drawX = screenX + (width - image.getWidth()) / 2;
        int drawY = screenY + height - image.getHeight();

        Composite oldComposite = g2.getComposite();    // remember the normal setting
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(image, drawX, drawY, null);
        g2.setComposite(oldComposite);                 // ALWAYS put it back
    }

    // ----- Walking -----

    // Walks one step in the direction of the vector (dirX, dirY), at the given speed.
    // The vector can be ANY length (it gets normalized) and can point anywhere:
    // (1, 0) = right, or "from me to the player" = (player.x - x, player.y - y).
    // It also turns the creature to face that way.
    // Returns true if the creature really moved, false if something blocked it.
    protected boolean walk(double dirX, double dirY, double moveSpeed, EntityManager manager) {
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        if (length == 0) return false;                       // no direction: nothing to do

        facing = Direction.fromVector(dirX, dirY);

        double oldX = x;
        double oldY = y;
        moveWithCollision(dirX / length * moveSpeed, dirY / length * moveSpeed, manager);
        return x != oldX || y != oldY;                       // did the position change?
    }

    // ----- Movement with collision -----

    // Moves one axis at a time, so the creature slides along walls.
    // It receives the EntityManager (not just the map), because to check solid
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
        List<Entity> nearby = manager.getEntitiesNear(newX + width / 2.0,
                                                      newY + height / 2.0, 64);
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