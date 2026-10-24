package com.mygame.entity;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

// A person who lives in the world and can be talked to.
// It is a Creature (it has a position, health, sprites) AND Talkable.
// It stands near its home and turns to face the player when he comes close.
public class Villager extends Creature implements Talkable {

    protected final String name;
    protected final List<String> dialogueLines;       // what it says (a child can replace getDialogue)
    protected final double homeX;                     // where it lives
    protected final double homeY;
    protected final double interactRange;             // how close the player must be to talk (pixels)

    // dialogue and respawn code needs the map sometimes, so we remember the manager of the last update
    protected EntityManager lastManager;

    public Villager(double x, double y, int width, int height, int maxHealth, double speed,
                    SpriteSet sprites, String name, List<String> dialogueLines, double interactRange) {
        super(x, y, width, height, maxHealth, speed, sprites);
        this.name = name;
        this.dialogueLines = new ArrayList<>(dialogueLines);
        this.homeX = x;
        this.homeY = y;
        this.interactRange = interactRange;

        solid = true;       // people are not walked through: the player and animals go around them
    }

    // ----- Update and draw -----

    @Override
    public void update(EntityManager manager) {
        lastManager = manager;
        tickTimers();

        // Turn toward the player when he is near (a bit farther than the talking range)
        Player player = manager.getPlayer();
        if (distanceTo(player) <= interactRange * 1.5) {
            facing = Direction.fromVector(player.getCenterX() - getCenterX(),
                                          player.getCenterY() - getCenterY());
        }
        updateAnimation(false);       // always the standing picture
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        drawSprite(g2, camX, camY);
    }

    // ----- Talkable (and so Interactable) -----

    @Override
    public boolean canInteract(Player player) {
        return !player.isTalking() && distanceTo(player) <= interactRange;
    }

    // Pressing E: the player starts a conversation with us
    @Override
    public void interact(Player player) {
        if (canInteract(player)) {
            player.startConversation(this);
        }
    }

    @Override
    public String getPrompt() {
        return "Talk to " + name;
    }

    @Override
    public String getSpeakerName() {
        return name;
    }

    @Override
    public List<String> getDialogue() {
        return dialogueLines;
    }

    // ----- Events -----

    // The EntityManager calls this on every villager when the player dies.
    // The default does nothing; a villager that cares (Old Tanyo) overrides it.
    public void onPlayerDied(Player player) {
    }

    // A villager that is killed simply disappears
    @Override
    protected void onDeath() {
        remove();
    }
}