package com.circularuins.twimap;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback {

    private TextView textView;

    private ProgressDialog progressDialog;

    private TweetObj tweetData; //画面回転時のテータ保存用
    private TweetObj restoreTweet;
    private boolean isRotate = false;

    //リストビュー関連変数
    private boolean mTwoPane; // 画面の縦横判定フラグ

    //ナビゲーションドロワー関連変数
    private String[] mDrawerTitles = {"キーワード検索", "現在地検索"};
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    //googleマップ関連変数
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private ArrayList<Marker> markerList = new ArrayList<Marker>(); //マーカー削除のために保存する
    //日本全体俯瞰
    public static final CameraPosition TOKYO =
            new CameraPosition.Builder().target(new LatLng(35.669757, 139.7513493))
                    .zoom(5)
                    .bearing(0)
                    .build();

    //AsyncTaskの拡張インナークラス
    private class GetSearchTask extends GetSearchApiTask {

        private GetSearchTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show(); //プログレスダイアログの表示
        }

        @Override
        protected void onPostExecute(TweetObj data) {
            super.onPostExecute(data);
            progressDialog.hide(); //プログレスダイアログの非表示

            //ツイートデータのメンバ変数への保存
            tweetData = null;
            tweetData = data;

            //ツイートマーカーの表示
            markerSet(data);

            if(data != null) {
                textView.setText("地図検索:" + data.numResult + "件");
            } else if(exception != null) {
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //横表示の時のリストビューの表示
            View tweetListFrame = findViewById(R.id.tweetsList);
            //image表示用のフレームレイアウトが表示されていない（すなわち縦表示の時）、mTwoPaneはfalse
            mTwoPane = tweetListFrame != null &&
                    tweetListFrame.getVisibility() == View.VISIBLE;

            //横表示の時、フレームレイアウトをフラグメントで置き換える
            if (mTwoPane) {
                TweetListFragment fragment = TweetListFragment.newInstance(tweetData);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.tweetsList, fragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

                ft.commit();
            }

            //検索終了したらナビゲーションドロワーを閉じる
            mDrawerLayout.closeDrawers();
        }
    }

    //AsyncTaskを実行するメソッド
    private void executeSearchTask(String keyword) {
        if (!keyword.equals("")) {
            new GetSearchTask(this).execute(keyword);
        }
    }

    //dialog fragmentが閉じられた時のコールバック
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode=37で返ってくる
        if (requestCode == 37) {
            String keyword = data.getStringExtra("keyword");
            //AsyncTaskでツイートの取得を行う
            executeSearchTask(keyword);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.tv_main); //検索数表示用

        //ナビゲーションドロワー
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_drawer);
        mDrawerList.setAdapter(
                new ArrayAdapter<String>(this, R.layout.drawer_text, mDrawerTitles));
        //リストアイテムのクリックリスナー
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // AlertDialogFragmentの呼び出し
                SearchDialogFragment dialog = SearchDialogFragment.newInstance(position);
                dialog.setTargetFragment(null, 37); // requestCodeを37に指定
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        //「検索中」のプログレスダイアログのセット
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("検索中");
        progressDialog.setMessage("しばらくお待ち下さい");

        //googleマップの初期化
        mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap = mapFragment.getMap(); //ここで初期化しておかないとNullPointExceptionになる
    }

    @Override
    protected void onResume() {
        super.onResume();

        //画面が回転した時に、地図の状態を復元する
        mMap = mapFragment.getMap();
        if(restoreTweet != null) {
            //ツイートマーカーの表示
            markerSet(restoreTweet);
            textView.setText("地図検索:" + restoreTweet.numResult + "件");

            tweetData = restoreTweet; //メンバ変数にツイートデータを保存
            isRotate = true; //画面回転したフラグ
        }

        //横表示の時のリストビューの表示
        View tweetListFrame = findViewById(R.id.tweetsList);
        //image表示用のフレームレイアウトが表示されていない（すなわち縦表示の時）、mTwoPaneはfalse
        mTwoPane = tweetListFrame != null &&
                tweetListFrame.getVisibility() == View.VISIBLE;

        //横表示の時、フレームレイアウトをフラグメントで置き換える
        if (mTwoPane && tweetData != null) {
            TweetListFragment fragment = TweetListFragment.newInstance(tweetData);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.tweetsList, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

            ft.commit();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //アプリ起動時（==画面がまだ回転していない時）のみカメラを初期位置に
        if(!isRotate) {
            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(TOKYO);
            mMap.moveCamera(cu);
        }
    }

    public void markerSet(final TweetObj data) {

        //前回分のマーカーの削除
        markerClear();

        if(data != null) {
            for (final Tweet tweet : data.tweetList) {

                //緯度経度情報のないツイートのみ地図にマークする
                if (!(tweet.latitude.equals("0.0") && tweet.longitude.equals("0.0"))) {
                    LatLng pos = new LatLng(Double.parseDouble(tweet.latitude), Double.parseDouble(tweet.longitude));
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title("@" + tweet.screenName)
                            .snippet(tweet.date)
                            .icon(BitmapDescriptorFactory.fromBitmap(tweet.bitmap))
                            .infoWindowAnchor(0.5f, 0.5f));

                    //マーカーidをtweetObjに保存
                    String id = marker.getId(); //"m0", "m14"といった文字列が取得される
                    int num = Integer.parseInt(id.substring(2 - 1)); //idの２文字目から後を取り出す
                    tweet.markerId = num;

                    markerList.add(marker);
                }
            }

            //マーカークリックで、ツイート内容表示
            //個別のマーカーではなく、mMapにリスナーをセットし、マーカーのidを取得して判別してイベントを発生させる
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker markerClicked) {
                    String idClicked = markerClicked.getId(); //マーカーIDが"m0", "m1"といった文字列で取得される
                    int numClicked = Integer.parseInt(idClicked.substring(2-1)); //数値を抜き出してintに変換

                    //マーカーIDが一致するTweetオブジェクトを探し出し、ダイアログに渡して表示する
                    for (int i = 0; i < data.tweetList.size(); i++) {
                        if(data.tweetList.get(i).markerId == numClicked) {
                            // AlertDialogFragmentの呼び出し
                            TweetDialogFragment dialog = TweetDialogFragment.newInstance(data.tweetList.get(i));
                            dialog.show(getFragmentManager(), "dialog");
                            break;
                        }
                    }
                    return false;
                }
            });

            //カメラの移動
            //ツイートリストの先頭の位置に移動する（正しい位置情報をもつデータに限定して探す）
            boolean cameraMove = false;
            if(data.tweetList.size() > 0) {
                for(Tweet tw : data.tweetList) {
                    if (!(tw.latitude.equals("0.0") && tw.longitude.equals("0.0"))) {
                        LatLng pos = new LatLng(Double.parseDouble(tw.latitude), Double.parseDouble(tw.longitude));
                        CameraPosition cp =
                                new CameraPosition.Builder().target(pos)
                                        .zoom(6)
                                        .bearing(0)
                                        .build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
                        cameraMove = true;

                        break;
                    }
                }
            }
            //位置表示できるツイートがない場合、デフォルトのカメラ位置へ
            if(!cameraMove) {
                CameraUpdate cu = CameraUpdateFactory.newCameraPosition(TOKYO);
                mMap.moveCamera(cu);
            }
        }
    }

    public void markerClear() {
        //画面上の全マーカーの削除
        for (Marker marker : markerList) {
            marker.remove();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //ツイートデータの保存
        if(tweetData != null) {
            outState.putInt("numResult", tweetData.numResult);
            outState.putParcelableArrayList("tweetList", tweetData.tweetList);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null) {
            //ツイートデータの復元
            int num = savedInstanceState.getInt("numResult");
            ArrayList<Tweet> tweetlist = savedInstanceState.getParcelableArrayList("tweetList");
            if (tweetlist != null) {
                restoreTweet = new TweetObj(num, tweetlist);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
