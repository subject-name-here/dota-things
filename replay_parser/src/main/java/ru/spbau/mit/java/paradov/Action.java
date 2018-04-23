package ru.spbau.mit.java.paradov;

import ru.spbau.mit.java.paradov.util.IntPair;

/** Possible types of action that our Nevermore can do, according to our state. */
public class Action {
    /**
     * Type of action:
     * 0 - Move on vector (coordinates x and y are param1 and param2)
     * 1 - Attack Hero
     * 2 - Attack creep (number of the creep is param1)
     * 3 - Use Ability (number of ability is param1)
     * 4 - Attack Tower
     */
    int actionType;

    /** Parameter 1 of action. */
    Object param1;

    /** Parameter 2 of action. (Only coordinate y of moving.) */
    Object param2;
}
