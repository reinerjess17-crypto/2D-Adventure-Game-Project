package com.mygame.entity;

import java.awt.image.BufferedImage;

// A plant the player can use on purpose (press E): berries, trees.
// It is a Plant (so it has the cooldown cycle) AND it signs the Interactable promise.
public abstract class UsefulPlant extends Plant implements Interactable {

    protected final double interactRange;   // how close the player must be (pixels, center to center)
    protected final String promptText;      // what the HUD shows, e.g. "Pick berry"

    protected UsefulPlant(double x, double y, int width, int height,
                          BufferedImage image, int cooldownTicks,
                          double interactRange, String promptText) {
        super(x, y, width, height, image, cooldownTicks);
        this.interactRange = interactRange;
        this.promptText = promptText;
    }

    // Possible only when the plant is ready AND the player is close enough
    @Override
    public boolean canInteract(Player player) {
        return ready && distanceTo(player) <= interactRange;   // distanceTo comes from Entity
    }

    // Pressing E: the safety check again, then use() (from Plant) does the rest
    @Override
    public void interact(Player player) {
        if (canInteract(player)) {
            use(player);
        }
    }

    @Override
    public String getPrompt() {
        return promptText;
    }
}
