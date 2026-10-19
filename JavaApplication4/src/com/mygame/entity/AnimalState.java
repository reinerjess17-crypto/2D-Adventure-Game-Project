package com.mygame.entity;

// What an animal is doing right now. The animal checks this every tick to decide how to act.
// Every state in your UML is listed, but only IDLE and WANDER are used in this step.
public enum AnimalState {
    IDLE,       // standing still for a while
    WANDER,     // walking in a random direction
    CHASE,      // running after a target          (dangerous animals, later)
    ATTACK,     // hitting the target              (dangerous animals, later)
    COOLDOWN,   // resting after an attack         (dangerous animals, later)
    FOLLOW      // walking behind its owner        (tamed animals, later)
}