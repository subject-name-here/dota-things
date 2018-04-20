package ru.spbau.mit.java.paradov;

import java.util.ArrayList;

/**
 * State of our Nevermore in a current tick.
 */
public class State {
    /*/***************************
      Data of the game in whole.
    *****************************/

    /** Team of our Nevermore: 0 for radiant, 1 for dire. */
    int ourTeam;

    /** Name of enemy hero. */
    String enemyName;

    /*/***************************
      Data of the game in this moment.
    *****************************/

    /** Time of the state. Measured in ticks. */
    int time;

    /** Points of our team. */
    int ourScore;

    /** Points of enemy team. */
    int theirScore;

    /*/***************************
      Data of our Nevermore in this moment.
    *****************************/

    /** Coordinate X. */
    int ourX;

    /** Coordinate Y. */
    int ourY;

    /** Level of our hero. */
    int lvl;

    /* Gold of our hero. */
    int gold;

    /** HP of our hero. */
    int hp;

    /** Maximum of our HP. */
    int maxHp;

    /** Mana of our hero. */
    int mana;

    /** Maximum of our mana. */
    int maxMana;

    /** Gets the facing of this unit on a 360 degree rotation. */
    float facing;

    boolean wasRecentlyDamagedByHero;

    boolean wasRecentlyDamagedByTower;

    boolean wasRecentlyDamagedByCreep;

    /*/***************************
      Data of enemy in this moment.
    *****************************/

    /**
     * Flag, detecting if enemy visible. Visibility equals to have distance less or equal 1600.
     * If enemy is not visible, other data is irrelevant.
     */
    boolean isEnemyVisible;

    int enemyX;

    int enemyY;

    /** HP of enemy hero. */
    int enemyHp;

    /** Maximum of enemy HP. */
    int enemyMaxHp;

    /** Mana of enemy hero. */
    int enemyMana;

    /** Maximum of enemy mana. */
    int enemyMaxMana;

    /** Level of enemy hero. */
    int enemyLvl;

    float enemyFacing;

    /*/***************************
      Data of nearby creeps.
    *****************************/

    class CreepState {
        /**
         * There are 8 types of creeps:
         * 0 - Melee creep;
         * 1 - Ranged creep;
         * 2 - Siege creep;
         * 3 - Super melee creep;
         * 4 - Super ranged creep;
         * 5 - Super siege creep;
         * 6 - Mega melee creep;
         * 7 - Mega ranged creep;
         * Probably, last five types don't spawn in 1v1 mode, but I'm not sure.
         */
        int type;

        /** Creep HP. */
        int hp;

        /** Creep max HP. */
        int maxHp;

        /** Creep X coordinate. */
        int x;

        /** Creep Y coordinate. */
        int y;
    }

    /** List of nearby friendly creeps. */
    ArrayList<CreepState> friendlyCreeps;

    /** List of nearby enemy creeps. */
    ArrayList<CreepState> EnemyCreeps;

    /*/***************************
      Data of middle towers.
    *****************************/

    /** HP of our tower. */
    int ourTowerHp;

    /** HP of enemy tower. If tower isn't visible, it's set either as max, or as last known value. */
    int enemyTowerHp;


}
