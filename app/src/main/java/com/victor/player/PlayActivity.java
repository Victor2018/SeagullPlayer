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
import com.victor.player.library.util.Constant;

public class PlayActivity extends AppCompatActivity {
    private String TAG = "PlayActivity";
    private static final String YOUTUBE_ID = "SMcXGeltEQQ";
    private String playUrl = YOUTUBE_ID;
    private SurfaceView mSvPlay;
    private ProgressBar mPbLoading;
    private PlayHelper mPlayHelper;
//    private FacebookHelper mFacebookHelper;

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
        setContentView(R.layout.activity_play);
        initData();
        initialze();
    }

    private void initialze () {
        mSvPlay = findViewById(R.id.sv_play);
        mPbLoading = findViewById(R.id.pb_loading);
        mPlayHelper = new PlayHelper(mSvPlay,mHandler);
        mPlayHelper.play(playUrl);

    }

    private void initData () {
        playUrl = getIntent().getStringExtra(Constant.PLAY_URL);
        Log.e(TAG,"initData-playUrl------>" + playUrl);
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
