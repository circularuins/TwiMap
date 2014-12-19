package com.circularuins.twimap;

import android.app.ListFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by wake on 2014/12/19.
 */
public class TweetListFragment extends ListFragment {
    private TweetAdapter adapter;

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
        ArrayList<RowModel> list = new ArrayList<RowModel>();

        for (Tweet tweet : tweetList) {
            list.add(new RowModel(tweet.bitmap, tweet.screenName, tweet.text, tweet.date));
        }

        adapter = new TweetAdapter(list);
        setListAdapter(adapter);
    }

    private RowModel getModel(int position) {
        return (((TweetAdapter) getListAdapter()).getItem(position));
    }

    class TweetAdapter extends ArrayAdapter<RowModel> {
        TweetAdapter(ArrayList<RowModel> list) {
            super(getActivity(), R.layout.tweet_row, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.tweet_row, parent, false);
            }

            //holderパターンを使用して高速化を狙う
            ViewHolder holder = (ViewHolder) row.getTag();
            //その入れ物が空かどうか確認する
            if (holder == null) {
                //各行内の個別のオブジェクトの参照を一度だけfindViewByIdする
                holder = new ViewHolder(row);
                row.setTag(holder);
                //リスト変更時に元データを変更させるためにリスナーを登録する
                //holder.rate.setOnRatingBarChangeListener(new RowViewRating(holder));
            }
            RowModel model = getModel(position);
            //holder.image.setTag(new Integer(position));
            holder.image.setImageBitmap(model.image);
            holder.name.setText("@" + model.name);
            holder.text.setText(model.text);
            holder.date.setText(model.date);

            return row;
        }
    }

    /*
    * 各行のデータを保存するクラス
    *（エンティティ）
     */
    class RowModel {
        Bitmap image;
        String name;
        String text;
        String date;

        RowModel(Bitmap image, String name, String text, String date) {
            this.image = image;
            this.name = name;
            this.text = text;
            this.date = date;
        }

        /*public String toString() {
            if(rating >= 3.0) {
                //大文字にする（元データは変更されない）
                return (label.toUpperCase());
            }
            return (label);
        }*/
    }
}
