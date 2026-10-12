package com.mygame.main;

import com.mygame.entity.Player;
import com.mygame.gui.GamePanel;
import com.mygame.input.KeyHandler;
import com.mygame.world.Camera;
import com.mygame.world.TileMap;

// The heart of the game: runs the loop that updates and draws 60 times per second.
public class GameEngine implements Runnable {
    private Thread gameThread;      // the thread that continuously runs the game loop
    private GamePanel gamePanel;    // the panel responsible for drawing the game
    private double drawInterval;    // how much time should pass between frames (nanoseconds)
    private double nextDrawTime;    // when the next frame should happen

    // Receives the GamePanel that this engine will control
    public GameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    // Creates and starts the game thread. This calls run() on a separate thread.
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    // The game loop starts here
    public void run() {
        drawInterval = 1_000_000_000.0 / GameSettings.FPS;   // nanoseconds per frame
        nextDrawTime = System.nanoTime() + drawInterval;     // target time of the first frame

        while (gameThread != null) {
            update();               // 1. change the game's data (move things)
            gamePanel.render();     // 2. draw the data onto the hidden canvas
            gamePanel.repaint();    // 3. ask Swing to show the canvas on the window

            // How long until the next frame is due?
            long remainingTime = (long) (nextDrawTime - System.nanoTime());
            if (remainingTime > 0) {
                try {
                    // Thread.sleep wants milliseconds, so convert from nanoseconds
                    long sleepTime = remainingTime / 1_000_000L;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            nextDrawTime += drawInterval;   // schedule the next frame
        }
    }

    // Runs once per frame. The ORDER of the steps matters.
    public void update() {
        // Get the objects we need from the panel
        KeyHandler keys = gamePanel.getKeyHandler();
        Camera camera = gamePanel.getCamera();
        TileMap map = gamePanel.getTileMap();
        Player player = gamePanel.getPlayer();

        // 1. Move the player (this includes the collision checks)
        player.update(keys, map);

        // TEMPORARY test: press K to lose 10 health. Delete when enemies exist.
        if (keys.damageRequested) {
            keys.damageRequested = false;
            player.takeDamage(10);
        }

        // 2. Point the camera at the player.
        //    This comes AFTER the player moves, or the camera would lag one frame behind.
        camera.follow(player.getCenterX(), player.getCenterY(),
                GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT);

        // 3. Keep the camera inside the world, so no black area shows
        camera.clamp(map.getWidthInPixels(), map.getHeightInPixels(),
                GameSettings.BASE_WIDTH, GameSettings.BASE_HEIGHT);
    }
}