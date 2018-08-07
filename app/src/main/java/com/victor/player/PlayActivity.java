package com.victor.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.victor.player.library.module.Player;
import com.victor.player.library.module.PlayHelper;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.Loger;
import com.victor.player.widget.VideoPlayCtrlView;

public class PlayActivity extends AppCompatActivity {
    private String TAG = "PlayActivity";
    private static final String YOUTUBE_ID = "SMcXGeltEQQ";
    private String playUrl = YOUTUBE_ID;
    private SurfaceView mSvPlay;
    private VideoPlayCtrlView mVideoPlayCtrlView;
    private HomeWatcherReceiver mHomeWatcherReceiver = null;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.Msg.PAUSE_PLAYER:
                    if (mVideoPlayCtrlView != null) {
                        mVideoPlayCtrlView.pause();
                    }
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
        registerReceiver();
        mSvPlay = findViewById(R.id.sv_play);
        mVideoPlayCtrlView = findViewById(R.id.play_ctrl_view);

        mVideoPlayCtrlView.init(this,mSvPlay);
        mVideoPlayCtrlView.setLandscape(true);
        mVideoPlayCtrlView.play(playUrl);
//        mPlayHelper = new PlayHelper(this,mSvPlay,mHandler);
//        mPlayHelper.play(playUrl);
    }

    private void initData () {
        playUrl = getIntent().getStringExtra(Constant.PLAY_URL);
        Log.e(TAG,"initData-playUrl------>" + playUrl);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVideoPlayCtrlView != null) {
            mVideoPlayCtrlView.onStart(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoPlayCtrlView != null) {
            mVideoPlayCtrlView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoPlayCtrlView != null) {
            mVideoPlayCtrlView.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoPlayCtrlView != null) {
            mVideoPlayCtrlView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        if (mVideoPlayCtrlView != null) {
            mVideoPlayCtrlView.onDestroy();
            mVideoPlayCtrlView = null;
        }
        unRegisterReceiver();
        super.onDestroy();
    }

    private void registerReceiver() {
        mHomeWatcherReceiver = new HomeWatcherReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeWatcherReceiver, filter);
    }
    private void unRegisterReceiver() {
        if (mHomeWatcherReceiver != null) {
            try {
                unregisterReceiver(mHomeWatcherReceiver);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {

            String intentAction = intent.getAction();
            Loger.e(TAG, "intentAction =" + intentAction);
            if (TextUtils.equals(intentAction, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Loger.e(TAG,  "reason =" + reason);
                if (TextUtils.equals(SYSTEM_DIALOG_REASON_HOME_KEY, reason)) {
                    mHandler.sendEmptyMessage(Constant.Msg.PAUSE_PLAYER);
                }
            }
        }

    }
}
