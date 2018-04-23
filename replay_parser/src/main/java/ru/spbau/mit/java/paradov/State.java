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

    /** Time of the state. Measured in ticks. If 0, state is invalid. */
    int time;

    /** Points of our team. */
    int ourScore;

    /** Points of enemy team. */
    int enemyScore;

    /*/***************************
      Data of our Nevermore in this moment.
    *****************************/

    /** Coordinate X. */
    int ourX;

    /** Coordinate Y. */
    int ourY;

    /** The facing of this unit on a 360 degree rotation. */
    float ourFacing;

    /** Level of our hero. If 0, basic data (coords, hp, mana, lvl) of our Nevermore is invalid. */
    int ourLvl;

    int ourHp;

    int ourMaxHp;

    int ourMana;

    int ourMaxMana;

    int ourAttackDamage;

    /** Gold of our hero. If -1, gold data is invalid. */
    int ourGold = -1;

    /** Is castable ability "Shadowraze", near. */
    boolean isOurAbility1Available;

    /** Is castable ability "Shadowraze", medium. */
    boolean isOurAbility2Available;

    /** Is castable ability "Shadowraze", far. */
    boolean isOurAbility3Available;

    /** Is castable ability "Requiem of Souls".*/
    boolean isOurAbility4Available;

    int timeSinceDamagedByHero;

    int timeSinceDamagedByTower;

    int timeSinceDamagedByCreep;

    /*/***************************
      Data of enemy in this moment.
    *****************************/

    /**
     * Flag, detecting if enemy visible. Visibility equals to have distance less or equal 1600.
     * If enemy is not visible, other data is irrelevant. // TODO: think about it.
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

    /** Level of enemy hero. If 0, data of enemy is invalid. */
    int enemyLvl;

    /** The facing of this unit on a 360 degree rotation. */
    float enemyFacing;

    int enemyAttackDamage;

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

    int ourTowerMaxHp;

    /** HP of enemy tower. If tower isn't visible, it's set either as max, or as last known value. */
    int enemyTowerHp;

    int enemyTowerMaxHp;

    @Override
    public String toString() {
        if (ourTeam == 2) {
            System.out.printf("DATA: Our Nevermore vs %s. ", enemyName);
            System.out.printf("Tick: %d. Score: %d : %d\n", time, ourScore, enemyScore);
        } else {
            System.out.printf("DATA: %s vs Our Nevermore. ", enemyName);
            System.out.printf("Tick: %d. Score: %d : %d\n", time, enemyScore, ourScore);
        }

        System.out.printf("OUR HERO DATA. HP: %d / %d  |  ", ourHp, ourMaxHp);
        System.out.printf("Mana: %d / %d  |  ", ourMana, ourMaxMana);
        System.out.printf("Level: %d  |  ", ourLvl);
        System.out.printf("Attack damage: %d  |  ", ourAttackDamage);
        System.out.printf("Gold: %d \n", ourGold);
        System.out.printf("Coordinates: (%d, %d); facing %f \n", ourX, ourY, ourFacing);

        System.out.print("OPPONENT DATA. ");
        System.out.printf("HP: %d / %d  |  ", enemyHp, enemyMaxHp);
        System.out.printf("Mana: %d / %d  |  ", enemyMana, enemyMaxMana);
        System.out.printf("Level: %d  |  ", enemyLvl);
        System.out.printf("Attack damage: %d \n", enemyAttackDamage);
        if (isEnemyVisible) {
            System.out.printf("Coordinates: (%d, %d); facing %f \n", enemyX, enemyY, enemyFacing);
        } else {
            System.out.printf("Coordinates: (%d, %d); Enemy isn't visible :(\n", enemyX, enemyY);
        }
        return "";
    }


}
