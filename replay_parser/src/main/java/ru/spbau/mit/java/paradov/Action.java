package ru.spbau.mit.java.paradov;

/** Possible types of action that our Nevermore can do, according to our state. */
public class Action {
    /**
     * Type of action:
     * 0 - Change coordinates
     * 1 - Attack Hero
     * 2 - Attack creep (number of the creep is param1. Numeration from 1!!)
     * 3 - Use Ability (number of ability is param1)
     * 4 - Attack Tower
     * -1 - do nothing (continue to make previous action)
     */
    int actionType = -1;

    /** Parameter of action. */
    int param;

    /** Coordinates change. */
    int dx;
    int dy;

    @Override
    public String toString() {
        String result = "Action: ";
        switch (actionType) {
            case 0:
                result += String.format("Move on vector (%d, %d).", dx, dy);
                break;
            case 1:
                result += "Attack hero.";
                break;
            case 2:
                result += String.format("Attack creep #%d", param);
                break;
            case 3:
                result += "Use ability " + (param == 4 ? "Requiem" : String.format("Shadowraze%d", param));
                break;
            case 4:
                result += "Attack tower.";
                break;
            case -1:
                result += "Do nothing (continue).";
                break;
            default:
                result += "Unknown.";
        }

        return result;
    }

}
