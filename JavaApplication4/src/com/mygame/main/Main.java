package com.mygame.main;

import com.mygame.gui.GamePanel;

import javax.swing.JFrame;

// The starting point of the program: creates the window and starts the game.
public class Main {
    public static JFrame window;

    public static void main(String[] args) {
        window = new JFrame("No Title Yet");
        GamePanel gamePanel = new GamePanel();               // the drawing surface
        GameEngine gameEngine = new GameEngine(gamePanel);   // the loop that runs the game

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // closing the window ends the program
        window.setResizable(false);     // keeps the 16:9 shape intact
        window.add(gamePanel);

        window.pack();                          // fits the window around the 1280x720 panel
        window.setLocationRelativeTo(null);     // centers the window on the screen
        window.setVisible(true);
        gamePanel.requestFocusInWindow();       // makes sure the panel receives key presses
        gameEngine.startGameThread();           // start the game loop
    }
} 