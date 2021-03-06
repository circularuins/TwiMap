package com.circularuins.twimap;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by wake on 2014/12/14.
 */
public class TweetObj {

    //JSONデータから変換された、Tweet内容のリスト
    public ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
    public int numResult;

    //検索データ保存用のコンストラクタ
    public TweetObj(int numResult, ArrayList<Tweet> tweetList) {
        this.numResult = numResult;
        this.tweetList = tweetList;
    }

    //JSON読み込み用のコンストラクタ
    public TweetObj(JSONObject jsonObject, Context context) throws JSONException {

        JSONArray tweetArray = jsonObject.getJSONArray("tweets");

        int length = tweetArray.length();
        int cnt = 0;
        for (int i = 0; i < length; i++) {
            JSONObject tweetJson = tweetArray.getJSONObject(i);
            Tweet tweet = new Tweet(tweetJson);

            //正しい位置情報を持つツイートのカウント
            if (!(tweet.latitude.equals("0.0") && tweet.longitude.equals("0.0"))) cnt++;

            tweetList.add(tweet);
        }
        numResult = cnt;
    }
}
