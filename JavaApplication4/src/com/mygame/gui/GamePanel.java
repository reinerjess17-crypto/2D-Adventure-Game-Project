package com.mygame.gui;

import com.mygame.entity.EntityManager;
import com.mygame.entity.Player;
import com.mygame.input.KeyHandler;
import com.mygame.main.GameSettings;
import com.mygame.world.Camera;
import com.mygame.world.TileMap;
import com.mygame.entity.EntityType;


import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// The window's drawing surface. It draws the game onto a small hidden canvas
// (640x360), then shows that canvas stretched to the real size (1280x720).
public class GamePanel extends JPanel {

    // ----- Fields -----

    // The hidden canvas: all game drawing happens here, at the SMALL size
    private BufferedImage gameScreen;

    // The world, loaded from the map file in res/maps
    private final TileMap tileMap = new TileMap("/res/maps/MainWorld.csv");

    // Remembers which keys are held down
    private final KeyHandler keyHandler = new KeyHandler();

    // Decides which part of the world is visible
    private final Camera camera = new Camera();

    // The player now receives the KeyHandler, because it reads the keys itself.
    // keyHandler is declared ABOVE this line, so it already exists when this runs.
    private final Player player = new Player(10 * GameSettings.TILE_SIZE,
        10 * GameSettings.TILE_SIZE, keyHandler);

    // Owns and runs every entity. The player is added to it automatically.
    private final EntityManager entityManager = new EntityManager(tileMap, player);

    private final Hud hud = new Hud();

    // ----- Constructor -----

    public GamePanel() {
        // The real size of the panel on the screen
        this.setPreferredSize(new Dimension(GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);       // smoother drawing
        this.setFocusable(true);            // lets the panel receive keyboard input
        this.addKeyListener(keyHandler);    // the panel now listens to the keyboard

        // The hidden canvas uses the SMALL size.
        // TYPE_INT_RGB: no transparency needed for the canvas.
        gameScreen = new BufferedImage(GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        
        // One berry near the start, so you always have something to test with.
        // spawn() builds it from its TYPE, so GamePanel no longer needs to import
        // LifeBerry, Tree or SharpCactus (you can delete those imports).
        entityManager.spawn(EntityType.LIFE_BERRY,
            player.getX() + 2 * GameSettings.TILE_SIZE, player.getY());

        entityManager.spawn(EntityType.CARABAO, player.getX() + 5 * GameSettings.TILE_SIZE, player.getY());

        entityManager.spawn(EntityType.WILDBOAR, player.getX() + 6 * GameSettings.TILE_SIZE, player.getY());

        // Fill the world. Reads as: "put this on about N% of these tiles".
        entityManager.scatter(EntityType.TREE, TileMap.LAND, 0.01);          // 1 grass tile in 100
        entityManager.scatter(EntityType.LIFE_BERRY, TileMap.LAND, 0.003);
        entityManager.scatter(EntityType.SHARP_CACTUS, TileMap.SAND, 0.005);}

    // ----- Getters: the engine uses these to reach things that are private here -----
    public KeyHandler getKeyHandler() { return keyHandler; }
    public Camera getCamera() { return camera; }
    public TileMap getTileMap() { return tileMap; }
    public Player getPlayer() { return player; }
    public EntityManager getEntityManager() { return entityManager;}

    // ----- Drawing -----

    // Draws the current frame onto the hidden canvas.
    // Whatever is drawn LAST ends up on top, so the order matters.
    public void render() {
        Graphics2D g2 = gameScreen.createGraphics();   // get the drawing tool

        // Clear the canvas first, or old frames stay on it
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT);

        // 1. The world first (the background)
        tileMap.draw(g2, camera.getX(), camera.getY());

        // 2. The player on top of the world
        player.draw(g2, camera.getX(), camera.getY());

        entityManager.draw(g2, camera.getX(), camera.getY());
        
        // 3. The HUD always goes last: it sits on top of everything
        hud.draw(g2, player);         // always last: it sits on top of everything

        g2.dispose();   // release the drawing tool
    }

    // Swing calls this when the panel needs to be painted (after repaint()).
    // It shows the hidden canvas on the real window.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Nearest-neighbor keeps pixels sharp instead of blurry when enlarged
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Draw the small canvas stretched to fill the whole panel
        g2.drawImage(gameScreen, 0, 0, getWidth(), getHeight(), null);
    }
}