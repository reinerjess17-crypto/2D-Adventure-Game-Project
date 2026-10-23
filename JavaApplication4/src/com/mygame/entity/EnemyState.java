package com.mygame.entity;

// What a generic enemy is doing right now (used by the Enemy base class).
// A specific enemy can have its own, more detailed states on top of these:
// the Tiyanak, for example, has TiyanakState (HIDDEN, LEAPING, LATCHED).
public enum EnemyState {
    IDLE,       // nothing to do
    CHASE,      // running after a target
    ATTACK,     // hitting the target
    COOLDOWN,   // resting after an attack
    STUNNED     // cannot act for a while (see WeakEnemy.stun)
}