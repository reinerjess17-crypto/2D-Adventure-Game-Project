package com.mygame.entity;

// The ONE place that knows how to build each kind of entity.
// "static" (like ImageLoader): you call EntityFactory.create(...) without making an object.
//
// Why a factory? Without it, every class that wants a tree has to write "new Tree(x, y)"
// and import Tree. With it, they only know EntityType.TREE. If a constructor changes
// later, we fix it here, in ONE line, not in ten files.
public class EntityFactory {

    // "private": nobody should write new EntityFactory(). It only has static methods.
    private EntityFactory() {
    }

    // Builds the entity that matches the type, standing at the WORLD position (x, y).
    public static Entity create(EntityType type, double x, double y) {
        switch (type) {
            case SHARP_CACTUS: return new ThornyRattan(x, y);   // random look
            case LIFE_BERRY:   return new LifeBerry(x, y);
            case TREE:         return new Tree(x, y);          // random look
            case CARABAO:      return new Carabao(x, y);
            case WILDBOAR:     return new WildBoar(x,y);
            default:
                // A type was added to the enum but not to this list. Fail LOUDLY, now,
                // instead of spawning nothing and leaving you confused.
                throw new IllegalArgumentException("EntityFactory cannot create: " + type);
        }
    }
}