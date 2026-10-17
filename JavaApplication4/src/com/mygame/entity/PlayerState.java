package com.mygame.entity;

// The phases of the player's life. Player uses this to decide what to do each tick.
// It used to be a private enum INSIDE Player. As its own file it matches your UML,
// and other classes (the HUD, a quest, a villager) can read it later if they need to.
public enum PlayerState {
    ALIVE,        // normal play: the keys move the player
    DYING,        // the sprite fades out and the screen goes black; the keys are ignored
    RESPAWNING    // the player is back at the spawn point and the screen fades back in
}