package com.circularuins.twimap;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wake on 2014/12/19.
 */
public class TweetListFragment extends ListFragment {

    //ファクトリーメソッド
    public static TweetListFragment newInstance(TweetObj tweetObj) {
        TweetListFragment fragment = new TweetListFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("tweets", tweetObj.tweetList);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Tweet> tweetList = getArguments().getParcelableArrayList("tweets");

        //ユーザー名だけリストアップしてみるテスト
        List<String> alist = new ArrayList<String>();
        for (Tweet tweet : tweetList) {
            alist.add(tweet.screenName + "\n" + tweet.text);
        }
        setListAdapter(new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                alist
        ));
    }
}
