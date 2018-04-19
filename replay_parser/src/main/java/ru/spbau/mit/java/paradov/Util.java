package ru.spbau.mit.java.paradov;

import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;

import java.util.ArrayList;

public class Util {
    public static ArrayList<Integer> getCoordFromEntity(Entity e) {
        ArrayList<Integer> coord = new ArrayList<>();

        Integer cellX = e.getProperty("CBodyComponent.m_cellX");
        Integer cellY = e.getProperty("CBodyComponent.m_cellY");
        Float vecX = e.getProperty("CBodyComponent.m_vecX");
        Float vecY = e.getProperty("CBodyComponent.m_vecY");

        coord.add(cellX * 128 + vecX.intValue());
        coord.add(32768 - cellY * 128 - vecY.intValue());

        return coord;
    }
}
