package ru.spbau.mit.java.paradov;

import ru.spbau.mit.java.paradov.util.IntPair;
import skadistats.clarity.Clarity;
import skadistats.clarity.model.*;
import skadistats.clarity.model.Vector;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.reader.OnTickEnd;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.wire.common.proto.Demo;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.abs;
import static ru.spbau.mit.java.paradov.Constants.*;

/**
 * Class that parses Dota 2 replay file and adds everything it saw in State and Action array.
 */
public class Parser {
    private State[] states;
    private Action[] actions;

    /**
     * Maps creep id to its state. We need to maintain creep list from state to state, add and remove
     * creeps. If we keep creep states in its outer state, there will be many difficulties
     * with updating and removing dead creeps. (Map is basically faster.)
     */
    private HashMap<Integer, State.CreepState> ourCreeps = new HashMap<>();
    private HashMap<Integer, State.CreepState> enemyCreeps = new HashMap<>();

    /** Team of our Nevermore. We require that it was the winner's team. */
    private int winnerTeam;

    /** Current tick. */
    private int tick = 0;
    /** Tick when state 4 began. */
    private int beginTick = 0;
    /** Tick when state 5 ended. */
    private int endTick = 0;

    /**
     * State of the game (has nothing to do with states), that describes game status.
     * 4 means that game in process without creeps, 5 - with creeps, other numbers - game isn't in process.
     * Useful because states, when status isn't 5 or 4, have no info.
     */
    private int gameState;

    /** Replay file name. */
    private final String replayFile;

    /**
     * Constructs Parser: gets basic info about match and creates empty states and actions,
     * then writes there basic match info.
     * @param args args given to Main; first is a way to file
     * @throws IOException if replay isn't found or can't be read
     */
    public Parser(String[] args) throws IOException {
        replayFile = args[0];

        Demo.CDemoFileInfo info = Clarity.infoForFile(replayFile);
        winnerTeam = info.getGameInfo().getDota().getGameWinner();

        states = new State[info.getPlaybackTicks()];
        actions = new Action[info.getPlaybackTicks()];
        String enemyName = info.getGameInfo().getDota().getPlayerInfoList().get(0).getGameTeam() == winnerTeam ?
                info.getGameInfo().getDota().getPlayerInfoList().get(0).getHeroName() :
                info.getGameInfo().getDota().getPlayerInfoList().get(1).getHeroName();

        for (int i = 0; i < info.getPlaybackTicks(); i++) {
            actions[i] = new Action();
            states[i] = new State();
            states[i].ourTeam = winnerTeam;
            states[i].enemyName = enemyName;
        }
    }


    public State[] getStates() {
        return states;
    }

    public Action[] getActions() {
        return actions;
    }

    public IntPair getTickBorders() {
        return new IntPair(beginTick, endTick);
    }

