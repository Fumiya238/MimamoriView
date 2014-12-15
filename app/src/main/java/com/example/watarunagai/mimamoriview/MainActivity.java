package com.example.watarunagai.mimamoriview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {
    private GoogleMap mMap;
    private LatLng fall,lab;
    private PolylineOptions line;
    private TextView textView;
    private Button button;
    ArrayList<String> max = new ArrayList<String>();
    ArrayList<Double> idobox = new ArrayList<Double>();
    ArrayList<Double> kdobox = new ArrayList<Double>();
    private WebView web;
    private boolean Run;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Run = false;
        lab = new LatLng(35.561888, 139.575263);
        fall = new LatLng(35.561888, 139.575263);
        mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lab, 16));
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        button = (Button)findViewById(R.id.button);
    }


    public  void doRes(View view){
       if (!Run){
           Run = true;
           button.setText("更新");
           exec_post();
           onDraw();
       }else {
           Run =false;
           mMap.clear();
           idobox.clear();
           kdobox.clear();
       }

    }

    private void exec_post() {
        Log.d("posttest", "postします");
        String ret = "";
        // URL
        URI url = null;
        try {
            url = new URI( "http://133.78.124.26/ino/post2.php" );
            Log.d("posttest", "URLはOK");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            ret = e.toString();
        }
        // POSTパラメータ付きでPOSTリクエストを構築
        HttpPost request = new HttpPost( url );
        List<NameValuePair> post_params = new ArrayList<NameValuePair>();
        post_params.add(new BasicNameValuePair("", ""));

        try {
            // 送信パラメータのエンコードを指定
            request.setEntity(new UrlEncodedFormEntity(post_params, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        // POSTリクエストを実行
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            Log.d("posttest", "POST開始");
            ret = httpClient.execute(request, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse response) throws IOException
                {
                    Log.d(
                            "posttest",
                            "レスポンスコード：" + response.getStatusLine().getStatusCode()
                    );
                    // 正常に受信できた場合は200
                    switch (response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_OK:
                            Log.d("posttest", "レスポンス取得に成功");

                            // レスポンスデータをエンコード済みの文字列として取得する
                            return EntityUtils.toString(response.getEntity(), "UTF-8");

                        case HttpStatus.SC_NOT_FOUND:
                            Log.d("posttest", "データが存在しない");
                            return null;

                        default:
                            Log.d("posttest", "通信エラー");
                            return null;
                    }
                }
            });
        } catch (IOException e) {
            Log.d("posttest", "通信に失敗：" + e.toString());
        } finally {
            // shutdownすると通信できなくなる
            httpClient.getConnectionManager().shutdown();
        }
        // 受信結果をUIに表示
        //  tv.setText( ret );
        //   Log.v("Post",""+ ret);

        String[] str = ret.split("=");


    //    Log.v("Post1",""+ ret);

        for (int i=1;i<str.length; i=i + 2){
            Double ido = Double.valueOf(str[i]);
            idobox.add(ido);
        }
        for (int b=2;b<str.length; b=b + 2){
            Double kdo = Double.valueOf(str[b]);
            kdobox.add(kdo);
        }
//        for (int i=0;i<idobox.size();i++){
//            Log.v("Post2",""+ kdobox.get(i));
//        }

    }

    public void onDraw(){
        int i;
        for (i=0;i<idobox.size() -1 ; i++){
            line = new PolylineOptions();
            line.add(new LatLng(idobox.get(i),kdobox.get(i)));
            line.add(new LatLng(idobox.get(i+1),kdobox.get(i+1)));
            line.color(Color.argb(50, 0, 255, 255));
            line.width(10);
            mMap.addPolyline(line);
        }
        fall = new LatLng(idobox.get(idobox.size()-1), kdobox.get(kdobox.size()-1));
        mMap.addMarker(new MarkerOptions().position(fall).title("現在地"));
    }


    public void dodel(View view){
        String url="http://133.78.124.26/ino/koushin3.php";
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        ArrayList <NameValuePair> params = new ArrayList <NameValuePair>();
        params.add( new BasicNameValuePair("", ""));

        HttpResponse res = null;

        try {
            post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            res = httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
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
