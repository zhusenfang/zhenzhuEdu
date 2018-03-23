package com.xieruinet.zhu.xieruiedu;




import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import java.security.cert.X509CRLEntry;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import android.net.Uri;


public class HomeActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
    private VideoView mVvv;

    //@Override
    //protected void onCreate(Bundle savedInstanceState) {
    //    super.onCreate(savedInstanceState);
    //    setContentView(R.layout.activity_home);
    //    mWebView = (WebView) findViewById(R.id.mywebview);
    //    LoadUrl("http://www.baidu.com");
    //}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        //一定要初始化
        Vitamio.initialize(this);
        mVvv = (VideoView)findViewById(R.id.vv);

        Intent _intent = getIntent();
        //从Intent当中根据key取得value
       if (_intent != null) {
           String _value = _intent.getStringExtra("url");
           Toast.makeText(HomeActivity.this, _value, Toast.LENGTH_SHORT).show();
           mVvv.setVideoURI(Uri.parse(_value));
       }
        //mVvv.setVideoURI(Uri.parse("http://zv.3gv.ifeng.com/live/zixun800k.m3u8"));
       // mVvv.setVideoURI(Uri.parse("http://qiubai-video.qiushibaike.com/91B2TEYP9D300XXH_3g.mp4"));
        mVvv.setMediaController(new MediaController(this));

        //设置监听
        mVvv.setOnPreparedListener(this);
        mVvv.setOnErrorListener(this);
        mVvv.setOnCompletionListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Toast.makeText(this,"准备好了", Toast.LENGTH_LONG).show();
        mVvv.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this,"Error", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent();
        myIntent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(myIntent);
//        return false;
//          返回 true
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        Toast.makeText(this,"播放完成", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent();
        myIntent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(myIntent);
        this.finish();
        //finish();
    }


}
