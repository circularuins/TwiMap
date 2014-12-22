package com.circularuins.twimap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.circularuins.twimap.volley.utils.LruBitmapCache;

import java.util.ArrayList;

/**
 * Created by wake on 2014/12/16.
 */
//public class TweetDialogFragment {
//}

public class TweetDialogFragment extends DialogFragment {
    //Volley関連の変数
    private RequestQueue mQueue;
    private ImageLoader imageLoader;

    public static TweetDialogFragment newInstance(Tweet tweet, ArrayList<Tweet> tweetList) {
        TweetDialogFragment instance = new TweetDialogFragment();

        Bundle arguments = new Bundle();
        //ツイート内容をBundleに保存する
        arguments.putString("id", tweet.id);
        arguments.putString("name", tweet.screenName);
        arguments.putString("user", tweet.userName);
        arguments.putString("loc", tweet.location);
        arguments.putString("fol", tweet.followers);
        arguments.putString("text", tweet.text);
        arguments.putString("date", tweet.date);
        arguments.putString("url", tweet.profileImage);
        //ビットマップは、parcelableで変換して保存する
        arguments.putParcelable("image", tweet.bitmap);
        arguments.putParcelableArrayList("tweetList", tweetList);
        //データを保存してセット
        instance.setArguments(arguments);

        return instance;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        //queue for imageLoader
        mQueue = Volley.newRequestQueue(getActivity());

        //タイトル
        dialog.setTitle("Tweet");
        //ダイアログ外タップで消えないように設定
        //dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //ツイートデータの読み込み
        //ビットマップはparcelableで変換して取得する
        Bitmap image = getArguments().getParcelable("image");
        String url = getArguments().getString("url");
        final String id = getArguments().getString("id");
        String name = getArguments().getString("name");
        String user = getArguments().getString("user");
        String loc = getArguments().getString("loc");
        String fol = getArguments().getString("fol");
        String text = getArguments().getString("text");
        String date = getArguments().getString("date");
        final ArrayList<Tweet> tweetList = getArguments().getParcelableArrayList("tweetList");

        //Viewの取得
        View content = inflater.inflate(R.layout.tweet_dialog, null);

        //各内容の表示処理
        final ImageView twImg = (ImageView) content.findViewById(R.id.twImg);
        TextView twName = (TextView)content.findViewById(R.id.twName);
        TextView twUser = (TextView) content.findViewById(R.id.twUser);
        TextView twLoc = (TextView)content.findViewById(R.id.twLoc);
        TextView twFol = (TextView)content.findViewById(R.id.twFol);
        TextView twText = (TextView)content.findViewById(R.id.twText);
        TextView twDate = (TextView)content.findViewById(R.id.twDate);
        Button btnOK = (Button) content.findViewById(R.id.btnOK);

        /*
        * 初めて画像を読み込む場合はvolleyでネットから。
        * それ以降は、tweetオブジェクトから読み込む。
        */
        if (image != null) {
            twImg.setImageBitmap(image);
        } else {
            imageLoader = new ImageLoader(mQueue, new LruBitmapCache());
            imageLoader.get(url, new ImageLoader.ImageListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", "Image Load Error: " + error.getMessage());
                }

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                    if (response.getBitmap() != null) {
                        //画像を非同期に読み込んで、ImageViewにセットする
                        twImg.setImageBitmap(response.getBitmap());
                        //該当のTweetオブジェクトにBitmapをセット
                        for (Tweet tweet : tweetList) {
                            if (id.equals(tweet.id)) tweet.bitmap = response.getBitmap();
                        }
                    }
                }
            });
        }

        //twImg.setImageBitmap(image);
        twName.setText("@" + name);
        twUser.setText("(" + user + ")");
        twLoc.setText("地域：" + loc);
        twFol.setText("フォロワー：" + fol + "人");
        twText.setText(text);
        twDate.setText(date);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); //ダイアログを閉じる
            }
        });

        return content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //カスタムのスタイルを適用する
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_CustomDialog);
    }
}
