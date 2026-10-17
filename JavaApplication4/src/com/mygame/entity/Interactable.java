package com.mygame.entity;

// A promise: "the player can press E near me and something happens".
// Berries, trees, villagers and tamed animals will all sign this promise,
// even though they are completely different classes.
// The EntityManager doesn't care WHAT they are, only that they are Interactable.
public interface Interactable {

    // Does the thing. Called when the player presses E.
    void interact(Player player);

    // Is it possible right now? (close enough, ready, not used up...)
    boolean canInteract(Player player);

    // The text for the HUD, for example "Pick berry". The HUD adds the "[E]".
    String getPrompt();
}