package com.mygame.entity;

import com.mygame.main.GameSettings;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Old Tanyo, the old man at the edge of the land. He is the first person the player meets:
//   - he warns about the tiyanak and explains what to do if it grabs you,
//   - he gives the first mission (bring him wood),
//   - after the first talk his hut becomes the place where the player wakes up after dying,
//   - he comments when the player keeps dying.
// His quest has stages, and what he says depends on the stage.
//
// Inheritance chain: Entity > Creature > Villager > OldTanyo
public class OldTanyo extends Villager {

    // He never walks, so his sprites have NO walking frames: each direction is one single picture.
    // The folder /res/old_tanyo must contain old_tanyo_<direction>.png for the 8 directions
    // (the same picture may be used for several directions if you do not draw all 8).
    private static final SpriteSet SPRITES = SpriteSet.loadSheets("/res/old_tanyo", "old_tanyo", 0);

    // missions[stage] = the task of that stage
    private static final String[] MISSIONS = {
        "Meet Old Tanyo",
        "Gather 3 wood and bring it to Old Tanyo",
        "Tame a carabao"
    };
    private static final int WOOD_NEEDED = 3;

    private int questStage = 0;                    // 0 = not met yet, 1 = wood mission, 2 = carabao mission
    private String currentMission = MISSIONS[0];
    private boolean hasGivenFirstMission = false;

    private int recentDeaths = 0;                  // times the player died since the last talk
    private Player talkingPlayer;                  // who is talking to him (his lines can depend on the player)

    public OldTanyo(double x, double y) {
        super(x, y, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE, 80, 0.0, SPRITES,
              "Old Tanyo", Arrays.asList("..."), 56);
    }

    // ----- Talking -----

    // Remember WHO is talking before the normal Villager behavior starts the conversation
    @Override
    public void interact(Player player) {
        talkingPlayer = player;
        super.interact(player);
    }

    // What he says now depends on how far the quest has gone.
    // Overrides Villager.getDialogue(): same method, different answer (polymorphism).
    @Override
    public List<String> getDialogue() {
        List<String> lines = new ArrayList<>();

        if (recentDeaths > 0) {
            lines.add("You look like you met something nasty. Dying is no shame, hijo. I will keep my fire lit.");
        }

        switch (questStage) {
            case 0:
                //"Aba! A traveler. Come closer, hijo, I do not bite."
                lines.add("Hoy, gago ba't ka nandito, anong ginagawa mo dito?");
                lines.add("They call me Old Tanyo. I have lived at the edge of this land longer than anyone.");
                lines.add("Listen well: not every bush is only a bush. Some hide a crying thing that jumps.");
                lines.add("If it grabs you, do not stand still. Press SPACE again and again until it lets go.");
                lines.add("And if you fall, you will wake up near my hut. That much I can promise.");
                lines.add("Now a favor for an old man: bring me 3 wood from the trees. My fire is getting low.");
                break;
            case 1:
                int wood = (talkingPlayer == null) ? 0 : talkingPlayer.getWood();
                if (wood >= WOOD_NEEDED) {
                    lines.add("Ah, you brought the wood! Give it here... that will keep the fire going all night.");
                    lines.add("You are not as helpless as you look. Next: I hear a carabao will trust anyone who feeds it rice.");
                    lines.add("Find one and make friends with it. A good ride saves a lot of walking.");
                } else {
                    lines.add("Still no wood? The trees are everywhere, hijo. Press E near one to gather it.");
                    lines.add("I need " + WOOD_NEEDED + ". You carry " + wood + ".");
                }
                break;
            default:
                lines.add("Have you made friends with a carabao yet? " + currentMission + ".");
                lines.add("Be careful out there, hijo.");
                break;
        }
        return lines;
    }

    // Called by the Player when the last line has been read
    @Override
    public void onConversationEnd(Player player) {
        recentDeaths = 0;

        if (questStage == 0) {
            giveMission();
            // His hut becomes the place where the player wakes up after dying
            Point2D spot = getRespawnPosition();
            player.setSpawnPoint(spot.getX(), spot.getY());
        } else if (questStage == 1 && player.spendWood(WOOD_NEEDED)) {
            advanceQuest();                // the wood is taken and the next mission starts
        }
        talkingPlayer = null;
    }

    // ----- Quest -----

    // Starts the quest: stage 0 -> 1
    public void giveMission() {
        hasGivenFirstMission = true;
        advanceQuest();
    }

    // Moves to the next stage. Other systems (an inventory, a quest log) can call this too.
    public void advanceQuest() {
        if (questStage < MISSIONS.length - 1) {
            questStage++;
            currentMission = MISSIONS[questStage];
        }
    }

    public int getQuestStage() { return questStage; }
    public String getCurrentMission() { return currentMission; }
    public boolean hasGivenFirstMission() { return hasGivenFirstMission; }

    // ----- Respawn -----

    // A free spot next to his hut, where the player will wake up after dying.
    // It tries below him first, then right, left and above, and uses the first one that is not water.
    public Point2D getRespawnPosition() {
        int tile = GameSettings.TILE_SIZE;
        int[][] offsets = { {0, tile}, {tile, 0}, {-tile, 0}, {0, -tile} };

        if (lastManager != null) {
            for (int[] offset : offsets) {
                double spotX = homeX + offset[0];
                double spotY = homeY + offset[1];
                // check the middle of that tile
                if (!lastManager.getMap().isSolidAtPixel(spotX + tile / 2.0, spotY + tile / 2.0)) {
                    return new Point2D.Double(spotX, spotY);
                }
            }
        }
        return new Point2D.Double(homeX, homeY + tile);      // nothing found: below him
    }

    // The EntityManager tells him when the player dies; he remembers it for the next talk
    @Override
    public void onPlayerDied(Player player) {
        recentDeaths++;
    }
}