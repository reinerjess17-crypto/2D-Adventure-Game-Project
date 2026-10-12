package com.mygame.gui;

import com.mygame.entity.Player;
import com.mygame.main.GameSettings;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

// Everything drawn on top of the game that is NOT part of the world:
// the life bar now, and later items, dialogue boxes and menus.
public class Hud {

    // Life bar position and size on the 640x360 canvas
    private static final int BAR_X = 10;
    private static final int BAR_Y = 10;
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 12;

    public void draw(Graphics2D g2, Player player) {
        drawHealthBar(g2, player);
        drawFadeOverlay(g2, player);   // last, so it covers the bar too
    }

    private void drawHealthBar(Graphics2D g2, Player player) {
        // What fraction of the health is left? 0.0 (empty) to 1.0 (full)
        double ratio = (double) player.getHealth() / player.getMaxHealth();
        int fillWidth = (int) Math.round(BAR_WIDTH * ratio);

        // Dark background: this is what you see where health is missing
        g2.setColor(new Color(50, 0, 0));
        g2.fillRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        // The filled part: green normally, red when health is low
        g2.setColor(ratio > 0.3 ? new Color(70, 200, 70) : new Color(220, 50, 50));
        g2.fillRect(BAR_X, BAR_Y, fillWidth, BAR_HEIGHT);

        // Border. drawRect covers one pixel MORE than its width and height,
        // so we start one pixel earlier and make it one bigger to wrap the fill exactly.
        g2.setColor(Color.WHITE);
        g2.drawRect(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 1, BAR_HEIGHT + 1);
    }

    // A black layer over the whole screen. Its transparency comes from the player.
    private void drawFadeOverlay(Graphics2D g2, Player player) {
        float fade = Math.min(1f, player.getScreenFade());
        if (fade <= 0f) return;                    // nothing to draw

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT);
        g2.setComposite(oldComposite);
    }
}
