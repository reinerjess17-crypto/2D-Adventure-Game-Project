package com.mygame.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Listens to the keyboard and REMEMBERS which keys are held down.
// It does not move anything itself. The game loop reads these flags
// 60 times per second, which gives smooth movement.
public class KeyHandler implements KeyListener {

    // true while the key is held down, false when released.
    // "volatile": the keyboard events arrive on Swing's thread, but the game
    // loop reads these on ITS OWN thread. volatile makes sure the game loop
    // always sees the newest value instead of an old copy.
    public volatile boolean upPressed, downPressed, leftPressed, rightPressed;

    // E = interact. A one-shot flag: true for ONE press, even if the key is held.
    // The player sets it back to false after using it.
    public volatile boolean interactRequested;
    private boolean interactKeyHeld;

    // SPACE = struggle (shake off an enemy that holds the player).
    // Same one-shot idea: every NEW press counts once, holding the key does nothing.
    public volatile boolean struggleRequested;
    private boolean struggleKeyHeld;

    // Called by Swing when a key goes DOWN
    @Override
    public void keyPressed(KeyEvent e) {
        setKey(e.getKeyCode(), true);
    }

    // Called by Swing when a key goes UP
    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false);
    }

    // Required by the KeyListener interface, but we don't need it.
    @Override
    public void keyTyped(KeyEvent e) {
    }

    // One shared method for both press and release, so we don't repeat code.
    // keyCode   = which key it was
    // isPressed = true when pressed, false when released
    private void setKey(int keyCode, boolean isPressed) {
        switch (keyCode) {
            // Two "case" lines in a row = either key does the same thing
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                upPressed = isPressed;
                break;      // "break" is required, or Java continues into the next case
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
            case KeyEvent.VK_E:
                if (isPressed && !interactKeyHeld) {
                    interactRequested = true;     // one press = one request, even if E is held
                }
                interactKeyHeld = isPressed;
                break;
            case KeyEvent.VK_SPACE:
                if (isPressed && !struggleKeyHeld) {
                    struggleRequested = true;     // every press counts: mash it!
                }
                struggleKeyHeld = isPressed;
                break;
        }
    }
}