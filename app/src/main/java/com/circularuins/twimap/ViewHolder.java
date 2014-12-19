package com.circularuins.twimap;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by wake on 2014/12/20.
 */
public class ViewHolder {
    ImageView image;
    TextView name;
    TextView text;
    TextView date;

    ViewHolder(View base) {
        this.image = (ImageView) base.findViewById(R.id.rowImg);
        this.name = (TextView) base.findViewById(R.id.rowName);
        this.text = (TextView) base.findViewById(R.id.rowText);
        this.date = (TextView) base.findViewById(R.id.rowDate);
    }
}
