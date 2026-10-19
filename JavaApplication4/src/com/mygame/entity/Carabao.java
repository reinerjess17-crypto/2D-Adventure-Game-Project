package com.mygame.entity;

import com.mygame.main.GameSettings;

// A carabao (water buffalo): slow and calm. For now it only wanders.
// Later it becomes a FriendlyAnimal that can be tamed and ridden.
public class Carabao extends Animal {

    private static final int MAX_HEALTH = 60;
    private static final double WALK_SPEED = 0.5;                       // pixels per tick: slow (30 per second)
    private static final double WANDER_RADIUS = 5 * GameSettings.TILE_SIZE;   // stays within 5 tiles of home

    // ONE SpriteSet shared by every carabao, loaded once. Sprites are only READ, never changed,
    // so sharing is safe. If each carabao loaded its own, 20 carabaos would read
    // 20 x (8 idle + 8 x 4 walk) image files from disk.
    // The folder must contain: idle_<direction>.png and walk_<direction>_<frame>.png
    private static final SpriteSet SPRITES = SpriteSet.load("/res/carabao", 4);

    public Carabao(double x, double y) {
        // The footprint is one tile (32x32). The PICTURE can be bigger (a carabao is large):
        // drawSprite centers it and puts its bottom on the bottom of the footprint.
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              MAX_HEALTH, WALK_SPEED, SPRITES, WANDER_RADIUS);

        walkTicksPerFrame = 12;    // a slow animal also moves its legs slowly (default is 8)
    }
}