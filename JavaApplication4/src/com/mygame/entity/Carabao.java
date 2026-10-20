package com.mygame.entity;

import com.mygame.item.Item;
import com.mygame.main.GameSettings;

import java.awt.Graphics2D;

// A carabao (water buffalo): slow and calm. In the wild it wanders. Give it rice
// (press E) and it becomes tame and follows you. Once tame, you can ride it.
public class Carabao extends FriendlyAnimal {

    private static final int MAX_HEALTH = 60;
    private static final double WALK_SPEED = 0.5;                       // pixels per tick: slow (30 per second)
    private static final double FOLLOW_SPEED = 1.8;                     // a bit slower than the player (2.0)
    private static final double RIDE_SPEED = 3.0;                       // faster than walking (2.0): that is the point of riding
    private static final double WANDER_RADIUS = 5 * GameSettings.TILE_SIZE;   // stays within 5 tiles of home
    private static final double INTERACT_RANGE = 48;                    // 1.5 tiles

    private static final int WALK_TICKS_PER_FRAME = 12;                 // slow legs while wandering
    private static final int RIDE_TICKS_PER_FRAME = 7;                  // quicker legs while carrying you

    // How many pixels higher than the carabao's feet the rider is drawn, so the rider
    // looks like it sits on the back. Change this number until it looks right with your art.
    private static final int RIDER_LIFT = 12;

    // ONE SpriteSet shared by every carabao, loaded once.
    // loadSheets: ONE strip per direction, with 5 frames side by side (1 idle + 4 walk).
    // The folder /res/carabao must contain carabao_down.png, carabao_down_left.png,
    // carabao_left.png, carabao_up_left.png, carabao_up.png, carabao_up_right.png,
    // carabao_right.png and carabao_down_right.png
    private static final SpriteSet SPRITES = SpriteSet.loadSheets("/res/carabao", "carabao", 4);

    // The food that makes a carabao trust you
    private static final Item RICE = new Item("Rice");

    // ----- Riding -----
    private boolean ridden = false;
    private Player rider;                           // who is on its back (null when nobody)
    private final double rideSpeed = RIDE_SPEED;

    public Carabao(double x, double y) {
        // The footprint is one tile (32x32). The PICTURE can be bigger (a carabao is large):
        // drawSprite centers it and puts its bottom on the bottom of the footprint.
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE,
              MAX_HEALTH, WALK_SPEED, SPRITES, WANDER_RADIUS,
              RICE, INTERACT_RANGE);

        followSpeed = FOLLOW_SPEED;   // faster than the lazy wander speed, so it can keep up
        walkTicksPerFrame = WALK_TICKS_PER_FRAME;
    }

    // ----- Mounting -----

    // The player climbs on. Only a tamed, free carabao can be ridden.
    public void mount(Player player) {
        if (!tamed || ridden) return;

        ridden = true;
        rider = player;
        animalState = AnimalState.IDLE;
        walkTicksPerFrame = RIDE_TICKS_PER_FRAME;

        player.setPosition(x, y);       // the player hops onto the carabao's spot
        player.startRiding(this);       // tells the player: "you are riding, move at my ride speed"
    }

    // The player climbs off. The carabao stays where it is and goes back to following.
    public void dismount() {
        if (!ridden) return;

        Player formerRider = rider;
        ridden = false;
        rider = null;
        walkTicksPerFrame = WALK_TICKS_PER_FRAME;

        formerRider.stopRiding();
    }

    public boolean isRidden() { return ridden; }
    public double getRideSpeed() { return rideSpeed; }

    // The PLAYER calls this at the end of its own update, after it has moved.
    // The carabao copies the player's position, direction and walking animation.
    // Letting the player call it (instead of the carabao updating itself) means the two
    // always match exactly, whatever order the manager updates them in.
    // No "public" or "private" on purpose: only classes in this package can call it.
    void followRider() {
        if (!ridden) return;
        setPosition(rider.getX(), rider.getY());
        facing = rider.getFacing();
        updateAnimation(rider.isMoving());
    }

    // ----- Behavior -----

    // While ridden, the carabao does not wander or follow: the rider moves it.
    @Override
    protected void updateBehavior(EntityManager manager) {
        if (ridden) return;
        super.updateBehavior(manager);      // wild: wander. tamed: follow the player.
    }

    // ----- Interaction -----

    // Wild: can be tamed (FriendlyAnimal). Tamed: can be ridden. Ridden: can be dismounted.
    @Override
    public boolean canInteract(Player player) {
        if (ridden) return player == rider;                       // only the rider can get off
        if (tamed) return distanceTo(player) <= interactRange;    // close enough to climb on
        return super.canInteract(player);                         // wild: close enough to tame
    }

    @Override
    public void interact(Player player) {
        if (!canInteract(player)) return;

        if (ridden) {
            dismount();
        } else if (tamed) {
            mount(player);
        } else {
            super.interact(player);          // taming
        }
    }

    @Override
    public String getPrompt() {
        if (ridden) return "Dismount";
        return tamed ? "Ride carabao" : "Tame carabao";
    }

    // ----- Draw -----

    // The carabao draws itself, then its rider on top and a little higher.
    // (The player skips its own draw while riding, so it is never drawn twice.)
    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        drawSprite(g2, camX, camY);
        if (ridden) {
            rider.drawAsRider(g2, camX, camY, RIDER_LIFT);
        }
    }
}