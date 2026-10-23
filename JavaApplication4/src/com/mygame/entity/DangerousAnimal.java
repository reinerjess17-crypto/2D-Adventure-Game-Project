package com.mygame.entity;

// An animal that attacks the player: boars, snakes, wolves...
// It is an Animal (so it wanders near its home when nothing is happening) AND a DamageSource.
// The loop it runs:
//
//   IDLE / WANDER --(player comes within aggroRange)--> CHASE
//   CHASE --(close enough and ready)--> performAttack --> COOLDOWN
//   COOLDOWN --(attackTimer reaches 0)--> CHASE again, or give up
//   CHASE --(timer runs out, player dead or too far)--> giveUpChase --> IDLE
//
// Children (WildBoar...) change HOW it chases or attacks by overriding the protected methods.
public abstract class DangerousAnimal extends Animal implements DamageSource {

    // The animal loses interest if the target gets farther than aggroRange times this number.
    // Without it, a chasing animal would follow you across the whole map.
    private static final double LOSE_INTEREST_FACTOR = 1.5;

    protected final int attackDamage;        // health taken from the target per hit
    protected final double aggroRange;       // starts chasing when the target is this close (pixels)
    protected final double attackRange;      // can bite when the target is this close (pixels)
    protected final int attackCooldown;      // ticks of rest after an attack

    protected int attackTimer = 0;           // ticks left until it can attack again
    protected int chaseTimer = 0;            // ticks left before it gets tired of chasing
    protected final int maxChaseTicks;       // how long a chase lasts at most
    protected final double chaseSpeed;       // pixels per tick while chasing (faster than wandering)

    protected DangerousAnimal(double x, double y, int width, int height,
                              int maxHealth, double speed, SpriteSet sprites, double wanderRadius,
                              int attackDamage, double aggroRange, double attackRange,
                              int attackCooldown, int maxChaseTicks, double chaseSpeed) {
        super(x, y, width, height, maxHealth, speed, sprites, wanderRadius);
        this.attackDamage = attackDamage;
        this.aggroRange = aggroRange;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.maxChaseTicks = maxChaseTicks;
        this.chaseSpeed = chaseSpeed;
    }

    // ----- Behavior: this REPLACES the "just wander" of Animal -----

    @Override
    protected void updateBehavior(EntityManager manager) {
        Player target = manager.getPlayer();

        if (attackTimer > 0) {
            attackTimer--;                       // the rest after an attack counts down
        }

        switch (animalState) {
            case CHASE:
                updateChase(target, manager);
                break;
            case COOLDOWN:
                updateCooldown(target);
                break;
            default:                             // IDLE or WANDER: live normally, but keep watching
                wander(manager);                 // the inherited loop from Animal
                if (canNotice(target)) {
                    startChase(target);
                }
                break;
        }
    }

    // Is the target alive and close enough to be noticed?
    private boolean canNotice(Creature target) {
        return target.isAlive() && distanceTo(target) <= aggroRange;
    }

    // One tick of chasing
    private void updateChase(Creature target, EntityManager manager) {
        double distance = distanceTo(target);
        chaseTimer--;

        // Reasons to stop: the target died, we are tired, or it got away
        if (!target.isAlive() || chaseTimer <= 0 || distance > aggroRange * LOSE_INTEREST_FACTOR) {
            giveUpChase();
            return;
        }

        if (distance <= attackRange && attackTimer == 0) {
            performAttack(target);               // close enough and ready: bite!
        } else {
            chaseTarget(target, manager);        // otherwise keep running at it
        }
    }

    // One tick of resting after an attack
    private void updateCooldown(Creature target) {
        updateAnimation(false);                  // stands still, catching its breath
        if (attackTimer > 0) return;             // still resting

        // Rested. Keep going if the target is still close, otherwise calm down.
        if (target.isAlive() && chaseTimer > 0 && distanceTo(target) <= aggroRange) {
            animalState = AnimalState.CHASE;
        } else {
            giveUpChase();
        }
    }

    // ----- The pieces a child can change -----

    // Begins a chase. The target is passed in so a child can remember or react to it.
    protected void startChase(Creature target) {
        animalState = AnimalState.CHASE;
        chaseTimer = maxChaseTicks;
    }

    // Runs one step toward the target. walk() needs the manager to check collisions,
    // so it is passed in (your UML only had the target; this is the one extra parameter).
    protected void chaseTarget(Creature target, EntityManager manager) {
        boolean moved = walk(target.getCenterX() - getCenterX(),    // the vector "from me to the target"
                             target.getCenterY() - getCenterY(),
                             chaseSpeed, manager);
        updateAnimation(moved);
    }

    // Hurts the target and starts the rest period.
    // takeDamage (in Creature) still decides if it counts: protected or dead targets ignore it.
    protected void performAttack(Creature target) {
        target.takeDamage(attackDamage);
        animalState = AnimalState.COOLDOWN;
        attackTimer = attackCooldown;
    }

    // Stops chasing and goes back to the normal idle/wander life
    protected void giveUpChase() {
        chaseTimer = 0;
        startIdle();                             // from Animal (now protected)
    }

    // ----- DamageSource -----

    @Override
    public int getContactDamage() {
        return attackDamage;
    }
}