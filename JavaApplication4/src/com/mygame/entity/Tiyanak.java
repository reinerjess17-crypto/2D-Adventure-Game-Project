package com.mygame.entity;

import com.mygame.main.GameSettings;

import java.awt.Graphics2D;

// The Tiyanak, a creature from Philippine folklore. Here it works like this:
//
//   1. It HIDES in a bush (or tall grass). Nothing is drawn, nothing can touch it.
//   2. When the player comes close, it LEAPS out and lands on the player with a surprise bite.
//   3. It stays LATCHED on the player and keeps draining life, and the player cannot walk.
//   4. The only escape: press SPACE again and again until the tiyanak lets go and dies.
//      If the player dies first, the tiyanak goes back to its hiding place.
//
// Inheritance chain: Entity > Creature > Enemy > WeakEnemy > Tiyanak
public class Tiyanak extends WeakEnemy {

    // ----- Numbers to tune -----
    private static final int MAX_HEALTH = 15;                  // = how many SPACE presses it takes to shake it off
    private static final int STRUGGLE_DAMAGE = 1;              // what each SPACE press does to the tiyanak
    private static final int BITE_DAMAGE = 5;                  // the surprise bite when it lands
    private static final int DRAIN_DAMAGE = 3;                 // life drained every DRAIN_INTERVAL ticks
    private static final int DRAIN_INTERVAL = 30;              // 30 ticks = every half second
    private static final double TRIGGER_RANGE = 1.25 * GameSettings.TILE_SIZE;  // how close the player must come
    private static final int LEAP_TICKS = 12;                  // how long the jump lasts (0.2 seconds)
    private static final int LEAP_HEIGHT = 14;                 // how high the jump arcs, in pixels
    private static final int LATCH_LIFT = 8;                   // while latched it is drawn this much higher (on the shoulder)

    // ONE SpriteSet shared by every tiyanak. The folder /res/tiyanak must contain
    // tiyanak_<direction>.png for the 8 directions, each a strip of 5 frames side by side:
    //   frame 1 (idle)    = the pose in the air, while jumping
    //   frames 2 to 5     = the biting animation, while latched
    private static final SpriteSet SPRITES = SpriteSet.loadSheets("/res/tiyanak", "tiyanak", 4);

    private TiyanakState tiyanakState = TiyanakState.HIDDEN;

    private final double nestX;          // the hiding place: where it was created
    private final double nestY;

    private Player victim;               // who it is stuck on (null when not latched)
    private int leapTimer = 0;           // ticks since the jump began
    private double leapStartX;           // where the jump began
    private double leapStartY;
    private int drainTimer = 0;          // ticks until the next drain

