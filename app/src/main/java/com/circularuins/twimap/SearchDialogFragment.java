package com.circularuins.twimap;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by wake on 2014/12/21.
 */
public class SearchDialogFragment extends DialogFragment {

    public static SearchDialogFragment newInstance(int position) {
        SearchDialogFragment instance = new SearchDialogFragment();

        Bundle arguments = new Bundle();
        //IDをBundleに保存する
        arguments.putInt("position", position);
        //データを保存してセット
        instance.setArguments(arguments);

        return instance;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        //タイトル
        dialog.setTitle("検索");
        //ダイアログ外タップで消えないように設定
        //dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Viewと各参照の取得
        View content = inflater.inflate(R.layout.search_dialog, null);
        TextView searchText = (TextView) content.findViewById(R.id.searchText);
        final EditText searchEdit = (EditText) content.findViewById(R.id.searchEdit);
        Button searchBtn = (Button) content.findViewById(R.id.searchBtn);

        /*
        * ナビゲーションドロワーで選択された種類の検索のためのキーワードを取得し、MainActivityのonActivityResultに渡す
        * 0 : キーワード検索（search API）
        * 1 : 現在位置検索（未実装）
        */
        int position = getArguments().getInt("position");
        switch (position) {
            case 0:
                searchText.setText("入力されたキーワードによるツイートの検索を行います。\n最大で最新の100件のツイートを表示し、そのうち位置情報を含むツイートは地図上に表示されます。");
                searchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //検索ワードをインテントにまとめて、MainActivityに返す
                        String keyword = searchEdit.getText().toString();
                        Intent result = new Intent();
                        result.putExtra("keyword", keyword);

                        if (getTargetFragment() != null) {
                            // 呼び出し元がFragmentの場合
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
                        } else {
                            // 呼び出し元がActivityの場合
                            PendingIntent pi = getActivity().createPendingResult(getTargetRequestCode(), result,
                                    PendingIntent.FLAG_ONE_SHOT);
                            try {
                                pi.send(Activity.RESULT_OK);
                            } catch (PendingIntent.CanceledException ex) {
                                // send failed
                            }
                        }

                        dismiss();
                    }
                });
                break;
            case 1:
                searchText.setText("現在位置周辺のツイートを検索します\n（未実装）");
                searchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                break;
        }

        return content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //カスタムのスタイルを適用する
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_CustomDialog);
    }
}
