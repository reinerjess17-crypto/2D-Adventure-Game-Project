package com.mygame.entity;

// An enemy with a weakness: it can be STUNNED, which makes it helpless for a while.
// Nothing stuns enemies yet (that will come with the player's tools and skills),
// but the rule already lives here, so every weak enemy gets it for free.
public abstract class WeakEnemy extends Enemy {

    protected int stunTimer = 0;     // ticks of stun left

    protected WeakEnemy(double x, double y, int width, int height,
                        int maxHealth, double speed, SpriteSet sprites,
                        int attackDamage, double aggroRange, double attackRange, int attackCooldown) {
        super(x, y, width, height, maxHealth, speed, sprites,
              attackDamage, aggroRange, attackRange, attackCooldown);
    }

    public void stun(int ticks) {
        stunTimer = ticks;
        state = EnemyState.STUNNED;
    }

    public boolean isStunned() {
        return stunTimer > 0;
    }

    // While stunned the enemy skips its AI completely. Otherwise it acts as any Enemy.
    @Override
    public void update(EntityManager manager) {
        if (stunTimer > 0) {
            stunTimer--;
            tickTimers();
            updateAnimation(false);
            if (stunTimer == 0) {
                state = EnemyState.IDLE;
            }
            return;
        }
        super.update(manager);
    }
}