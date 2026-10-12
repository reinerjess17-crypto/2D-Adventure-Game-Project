package com.mygame.entity;

import com.mygame.input.KeyHandler;
import com.mygame.main.GameSettings;
import com.mygame.world.TileMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import java.awt.Composite;

// The player: it has its own position in the WORLD, moves with the keys,
// and is stopped by solid tiles. The camera follows it.
public class Player {

    // Position in the WORLD (pixels). "double" allows fractions of a pixel,
    // which diagonal movement needs.
    private double x;
    private double y;

    private final double speed = 2.0;                   // pixels per frame (2 x 60 = 120 per second)
    private final int size = GameSettings.TILE_SIZE;    // the picture is one tile big (32x32)
    private static final int IDLE_TICKS = 30;   // 30 ticks = half a second per step

    // The HITBOX is the invisible box used for collision.
    // It is smaller than the picture and sits inside it, so only the "feet"
    // touch walls. It starts 4 pixels inside the picture and is 24x24.
    private final int hitboxOffset = 4;
    private final int hitboxSize = 24;

    // ----- Health -----
    private final int maxHealth = 100;
    private int health = maxHealth;

    // ----- Death and respawn -----
    private enum State { ALIVE, DYING, RESPAWNING }
    private State state = State.ALIVE;
    private int stateTimer = 0;                          // ticks spent in the current state
    private static final int DYING_TICKS = 90;           // 1.5 seconds at 60 ticks per second
    private static final int RESPAWN_TICKS = 45;         // 0.75 seconds

    // Where the player comes back after dying (world pixels)
    private double spawnX;
    private double spawnY;

