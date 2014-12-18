package com.circularuins.twimap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by wake on 2014/12/16.
 */
//public class TweetDialogFragment {
//}

public class TweetDialogFragment extends DialogFragment {

    public static TweetDialogFragment newInstance(Tweet tweet) {
        TweetDialogFragment instance = new TweetDialogFragment();

        Bundle arguments = new Bundle();
        //ツイート内容をBundleに保存する
        arguments.putString("name", tweet.screenName);
        arguments.putString("loc", tweet.location);
        arguments.putString("fol", tweet.followers);
        arguments.putString("text", tweet.text);
        arguments.putString("date", tweet.date);
        //ビットマップは、parcelableで変換して保存する
        arguments.putParcelable("image", tweet.bitmap);
        //データを保存してセット
        instance.setArguments(arguments);

        return instance;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.TransparentDialogTheme);
        //ダイアログの背景を完全に透過
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //フルスクリーンでダイアログを表示
        //dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        return dialog;

        /*String text = getArguments().getString("text");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle("タイトル");
        builder.setMessage(text);
        builder.setPositiveButton("閉じる",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });

        return builder.create();*/
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //ツイートデータの読み込み
        //ビットマップはparcelableで変換して取得する
        Bitmap image = getArguments().getParcelable("image");
        String name = getArguments().getString("name");
        String loc = getArguments().getString("loc");
        String fol = getArguments().getString("fol");
        String text = getArguments().getString("text");
        String date = getArguments().getString("date");

        //Viewの取得
        View content = inflater.inflate(R.layout.tweetdialog, null);

        //各内容の表示処理
        ImageView twImg = (ImageView)content.findViewById(R.id.twImg);
        TextView twName = (TextView)content.findViewById(R.id.twName);
        TextView twLoc = (TextView)content.findViewById(R.id.twLoc);
        TextView twFol = (TextView)content.findViewById(R.id.twFol);
        TextView twText = (TextView)content.findViewById(R.id.twText);
        TextView twDate = (TextView)content.findViewById(R.id.twDate);

        twImg.setImageBitmap(image);
        twName.setText("@" + name);
        twLoc.setText("地域：" + loc);
        twFol.setText("フォロワー：" + fol + "人");
        twText.setText(text);
        twDate.setText(date);

        return content;
    }
}
