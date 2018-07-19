package com.victor.player;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.victor.player.library.module.Player;
import com.victor.player.library.module.PlayHelper;
import com.victor.player.library.util.Constant;
import com.victor.player.library.youtube.PlayStatusListener;
import com.victor.player.library.ytparser.VideoMeta;
import com.victor.player.library.ytparser.YouTubeExtractor;
import com.victor.player.library.ytparser.YtFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PlayActivity extends AppCompatActivity implements PlayStatusListener{
    private String TAG = "PlayActivity";
    private static final String YOUTUBE_ID = "SMcXGeltEQQ";
    private String playUrl = YOUTUBE_ID;
//    private FrameLayout mFlPlayContainer;
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
                    break;
                case Constant.Msg.PLAY_BY_YOUTUBE_VIEW:
                    mPbLoading.setVisibility(View.GONE);
//                    mPlayHelper.playByYoutubeView();
                    break;
                case Constant.Msg.PLAY_VIDEO:
                    mPlayHelper.play(playUrl);
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
//        mFlPlayContainer = findViewById(R.id.fl_player_container);
        mSvPlay = findViewById(R.id.sv_play);
        mPbLoading = findViewById(R.id.pb_loading);

//        mPlayHelper = new PlayHelper(this,mFlPlayContainer,mHandler);
//        mPlayHelper.play(playUrl);
//        mPlayHelper.setPlayStatusListener(this);
//        mPlayHelper.setYoutubeVideoName("王牌对王牌");
        mPlayHelper = new PlayHelper(this,mSvPlay,mHandler);
        mPlayHelper.play(playUrl);
    }

    private void initData () {
        playUrl = getIntent().getStringExtra(Constant.PLAY_URL);
        Log.e(TAG,"initData-playUrl------>" + playUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayHelper != null) {
            mPlayHelper.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayHelper != null && mPlayHelper.getPlayer() != null) {
            mPlayHelper.resume();
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

    @Override
    public void onPlayStatus(int status, int duration) {
        switch (status) {
            case Player.PLAYER_PREPARING:
                Log.e(TAG,"onPlayStatus-Player.PLAYER_PREPARING.............");
                break;
            case Player.PLAYER_BUFFERING_START:
                Log.e(TAG,"onPlayStatus-Player.PLAYER_BUFFERING_START.............");
                break;
            case Player.PLAYER_PREPARED:
                Log.e(TAG,"onPlayStatus-Player.PLAYER_PREPARED.............duration = " + duration);
                break;
            case Player.PLAYER_PROGRESS_INFO:
                Log.e(TAG,"onPlayStatus-Player.PLAYER_PROGRESS_INFO.............duration = " + duration);
                break;
            case Player.PLAYER_PAUSE:
                Log.e(TAG,"onPlayStatus-Player.PLAYER_PAUSE.............");
                break;
            case Player.PLAYER_COMPLETE:
                Log.e(TAG,"onPlayStatus-Player.PLAYER_PAUSE.............");
                break;
            case Player.PLAYER_ERROR:
                Log.e(TAG,"onPlayStatus-Player.PLAYER_ERROR.............");
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPlayHelper != null) {
//            mPlayHelper.onKeyDown(keyCode,event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mPlayHelper != null) {
//            mPlayHelper.onKeyUp(keyCode,event);
        }
        return super.onKeyUp(keyCode, event);
    }
}
