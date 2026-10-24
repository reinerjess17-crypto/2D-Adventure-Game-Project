package com.mygame.entity;

import java.util.ArrayList;
import java.util.List;

// One conversation in progress: who we talk to, the lines, and which line we are on.
// The Player owns it while talking; the HUD reads it to draw the dialogue box.
// Keeping this in its own small class means Player and Villager don't both
// have to track the "current line" themselves.
public class Conversation {

    private final Talkable partner;
    private final List<String> lines;
    private int index = 0;                       // 0 = the first line

    public Conversation(Talkable partner) {
        this.partner = partner;
        // A COPY: if the villager's lines change during the talk, this talk does not change.
        this.lines = new ArrayList<>(partner.getDialogue());
        if (lines.isEmpty()) {
            lines.add("...");                    // never leave the dialogue box empty
        }
    }

    public Talkable getPartner() { return partner; }
    public String getSpeakerName() { return partner.getSpeakerName(); }
    public String getCurrentLine() { return lines.get(index); }

    public int getLineNumber() { return index + 1; }          // for "2/6" on screen
    public int getLineCount() { return lines.size(); }
    public boolean isLastLine() { return index >= lines.size() - 1; }

    // Moves to the next line. Returns false if there was none, which means the talk is over.
    public boolean advance() {
        if (isLastLine()) return false;
        index++;
        return true;
    }
}