    // Create it at the position of the bush or grass where it hides
    public Tiyanak(double x, double y) {
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              MAX_HEALTH, 0.0, SPRITES,                      // speed 0: a tiyanak never walks or crawls
              BITE_DAMAGE, TRIGGER_RANGE, TRIGGER_RANGE, 0);
        this.nestX = x;
        this.nestY = y;
        walkTicksPerFrame = 6;                                // the biting animation plays fast
    }

    // ----- The brain: one small state machine -----

    @Override
    protected void updateAI(EntityManager manager) {
        Player player = manager.getPlayer();

        switch (tiyanakState) {
            case HIDDEN:
                // canSee() comes from Enemy: "alive and within aggroRange". Here the range is TRIGGER_RANGE.
                if (canSee(player) && !player.isGrabbed()) {
                    startLeap(player);
                }
                break;
            case LEAPING:
                updateLeap(player);
                break;
            case LATCHED:
                updateLatched(player);
                break;
        }
    }

    // ----- Leaping -----

    private void startLeap(Player target) {
        tiyanakState = TiyanakState.LEAPING;
        leapTimer = 0;
        leapStartX = x;
        leapStartY = y;
        updateAnimation(false);                               // the "in the air" pose
    }

    // Every tick of the jump moves it a bit closer to where the player IS RIGHT NOW,
    // so it always reaches a player who keeps moving. It flies over everything:
    // no collision checks, because it is jumping, not walking.
    private void updateLeap(Player target) {
        leapTimer++;

        double progress = Math.min(1.0, leapTimer / (double) LEAP_TICKS);   // 0.0 at the start, 1.0 at the end
        setPosition(leapStartX + (target.getX() - leapStartX) * progress,   // straight line start -> player
                    leapStartY + (target.getY() - leapStartY) * progress);
        facing = Direction.fromVector(target.getCenterX() - getCenterX(),
                                      target.getCenterY() - getCenterY());

        if (leapTimer >= LEAP_TICKS) {
            land(target);
        }
    }

    // The jump is over: grab the player, unless someone else already did (or the player died)
    private void land(Player target) {
        if (!target.isAlive() || target.isGrabbed()) {
            returnToNest();
            return;
        }

        tiyanakState = TiyanakState.LATCHED;
        victim = target;
        drainTimer = DRAIN_INTERVAL;
        target.startGrab(this);        // the player now knows it is being held
        performAttack(target);         // the surprise bite
    }

    // ----- Latched -----

    private void updateLatched(Player target) {
        // Stay glued to the player: same position, same direction, biting animation
        setPosition(target.getX(), target.getY());
        facing = target.getFacing();
        updateAnimation(true);

        drainTimer--;
        if (drainTimer <= 0) {
            drainTimer = DRAIN_INTERVAL;
            // drainHealth ignores the player's hit protection (see Creature),
            // otherwise the drain would sometimes be blocked by the previous bite.
            target.drainHealth(DRAIN_DAMAGE);
        }
    }

    // The PLAYER calls this each time SPACE is pressed while grabbed.
    // Every press hurts the tiyanak; when its health reaches 0, onDeath() runs.
    public void struggle() {
        if (tiyanakState == TiyanakState.LATCHED) {
            takeDamage(STRUGGLE_DAMAGE);
        }
    }

    // Called by the player when the PLAYER dies: the tiyanak lets go and goes back to hiding
    void release() {
        if (tiyanakState != TiyanakState.LATCHED) return;
        victim.endGrab();
        victim = null;
        returnToNest();
    }

    // Back inside the bush, ready to ambush again, with its strength restored
    private void returnToNest() {
        tiyanakState = TiyanakState.HIDDEN;
        setPosition(nestX, nestY);
        health = maxHealth;
        updateAnimation(false);
    }

    // ----- Attack and death -----

    // The surprise bite: plain damage, no cooldown. (The draining is separate, in updateLatched.)
    @Override
    protected void performAttack(Creature target) {
        target.takeDamage(attackDamage);
    }

    // A hidden tiyanak cannot be hurt by anything: nobody can find it
    @Override
    public void takeDamage(int amount) {
        if (tiyanakState == TiyanakState.HIDDEN) return;
        super.takeDamage(amount);
    }

    // It was shaken off: free the player first, then die like any enemy (loot, disappear)
    @Override
    protected void onDeath() {
        if (victim != null) {
            victim.endGrab();
            victim = null;
        }
        super.onDeath();
    }

    // ----- Draw -----

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        if (tiyanakState == TiyanakState.HIDDEN) return;      // hiding: you see only the bush

        int lift;
        if (tiyanakState == TiyanakState.LEAPING) {
            // A smooth arc: sin goes 0 -> 1 -> 0 while the progress goes 0 -> 1
            double progress = Math.min(1.0, leapTimer / (double) LEAP_TICKS);
            lift = (int) Math.round(Math.sin(Math.PI * progress) * LEAP_HEIGHT);
        } else {
            lift = LATCH_LIFT;
        }
        drawSprite(g2, camX, camY, 1f, -lift);                // negative offset = UP on the screen
    }

    // While latched, draw it IN FRONT of the player (a hair lower than the player's feet),
    // otherwise the player's picture would hide it. EntityManager sorts with this number.
    @Override
    public double getSortY() {
        if (tiyanakState == TiyanakState.LATCHED) {
            return y + height + 1;
        }
        return super.getSortY();
    }

    // ----- Info -----

    public boolean isRevealed() {
        return tiyanakState != TiyanakState.HIDDEN;
    }

    // Package-private: for tests and for classes in this package
    TiyanakState getTiyanakState() {
        return tiyanakState;
    }
}