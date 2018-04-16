package ru.spbau.mit.java.paradov;


import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skadistats.clarity.Clarity;
import skadistats.clarity.model.CombatLogEntry;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.model.GameEvent;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.gameevents.OnGameEvent;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.wire.common.proto.Demo;
import skadistats.clarity.wire.common.proto.DotaUserMessages;

public class Main {
    private static int winnerTeam;

    private final Logger log = LoggerFactory.getLogger(Main.class.getPackage().getClass());

    private final PeriodFormatter GAMETIME_FORMATTER = new PeriodFormatterBuilder()
            .minimumPrintedDigits(2)
            .printZeroAlways()
            .appendHours()
            .appendLiteral(":")
            .appendMinutes()
            .appendLiteral(":")
            .appendSeconds()
            .appendLiteral(".")
            .appendMillis3Digit()
            .toFormatter();

    private String compileName(String attackerName, boolean isIllusion, Integer team) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") + team.toString(): "UNKNOWN";
    }

    private String getAttackerNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getAttackerName(), cle.isAttackerIllusion(), cle.getAttackerTeam());
    }

    private String getTargetNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getTargetName(), cle.isTargetIllusion(), cle.getTargetTeam());
    }

    private FieldPath mana;
    private FieldPath maxMana;

    private boolean isHero(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTA_Unit_Hero");
    }

    private void ensureFieldPaths(Entity e) {
        if (mana == null) {
            mana = e.getDtClass().getFieldPathForName("m_flMana");
            maxMana = e.getDtClass().getFieldPathForName("m_flMaxMana");
        }
    }

    @OnEntityCreated
    public void onCreated(Entity e) {
        if (!isHero(e)) {
            return;
        }
        ensureFieldPaths(e);
        System.out.format("%s (%s/%s)\n", e.getDtClass().getDtName(), e.getPropertyForFieldPath(mana), e.getPropertyForFieldPath(maxMana));
    }

    @OnEntityUpdated
    public void onUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {
        if (!isHero(e)) {
            return;
        }
        ensureFieldPaths(e);
        boolean update = false;
        for (int i = 0; i < updateCount; i++) {
            if (updatedPaths[i].equals(mana) || updatedPaths[i].equals(maxMana)) {
                update = true;
                break;
            }
        }
        if (update) {
            System.out.format("%s (%s/%s)\n", e.getDtClass().getDtName(), e.getPropertyForFieldPath(mana), e.getPropertyForFieldPath(maxMana));
        }
    }

    @OnGameEvent
    public void onGameEvent(GameEvent event) {
        log.info("{}", event.toString());
    }

    @OnCombatLogEntry
    public void onCombatLogEntry(CombatLogEntry cle) {
        String time = "[" + GAMETIME_FORMATTER.print(Duration.millis((int) (1000.0f * cle.getTimestamp())).toPeriod()) + "]";
        switch (cle.getType()) {
            case DOTA_COMBATLOG_DAMAGE:
                log.info("{} {} hits {}{} for {} damage{}",
                        time,
                        getAttackerNameCompiled(cle),
                        getTargetNameCompiled(cle),
                        cle.getInflictorName() != null ? String.format(" with %s", cle.getInflictorName()) : "",
                        cle.getValue(),
                        cle.getHealth() != 0 ? String.format(" (%s->%s)", cle.getHealth() + cle.getValue(), cle.getHealth()) : ""
                );
                break;
            case DOTA_COMBATLOG_HEAL:
                log.info("{} {}'s {} heals {} for {} health ({}->{})",
                        time,
                        getAttackerNameCompiled(cle),
                        cle.getInflictorName(),
                        getTargetNameCompiled(cle),
                        cle.getValue(),
                        cle.getHealth() - cle.getValue(),
                        cle.getHealth()
                );
                break;
            case DOTA_COMBATLOG_MODIFIER_ADD:
                log.info("{} {} receives {} buff/debuff from {}",
                        time,
                        getTargetNameCompiled(cle),
                        cle.getInflictorName(),
                        getAttackerNameCompiled(cle)
                );
                break;
            case DOTA_COMBATLOG_MODIFIER_REMOVE:
                log.info("{} {} loses {} buff/debuff",
                        time,
                        getTargetNameCompiled(cle),
                        cle.getInflictorName()
                );
                break;
            case DOTA_COMBATLOG_DEATH:
                log.info("{} {} is killed by {}",
                        time,
                        getTargetNameCompiled(cle),
                        getAttackerNameCompiled(cle)
                );
                break;
            case DOTA_COMBATLOG_ABILITY:
                log.info("{} {} {} ability {} (lvl {}){}{}",
                        time,
                        getAttackerNameCompiled(cle),
                        cle.isAbilityToggleOn() || cle.isAbilityToggleOff() ? "toggles" : "casts",
                        cle.getInflictorName(),
                        cle.getAbilityLevel(),
                        cle.isAbilityToggleOn() ? " on" : cle.isAbilityToggleOff() ? " off" : "",
                        cle.getTargetName() != null ? " on " + getTargetNameCompiled(cle) : ""
                );
                break;
            case DOTA_COMBATLOG_ITEM:
                log.info("{} {} uses {}",
                        time,
                        getAttackerNameCompiled(cle),
                        cle.getInflictorName()
                );
                break;
            case DOTA_COMBATLOG_GOLD:
                if (getTargetNameCompiled(cle) == "npc_dota_hero_nevermore"
                        && cle.getTargetTeam() == winnerTeam) {
                    log.info("{} {} {} {} gold",
                            time,
                            getTargetNameCompiled(cle),
                            cle.getValue() < 0 ? "looses" : "receives",
                            Math.abs(cle.getValue())
                    );
                }
                break;
            case DOTA_COMBATLOG_GAME_STATE:
                log.info("{} game state is now {}",
                        time,
                        cle.getValue()
                );
                break;
            case DOTA_COMBATLOG_XP:
                if (getTargetNameCompiled(cle) == "npc_dota_hero_nevermore"
                        && cle.getTargetTeam() == winnerTeam) {
                    log.info("{} {} gains {} XP",
                            time,
                            getTargetNameCompiled(cle),
                            cle.getValue()
                    );
                }
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
                break;

            default:
                DotaUserMessages.DOTA_COMBATLOG_TYPES type = cle.getType();
                log.info("\n{} ({}): {}\n", type.name(), type.ordinal(), cle);
                break;

        }
    }

    public void run(String[] args) throws Exception {
        long tStart = System.currentTimeMillis();
        new SimpleRunner(new MappedFileSource(args[0])).runWith(this);
        long tMatch = System.currentTimeMillis() - tStart;
        log.info("total time taken: {}s", (tMatch) / 1000.0);
    }

    public static void main(String[] args) throws Exception {
        Demo.CDemoFileInfo info = Clarity.infoForFile(args[0]);
        winnerTeam = info.getGameInfo().getDota().getGameWinner();

        new Main().run(args);
    }

}