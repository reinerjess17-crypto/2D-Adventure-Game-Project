package com.mygame.entity;

import com.mygame.input.KeyHandler;
import com.mygame.main.GameSettings;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// The player. It is a Creature, so it INHERITS from Creature (and Entity):
//   position, size, hitbox, health, speed, facing, sprites, takeDamage(), heal(),
//   moveWithCollision(), getCenterX()/getCenterY()
// This class only contains what is special about the player:
// keyboard control, walk animation, and the death and respawn sequence.
public class Player extends Creature {

    // ----- Constants -----
    private static final int MAX_HEALTH = 100;
    private static final double WALK_SPEED = 2.0;       // pixels per tick (2 x 60 = 120 per second)
    private static final int FRAMES_PER_STEP = 8;       // 8 ticks per walking frame = 7.5 frames per second
    private static final int IDLE_TICKS = 30;           // 30 ticks = half a second per idle step
    private static final int DYING_TICKS = 90;          // 1.5 seconds at 60 ticks per second
    private static final int RESPAWN_TICKS = 45;        // 0.75 seconds
    private static final int HIT_PROTECTION_TICKS = 30; // half a second of protection after a hit

    // ----- Fields that only the player has -----
    private final KeyHandler keys;        // where the player reads the keyboard from

    private boolean moving = false;       // true while a movement key is held
    private int animTimer = 0;            // counts ticks for the animation
    private int frameIndex = 0;           // which walking frame is showing (0 to 3)

    private PlayerState state = PlayerState.ALIVE;
    private int stateTimer = 0;           // ticks spent in the current state
    private Interactable interactTarget;    // the closest thing we could use right now (or null)

    // Where the player comes back after dying (world pixels)
    private double spawnX;
    private double spawnY;

    // ----- Constructor -----

    // Creates the player at a WORLD position (in pixels).
    // Make sure the start is not on water, or the player is stuck.
    public Player(double startX, double startY, KeyHandler keys) {
        // super(...) runs the Creature constructor, which runs the Entity constructor.
        // We hand them the size (one tile), health, speed and the player's images.
        super(startX, startY, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              MAX_HEALTH, WALK_SPEED, SpriteSet.load("/res/player", 4));
        this.keys = keys;
        this.spawnX = startX;
        this.spawnY = startY;
    }

    // ----- Update: runs once per tick, called by the EntityManager -----

    @Override
    public void update(EntityManager manager) {
        tickTimers();   // counts down invulnerableTicks (from Creature)

        if (state != PlayerState.ALIVE) {
            interactTarget = null;                  // nothing to use while dying
            keys.interactRequested = false;         // forget presses made during the fade
            updateDeathSequence();
            return;
        }

        // Look around every tick, so the HUD can show a prompt while we stand near something
        interactTarget = manager.findInteractableNear(this);
        if (keys.interactRequested) {
            keys.interactRequested = false;         // consume the press: one press = one use
            tryInteract();
        }

        // 1. Turn the keys into a direction: -1, 0 or 1 on each axis.
        //    Opposite keys cancel each other (left + right = 0).
        int dirX = 0;
        int dirY = 0;
        if (keys.leftPressed)  dirX -= 1;
        if (keys.rightPressed) dirX += 1;
        if (keys.upPressed)    dirY -= 1;
        if (keys.downPressed)  dirY += 1;

        // 2. Normalize: make the direction always have length 1, so a diagonal
        //    (length 1.41) is not faster than a straight line.
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        boolean isMoving = length > 0;

        updateAnimation(isMoving);
        if (!isMoving) return;                       // no keys: nothing to move

        facing = Direction.fromInput(dirX, dirY);    // 'facing' lives in Creature

        // 3. Move. moveWithCollision (in Creature) checks X and Y separately,
        //    so the player slides along walls. The map comes from the manager.
        double dx = dirX / length * speed;           // 'speed' lives in Creature
        double dy = dirY / length * speed;
        moveWithCollision(dx, dy, manager);
    }

