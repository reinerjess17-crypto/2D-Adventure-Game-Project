package com.mygame.main;

import com.mygame.gui.GamePanel;
import com.mygame.world.Camera;
import com.mygame.input.KeyHandler;
import com.mygame.world.TileMap;

public class GameEngine implements Runnable {
    private Thread gameThread;// The thread that continuously runs the game loop
    private GamePanel gamePanel;// Reference to the panel responsible for drawing the game
    private double drawInterval;//how much time should pass between frames
    private double nextDrawTime;//when should the next frame happen

    // Receives the GamePanel that this engine will control
    public GameEngine(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }
    // Creates and starts the game thread
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override 
    //dito nagsismula ang game loop pare.
    public void run() {
        drawInterval = 1_000_000_000.0 / GameSettings.FPS;// Calculates how many nanoseconds one frame should take
        nextDrawTime = System.nanoTime() + drawInterval;// Sets the target time for the first frame
        // Keeps the game running while the thread exists
        while (gameThread != null) {
            update();// Updates the game's objects and logic
            gamePanel.render();
            gamePanel.repaint();// Tells Swing that the GamePanel needs to be redrawn
            long remainingTime = (long)(nextDrawTime - System.nanoTime());
            //wait until it's time for the next frame
            if (remainingTime > 0){
                try{
                    // Converts nanoseconds to milliseconds for Thread.sleep()
                     long sleepTime = remainingTime / 1_000_000L;
                    Thread.sleep(sleepTime);
                }catch(InterruptedException e){e.printStackTrace();// Handles an interruption of the game thread
                }
            }
            nextDrawTime += drawInterval;// Sets the target time for the next frame
        }
    }
    //dito nangyayari ang mga pangyayari! really?! yes
    public void update(){
        final int speed = 16; //pixels per fram (4 x 60 FPS = 240 pixels per second);

        KeyHandler Keys = gamePanel.getKeyHandler();
        Camera camera = gamePanel.getCamera();
        
        if (Keys.upPressed)   camera.move(0, -speed);
        if (Keys.downPressed) camera.move(0, speed);
        if (Keys.leftPressed) camera.move(-speed, 0);
        if (Keys.rightPressed)camera.move(speed,0);

        // After moving, make sure the camera hasn't left the world
        TileMap map = gamePanel.getTileMap();
        camera.clamp(map.getWidthInPixels(), map.getHeightInPixels(), GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT);
    }
   
}
