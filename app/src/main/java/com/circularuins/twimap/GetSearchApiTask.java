package com.circularuins.twimap;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by wake on 2014/12/13.
 */
public class GetSearchApiTask extends AsyncTask<String, Void, TweetObj> {

    private final Context context;
    Exception exception;

    public GetSearchApiTask(Context context) {
        this.context = context;
    }

    @Override
    protected TweetObj doInBackground(String... params) {
        try {
            return SearchApi.getTweet(context, params[0]);
        } catch (IOException e) {
            exception = e;
        } catch (JSONException e) {
            exception = e;
        }
        return null;
    }
}
