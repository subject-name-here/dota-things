package ru.spbau.mit.java.paradov;


import com.google.gson.Gson;

public class Main {

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser(args);
        parser.run();

        State[] states = parser.getStates();
        Action[] actions = parser.getActions();


        int beginTick = parser.getTickBorders().fst;
        int endTick = parser.getTickBorders().snd;

        for (int i = beginTick; i < endTick; i++) {
            if (states[i].time != 0) {
                Gson gson = new Gson();
                /*System.out.println(i);
                states[i].print();
                System.out.println(actions[i]);
                System.out.println();*/
                String jsonState = gson.toJson(states[i]);
                String jsonAction = gson.toJson(actions[i]);
                System.out.println(jsonState);
                System.out.println(jsonAction);
                // TODO: sent it to server
            }
        }
    }
}