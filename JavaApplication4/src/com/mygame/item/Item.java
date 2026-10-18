package com.mygame.item;

// PLACEHOLDER until the inventory system exists.
// For now an item is just a name. Later it can get an icon, a stack size, a description...
// Tree uses it already, so that its code won't need to change when the real inventory arrives.
public class Item {

    private final String name;

    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