    /**
     * Gets entity, looks what type it is, depending on type saves info to state.
     * @param e given entity
     */
    private void saveInfoFromEntity(Entity e) {
        switch (Util.getEntityType(e, winnerTeam)) {
            case OUR_HERO:
                states[tick].ourHp = e.getProperty(HP);
                states[tick].ourMaxHp = e.getProperty(MAX_HP);
                states[tick].ourMana = ((Float) e.getProperty(MANA)).intValue();
                states[tick].ourMaxMana = ((Float) e.getProperty(MAX_MANA)).intValue();
                states[tick].ourLvl = e.getProperty(LVL);
                states[tick].ourX = Util.getCoordFromEntity(e).fst;
                states[tick].ourY = Util.getCoordFromEntity(e).snd;
                states[tick].ourFacing = ((Vector) e.getProperty(FACING)).getElement(1);
                states[tick].ourAttackDamage = (Integer) e.getProperty(ATTACK_DAMAGE_BONUS)
                        + ((Integer) e.getProperty(ATTACK_DAMAGE_MIN) + (Integer) e.getProperty(ATTACK_DAMAGE_MAX)) / 2;
                break;

            case ENEMY_HERO:
                states[tick].isEnemyVisible = ((Integer) e.getProperty(VISIBILITY) & (1 << winnerTeam)) != 0;
                states[tick].enemyHp = e.getProperty(HP);
                states[tick].enemyMaxHp = e.getProperty(MAX_HP);
                states[tick].enemyMana = ((Float) e.getProperty(MANA)).intValue();
                states[tick].enemyMaxMana = ((Float) e.getProperty(MAX_MANA)).intValue();
                states[tick].enemyLvl = e.getProperty(LVL);
                states[tick].enemyX = Util.getCoordFromEntity(e).fst;
                states[tick].enemyY = Util.getCoordFromEntity(e).snd;
                states[tick].enemyFacing = ((Vector) e.getProperty(FACING)).getElement(1);
                states[tick].enemyAttackDamage = (Integer) e.getProperty(ATTACK_DAMAGE_BONUS)
                        + ((Integer) e.getProperty(ATTACK_DAMAGE_MIN) + (Integer) e.getProperty(ATTACK_DAMAGE_MAX)) / 2;
                break;

            case OUR_TOWER:
                states[tick].ourTowerHp = e.getProperty(HP);
                states[tick].ourTowerMaxHp = e.getProperty(MAX_HP);
                break;

            case ENEMY_TOWER:
                states[tick].enemyTowerHp = e.getProperty(HP);
                states[tick].enemyTowerMaxHp = e.getProperty(MAX_HP);
                break;

            case OUR_TEAM:
                states[tick].ourScore = Util.getScoreFromEntity(e);
                break;

            case ENEMY_TEAM:
                states[tick].enemyScore = Util.getScoreFromEntity(e);
                break;

            case OUR_DATA:
                states[tick].ourGold = (Integer) e.getProperty("m_vecDataTeam.0000.m_iReliableGold")
                        + (Integer) e.getProperty("m_vecDataTeam.0000.m_iUnreliableGold");

                break;

            case OUR_ABILITY:
                int mana = states[tick - 1].ourMana;
                switch (Util.getAbilityTypeFromEntity(e)) {
                    case 1:
                        states[tick].isOurAbility1Available = Util.isAbilityAvailable(e, mana);
                        break;
                    case 2:
                        states[tick].isOurAbility2Available = Util.isAbilityAvailable(e, mana);
                        break;
                    case 3:
                        states[tick].isOurAbility3Available = Util.isAbilityAvailable(e, mana);
                        break;
                    case 4:
                        states[tick].isOurAbility4Available = Util.isAbilityAvailable(e, mana);
                        break;
                }

                break;

            case OUR_CREEP:
                if (gameState != 5 || Util.getCreepTypeFromEntity(e) == -1)
                    break;

                Util.updateCreepMapFromEntity(e, ourCreeps);
                break;
            case ENEMY_CREEP:
                if (gameState != 5 || Util.getCreepTypeFromEntity(e) == -1)
                    break;

                Util.updateCreepMapFromEntity(e, enemyCreeps);
                break;

        }
    }



    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        tick = ctx.getTick();
    }

    @OnTickEnd
    public void onTickEnd(Context ctx, boolean synthetic) {
        if (gameState == 4 || gameState == 5) {
            Util.stateClosure(tick, states);

            Util.saveCreepInfoToState(states[tick], ourCreeps, true);
            Util.saveCreepInfoToState(states[tick], enemyCreeps, false);
        }
    }

    @OnEntityCreated
    public void onCreated(Entity e) {
        saveInfoFromEntity(e);
    }

    @OnEntityUpdated
    public void onUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {
        if (gameState != 5 && gameState != 4) {
            return;
        }

        states[tick].time = tick;
        saveInfoFromEntity(e);
    }

    @OnCombatLogEntry
    public void onCombatLogEntry(CombatLogEntry cle) {
        switch (cle.getType()) {
            case DOTA_COMBATLOG_DAMAGE:
                if (cle.getTargetName().equals("npc_dota_hero_nevermore") && cle.getTargetTeam() == winnerTeam - 2) {
                    String attackerName = cle.getAttackerName();

                    if (attackerName.startsWith("npc_dota_hero_")) {
                        states[tick].timeSinceDamagedByHero = 0;
                    } else if (attackerName.startsWith("npc_dota_creep_")) {
                        states[tick].timeSinceDamagedByCreep = 0;
                    } else if (attackerName.startsWith("npc_dota_badguys_tower")
                            || attackerName.startsWith("npc_dota_goodguys_tower")) {
                        states[tick].timeSinceDamagedByTower = 0;
                    }
                } else if (cle.getAttackerName().equals("npc_dota_hero_nevermore")
                        && cle.getAttackerTeam() == winnerTeam
                        && cle.getInflictorName().equals("dota_unknown")) {

                    String target = cle.getTargetName();
                    if (target.startsWith("npc_dota_hero_")) {
                        actions[tick].actionType = 1;
                    } else if (target.startsWith("npc_dota_creep_")) {
                        actions[tick].actionType = 2;
                    } else if (target.startsWith("npc_dota_badguys_tower_")
                            || target.startsWith("npc_dota_goodguys_tower")) {
                        actions[tick].actionType = 4;
                    }
                }

                break;
            case DOTA_COMBATLOG_ABILITY:
                if (cle.getAttackerName().equals("npc_dota_hero_nevermore")
                        && cle.getAttackerTeam() == winnerTeam - 2) {
                    actions[tick].actionType = 3;
                    String ability = cle.getInflictorName();
                    switch (ability) {
                        case "nevermore_shadowraze1" :
                            actions[tick].param = 1;
                            break;
                        case "nevermore_shadowraze2" :
                            actions[tick].param = 2;
                            break;
                        case "nevermore_shadowraze3" :
                            actions[tick].param = 3;
                            break;
                        case "nevermore_requiem" :
                            actions[tick].param = 4;
                            break;
                    }
                }
                break;
            case DOTA_COMBATLOG_GAME_STATE:
                if (cle.getValue() == 4) {
                    beginTick = tick;
                } else if (gameState == 5) {
                    endTick = tick;

                }
                gameState = cle.getValue();
                break;
            /*case DOTA_COMBATLOG_ITEM:
                log.info("{} {} uses {}",
                        time,
                        getAttackerNameCompiled(cle),
                        cle.getInflictorName()
                );
                break;
            case DOTA_COMBATLOG_PURCHASE:
                if (getTargetNameCompiled(cle) == "npc_dota_hero_nevermore"
                        && cle.getTargetTeam() == winnerTeam) {
                    log.info("{} {} buys item {}",
                            time,
                            getTargetNameCompiled(cle),
                            cle.getValueName()
                    );
                }
                break;*/
        }
    }

    public void run() throws Exception {
        new SimpleRunner(new MappedFileSource(replayFile)).runWith(this);
    }


}