    // Advances the walk/idle animation. isMoving = is a movement key held THIS tick?
    private void updateAnimation(boolean isMoving) {
        // We just started walking or just stopped: restart the cycle from frame 0
        if (isMoving != moving) {
            moving = isMoving;
            animTimer = 0;
            frameIndex = 0;
        }

        animTimer++;
        if (moving) {
            if (animTimer >= FRAMES_PER_STEP) {
                animTimer = 0;
                frameIndex = (frameIndex + 1) % sprites.getWalkFrames();
            }
        } else if (animTimer >= IDLE_TICKS) {
            animTimer = 0;
            frameIndex = 1 - frameIndex;   // flips 0 -> 1 -> 0 (draw() only has one idle image for now)
        }
    }

    // ----- Draw -----

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        // WORLD position minus camera position = SCREEN position
        int screenX = (int) Math.round(x) - camX;
        int screenY = (int) Math.round(y) - camY;

        // Walking frame while moving, idle image when still
        BufferedImage image = moving ? sprites.getWalk(facing, frameIndex)
                                     : sprites.getIdle(facing);
        if (image == null) return;   // the file failed to load: draw nothing

        // 1 = fully visible, 0 = invisible. While dying, the sprite is gone by the halfway point.
        float alpha = 1f;
        if (state == PlayerState.DYING) {
            alpha = Math.max(0f, 1f - 2f * stateTimer / DYING_TICKS);
        }

        // Center the picture horizontally and put its bottom on the bottom of the tile
        int drawX = screenX + (width - image.getWidth()) / 2;
        int drawY = screenY + height - image.getHeight();

        Composite oldComposite = g2.getComposite();    // remember the normal setting
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(image, drawX, drawY, null);
        g2.setComposite(oldComposite);                 // ALWAYS put it back
    }

    // ----- Health -----

    // We override takeDamage only to ADD something after the normal behavior.
    @Override
    public void takeDamage(int amount) {
        int healthBefore = health;
        super.takeDamage(amount);     // Creature does the real work (and calls onDeath at 0)

        // If the hit really landed and we survived, give a short protection window,
        // so one enemy touch can't drain the whole bar in a few ticks.
        if (health < healthBefore && isAlive()) {
            invulnerableTicks = HIT_PROTECTION_TICKS;
        }
    }

    // Creature calls this by itself when health reaches 0.
    // The player does not vanish: it starts the dying sequence.
    @Override
    protected void onDeath() {
        state = PlayerState.DYING;
        stateTimer = 0;
        moving = false;               // stand still while dying
        frameIndex = 0;
        animTimer = 0;
    }

    // ----- Death and respawn -----

    // Where he comes back after dying. Later, a campfire can call this to set a checkpoint.
    public void setSpawnPoint(double newSpawnX, double newSpawnY) {
        spawnX = newSpawnX;
        spawnY = newSpawnY;
    }

    private void updateDeathSequence() {
        stateTimer++;

        if (state == PlayerState.DYING && stateTimer >= DYING_TICKS) {
            // The screen is fully black now, so the jump back is hidden.
            x = spawnX;
            y = spawnY;
            health = maxHealth;
            facing = Direction.DOWN;
            state = PlayerState.RESPAWNING;
            stateTimer = 0;
            // Health is full again, but nothing should hurt the player while the
            // screen is still fading in. The new invulnerability timer covers that.
            invulnerableTicks = RESPAWN_TICKS;

        } else if (state == PlayerState.RESPAWNING && stateTimer >= RESPAWN_TICKS) {
            state = PlayerState.ALIVE;    // the screen is clear again: control returns
            stateTimer = 0;
        }
    }

    // How black the screen should be: 0 = clear, 1 = fully black. The HUD reads this.
    public float getScreenFade() {
        switch (state) {
            case DYING:      return (float) stateTimer / DYING_TICKS;           // 0 -> 1
            case RESPAWNING: return 1f - (float) stateTimer / RESPAWN_TICKS;    // 1 -> 0
            default:         return 0f;
        }
    }
    // Uses whatever we found nearby (if anything)
    private void tryInteract() {
        if (interactTarget != null) {
            interactTarget.interact(this);
        }
    } 

    // The HUD reads this to show the prompt
    public Interactable getInteractTarget() { return interactTarget; }
}