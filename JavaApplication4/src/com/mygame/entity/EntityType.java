package com.mygame.entity;

// A NAME for every kind of entity that can be spawned into the world.
// Other code says "give me a TREE here" without knowing the Tree class exists:
// the EntityFactory turns the name into a real object.
//
// Only the types that are already built are listed. When we create the animals,
// enemies and villagers, we add WILD_BOAR, CARABAO, TIYANAK... here
// (and one line for each in EntityFactory).
public enum EntityType {
    SHARP_CACTUS,
    LIFE_BERRY,
    TREE,
    CARABAO,
    WILDBOAR,
}