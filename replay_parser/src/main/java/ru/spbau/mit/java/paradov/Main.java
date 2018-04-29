package ru.spbau.mit.java.paradov;


public class Main {

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser(args);
        parser.run();

        State[] states = parser.getStates();
        Action[] actions = parser.getActions();


        int beginTick = parser.getTickBorders().fst;
        int endTick = parser.getTickBorders().snd;

        for (State s: states) {
            if (s.time >= beginTick && s.time < endTick && s.time != 0) {
                s.print();
            }
        }

        int prevChangedState = 0;
        for (int i = beginTick; i + 1 < endTick; i++) {
            State s = states[i];
            Action a = actions[i];
            if (s.time != 0 || a.actionType != -1) {
                if (a.actionType == 3) {
                    continue;
                }

                if (a.actionType == 1 || a.actionType == 2 || a.actionType == 4) {
                    // TODO: get creep number
                    actions[i].param = 1;
                    for (int j = 1; j <= 30; j++) {
                        actions[i - j].actionType = a.actionType;
                        actions[i - j].param = a.param;
                    }
                } else {
                    a.actionType = 0;
                    // TODO: get dx and dy from previous changed state
                    a.dx = states[i].ourX - states[prevChangedState].ourX;
                    a.dy = states[i].ourY - states[prevChangedState].ourY;
                }
                prevChangedState = i;
            }
        }

        for (int i = beginTick; i < endTick; i++) {
            if (states[i].time != 0) {
                System.out.println(i);
                System.out.println(actions[i]);
            }
        }

    }
}