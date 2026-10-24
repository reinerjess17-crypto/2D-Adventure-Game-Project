package com.mygame.entity;

import com.mygame.input.KeyHandler;
import com.mygame.main.GameSettings;

import java.awt.Graphics2D;

// The player. It is a Creature, so it INHERITS from Creature (and Entity):
//   position, size, hitbox, health, speed, facing, sprites, takeDamage(), heal(),
//   moveWithCollision(), the walk animation, drawSprite()
// This class only contains what is special about the player:
// keyboard control, interaction, riding, and the death and respawn sequence.
public class Player extends Creature {

    // ----- Constants -----
    private static final int MAX_HEALTH = 100;
    private static final double WALK_SPEED = 2.0;       // pixels per tick (2 x 60 = 120 per second)
    private static final int DYING_TICKS = 90;          // 1.5 seconds at 60 ticks per second
    private static final int RESPAWN_TICKS = 45;        // 0.75 seconds
    private static final int HIT_PROTECTION_TICKS = 30; // half a second of protection after a hit

    // ----- Fields that only the player has -----
    private final KeyHandler keys;        // where the player reads the keyboard from

    private PlayerState state = PlayerState.ALIVE;
    private int stateTimer = 0;           // ticks spent in the current state

    private Interactable interactTarget;  // the closest thing we could use right now (or null)
    private Carabao mount;                // the animal we are riding (null = walking)
    private Tiyanak grabber;              // the enemy holding on to us (null = free)
    private Conversation conversation;    // the talk we are in right now (null = not talking)
    private int deathCount = 0;           // how many times we have died (villagers react to it)
    private int wood = 0;                 // TEMPORARY: a tiny "inventory" until the real one exists

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

        // While dying or respawning, ignore the keys and just run the sequence
        if (state != PlayerState.ALIVE) {
            interactTarget = null;                  // nothing to use while dying
            keys.interactRequested = false;         // forget presses made during the fade
            keys.struggleRequested = false;
            updateDeathSequence();
            return;
        }

        // GRABBED: we cannot walk or use anything. The only thing left is to shake the enemy off:
        // every SPACE press hurts it, and when it dies, endGrab() sets us free.
        if (grabber != null) {
            interactTarget = null;
            keys.interactRequested = false;
            updateAnimation(false);                 // standing still
            if (keys.struggleRequested) {
                keys.struggleRequested = false;     // one press = one hit
                grabber.struggle();
            }
            if (mount != null) {
                mount.followRider();                // the animal stays under us
            }
            return;
        }
        keys.struggleRequested = false;             // SPACE does nothing when we are free

        // TALKING: we stand still and E turns the page. This comes BEFORE the interaction code
        // below, so the press that STARTED the talk is not also used to turn the first page.
        if (conversation != null) {
            interactTarget = null;
            updateAnimation(false);
            if (keys.interactRequested) {
                keys.interactRequested = false;     // one press = one page
                advanceConversation();
            }
            if (mount != null) {
                mount.followRider();
            }
            return;
        }

        // What can E do right now?
        //  - riding: the only thing is getting off (the HUD shows "[E] Dismount")
        //  - walking: use the closest thing nearby (if any)
        if (mount != null) {
            interactTarget = mount;
        } else {
            interactTarget = manager.findInteractableNear(this);
        }
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

        updateAnimation(isMoving);                   // now lives in Creature

        if (isMoving) {
            facing = Direction.fromInput(dirX, dirY);    // 'facing' lives in Creature

            // 3. Move. moveWithCollision (in Creature) checks X and Y separately,
            //    so the player slides along walls and around trees.
            //    'speed' lives in Creature: it is the walk speed, or the ride speed while riding.
            double dx = dirX / length * speed;
            double dy = dirY / length * speed;
            moveWithCollision(dx, dy, manager);
        }

        // 4. If we ride, the animal copies our position, direction and animation NOW,
        //    after we moved. This must run whether we moved or not, so it has no "return" before it.
        if (mount != null) {
            mount.followRider();
        }
    }

    // ----- Interaction -----

    // Uses whatever we found nearby (if anything)
    private void tryInteract() {
        if (interactTarget != null) {
            interactTarget.interact(this);
        }
    }

    // The HUD reads this to show the prompt
    public Interactable getInteractTarget() { return interactTarget; }

    // ----- Riding -----

    // Called by Carabao.mount(). From now on we move at the animal's speed.
    void startRiding(Carabao carabao) {
        mount = carabao;
        speed = carabao.getRideSpeed();
    }

    // Called by Carabao.dismount(). Back to walking.
    void stopRiding() {
        mount = null;
        speed = WALK_SPEED;
    }

    public boolean isRiding() { return mount != null; }

    // ----- Talking -----

    // Called by a Talkable (Villager.interact) when we press E near it
    void startConversation(Talkable partner) {
        conversation = new Conversation(partner);
    }

    // E on the last line ends the talk and tells the other person (it may react: give a mission...)
    private void advanceConversation() {
        if (!conversation.advance()) {
            Talkable partner = conversation.getPartner();
            conversation = null;
            partner.onConversationEnd(this);
        }
    }

    public boolean isTalking() { return conversation != null; }

    // The HUD reads this to draw the dialogue box
    public Conversation getConversation() { return conversation; }

    // ----- TEMPORARY wood counter (replace with the inventory later) -----

    public void addWood(int amount) { wood += amount; }
    public int getWood() { return wood; }

    // Takes the wood if we have enough. Returns true if it worked.
    public boolean spendWood(int amount) {
        if (wood < amount) return false;
        wood -= amount;
        return true;
    }

    public int getDeathCount() { return deathCount; }

    // ----- Being grabbed -----

    // Called by Tiyanak when it lands on us
    void startGrab(Tiyanak enemy) {
        grabber = enemy;
        conversation = null;          // an ambush interrupts any talk
    }

    // Called by Tiyanak when it dies or lets go
    void endGrab() {
        grabber = null;
    }

    public boolean isGrabbed() { return grabber != null; }

    // The HUD reads this to show how much strength the enemy has left
    public Creature getGrabber() { return grabber; }

    // ----- Draw -----

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        // While riding, the carabao draws us on its back. If we drew ourselves too,
        // there would be two of us.
        if (mount != null) return;

        // 1 = fully visible, 0 = invisible. While dying, the sprite is gone by the halfway point.
        float alpha = 1f;
        if (state == PlayerState.DYING) {
            alpha = Math.max(0f, 1f - 2f * stateTimer / DYING_TICKS);
        }
        drawSprite(g2, camX, camY, alpha);           // the drawing itself lives in Creature
    }

    // Called by the carabao to draw us on its back, 'lift' pixels higher than our feet
    void drawAsRider(Graphics2D g2, int camX, int camY, int lift) {
        drawSprite(g2, camX, camY, 1f, -lift);       // negative offset = UP on the screen
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
        if (mount != null) {
            mount.dismount();         // you fall off. dismount() also calls stopRiding() on us.
        }
        if (grabber != null) {
            grabber.release();        // the enemy lets go and goes back to hiding
        }
        conversation = null;          // a talk ends if we die
        deathCount++;                 // villagers are told about it by the EntityManager
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
            // screen is still fading in. The invulnerability timer covers that.
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
}