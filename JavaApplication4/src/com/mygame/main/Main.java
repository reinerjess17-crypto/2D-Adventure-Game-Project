package com.mygame.main;
import com.mygame.gui.GamePanel;

import javax.swing.JFrame;

public class Main{
    public static JFrame window;
    public static void main(String[] args){
        window = new JFrame("No Title Yet");
        GamePanel gamePanel = new GamePanel();
        GameEngine gameEngine = new GameEngine(gamePanel);

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false); // Keeps 16:9 aspect ratio intact
        window.add(gamePanel);
        
        window.pack(); // Tightly fits window around the 960x540 panel
        window.setLocationRelativeTo(null); // Centers window on screen
        window.setVisible(true);
        gamePanel.requestFocusInWindow(); //makes sure the panel receives key presses

        gameEngine.startGameThread(); //import from the GameEngine

    }
}
  