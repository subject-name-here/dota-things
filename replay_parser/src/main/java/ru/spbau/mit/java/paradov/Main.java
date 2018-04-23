package ru.spbau.mit.java.paradov;


public class Main {

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser(args);
        parser.run();

        State[] states = parser.getStates();

        for (State s: states) {
            if (s.time + 5 >= parser.getTickBorders().fst && s.time <= parser.getTickBorders().snd + 5
                    && s.time != 0 && s.time <= 10000) {
                //System.out.println(s);
            }


        }
    }
}


/*
CDOTA_BaseNPC_Barracks
CDOTAGamerulesProxy
CDOTA_Ability_Nevermore_Requiem
CDOTA_Unit_Hero_Nevermore
CDOTA_DataRadiant
CDOTA_DataCustomTeam
CDOTAPlayer
CDOTA_BaseNPC_Tower
CDOTA_BaseNPC_Creep_Siege
CDOTA_DataDire
CDOTABaseAbility
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