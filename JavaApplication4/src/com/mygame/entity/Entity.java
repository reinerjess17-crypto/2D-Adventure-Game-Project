package com.mygame.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;

// The root of everything that exists in the world.
// "abstract" means: you can never write new Entity(). It only exists to be extended.
public abstract class Entity {

    // Position in the WORLD (pixels). "protected": visible to subclasses.
    protected double x;
    protected double y;

    // Size of the picture
    protected int width;
    protected int height;

    // The invisible collision box, RELATIVE to x and y.
    // (4, 4, 24, 24) means: starts 4 pixels right and 4 pixels down from the top-left corner.
    protected Rectangle hitbox;

    // Does this entity block other creatures from walking through it? (trees: yes)
    protected boolean solid = false;

    // false = this entity has been removed and the manager will throw it away
    protected boolean active = true;

    protected Entity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitbox = new Rectangle(0, 0, width, height);   // default: the whole picture
    }

    // Every subclass MUST write these two. "abstract" = no body here, only the rule.
    // That is what lets the game loop call update() and draw() on anything.
    public abstract void update(EntityManager manager);
    public abstract void draw(Graphics2D g2, int camX, int camY);

    // ----- Shared helpers: written ONCE here, free for every subclass -----

    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }

    // The number EntityManager uses to decide who is drawn in front: the entity's FEET.
    // A bigger number = drawn later = in front. A child can override it when it must
    // appear in front of (or behind) something that stands at the same place.
    public double getSortY() { return y + height; }

    // The hitbox as it would be if the entity stood at (newX, newY), in world pixels
    public Rectangle getHitboxAt(double newX, double newY) {
        return new Rectangle((int) Math.round(newX) + hitbox.x,
                             (int) Math.round(newY) + hitbox.y,
                             hitbox.width, hitbox.height);
    }

    // Do the hitboxes of the two entities touch?
    public boolean overlaps(Entity other) {
        return getHitboxAt(x, y).intersects(other.getHitboxAt(other.x, other.y));
    }

    // Straight-line distance between the centers (Pythagoras again)
    public double distanceTo(Entity other) {
        double dx = getCenterX() - other.getCenterX();
        double dy = getCenterY() - other.getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isSolid() { return solid; }
    public boolean isActive() { return active; }
    public void remove() { active = false; }

    // Puts the entity at a new WORLD position. Used when something must be placed exactly:
    // the player hopping onto a carabao, a rider following its mount, a respawn...
    public void setPosition(double newX, double newY) {
        x = newX;
        y = newY;
    }

    // Getters. Other classes can't read protected fields, so they ask through these.
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}