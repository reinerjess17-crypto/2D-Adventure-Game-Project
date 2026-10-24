package com.mygame.entity;

import java.util.List;

// A promise: "you can talk to me". It EXTENDS Interactable, because talking starts
// with pressing E near someone, just like picking a berry does. So anything Talkable
// is also Interactable and the EntityManager already knows how to find it.
public interface Talkable extends Interactable {

    // The name shown above the dialogue
    String getSpeakerName();

    // The lines of the conversation that is about to start.
    // It is asked EVERY time you start talking, so the answer can change
    // (Old Tanyo says different things as the quest moves on).
    List<String> getDialogue();

    // Called when the player reads the last line. A "default" method has a body here,
    // so classes that don't care about it don't have to write anything.
    default void onConversationEnd(Player player) {
    }
}