package ru.spbau.mit.java.paradov;


public class Main {
    private static void statesClosure(State[] states) {

    }

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser(args);
        parser.run();

        State[] states = parser.getStates();
        statesClosure(states);

        for (State s: states) {
            /*if (s.time + 5 >= parser.getTickBorders().fst && s.time <= parser.getTickBorders().snd + 5
                    && s.hp != 0) {
                System.out.format("Tick: %d. HP: %d; MANA: %d; LVL: %d; X: %d; Y: %d; Angle: %f; \n" +
                                "ENEMY_HP: %d; ENEMY_MANA: %d; ENEMY_LVL: %d; ENEMY_X: %d; ENEMY_Y: %d; \n",
                        s.time,
                        s.hp, s.mana, s.lvl, s.ourX, s.ourY, s.facing,
                        s.enemyHp, s.enemyMana, s.enemyLvl, s.enemyX, s.enemyY, s.enemyFacing);
                System.out.println();
            }*/


        }
    }
}


/*
CDOTA_BaseNPC_Barracks
CDOTAGamerulesProxy
CDOTA_Ability_Nevermore_Requiem
CDOTA_Unit_Hero_Nevermore
CDynamicProp
CDOTA_DataRadiant
CDOTA_DataCustomTeam
CDOTAPlayer
CDOTA_BaseNPC_Tower
CDOTA_BaseNPC_Creep_Siege
CDOTA_DataDire
CDOTABaseAbility
CDOTATeam
CInfoWorldLayer
CDOTA_BaseNPC_Effigy_Statue
CDOTAWearableItem
CDOTA_BaseNPC_Fort
CParticleSystem
CDOTA_Ability_Nevermore_Shadowraze
CDOTA_BaseNPC_Creep_Neutral
CDOTA_BaseNPC_Creep_Lane
CDOTA_PlayerResource
 */