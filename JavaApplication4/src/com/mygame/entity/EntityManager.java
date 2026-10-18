package com.mygame.entity;

import com.mygame.world.TileMap;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Owns every entity in the world and runs them all each frame.
public class EntityManager {

    private final List<Entity> entities = new ArrayList<>();
    // New entities wait here, because adding to a list WHILE looping over it crashes Java.
    private final List<Entity> pendingAdd = new ArrayList<>();

    private final Player player;
    private final TileMap map;

    public EntityManager(TileMap map, Player player) {
        this.map = map;
        this.player = player;
        add(player);                    // the player is an entity too
    }

    public void add(Entity entity) {
        pendingAdd.add(entity);
    }

    // ----- Update -----

    public void update() {
        // 1. Let the new arrivals in
        entities.addAll(pendingAdd);
        pendingAdd.clear();

        // 2. Update everyone. This single loop works for ANY kind of entity.
        for (Entity e : entities) {
            e.update(this);
        }

        // 3. Now that everyone has moved, check who touches the player
        checkContactDamage();

        // 4. Throw away removed entities
        removeInactive();
    }

    // Hurts the player if they touch anything that can cause damage
    private void checkContactDamage() {
        if (!player.isAlive()) return;                 // nothing to hurt while dying

        for (Entity e : entities) {
            if (!(e instanceof DamageSource)) continue;    // only things that can hurt
            if (!e.overlaps(player)) continue;             // hitboxes must touch

            if (e instanceof HarmfulPlant) {
                ((HarmfulPlant) e).onTouch(player);        // the plant applies its own cooldown
            }
            // Enemies and dangerous animals will get their own branch when we build them
        }
    }

    private void removeInactive() {
        entities.removeIf(e -> !e.isActive());
    }

    // ----- Draw -----

    public void draw(Graphics2D g2, int camX, int camY) {
        // Sort by the entity's FEET (y + height): the one lower on screen draws last,
        // so it appears in FRONT. This is how a player can walk behind a tree.
        entities.sort(Comparator.comparingDouble(e -> e.getY() + e.getHeight()));
        for (Entity e : entities) {
            e.draw(g2, camX, camY);
        }
    }

    // ----- Searching -----

    // Finds the CLOSEST thing the player can interact with right now, or null if none.
    public Interactable findInteractableNear(Player player) {
        Interactable best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Entity e : entities) {
            if (!(e instanceof Interactable)) continue;       // trees, cacti, boars... skip
            Interactable candidate = (Interactable) e;        // treat it as the promise it signed
            if (!candidate.canInteract(player)) continue;     // too far, or not ready

            double distance = e.distanceTo(player);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    // Returns every active entity whose center is within 'radius' pixels of the point (x, y).
    // Other code uses this to avoid looping over every entity in the world.
    public List<Entity> getEntitiesNear(double x, double y, double radius) {
        List<Entity> result = new ArrayList<>();
        for (Entity e : entities) {
            if (!e.isActive()) continue;
            double dx = e.getCenterX() - x;
            double dy = e.getCenterY() - y;
            // Compare SQUARED distances: same answer as Pythagoras, but no slow square root
            if (dx * dx + dy * dy <= radius * radius) {
                result.add(e);
            }
        }
        return result;
    }

    // ----- Getters -----

    public Player getPlayer() { return player; }
    public TileMap getMap() { return map; }
}