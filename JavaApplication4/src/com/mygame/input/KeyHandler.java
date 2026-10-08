package com.mygame.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener{
    // true while the key is held down. "volatile" is needed because the keyboard
    // events arrive on Swing's thread, but the game loop reads these on ITS thread.
    // It makes sure the game thread always sees the newest value.
    public volatile boolean upPressed, downPressed, leftPressed, rightPressed;
    
    @Override 
    public void keyPressed(KeyEvent e){
        setKey(e.getKeyCode(), true);
    }
    
    @Override
    public void keyReleased(KeyEvent e){
        setKey(e.getKeyCode(), false);
    }

    @Override 
    public void keyTyped(KeyEvent e){

    }
    // One place to handle both press and release, so we don't repeat code.
    // WASD and the arrow keys both work.
    private void setKey(int keyCode, boolean isPressed){
        switch (keyCode){
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                upPressed = isPressed;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                downPressed = isPressed;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                leftPressed = isPressed;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                rightPressed = isPressed;
                break;             
        }
    }
}
