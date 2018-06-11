package com.victor.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.victor.player.library.module.PlayHelper;
import com.victor.player.library.module.Player;
import com.victor.player.library.widget.SeagullPlayeView;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "MainActivity2";
    private static final String YOUTUBE_ID = "SMcXGeltEQQ";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=vqcxM7dSJtw";
    private static final String VIMEO_ID   = "204150149";
    private static final String VIMEO_URL = "https://vimeo.com/channels/staffpicks/262705319";
    private static final String M3U8_URL = "http://ivi.bupt.edu.cn/hls/cctv3.m3u8";
    private SeagullPlayeView mSvPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initialze();
    }

    private void initialze () {
        mSvPlay = findViewById(R.id.spv_play);
        mSvPlay.play(YOUTUBE_ID);
        mSvPlay.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSvPlay.release();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.spv_play:
                mSvPlay.toggleFullScreen();
                break;
        }
    }
}
