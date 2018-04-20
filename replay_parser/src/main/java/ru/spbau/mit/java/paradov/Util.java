package ru.spbau.mit.java.paradov;

import ru.spbau.mit.java.paradov.util.IntPair;
import skadistats.clarity.model.Entity;


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

        return new IntPair(cellX * 128 + vecX.intValue(), 32768 - cellY * 128 - vecY.intValue());
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
