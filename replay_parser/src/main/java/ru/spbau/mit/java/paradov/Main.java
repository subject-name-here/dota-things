package ru.spbau.mit.java.paradov;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        File folder = new File(args[0]);
        for (File f : folder.listFiles()){
            String arg = f.getPath();
            Parser parser = new Parser(arg);
            parser.run();

            State[] states = parser.getStates();
            Action[] actions = parser.getActions();

            int beginTick = parser.getTickBorders().fst;
            int endTick = parser.getTickBorders().snd;

            String url = "http://127.0.0.1:22229/save";
            Gson gson = new Gson();

            int cnt = 0;
            for (int i = beginTick; i < endTick; i++) {
                if (states[i].time != 0) {
                    if (cnt % 100 == 0) {
                        System.err.println(cnt);
                    }

                    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(1000).setSocketTimeout(1000).build();
                    HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
                    HttpPost post = new HttpPost(url);

                    /* String jsonState = gson.toJson(states[i]);
                    String jsonAction = gson.toJson(actions[i]);
                    System.out.println(jsonState);
                    System.out.println(jsonAction); */

                    StringEntity json = new StringEntity(gson.toJson(new StateActionPair(states[i], actions[i])));
                    post.setEntity(json);
                    post.setHeader("Content-type", "application/json");
                    try {
                        HttpResponse response = client.execute(post);
                    } catch (Exception e) {

                    }
                    cnt++;
                }
            }
        }
    }
}