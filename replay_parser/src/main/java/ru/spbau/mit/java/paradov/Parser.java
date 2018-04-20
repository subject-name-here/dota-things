package ru.spbau.mit.java.paradov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbau.mit.java.paradov.util.EntityType;
import ru.spbau.mit.java.paradov.util.IntPair;
import skadistats.clarity.Clarity;
import skadistats.clarity.model.*;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.gameevents.OnGameEvent;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.wire.common.proto.Demo;

import java.io.IOException;

import static ru.spbau.mit.java.paradov.Constants.*;

/**
 * Class that parses Dota 2 replay file and adds everything it saw in State array.
 */
public class Parser {
    private State[] states;

    private int winnerTeam;

    private int tick = 0;
    private int beginTick = 0;
    private int endTick = 0;

    /**
     * State of the game (has nothing to do with states), that describes game status.
     * 5 means that game in process, other numbers - game isn't in process.
     * Useful because states, when status isn't 5, have no info.
     */
    private int gameState;

    private final Logger log = LoggerFactory.getLogger(Main.class.getPackage().getClass());
    private final String replayFile;

    /**
     * Constructs Parser: gets basic info about match and creates empty states, then writes there
     * basic match info.
     * @param args args given to Main; first is a way to file
     * @throws IOException if replay isn't found or can't be read
     */
    public Parser(String[] args) throws IOException {
        replayFile = args[0];

        Demo.CDemoFileInfo info = Clarity.infoForFile(replayFile);
        winnerTeam = info.getGameInfo().getDota().getGameWinner();

        states = new State[info.getPlaybackTicks()];
        String enemyName = info.getGameInfo().getDota().getPlayerInfoList().get(0).getGameTeam() == winnerTeam ?
                info.getGameInfo().getDota().getPlayerInfoList().get(0).getHeroName() :
                info.getGameInfo().getDota().getPlayerInfoList().get(1).getHeroName();

        for (int i = 0; i < info.getPlaybackTicks(); i++) {
            states[i] = new State();
            states[i].ourTeam = winnerTeam - 2;
            states[i].enemyName = enemyName;
            states[i].time = i;
        }
    }


    public State[] getStates() {
        return states;
    }

    public IntPair getTickBorders() {
        return new IntPair(beginTick, endTick);
    }



    private boolean isCreep(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTA_BaseNPC_Creep");
    }


    private EntityType getEntityType(Entity e) {
        String entityName = e.getDtClass().getDtName();
        if (entityName.startsWith("CDOTA_Unit_Hero")) {
            return e.getProperty("m_iTeamNum") == (Integer) winnerTeam ?
                    EntityType.OUR_HERO :
                    EntityType.ENEMY_HERO;
        }

        if (entityName.startsWith("CDOTA_BaseNPC_Tower")) {
            return e.getProperty("m_iTeamNum") == (Integer) winnerTeam ?
                    EntityType.OUR_TOWER :
                    EntityType.ENEMY_TOWER;
        }


        return EntityType.UNKNOWN;
    }

    /**
     * Saves piece of state from entity.
     * @param e given entity
     */
    private void saveInfoFromEntity(Entity e) {
        switch (getEntityType(e)) {
            case OUR_HERO:
                states[tick].hp = e.getProperty(HP);
                states[tick].maxHp = e.getProperty(MAX_HP);
                states[tick].mana = ((Float) e.getProperty(MANA)).intValue();
                states[tick].maxMana = ((Float) e.getProperty(MAX_MANA)).intValue();
                states[tick].lvl = e.getProperty(LVL);
                states[tick].ourX = Util.getCoordFromEntity(e).fst;
                states[tick].ourY = Util.getCoordFromEntity(e).snd;
                states[tick].facing = ((Vector) e.getProperty(FACING)).getElement(1);
                break;

            case ENEMY_HERO:
                states[tick].enemyHp = e.getProperty(HP);
                states[tick].enemyMaxHp = e.getProperty(MAX_HP);
                states[tick].enemyMana = ((Float) e.getProperty(MANA)).intValue();
                states[tick].enemyMaxMana = ((Float) e.getProperty(MAX_MANA)).intValue();
                states[tick].enemyLvl = e.getProperty(LVL);
                states[tick].enemyX = Util.getCoordFromEntity(e).fst;
                states[tick].enemyY = Util.getCoordFromEntity(e).snd;
                states[tick].enemyFacing = ((Vector) e.getProperty(FACING)).getElement(1);
                break;

            case OUR_TOWER:
                states[tick].ourTowerHp = e.getProperty(HP);
                break;

            case ENEMY_TOWER:
                states[tick].enemyTowerHp = e.getProperty(HP);
                break;
        }
    }

    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        tick = ctx.getTick();


    }

    @OnEntityCreated
    public void onCreated(Entity e) {
        saveInfoFromEntity(e);
    }

    @OnEntityUpdated
    public void onUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {
        if (gameState != 5) {
            return;
        }

        saveInfoFromEntity(e);

    }

    @OnCombatLogEntry
    public void onCombatLogEntry(CombatLogEntry cle) {
        String time = "[Tick " + tick + "]";
        switch (cle.getType()) {
            case DOTA_COMBATLOG_DAMAGE:
                /*log.info("{} {} hits {}{} for {} damage{}",
                        time,
                        getAttackerNameCompiled(cle),
                        getTargetNameCompiled(cle),
                        cle.getInflictorName().equals("0") ? String.format(" with %s", cle.getInflictorName()) : "",
                        cle.getValue(),
                        cle.getHealth() != 0 ? String.format(" (%s->%s)", cle.getHealth() + cle.getValue(), cle.getHealth()) : ""
                );*/
                break;
            case DOTA_COMBATLOG_ABILITY:
                /*log.info("{} {} {} ability {} (lvl {}){}{}",
                        time,
                        getAttackerNameCompiled(cle),
                        cle.isAbilityToggleOn() || cle.isAbilityToggleOff() ? "toggles" : "casts",
                        cle.getInflictorName(),
                        cle.getAbilityLevel(),
                        cle.isAbilityToggleOn() ? " on" : cle.isAbilityToggleOff() ? " off" : "",
                        cle.getTargetName().equals("0") ? " on " + getTargetNameCompiled(cle) : ""
                );*/
                break;
            case DOTA_COMBATLOG_ITEM:
                /*log.info("{} {} uses {}",
                        time,
                        getAttackerNameCompiled(cle),
                        cle.getInflictorName()
                );*/
                break;
            case DOTA_COMBATLOG_GAME_STATE:
                log.info("State is now {}",
                        cle.getValue()
                );
                if (cle.getValue() == 5) {
                    beginTick = tick;
                } else if (gameState == 5) {
                    endTick = tick;
                }
                gameState = cle.getValue();
                break;
            /*case DOTA_COMBATLOG_PURCHASE:
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
        long tStart = System.currentTimeMillis();
        new SimpleRunner(new MappedFileSource(replayFile)).runWith(this);
        long tMatch = System.currentTimeMillis() - tStart;
        log.info("total time taken: {}s", (tMatch) / 1000.0);
    }



}
