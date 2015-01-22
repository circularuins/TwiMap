package com.circularuins.twimap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by wake on 2015/01/22.
 */
public class CustumProgressDialogFragment extends DialogFragment {

    public static CustumProgressDialogFragment newInstance() {
        CustumProgressDialogFragment instance = new CustumProgressDialogFragment();

        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("検索中...");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.custum_progress_dialog, null);
        TextView progressText = (TextView) content.findViewById(R.id.progressText);
        progressText.setText("しばらくお待ち下さいm(_ _)m");

        return content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_CustomDialog);
    }
}
