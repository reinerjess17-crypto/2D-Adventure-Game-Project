package com.mygame.entity;

// A promise: "if you touch me, I hurt you, and this is how much".
// Cacti sign it now. Enemies and dangerous animals will sign it later.
// The EntityManager looks for this promise to know which entities can cause damage.
public interface DamageSource {

    // How much damage one touch does
    int getContactDamage();
}