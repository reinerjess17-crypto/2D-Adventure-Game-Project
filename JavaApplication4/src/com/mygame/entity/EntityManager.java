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

    public void update() {
        // 1. Let the new arrivals in
        entities.addAll(pendingAdd);
        pendingAdd.clear();

        // 2. Update everyone. This single loop works for ANY kind of entity.
        for (Entity e : entities) {
            e.update(this);
        }

        // 3. Throw away removed entities
        entities.removeIf(e -> !e.isActive());
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        // Sort by the entity's FEET (y + height): the one lower on screen draws last,
        // so it appears in FRONT. This is how a player can walk behind a tree.
        entities.sort(Comparator.comparingDouble(e -> e.getY() + e.getHeight()));
        for (Entity e : entities) {
            e.draw(g2, camX, camY);
        }
    }
 
    public Player getPlayer() { return player; }
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
    public TileMap getMap() { return map; }
}