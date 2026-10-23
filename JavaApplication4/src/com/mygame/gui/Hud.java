package com.mygame.gui;

import com.mygame.entity.Creature;
import com.mygame.entity.Interactable;
import com.mygame.entity.Player;
import com.mygame.main.GameSettings;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

// Everything drawn on top of the game that is NOT part of the world:
// the life bar, the interaction prompt, the struggle bar, and later items, dialogue and menus.
public class Hud {

    // Life bar position and size on the 640x360 canvas
    private static final int BAR_X = 10;
    private static final int BAR_Y = 10;
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 12;

    public void draw(Graphics2D g2, Player player) {
        drawHealthBar(g2, player);
        drawInteractPrompt(g2, player);
        drawStruggle(g2, player);
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

    // Shows "[E] Pick berry" at the bottom center while something is in reach
    private void drawInteractPrompt(Graphics2D g2, Player player) {
        Interactable target = player.getInteractTarget();
        if (target == null) return;                          // nothing nearby: draw nothing

        drawCenteredText(g2, "[E] " + target.getPrompt(), GameSettings.BASE_HEIGHT - 30);
    }

    // While an enemy holds the player: a message and a bar with the enemy's remaining strength.
    // Every SPACE press makes the bar shrink; when it is empty the enemy lets go.
    private void drawStruggle(Graphics2D g2, Player player) {
        Creature grabber = player.getGrabber();
        if (grabber == null) return;                         // not grabbed: draw nothing

        int textY = GameSettings.BASE_HEIGHT - 48;
        drawCenteredText(g2, "Mash [SPACE] to shake it off!", textY);

        int barWidth = 120;
        int barHeight = 8;
        int barX = (GameSettings.BASE_WIDTH - barWidth) / 2;
        int barY = textY + 12;
        double ratio = (double) grabber.getHealth() / grabber.getMaxHealth();

        g2.setColor(new Color(50, 0, 0));
        g2.fillRect(barX, barY, barWidth, barHeight);
        g2.setColor(new Color(230, 160, 40));
        g2.fillRect(barX, barY, (int) Math.round(barWidth * ratio), barHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(barX - 1, barY - 1, barWidth + 1, barHeight + 1);
    }

    // Draws a line of text centered on the screen, on a dark see-through box
    // so it can be read on any background. The y is the text's baseline.
    private void drawCenteredText(Graphics2D g2, String text, int y) {
        FontMetrics metrics = g2.getFontMetrics();           // lets us measure the text
        int textWidth = metrics.stringWidth(text);
        int x = (GameSettings.BASE_WIDTH - textWidth) / 2;   // centered

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x - 6, y - metrics.getAscent() - 2,
                textWidth + 12, metrics.getHeight() + 4, 6, 6);
        g2.setComposite(oldComposite);                       // ALWAYS put it back

        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 1, y + 1);                   // shadow
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
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