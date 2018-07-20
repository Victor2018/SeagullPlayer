package com.victor.player;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.victor.player.library.util.Constant;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "PlayActivity";
    private static final String YOUTUBE_ID = "lzbJ8E-WjFI";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=SMcXGeltEQQ";
//    private static final String YOUTUBE_URL_WB = "https://www.youtube.com/watch?v=unh8kWUuNt4";
//    private static final String YOUTUBE_URL_WB = "https://www.youtube.com/watch?v=h6DwAox2hF8";
//    private static final String YOUTUBE_URL_WB = "https://www.youtube.com/watch?v=8g_wa06LlCA";
    private static final String YOUTUBE_URL_WB = "https://www.youtube.com/watch?v=j-dv_dcdd_a&list=plfgqulnl59anxjegnq9iu12s2j03nmsrr&index=9&t=0s";
//    private static final String YOUTUBE_URL_WB = "https://www.youtube.com/watch?v=yk2CUjbyyQY";//live
    private static final String VIMEO_ID   = "204150149";
    private static final String VIMEO_URL   = "https://vimeo.com/channels/staffpicks/262705319";
//	    private static final String VIMEO_URL   = "https://vimeo.com/269827127";

    /**
     * SDMC 播放地址为鉴权后的真实播放地址，播放器不做鉴权业务处理
     */
    private static final String M3U8_URL   = "http://ivi.bupt.edu.cn/hls/cctv3.m3u8";
//    private static final String FACEBOOK_URL   = "https://www.facebook.com/misswymma2/videos/599083000439930/";
    private static final String FACEBOOK_URL   = "https://www.facebook.com/1541202502800731/videos/1995585847362392/";

    private Button mBtnPlayYoutubeId,mBtnPlayYoutubeUrl,mBtnPlayYoutubeByWebView,mBtnPlayVimeoId,mBtnPlayVimeoUrl,mBtnPlayM3u8,mBtnPlayFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize () {
        mBtnPlayYoutubeId = (Button) findViewById(R.id.btn_play_youtube_id);
        mBtnPlayYoutubeUrl = (Button) findViewById(R.id.btn_play_youtube_url);
        mBtnPlayYoutubeByWebView = (Button) findViewById(R.id.btn_play_youtube_url_wb);
        mBtnPlayVimeoId = (Button) findViewById(R.id.btn_play_vimeo_id);
        mBtnPlayVimeoUrl = (Button) findViewById(R.id.btn_play_vimeo_url);
        mBtnPlayM3u8 = (Button) findViewById(R.id.btn_play_m3u8);
        mBtnPlayFacebook = (Button) findViewById(R.id.btn_play_facebook);
        mBtnPlayYoutubeId.setOnClickListener(this);
        mBtnPlayYoutubeUrl.setOnClickListener(this);
        mBtnPlayYoutubeByWebView.setOnClickListener(this);
        mBtnPlayVimeoId.setOnClickListener(this);
        mBtnPlayVimeoUrl.setOnClickListener(this);
        mBtnPlayM3u8.setOnClickListener(this);
        mBtnPlayFacebook.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, PlayActivity.class);
        switch (v.getId()) {
            case R.id.btn_play_youtube_id:
                intent.putExtra(Constant.PLAY_URL,YOUTUBE_ID);
                startActivity(intent);
                break;
            case R.id.btn_play_youtube_url:
                intent.putExtra(Constant.PLAY_URL,YOUTUBE_URL);
                startActivity(intent);
                break;
            case R.id.btn_play_youtube_url_wb:
                intent.putExtra(Constant.PLAY_URL,YOUTUBE_URL_WB);
                startActivity(intent);
                break;
            case R.id.btn_play_vimeo_id:
                intent.putExtra(Constant.PLAY_URL,VIMEO_ID);
                startActivity(intent);
                break;
            case R.id.btn_play_vimeo_url:
                intent.putExtra(Constant.PLAY_URL,VIMEO_URL);
                startActivity(intent);
                break;
            case R.id.btn_play_m3u8:
                intent.putExtra(Constant.PLAY_URL,M3U8_URL);
                startActivity(intent);
                break;
            case R.id.btn_play_facebook:
                intent.putExtra(Constant.PLAY_URL,FACEBOOK_URL);
                startActivity(intent);
                break;
        }
    }

}
