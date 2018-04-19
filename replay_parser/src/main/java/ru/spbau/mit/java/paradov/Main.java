package ru.spbau.mit.java.paradov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skadistats.clarity.Clarity;
import skadistats.clarity.model.*;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityPropertyChanged;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.gameevents.OnGameEvent;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.wire.common.proto.Demo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static int winnerTeam;

    private static State[] states;
    private static int tick = 0;

    private static int gameState;

    private final Logger log = LoggerFactory.getLogger(Main.class.getPackage().getClass());


    private String compileName(String attackerName, boolean isIllusion, Integer team) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") + team.toString(): "UNKNOWN";
    }

    private String getAttackerNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getAttackerName(), cle.isAttackerIllusion(), cle.getAttackerTeam());
    }

    private String getTargetNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getTargetName(), cle.isTargetIllusion(), cle.getTargetTeam());
    }

    private static final String HP = "m_iHealth";
    private static final String MAX_HP = "m_iMaxHealth";

    private static final String MANA = "m_flMana";
    private static final String MAX_MANA = "m_flMaxMana";

    private static final String LVL = "m_iCurrentLevel";

    private static Set<String> entities = new HashSet<>();

    private boolean isOurHero(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTA_Unit_Hero")
                && e.getProperty("m_iTeamNum") == (Integer) winnerTeam;
    }

    private boolean isEnemyHero(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTA_Unit_Hero")
                && e.getProperty("m_iTeamNum") != (Integer) winnerTeam;
    }

    private boolean isCreep(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTA_BaseNPC_Creep");
    }

    private boolean isOurTower(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTA_BaseNPC_Tower")
                && e.getProperty("m_iTeamNum") == (Integer) winnerTeam;
    }

    private boolean isEnemyTower(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTA_BaseNPC_Tower")
                && e.getProperty("m_iTeamNum") != (Integer) winnerTeam;
    }

    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        tick = ctx.getTick();


    }

    @OnEntityCreated
    public void onCreated(Entity e) {
        if (!isOurHero(e) && !isEnemyHero(e)
                && !isCreep(e)
                && !isOurTower(e) && !isEnemyTower(e)) {
            return;
        }

        //System.out.format("Tick %d: %s (%s/%s)\n", tick, e.getDtClass().getDtName(), e.getPropertyForFieldPath(mana), e.getPropertyForFieldPath(maxMana));
        if (isOurHero(e)) {
            states[tick].hp = e.getProperty(HP);
            states[tick].maxHp = e.getProperty(MAX_HP);
            states[tick].mana = ((Float) e.getProperty(MANA)).intValue();
            states[tick].maxMana = ((Float) e.getProperty(MAX_MANA)).intValue();
            states[tick].lvl = e.getProperty(LVL);
            states[tick].ourX = Util.getCoordFromEntity(e).get(0);
            states[tick].ourY = Util.getCoordFromEntity(e).get(1);
        }

        if (isEnemyHero(e)) {
            states[tick].enemyHp = e.getProperty(HP);
            states[tick].enemyMaxHp = e.getProperty(MAX_HP);
            states[tick].enemyMana = ((Float) e.getProperty(MANA)).intValue();
            states[tick].enemyMaxMana = ((Float) e.getProperty(MAX_MANA)).intValue();
            states[tick].enemyLvl = e.getProperty(LVL);
            states[tick].enemyX = Util.getCoordFromEntity(e).get(0);
            states[tick].enemyY = Util.getCoordFromEntity(e).get(1);
        }

        if (isOurTower(e)) {
            states[tick].ourTowerHp = e.getProperty(HP);
        }

        if (isEnemyTower(e)) {
            states[tick].enemyTowerHp = e.getProperty(HP);
        }


    }

    @OnEntityUpdated
    public void onUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {
        if (!entities.contains(e.getDtClass().getDtName())) {
            System.out.println(e);
            entities.add(e.getDtClass().getDtName());
        }


        if (gameState != 5
                || !isOurHero(e) && !isEnemyHero(e)
                && !isCreep(e)
                && !isOurTower(e) && !isEnemyTower(e)) {

            //System.out.println(e.getDtClass());
            return;
        }
        if (isOurHero(e)) {
            System.out.println(e.getProperty("CBodyComponent.m_angRotation"));
        }

    }

    @OnGameEvent
    public void onGameEvent(GameEvent event) {
        //log.info("{}", event.toString());
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

    public void run(String[] args) throws Exception {
        long tStart = System.currentTimeMillis();
        new SimpleRunner(new MappedFileSource(args[0])).runWith(this);
        long tMatch = System.currentTimeMillis() - tStart;
        //log.info("total time taken: {}s", (tMatch) / 1000.0);
    }

    public static void main(String[] args) throws Exception {
        Demo.CDemoFileInfo info = Clarity.infoForFile(args[0]);
        winnerTeam = info.getGameInfo().getDota().getGameWinner();

        System.out.println(info);
        states = new State[info.getPlaybackTicks()];
        for (int i = 0; i < info.getPlaybackTicks(); i++) {
            states[i] = new State();
            states[i].ourTeam = winnerTeam - 2;
            states[i].enemyName = info.getGameInfo().getDota().getPlayerInfoList().get(0).getGameTeam() == winnerTeam ?
                            info.getGameInfo().getDota().getPlayerInfoList().get(0).getHeroName() :
                            info.getGameInfo().getDota().getPlayerInfoList().get(1).getHeroName();
            states[i].time = i;
        }

        new Main().run(args);

        for (String s : entities) {
            System.out.println(s);
        }

    }

}