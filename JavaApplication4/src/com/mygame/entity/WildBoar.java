package com.mygame.entity;

import com.mygame.main.GameSettings;

import java.awt.Graphics2D;

// A wild boar. It wanders quietly, but if you come close it CHARGES: it starts slowly,
// speeds up the longer it runs, rams you, and then has to stop and catch its breath.
// While it is tired it ignores you completely, which is your chance to escape.
public class WildBoar extends DangerousAnimal {

    private static final int MAX_HEALTH = 40;
    private static final double WALK_SPEED = 0.7;                           // calm wandering
    private static final double CHARGE_SPEED = 2.6;                         // top speed (the player walks at 2.0)
    private static final double CHARGE_ACCELERATION = 0.04;                 // speed gained per tick while charging
    private static final double WANDER_RADIUS = 4 * GameSettings.TILE_SIZE;

    private static final int ATTACK_DAMAGE = 15;
    private static final double AGGRO_RANGE = 6 * GameSettings.TILE_SIZE;   // notices you from 6 tiles
    private static final double ATTACK_RANGE = 30;                          // almost touching (centers 30 px apart)
    private static final int ATTACK_COOLDOWN = 60;                          // 1 second

    private static final int CHASE_SECONDS = 5;                             // a charge lasts at most 5 seconds
    private static final int EXHAUST_SECONDS = 3;                           // then it rests for 3 seconds

    private static final int WALK_TICKS_PER_FRAME = 10;                     // legs while wandering
    private static final int CHARGE_TICKS_PER_FRAME = 5;                    // legs while charging: much faster

    // ONE SpriteSet shared by every boar, loaded once.
    // The folder /res/boar must contain boar_<direction>.png for the 8 directions:
    // each one is a strip of 5 frames side by side (1 idle + 4 walk).
    private static final SpriteSet SPRITES = SpriteSet.loadSheets("/res/boar", "wild_boar", 4);

    private boolean exhausted = false;   // true = too tired to do anything
    private int exhaustTimer = 0;        // ticks of rest left
    private int chargeTicks = 0;         // how long the current charge has lasted (for the acceleration)

    public WildBoar(double x, double y) {
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              MAX_HEALTH, WALK_SPEED, SPRITES, WANDER_RADIUS,
              ATTACK_DAMAGE, AGGRO_RANGE, ATTACK_RANGE, ATTACK_COOLDOWN,
              CHASE_SECONDS * GameSettings.FPS, CHARGE_SPEED);

        walkTicksPerFrame = WALK_TICKS_PER_FRAME;
    }

    // ----- Behavior -----

    // A tired boar does NOTHING else: no wandering, no noticing the player.
    // Otherwise it behaves like any DangerousAnimal.
    @Override
    protected void updateBehavior(EntityManager manager) {
        if (exhausted) {
            updateAnimation(false);          // standing still, panting
            exhaustTimer--;
            if (exhaustTimer <= 0) {
                exhausted = false;           // rested: it can notice you again
            }
            return;
        }
        super.updateBehavior(manager);
    }

    // The charge begins: reset the speed-up counter and run the legs faster
    @Override
    protected void startChase(Creature target) {
        super.startChase(target);
        chargeTicks = 0;
        walkTicksPerFrame = CHARGE_TICKS_PER_FRAME;
    }

    // Instead of running at full speed at once, the boar ACCELERATES like a real charge.
    // Math.min keeps the speed from going above the top speed.
    @Override
    protected void chaseTarget(Creature target, EntityManager manager) {
        chargeTicks++;
        double currentSpeed = Math.min(chaseSpeed, speed + chargeTicks * CHARGE_ACCELERATION);

        boolean moved = walk(target.getCenterX() - getCenterX(),
                             target.getCenterY() - getCenterY(),
                             currentSpeed, manager);
        updateAnimation(moved);
    }

    // The boar rams you ONCE and then it is spent: the normal attack, then the charge ends.
    @Override
    protected void performAttack(Creature target) {
        super.performAttack(target);   // damage + cooldown from DangerousAnimal
        giveUpChase();                 // the charge is over: this makes the boar tired (see below)
    }

    // Every way a charge can end (rammed you, ran out of time, lost you) leaves the boar tired.
    @Override
    protected void giveUpChase() {
        super.giveUpChase();           // back to idle
        walkTicksPerFrame = WALK_TICKS_PER_FRAME;
        exhausted = true;
        exhaustTimer = EXHAUST_SECONDS * GameSettings.FPS;
    }

    // ----- Draw -----

    // A tired boar is drawn a bit see-through, so you can SEE that now is a safe moment.
    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        drawSprite(g2, camX, camY, exhausted ? 0.6f : 1f);
    }
}