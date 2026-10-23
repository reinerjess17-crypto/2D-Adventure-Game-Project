package com.mygame.entity;

import java.awt.Graphics2D;
import java.util.Random;

// Base class for every animal. An animal lives near its "home" and keeps doing the
// same simple loop: stand still for a while, walk somewhere for a while, repeat.
// Children (friendly, dangerous...) change WHAT the animal does by overriding updateBehavior().
public abstract class Animal extends Creature {

    // How long (in ticks, 60 = one second) an animal stands still or walks each time.
    // Each time a random number between MIN and MAX is chosen, so animals don't move in sync.
    private static final int IDLE_MIN = 60,  IDLE_MAX = 240;
    private static final int WALK_MIN = 60,  WALK_MAX = 180;

    // "static": ONE random generator shared by all animals
    private static final Random RANDOM = new Random();

    protected AnimalState animalState = AnimalState.IDLE;
    protected int wanderTimer;                              // ticks left in the current IDLE or WANDER
    protected Direction wanderDirection = Direction.DOWN;   // where we walk while WANDERing

    // The animal's home is where it was created. It never wanders farther than wanderRadius from it.
    protected final double homeX;
    protected final double homeY;
    protected final double wanderRadius;

    protected Animal(double x, double y, int width, int height,
                     int maxHealth, double speed, SpriteSet sprites, double wanderRadius) {
        super(x, y, width, height, maxHealth, speed, sprites);
        this.homeX = x;
        this.homeY = y;
        this.wanderRadius = wanderRadius;
        startIdle();                // begin by standing still for a random time
    }

    // ----- Update and draw -----

    // The same for all animals: count timers, then run the behavior.
    // It is not abstract any more: Animal now writes the update() that Entity demanded.
    @Override
    public void update(EntityManager manager) {
        tickTimers();
        updateBehavior(manager);
    }

    // What the animal does each tick. By default it just wanders.
    // A tamed animal will override this to follow the player, a boar to chase it.
    protected void updateBehavior(EntityManager manager) {
        wander(manager);
    }

    // Most animals draw just their picture. A child can override this to add extras
    // (for example, a carabao could draw its rider).
    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        drawSprite(g2, camX, camY);
    }

    // ----- Wandering -----

    // One tick of the IDLE <-> WANDER loop
    protected void wander(EntityManager manager) {
        wanderTimer--;

        if (animalState == AnimalState.WANDER) {
            // Walk one step. walk() tells us if something blocked the way.
            boolean moved = walk(wanderDirection.getDx(), wanderDirection.getDy(), speed, manager);
            updateAnimation(moved);

            if (!moved || wanderTimer <= 0) {   // bumped into something, or walked long enough
                startIdle();
            }
        } else {
            updateAnimation(false);             // standing still: idle picture
            if (wanderTimer <= 0) {
                startWandering();
            }
        }
    }

    protected void startIdle() {
        animalState = AnimalState.IDLE;
        wanderTimer = randomBetween(IDLE_MIN, IDLE_MAX);
    }

    private void startWandering() {
        chooseWanderDirection();
        animalState = AnimalState.WANDER;
        wanderTimer = randomBetween(WALK_MIN, WALK_MAX);
    }

    // Picks where to walk next: a random direction, unless we are too far from home,
    // in which case we head back. That is what keeps the animal in its area.
    protected void chooseWanderDirection() {
        double distanceFromHome = Math.hypot(x - homeX, y - homeY);   // straight-line distance

        if (distanceFromHome > wanderRadius) {
            wanderDirection = Direction.fromVector(homeX - x, homeY - y);   // toward home
        } else {
            Direction[] all = Direction.values();
            wanderDirection = all[RANDOM.nextInt(all.length)];              // any of the 8
        }
    }

    // A random whole number from min to max (both included)
    private int randomBetween(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }

    // ----- Death -----

    // The default: the animal simply disappears. Entity.remove() marks it inactive and the
    // manager throws it away. Later, animals can drop meat or play a death animation.
    @Override
    protected void onDeath() {
        remove();
    }
}