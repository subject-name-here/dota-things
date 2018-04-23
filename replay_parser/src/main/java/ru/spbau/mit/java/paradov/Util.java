package ru.spbau.mit.java.paradov;

import ru.spbau.mit.java.paradov.util.IntPair;
import skadistats.clarity.model.Entity;

import static ru.spbau.mit.java.paradov.Constants.HERO_KILLS;
import static ru.spbau.mit.java.paradov.Constants.TOWER_KILLS;


/**
 * Utility Functions for replay parser.
 */
public class Util {
    /**
     * Gets coordinates on the field from entity.
     * @param e given entity
     * @return pair of coordinates (x, y)
     */
    public static IntPair getCoordFromEntity(Entity e) {
        Integer cellX = e.getProperty("CBodyComponent.m_cellX");
        Integer cellY = e.getProperty("CBodyComponent.m_cellY");
        Float vecX = e.getProperty("CBodyComponent.m_vecX");
        Float vecY = e.getProperty("CBodyComponent.m_vecY");

        // This is magic to get map coordinates. Taken from here:
        // https://github.com/spheenik/clarity-analyzer/blob/master/src/main/java/skadistats/clarity/analyzer/main/icon/EntityIcon.java
        return new IntPair(cellX * 128 + vecX.intValue() - 16384, cellY * 128 + vecY.intValue() - 16384);
    }

    public static int getScoreFromEntity(Entity e) {
        Integer towerKills = e.getProperty(TOWER_KILLS);
        if (towerKills > 0) {
            return 2;
        }

        return (Integer) e.getProperty(HERO_KILLS);
    }


    public static void stateClosure(int tick, State[] states) {
        //states[tick].time = tick;

        if (states[tick].ourScore == 0) {
            states[tick].ourScore = states[tick - 1].ourScore;
        }

        if (states[tick].enemyScore == 0) {
            states[tick].enemyScore = states[tick - 1].enemyScore;
        }

        if (states[tick].ourLvl == 0) {
            states[tick].ourHp = states[tick - 1].ourHp;
            states[tick].ourMaxHp = states[tick - 1].ourMaxHp;
            states[tick].ourMana = states[tick - 1].ourMana;
            states[tick].ourMaxMana = states[tick - 1].ourMaxMana;
            states[tick].ourLvl = states[tick - 1].ourLvl;
            states[tick].ourX = states[tick - 1].ourX;
            states[tick].ourY = states[tick - 1].ourY;
            states[tick].ourFacing = states[tick - 1].ourFacing;
        }

        if (states[tick].ourGold == -1) {
            states[tick].ourGold = states[tick - 1].ourGold;
        }

        if (states[tick].enemyLvl == 0) {
            states[tick].enemyHp = states[tick - 1].enemyHp;
            states[tick].enemyMaxHp = states[tick - 1].enemyMaxHp;
            states[tick].enemyMana = states[tick - 1].enemyMana;
            states[tick].enemyMaxMana = states[tick - 1].enemyMaxMana;
            states[tick].enemyLvl = states[tick - 1].enemyLvl;
            states[tick].enemyX = states[tick - 1].enemyX;
            states[tick].enemyY = states[tick - 1].enemyY;
            states[tick].enemyFacing = states[tick - 1].enemyFacing;
        }
    }



    /*private String compileName(String attackerName, boolean isIllusion, Integer team) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") + team.toString(): "UNKNOWN";
    }

    private String getAttackerNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getAttackerName(), cle.isAttackerIllusion(), cle.getAttackerTeam());
    }

    private String getTargetNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getTargetName(), cle.isTargetIllusion(), cle.getTargetTeam());
    }*/
}
