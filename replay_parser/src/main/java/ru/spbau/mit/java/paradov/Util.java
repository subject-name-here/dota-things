package ru.spbau.mit.java.paradov;

import ru.spbau.mit.java.paradov.util.EntityType;
import ru.spbau.mit.java.paradov.util.IntPair;
import skadistats.clarity.model.CombatLogEntry;
import skadistats.clarity.model.Entity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import static java.lang.Integer.min;
import static ru.spbau.mit.java.paradov.Constants.*;


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

    /**
     * Gets entity type.
     * @param e given entity
     * @param winnerTeam team which we are belong to
     * @return type of entity or UNKNOWN, if type is unknown
     */
    public static EntityType getEntityType(Entity e, Integer winnerTeam) {
        String entityName = e.getDtClass().getDtName();
        if (entityName.startsWith("CDOTA_Unit_Hero")) {
            return e.getProperty(TEAM) == winnerTeam ?
                    EntityType.OUR_HERO :
                    EntityType.ENEMY_HERO;
        }

        if (entityName.startsWith("CDOTA_BaseNPC_Tower")) {
            int team = e.getProperty(TEAM);
            return team == winnerTeam ?
                    EntityType.OUR_TOWER :
                    team == 5 - winnerTeam ? EntityType.ENEMY_TOWER : EntityType.UNKNOWN;
        }

        if (entityName.startsWith("CDOTA_BaseNPC_Creep")) {
            int team = e.getProperty(TEAM);
            return team == winnerTeam ?
                    EntityType.OUR_CREEP :
                    team == 5 - winnerTeam ? EntityType.ENEMY_CREEP : EntityType.UNKNOWN;
        }

        if (entityName.startsWith("CDOTATeam")) {
            int team = e.getProperty(TEAM);
            return team == winnerTeam ?
                    EntityType.OUR_TEAM :
                    team == 5 - winnerTeam ? EntityType.ENEMY_TEAM : EntityType.UNKNOWN;
        }

        if (entityName.startsWith("CDOTA_Data")) {
            int team = e.getProperty(TEAM);
            return team == winnerTeam ?
                    EntityType.OUR_DATA :
                    team == 5 - winnerTeam ? EntityType.ENEMY_DATA : EntityType.UNKNOWN;
        }

        if (entityName.startsWith("CDOTA_Ability_Nevermore_")) {
            int team = e.getProperty(TEAM);
            return team == winnerTeam ?
                    EntityType.OUR_ABILITY :
                    team == 5 - winnerTeam ? EntityType.ENEMY_ABILITY : EntityType.UNKNOWN;
        }


        return EntityType.UNKNOWN;
    }

    /**
     * Gets score of team from team entity.
     * @param e given team entity
     * @return score of the team
     */
    public static int getScoreFromEntity(Entity e) {
        Integer towerKills = e.getProperty(TOWER_KILLS);
        if (towerKills > 0) {
            return 2;
        }

        return (Integer) e.getProperty(HERO_KILLS);
    }

    /**
     * Gets ability number from given ability entity.
     * @param e given ability entity
     * @return number of ability or 0, if ability doesn't fit
     */
    public static int getAbilityTypeFromEntity(Entity e) {
        String entityName = e.getDtClass().getDtName();
        if (entityName.endsWith("Requiem")) {
            return 4;
        }

        int range = e.getProperty(ABILITY_RANGE);
        switch (range) {
            case 200:
                return 1;
            case 450:
                return 2;
            case 700:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Gets creep type from creep entity.
     * @param e given creep entity
     * @return creep type or -1, if type is unknown
     */
    public static int getCreepTypeFromEntity(Entity e) {
        if (!e.getDtClass().getDtName().equals("CDOTA_BaseNPC_Creep_Lane")
                && !e.getDtClass().getDtName().equals("CDOTA_BaseNPC_Creep_Siege")) {
            return -1;
        }

        // Identifies siege creep
        if (e.getDtClass().getDtName().equals("CDOTA_BaseNPC_Creep_Siege")) {
            return 2;
        }

        // Identifies range creep
        if ((Float) e.getProperty(MAX_MANA) != 0) {
            return 1;
        }

        // Identifies melee creep
        if ((Float) e.getProperty(CREEP_MAGIC_RESIST) == 0) {
            return 0;
        }

        return -1;
    }

    /**
     * Updates given map with creep state using given entity. If entity has id that is already in the map,
     * state is being updated (or deleted, if entity hp == 0). If not, state is added.
     * @param e given entity
     * @param creeps creeps map
     */
    public static void updateCreepMapFromEntity(Entity e, Map<Integer, State.CreepState> creeps) {
        Integer id = e.getProperty(CREEP_ID);
        if (creeps.containsKey(id)) {
            if ((Integer) e.getProperty(HP) == 0) {
                creeps.remove(id);
            } else {
                State.CreepState state = creeps.get(id);
                Util.updateCreepStateFromEntity(e, state);
            }
        } else if ((Integer) e.getProperty(HP) != 0) {
            State.CreepState state = new State.CreepState();
            Util.updateCreepStateFromEntity(e, state);
            creeps.put(id, state);
        }
    }

    /**
     * Gets info if ability available. It is iff cooldown is 0 and it isn't activated and mana is enough.
     * @param e given ability entity
     * @param mana mana of ability holder
     * @return is ability available
     */
    public static boolean isAbilityAvailable(Entity e, int mana) {
        return (Float) e.getProperty(ABILITY_COOLDOWN) == 0
                && !(Boolean) e.getProperty(IS_ABILITY_ACTIVATED)
                && mana >= (Integer) e.getProperty(ABILITY_COST);
    }


    /**
     * Completes state info of states[tick], using info from previous state. If some state wasn't updated
     * on this tick, then it hadn't changed, therefore it's the same as previous one.
     * It doesn't complete creep info, because it requires other things and more difficult.
     * @param tick number (tick) of updated state
     * @param states states where all states are (including this and previous)
     */
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
            states[tick].ourAttackDamage = states[tick - 1].ourAttackDamage;
        }

        if (states[tick].timeSinceDamagedByHero == null) {
            Integer lastTime = states[tick - 1].timeSinceDamagedByHero;
            if (lastTime == null || lastTime < 0 || lastTime >= 900) {
                states[tick].timeSinceDamagedByHero = -1;
            } else {
                states[tick].timeSinceDamagedByHero = lastTime + 1;
            }
        }

        if (states[tick].timeSinceDamagedByTower == null) {
            Integer lastTime = states[tick - 1].timeSinceDamagedByTower;
            if (lastTime == null || lastTime < 0 || lastTime >= 900) {
                states[tick].timeSinceDamagedByTower = -1;
            } else {
                states[tick].timeSinceDamagedByTower = lastTime + 1;
            }
        }

        if (states[tick].timeSinceDamagedByCreep == null) {
            Integer lastTime = states[tick - 1].timeSinceDamagedByCreep;
            if (lastTime == null || lastTime < 0 || lastTime >= 900) {
                states[tick].timeSinceDamagedByCreep = -1;
            } else {
                states[tick].timeSinceDamagedByCreep = lastTime + 1;
            }
        }

        if (states[tick].ourGold == -1) {
            states[tick].ourGold = states[tick - 1].ourGold;
        }
        
        if (states[tick].isOurAbility1Available == null) {
            states[tick].isOurAbility1Available = states[tick - 1].isOurAbility1Available;
        }

        if (states[tick].isOurAbility2Available == null) {
            states[tick].isOurAbility2Available = states[tick - 1].isOurAbility2Available;
        }

        if (states[tick].isOurAbility3Available == null) {
            states[tick].isOurAbility3Available = states[tick - 1].isOurAbility3Available;
        }

        if (states[tick].isOurAbility4Available == null) {
            states[tick].isOurAbility4Available = states[tick - 1].isOurAbility4Available;
        }

        if (states[tick].enemyLvl == 0) {
            states[tick].isEnemyVisible = states[tick - 1].isEnemyVisible;
            states[tick].enemyHp = states[tick - 1].enemyHp;
            states[tick].enemyMaxHp = states[tick - 1].enemyMaxHp;
            states[tick].enemyMana = states[tick - 1].enemyMana;
            states[tick].enemyMaxMana = states[tick - 1].enemyMaxMana;
            states[tick].enemyLvl = states[tick - 1].enemyLvl;
            states[tick].enemyX = states[tick - 1].enemyX;
            states[tick].enemyY = states[tick - 1].enemyY;
            states[tick].enemyFacing = states[tick - 1].enemyFacing;
            states[tick].enemyAttackDamage = states[tick - 1].enemyAttackDamage;
        }

        if (states[tick].ourTowerHp == 0) {
            states[tick].ourTowerHp = states[tick - 1].ourTowerHp;
            states[tick].ourTowerMaxHp = states[tick - 1].ourTowerMaxHp;
        }

        if (states[tick].enemyTowerHp == 0) {
            states[tick].enemyTowerHp = states[tick - 1].enemyTowerHp;
            states[tick].enemyTowerMaxHp = states[tick - 1].enemyTowerMaxHp;
        }
    }

    // Calculates square of distance from creep to hero.
    private static int squareDistToHero(State.CreepState s, int heroX, int heroY) {
        return (s.x - heroX) * (s.x - heroX) + (s.y - heroY) * (s.y - heroY);
    }

    /**
     * Updates creep state from its entity.
     * @param e creep entity
     * @param state creep state that has to be updated
     */
    public static void updateCreepStateFromEntity(Entity e, State.CreepState state) {
        state.type = Util.getCreepTypeFromEntity(e);
        state.hp = e.getProperty(HP);
        state.maxHp = e.getProperty(MAX_HP);

        IntPair p = Util.getCoordFromEntity(e);
        state.x = p.fst;
        state.y = p.snd;

        int oppositeTeam = 5 - (Integer) e.getProperty(TEAM);
        state.isVisible = ((Integer) e.getProperty(VISIBILITY) & (1 << oppositeTeam)) != 0;
    }

    /**
     * Saves creeps info from creep map to given state. It is separated from stateClosure, because
     * creep info is kept in different way while parsing.
     * @param state outer state of creep states
     * @param creeps map that has creep id and its state
     * @param isOurs are those creeps belong to our team
     */
    public static void saveCreepInfoToState(State state, Map<Integer, State.CreepState> creeps, boolean isOurs) {
        ArrayList<State.CreepState> list = new ArrayList<>(creeps.values());
        int heroX = state.ourX;
        int heroY = state.ourY;
        list.sort(Comparator.comparingInt(s -> squareDistToHero(s, heroX, heroY)));

        ArrayList<State.CreepState> stateList;
        if (isOurs) {
            stateList = state.ourCreeps = new ArrayList<>();
        } else {
            stateList = state.enemyCreeps = new ArrayList<>();
        }

        for (int i = 0; i < min(10, list.size()); i++) {
            if (squareDistToHero(list.get(i), heroX, heroY) >= 1600 * 1600)
                break;

            if (isOurs || list.get(i).isVisible)
                stateList.add(new State.CreepState(list.get(i)));
        }
    }


    public static String compileName(String attackerName, boolean isIllusion, Integer team) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") + team.toString(): "UNKNOWN";
    }

    public static String getAttackerNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getAttackerName(), cle.isAttackerIllusion(), cle.getAttackerTeam());
    }

    public static String getTargetNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getTargetName(), cle.isTargetIllusion(), cle.getTargetTeam());
    }

}
