package com.circularuins.twimap;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<TweetObj>, OnMapReadyCallback {

    private TextView textView;
    private EditText editSearch;
    private Button btnSearch;

    private ProgressDialog progressDialog;

    private TweetObj tweetData; //画面回転時のテータ保存用
    private TweetObj restoreTweet;
    private boolean isRotate = false;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private ArrayList<Marker> markerList = new ArrayList<Marker>(); //マーカー削除のために保存する
    //日本全体俯瞰
    public static final CameraPosition TOKYO =
            new CameraPosition.Builder().target(new LatLng(35.669757, 139.7513493))
                    .zoom(5)
                    .bearing(0)
                    .build();

    private class GetSearchTask extends GetSearchApiTask {

        private GetSearchTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(TweetObj data) {
            super.onPostExecute(data);
            progressDialog.hide();

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
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.tv_main);
        editSearch = (EditText)findViewById(R.id.editSearch);
        btnSearch = (Button)findViewById(R.id.btnSearch);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("検索中");
        progressDialog.setMessage("しばらくお待ち下さい");

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("keyWord", editSearch.getText().toString());

                //LoaderManegerの初期化
                //getLoaderManager().initLoader(0, bundle, MainActivity.this); //AsyncTaskLoader利用
                new GetSearchTask(MainActivity.this).execute(editSearch.getText().toString()); //AsyncTask利用
            }
        });

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

                LatLng pos = new LatLng(Double.parseDouble(tweet.latitude), Double.parseDouble(tweet.longitude));
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title("@" + tweet.screenName)
                        .snippet(tweet.date)
                        .icon(BitmapDescriptorFactory.fromBitmap(tweet.bitmap))
                        .infoWindowAnchor(0.5f, 0.5f));

                //マーカーidをtweetObjに保存
                String id = marker.getId(); //"m0", "m14"といった文字列が取得される
                int num = Integer.parseInt(id.substring(2-1)); //idの２文字目から後を取り出す
                tweet.markerId = num;

                markerList.add(marker);
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
                            //Toast.makeText(getApplicationContext(), "" + data.tweetList.get(i).markerId, Toast.LENGTH_LONG).show();
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
                    if(!tw.latitude.equals("0.0") && !tw.longitude.equals("0.0")) {
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
            if(num != 0 && tweetlist != null) {
                restoreTweet = new TweetObj(num, tweetlist);
            }
        }
    }

    @Override
    public Loader<TweetObj> onCreateLoader(int id, Bundle args) {
        //Loaderを初期化する
        ApiLoader loader = new ApiLoader(this, args.getString("keyWord", ""));
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<TweetObj> loader, TweetObj data) {
        // dataでは、Loderクラスの戻り値が返される
        if(data != null) {
            //textView.setText(data);
        } else if(data != null) {
            Toast.makeText(MainActivity.this, "データの読み込みに失敗しました", Toast.LENGTH_SHORT).show();
        }

        Log.d("", "onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<TweetObj> loader) {
        // Loderが、リセットされるときに呼ばれる。
        // ここで、もらっているdataを破棄する必要がある。
        Log.d("","onLoaderReset");
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