    // Creates the player at a WORLD position (in pixels).
    // Make sure the start is not on water, or the player is stuck.
    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.spawnX = startX;
        this.spawnY = startY;
    }

    // Asks: "if the player stood at (newX, newY), would it touch a solid tile?"
    // Returns true = blocked, false = free.
    private boolean collides(TileMap map, double newX, double newY) {
        // The four edges of the hitbox at that position
        double left = newX + hitboxOffset;
        double top = newY + hitboxOffset;
        // "- 1" because a box 24 wide starting at pixel 4 covers pixels 4 to 27,
        // so the last pixel INSIDE the box is one less than start + size
        double right = left + hitboxSize - 1;
        double bottom = top + hitboxSize - 1;

        // Check the four corners. The box is smaller than a tile, so if any
        // part of it touches a solid tile, at least one corner is on it.
        // "||" means "or": one blocked corner is enough.
        return map.isSolidAtPixel(left, top)
            || map.isSolidAtPixel(right, top)
            || map.isSolidAtPixel(left, bottom)
            || map.isSolidAtPixel(right, bottom);
    }

    // Runs once per frame: reads the keys and moves the player.
    public void update(KeyHandler keys, TileMap map) {
        // While dying or respawning, ignore the keys and just run the sequence
        if (state != State.ALIVE) {
            updateDeathSequence();
            return;
        }
        // 1. Turn the keys into a direction: -1, 0 or 1 on each axis.
        //    Opposite keys cancel each other (left + right = 0).
        int dirX = 0;
        int dirY = 0;
        if (keys.leftPressed)  dirX -= 1;
        if (keys.rightPressed) dirX += 1;
        if (keys.upPressed)    dirY -= 1;
        if (keys.downPressed)  dirY += 1;

        // 2. Normalize: make the direction always have length 1.
        //    A diagonal (1,1) is 1.41 long, which made diagonal movement faster.
        //    Dividing by the length fixes that.
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        if (length == 0) {
            if (moving) {                 // we JUST stopped: start the idle cycle fresh
                moving = false;
                animTimer = 0;
                frameIndex = 0;
            }
            animTimer++;
            if (animTimer >= IDLE_TICKS) {
                animTimer = 0;
                frameIndex = 1 - frameIndex;   // flips 0 -> 1 -> 0 -> 1 ...
            }
            return;
        }

        if (!moving) {                    // we JUST started walking: start the walk cycle fresh
            moving = true;
            animTimer = 0;
            frameIndex = 0;
        }
        facing = Direction.fromInput(dirX, dirY);

        animTimer++;
        if (animTimer >= FRAMES_PER_STEP) {
            animTimer = 0;
            frameIndex = (frameIndex + 1) % PlayerSprites.WALK_FRAMES;
        }

        double dx = dirX / length * speed;   // distance to move on X this frame
        double dy = dirY / length * speed;   // distance to move on Y this frame

        // 3. Move on X only if the new spot is free
        if (!collides(map, x + dx, y)) {
            x += dx;
        }
        // 4. Then move on Y with its own separate check.
        //    Testing the axes separately lets the player SLIDE along walls
        //    instead of sticking when pushing diagonally.
        if (!collides(map, x, y + dy)) {
            y += dy;
        }
    }

    // Draws the player. WORLD position minus camera position = SCREEN position.
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        int screenX = (int) Math.round(x) - cameraX;
        int screenY = (int) Math.round(y) - cameraY;

        // Pick the right image: walking frame while moving, idle image when still
        BufferedImage image = moving ? sprites.getWalk(facing, frameIndex)
            : sprites.getIdle(facing);

        if (image != null) {
            // 1 = fully visible, 0 = invisible. While dying, the sprite is gone by the halfway point.
            float alpha = 1f;
            if (state == State.DYING) {
            alpha = Math.max(0f, 1f - 2f * stateTimer / DYING_TICKS);
            }

            int drawX = screenX + (size - image.getWidth()) / 2;
            int drawY = screenY + size - image.getHeight();

            Composite oldComposite = g2.getComposite();    // remember the normal setting
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(image, drawX, drawY, null);
            g2.setComposite(oldComposite);                 // ALWAYS put it back
        }

    }

    // The middle of the player: this is the point the camera follows
    public double getCenterX() { return x + size / 2.0; }
    public double getCenterY() { return y + size / 2.0; }

    private final PlayerSprites sprites = new PlayerSprites();

    private Direction facing = Direction.DOWN;   // which way the player looks
    private boolean moving = false;              // true while a movement key is held

    // Animation: every FRAMES_PER_STEP game ticks, we show the next walking frame.
    // update() runs 60 times per second, so 8 ticks = 7.5 frames per second.
    private static final int FRAMES_PER_STEP = 8;
    private int animTimer = 0;      // counts ticks
    private int frameIndex = 0;     // which walking frame is showing (0 to 3)

    // Lose health. Dying starts when it reaches 0.
    public void takeDamage(int amount) {
        if (state != State.ALIVE) return;             // already dying: can't die twice

        health = Math.max(0, health - amount);        // never go below 0

        if (health == 0) {
            state = State.DYING;
            stateTimer = 0;
            moving = false;                           // stand still while dying
            frameIndex = 0;
            animTimer = 0;
        }
    }

    // Gain health, never more than the maximum
    public void heal(int amount) {
        if (state != State.ALIVE) return;
        health = Math.min(maxHealth, health + amount);
    }

    // Where he comes back after dying. Later, a campfire can call this to set a checkpoint.
    public void setSpawnPoint(double newSpawnX, double newSpawnY) {
        spawnX = newSpawnX;
        spawnY = newSpawnY;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    private void updateDeathSequence() {
        stateTimer++;

        if (state == State.DYING && stateTimer >= DYING_TICKS) {
            // The screen is fully black now, so the jump back is hidden.
            x = spawnX;
            y = spawnY;
            health = maxHealth;
            facing = Direction.DOWN;
            state = State.RESPAWNING;
            stateTimer = 0;

        } else if (state == State.RESPAWNING && stateTimer >= RESPAWN_TICKS) {
            state = State.ALIVE;                  // the screen is clear again: control returns
            stateTimer = 0;
        }
    }
    public float getScreenFade() {
        switch (state) {
            case DYING:      return (float) stateTimer / DYING_TICKS;           // 0 -> 1
            case RESPAWNING: return 1f - (float) stateTimer / RESPAWN_TICKS;    // 1 -> 0
            default:         return 0f;
        }
    }
}