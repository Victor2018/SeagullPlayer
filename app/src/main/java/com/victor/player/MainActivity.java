package com.victor.player;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.victor.player.library.module.Player;
import com.victor.player.library.module.PlayHelper;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private static final String YOUTUBE_ID = "SMcXGeltEQQ";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=vqcxM7dSJtw";
    private static final String VIMEO_ID   = "204150149";
    private static final String VIMEO_URL = "https://vimeo.com/channels/staffpicks/262705319";
    private static final String M3U8_URL = "http://ivi.bupt.edu.cn/hls/cctv3.m3u8";
    private SurfaceView mSvPlay;
    private ProgressBar mPbLoading;
    private PlayHelper mPlayHelper;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Player.PLAYER_PREPARING:
                    mPbLoading.setVisibility(View.VISIBLE);
                    break;
                case Player.PLAYER_BUFFERING_START:
                    mPbLoading.setVisibility(View.VISIBLE);
                    break;
                case Player.PLAYER_BUFFERING_END:
                    mPbLoading.setVisibility(View.GONE);
                    break;
                case Player.PLAYER_PREPARED:
                    mPbLoading.setVisibility(View.GONE);
                    break;
                case Player.PLAYER_PROGRESS_INFO:
                    break;
                case Player.PLAYER_COMPLETE:
                    mPlayHelper.play(VIMEO_URL);
                    break;
                case Player.PLAYER_SEEK_END:
                    break;
                case Player.PLAYER_PLAYING:
                    break;
                case Player.PLAYER_PAUSE:
                    break;
                case Player.PLAYER_ERROR:
                    mPbLoading.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "播放失败！", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Player.PLAYER_ERROR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialze();
    }

    private void initialze () {
        mSvPlay = findViewById(R.id.sv_play);
        mPbLoading = findViewById(R.id.pb_loading);
        mPlayHelper = new PlayHelper(mSvPlay,mHandler);
        mPlayHelper.play(YOUTUBE_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayHelper != null && mPlayHelper.getPlayer() != null) {
            mPlayHelper.getPlayer().pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayHelper != null && mPlayHelper.getPlayer() != null) {
            mPlayHelper.getPlayer().resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayHelper != null) {
            mPlayHelper.onDestroy();
            mPlayHelper = null;
        }
    }
}
