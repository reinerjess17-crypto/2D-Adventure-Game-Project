package com.mygame.entity;


import com.mygame.item.Item;
import com.mygame.main.GameSettings;

// An animal that does not attack. The player can TAME it with an item (press E),
// and from then on it follows the player around.
// It is an Animal (wandering, state, home) AND it signs the Interactable promise.
public abstract class FriendlyAnimal extends Animal implements Interactable {

    // A tamed animal starts walking toward the player when it is farther than this...
    private static final double FOLLOW_START_DISTANCE = 3 * GameSettings.TILE_SIZE;
    // ...and stops when it gets this close. Two different numbers on purpose:
    // with only one, the animal would flicker between "walk" and "stand" at the border.
    private static final double FOLLOW_STOP_DISTANCE = 1.5 * GameSettings.TILE_SIZE;

    protected boolean tamed = false;
    protected final Item tameItem;          // the item that makes this animal trust the player
    protected final double interactRange;   // how close the player must be (pixels, center to center)

    // How fast it walks while following. The wandering speed is usually slow and relaxed,
    // but a follower must keep up with the player, so it gets its own (faster) number.
    protected double followSpeed;

    protected FriendlyAnimal(double x, double y, int width, int height,
                             int maxHealth, double speed, SpriteSet sprites, double wanderRadius,
                             Item tameItem, double interactRange) {
        super(x, y, width, height, maxHealth, speed, sprites, wanderRadius);
        this.tameItem = tameItem;
        this.interactRange = interactRange;
        this.followSpeed = speed;           // by default the same; a child can raise it
    }

    // ----- Behavior -----

    // This REPLACES the "just wander" of Animal: a wild friendly animal wanders,
    // a tamed one follows the player.
    @Override
    protected void updateBehavior(EntityManager manager) {
        if (tamed) {
            followPlayer(manager);
        } else {
            wander(manager);                // the inherited loop from Animal
        }
    }

    private void followPlayer(EntityManager manager) {
        Player player = manager.getPlayer();
        double distance = distanceTo(player);       // from Entity

        // Start or stop following depending on the distance. Between the two numbers,
        // the animal keeps doing whatever it was already doing.
        if (distance > FOLLOW_START_DISTANCE) {
            animalState = AnimalState.FOLLOW;
        } else if (distance <= FOLLOW_STOP_DISTANCE) {
            animalState = AnimalState.IDLE;
        }

        if (animalState == AnimalState.FOLLOW) {
            // The vector "from me to the player" = player position minus my position.
            // walk() turns it into a step and makes the animal face that way.
            boolean moved = walk(player.getCenterX() - getCenterX(),
                                 player.getCenterY() - getCenterY(),
                                 followSpeed, manager);
            updateAnimation(moved);
        } else {
            updateAnimation(false);                 // standing next to the player
        }
    }

    // ----- Taming -----

    // Tries to tame the animal with an item. Returns true if it worked.
    public boolean tryTame(Item item) {
        if (tamed) return false;                    // already tamed
        if (item == null) return false;

        // Items only have a name for now, so we compare names.
        if (!item.getName().equals(tameItem.getName())) return false;   // wrong food

        tamed = true;
        animalState = AnimalState.IDLE;             // stop wandering; next tick it decides to follow
        return true;
    }

    public boolean isTamed() { return tamed; }

    // ----- Interactable -----

    // Only a wild animal can be tamed, and only when the player is close
    @Override
    public boolean canInteract(Player player) {
        return !tamed && distanceTo(player) <= interactRange;
    }

    // TEMPORARY: there is no inventory yet, so we pretend the player is carrying the item.
    // When the inventory exists, this becomes:
    //   if (player.getInventory().remove(tameItem)) { tryTame(tameItem); }
    @Override
    public void interact(Player player) {
        if (canInteract(player)) {
            tryTame(tameItem);
        }
    }

    // A child overrides this with a nicer text, for example "Tame carabao"
    @Override
    public String getPrompt() {
        return "Tame";
    }
}