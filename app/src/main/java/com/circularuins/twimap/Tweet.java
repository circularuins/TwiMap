package com.circularuins.twimap;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wake on 2014/12/18.
 */
public class Tweet implements Parcelable {
    public final String screenName;
    public final String profileImage;
    public final String text;
    public final String longitude;
    public final String latitude;
    public final String date;
    public int markerId;
    public Bitmap bitmap;
    public final String location;
    public final String followers;

    //JSONから読み込むコンストラクタ
    public Tweet(JSONObject jsonObject) throws JSONException {
        screenName = jsonObject.getString("screenName");
        profileImage = jsonObject.getString("profileImage");
        text = jsonObject.getString("text");
        longitude = jsonObject.getString("longitude");
        latitude = jsonObject.getString("latitude");
        location = jsonObject.getString("location");
        followers = jsonObject.getString("followers");
        date = jsonObject.getString("date");
    }

    /*
    * 以下、Parcelableをこのクラスに実装する記述
    */
    @Override
    public int describeContents() {
        return 0;
    }

    private Tweet(final Parcel in) {
        screenName = in.readString();
        profileImage = in.readString();
        text = in.readString();
        longitude = in.readString();
        latitude = in.readString();
        date = in.readString();
        markerId = in.readInt();
        bitmap = in.readParcelable(null);
        location = in.readString();
        followers = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(screenName);
        dest.writeString(profileImage);
        dest.writeString(text);
        dest.writeString(longitude);
        dest.writeString(latitude);
        dest.writeString(date);
        dest.writeInt(markerId);
        dest.writeParcelable(bitmap, 0); //第二引数は？
        dest.writeString(location);
        dest.writeString(followers);
    }

    //よくわからないが、staticで実装しないといけないらしい
    public static final Parcelable.Creator<Tweet> CREATOR
            = new Parcelable.Creator<Tweet>() {
        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
