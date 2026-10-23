package com.mygame.entity;

// The three phases of a Tiyanak ambush.
// (Your first diagram also had CRAWLING, CRYING, CROUCHING and STUNNED.
//  The new behavior does not need them: it hides, leaps once, and holds on.)
public enum TiyanakState {
    HIDDEN,     // invisible inside a bush or tall grass, waiting for someone to come close
    LEAPING,    // in the air, jumping at the player
    LATCHED     // stuck on the player, biting and draining life until it is shaken off
}