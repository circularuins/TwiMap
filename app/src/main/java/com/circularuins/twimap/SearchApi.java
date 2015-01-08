package com.circularuins.twimap;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by wake on 2014/12/13.
 */
public class SearchApi {

    private static final String USER_AGENT = "TwiMap Client";
    private static final String URL = "http://circularuins.com:8080/twitter/search/";

    public static TweetObj getTweet(Context context, String keyWord)
            throws IOException, JSONException {

        AndroidHttpClient client = AndroidHttpClient.newInstance(USER_AGENT, context);
        HttpGet get = new HttpGet(URL + keyWord);

        StringBuilder sb = new StringBuilder();
        try {
            HttpResponse response = client.execute(get);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent())
            );
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            client.close();
        }

        return new TweetObj(new JSONObject(sb.toString()), context);
    }
}
