package com.circularuins.twimap;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by wake on 2014/12/13.
 */
public class ApiLoader extends AsyncTaskLoader<TweetObj> {

    private TweetObj data;
    private String apiWord;
    private final Context context;

    public ApiLoader(Context context, String s) {
        super(context);
        this.context = context;
        this.apiWord = s;
    }

    @Override
    public void deliverResult(TweetObj data) {
        // Loderが処理した結果を返す。(メインスレッドで実行される)
        if(isReset()) {
            // An async query came in while the loader is stopped
            return;
        }
        this.data = data;
        super.deliverResult(data);
    }

    @Override
    public TweetObj loadInBackground() {
        // Loderが実行するバックグラウンド処理
        try {
            return SearchApi.getTweet(context, apiWord);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        // Loder側の準備ができたタイミングで呼び出される
        if(data != null) {
            deliverResult(data);
        }
        // UIスレッドで実装される
        if(takeContentChanged() || data == null) {
            forceLoad();
            Log.d("", "forceLoad");
        }
        Log.d("", "onStartLoading");
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        data = null;
    }
}
