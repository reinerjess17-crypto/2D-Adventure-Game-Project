package com.mygame.entity;

import com.mygame.item.Item;

import java.util.ArrayList;
import java.util.List;

// Base class for everything that wants to hurt the player on purpose.
// It is a Creature (health, movement, animation) AND a DamageSource.
// It writes the update() that Entity demands, and it hands the real thinking to
// updateAI(), which EVERY kind of enemy must write for itself.
public abstract class Enemy extends Creature implements DamageSource {

    protected final int attackDamage;        // health taken from the target per hit
    protected final double aggroRange;       // notices the target when it is this close (pixels)
    protected final double attackRange;      // can hit the target when it is this close (pixels)
    protected final int attackCooldown;      // ticks of rest after an attack

    protected int attackTimer = 0;           // ticks left until it can attack again
    protected EnemyState state = EnemyState.IDLE;

    // What the enemy leaves behind when it dies
    protected final List<Item> drops = new ArrayList<>();

    // dropLoot() runs when the enemy dies, and death can happen outside update().
    // So we remember the manager from the last update.
    private EntityManager lastManager;

    protected Enemy(double x, double y, int width, int height,
                    int maxHealth, double speed, SpriteSet sprites,
                    int attackDamage, double aggroRange, double attackRange, int attackCooldown) {
        super(x, y, width, height, maxHealth, speed, sprites);
        this.attackDamage = attackDamage;
        this.aggroRange = aggroRange;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
    }

    // ----- Update: the same for all enemies -----

    @Override
    public void update(EntityManager manager) {
        lastManager = manager;
        tickTimers();                        // invulnerability countdown (from Creature)
        if (attackTimer > 0) {
            attackTimer--;
        }
        updateAI(manager);                   // the part that makes each enemy different
    }

    // The enemy's "brain", called once per tick. Every enemy writes its own.
    protected abstract void updateAI(EntityManager manager);

    // ----- Building blocks a child can use or override -----

    // Hurts the target and starts the rest period
    protected void performAttack(Creature target) {
        target.takeDamage(attackDamage);
        state = EnemyState.COOLDOWN;
        attackTimer = attackCooldown;
    }

    // Runs one step toward the target
    protected void chase(Creature target, EntityManager manager) {
        state = EnemyState.CHASE;
        boolean moved = walk(target.getCenterX() - getCenterX(),
                             target.getCenterY() - getCenterY(),
                             speed, manager);
        updateAnimation(moved);
    }

    // Is the target alive and close enough to be noticed?
    protected boolean canSee(Creature target) {
        return target.isAlive() && distanceTo(target) <= aggroRange;
    }

    // ----- DamageSource -----

    @Override
    public int getContactDamage() {
        return attackDamage;
    }

    // ----- Death -----

    // TEMPORARY: there are no item entities in the world yet, so the loot is only printed.
    protected void dropLoot(EntityManager manager) {
        for (Item item : drops) {
            System.out.println("Dropped: " + item.getName());
        }
    }

    // Default death for an enemy: drop the loot and disappear. A child can add things first.
    @Override
    protected void onDeath() {
        dropLoot(lastManager);
        remove();                            // Entity.remove(): the manager throws it away
    }
}