package com.mygame.gui;

import com.mygame.main.GameSettings;
import com.mygame.world.TileMap;
import com.mygame.input.KeyHandler;
import com.mygame.world.Camera;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel {
    // Internal rendering canvas
    private BufferedImage gameScreen;
    private TileMap tileMap = new TileMap("/res/maps/MainWorld.csv");
    private final KeyHandler keyHandler = new KeyHandler();
    private final Camera camera = new Camera();

    
    public GamePanel() {
        this.setPreferredSize(new Dimension(GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Smooth rendering
        this.setFocusable(true);      // Receives keyboard input
        this.addKeyListener(keyHandler);// the panel now listens to the keyboard

        // Create an internal canvas locked to 960x540
        gameScreen = new BufferedImage(GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        
        
    }
    // ----- Getters (the engine uses these to reach the keys and camera) -----
    public KeyHandler getKeyHandler() {return keyHandler;}
    public Camera getCamera() {return camera;}
    public TileMap getTileMap() {return tileMap;}
    //Draws the current game frame
    public void render(){
        Graphics2D g2 = gameScreen.createGraphics();

        // Clear the canvas first, or old frames stay on it
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT);
        
        tileMap.draw(g2, camera.getX(), camera.getY()); 

        // Temporary test square: one tile
        g2.setColor(Color.GREEN);
        g2.fillRect(320,180, GameSettings.TILE_SIZE, GameSettings.TILE_SIZE);
       
        g2.dispose();//release the drawing tool
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable Nearest-Neighbor interpolation to keep 32x32 pixel art sharp when scaled
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // 3. Draw the canvas stretched to fit the full JPanel size
        g2.drawImage(gameScreen, 0, 0, getWidth(), getHeight(), null);
    }    
    //allows a section of the program to run independently.
    
}   

