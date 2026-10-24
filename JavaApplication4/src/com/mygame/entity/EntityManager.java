package com.mygame.entity;

import com.mygame.main.GameSettings;
import com.mygame.world.TileMap;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Owns every entity in the world and runs them all each frame.
public class EntityManager {

    // scatter() leaves this much space free around the player's start, so the player
    // never begins the game inside a tree (a solid entity would trap them)
    private static final double CLEAR_RADIUS = 4 * GameSettings.TILE_SIZE;

    private final List<Entity> entities = new ArrayList<>();
    // New entities wait here, because adding to a list WHILE looping over it crashes Java.
    private final List<Entity> pendingAdd = new ArrayList<>();

    private final Player player;
    private final TileMap map;

    private final Random random = new Random();

    // The last number of deaths we knew about. When the player's number grows, he just died.
    private int knownDeaths = 0;

    // Tiles that already got an entity from scatter(). It stops a berry from
    // spawning inside a tree. Each tile is stored as ONE number: row * columns + col.
    private final Set<Long> occupiedTiles = new HashSet<>();

    public EntityManager(TileMap map, Player player) {
        this.map = map;
        this.player = player;
        add(player);                    // the player is an entity too
    }

    public void add(Entity entity) {
        pendingAdd.add(entity);
    }

    // ----- Spawning -----

    // Creates an entity from its TYPE and adds it to the world. Returns it, in case
    // the caller wants to keep a reference (for example to configure it).
    public Entity spawn(EntityType type, double x, double y) {
        Entity entity = EntityFactory.create(type, x, y);
        add(entity);
        return entity;
    }

    // Sprinkles entities over the map: every tile of type 'onTile' has a
    // 'chance' (0.0 to 1.0) of getting one. Example: scatter(TREE, TileMap.LAND, 0.01)
    // puts a tree on about 1 of every 100 grass tiles.
    public void scatter(EntityType type, byte onTile, double chance) {
        int tileSize = GameSettings.TILE_SIZE;

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                // Cheapest checks first: most tiles are the wrong kind or lose the dice roll
                if (map.getTile(col, row) != onTile) continue;
                if (random.nextDouble() >= chance) continue;

                long tileKey = (long) row * map.getCols() + col;
                if (occupiedTiles.contains(tileKey)) continue;   // something is already here

                double worldX = col * tileSize;
                double worldY = row * tileSize;
                if (isNearPlayer(worldX, worldY)) continue;      // keep the start clear

                spawn(type, worldX, worldY);
                occupiedTiles.add(tileKey);
            }
        }
    }

    // Is this world position inside the clear zone around the player?
    private boolean isNearPlayer(double worldX, double worldY) {
        return Math.abs(worldX - player.getX()) < CLEAR_RADIUS
            && Math.abs(worldY - player.getY()) < CLEAR_RADIUS;
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
        checkPlayerDeath();

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

    // If the player died since the last tick, tell every villager (they may comment on it later)
    private void checkPlayerDeath() {
        if (player.getDeathCount() == knownDeaths) return;
        knownDeaths = player.getDeathCount();

        for (Entity e : entities) {
            if (e instanceof Villager) {
                ((Villager) e).onPlayerDied(player);
            }
        }
    }

    private void removeInactive() {
        entities.removeIf(e -> !e.isActive());
    }

    // ----- Draw -----

    public void draw(Graphics2D g2, int camX, int camY) {
        // Sort by the entity's FEET (see Entity.getSortY): the one lower on screen draws last,
        // so it appears in FRONT. This is how a player can walk behind a tree.
        entities.sort(Comparator.comparingDouble(Entity::getSortY));
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