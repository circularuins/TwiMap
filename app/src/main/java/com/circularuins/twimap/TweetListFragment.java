package com.circularuins.twimap;

import android.app.ListFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.circularuins.twimap.volley.utils.LruBitmapCache;

import java.util.ArrayList;

/**
 * Created by wake on 2014/12/19.
 */
public class TweetListFragment extends ListFragment {
    private ArrayList<Tweet> tweetList;
    private TweetAdapter adapter;
    //Volley関連の変数
    private RequestQueue mQueue;
    private ImageLoader imageLoader;

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

        //queue for imageLoader
        mQueue = Volley.newRequestQueue(getActivity());

        tweetList = getArguments().getParcelableArrayList("tweets");
        ArrayList<RowModel> list = new ArrayList<RowModel>();

        for (Tweet tweet : tweetList) {
            list.add(new RowModel(tweet.bitmap, tweet.profileImage, tweet.id, tweet.screenName, tweet.text, tweet.date));
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
            imageLoader = new ImageLoader(mQueue, new LruBitmapCache());
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
            }
            final RowModel model = getModel(position);
            //holder.image.setTag(new Integer(position));

            if (model.image != null) {
                holder.image.setImageBitmap(model.image);
            } else {
                //画像を非同期に読み込んで、ImageViewにセットする
                ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                        holder.image, R.drawable.notfound, R.drawable.notfound);
                imageLoader.get(model.url, listener);
                // TODO: 上の処理と統合して1回のリクエストに抑えたい
                //もう一度読み込んで、Tweetオブジェクトにも保存する（自前キャッシュ）
                imageLoader.get(model.url, new ImageLoader.ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "Image Load Error: " + error.getMessage());
                    }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                        if (response.getBitmap() != null) {
                            //該当のTweetオブジェクトにBitmapをセット
                            for (Tweet tweet : tweetList) {
                                if (model.id.equals(tweet.id)) tweet.bitmap = response.getBitmap();
                            }
                        }
                    }
                });
            }

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
        String url;
        String id;
        String name;
        String text;
        String date;

        RowModel(Bitmap image, String url, String id, String name, String text, String date) {
            this.image = image;
            this.url = url;
            this.id = id;
            this.name = name;
            this.text = text;
            this.date = date;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        RowModel model = getModel(position);
        //ツイートIDが一致するTweetオブジェクトを探し出し、ダイアログに渡して表示する
        for (int i = 0; i < tweetList.size(); i++) {
            if (tweetList.get(i).id.equals(model.id)) {
                // AlertDialogFragmentの呼び出し
                TweetDialogFragment dialog = TweetDialogFragment.newInstance(tweetList.get(i), tweetList);
                dialog.show(getFragmentManager(), "dialog");
                break;
            }
        }
    }
}
