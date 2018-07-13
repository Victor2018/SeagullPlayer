package com.victor.player.library.module;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.victor.player.library.R;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.presenter.YoutubePresenterImpl;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.PlayUtil;
import com.victor.player.library.util.YoutubeParser;
import com.victor.player.library.youtube.AbstractYouTubePlayerListener;
import com.victor.player.library.youtube.PlayStatusListener;
import com.victor.player.library.youtube.YouTubePlayer;
import com.victor.player.library.youtube.YouTubePlayerInitListener;
import com.victor.player.library.youtube.YouTubePlayerView;
import com.victor.player.library.youtube.YoutubePlayController;
import com.victor.player.library.youtube.YoutubePlayEmptyController;

public class YoutubePlayHelper {
    private String TAG = "YoutubePlayHelper";
    private Context mContext;

    private YouTubePlayerView mYouTubePlayerView;
    private YoutubePlayController mYoutubePlayController;
    private YoutubePlayEmptyController mYoutubePlayEmptyController;
    private View youtubePlayerUI;
    private YouTubePlayer mYouTubePlayer;
    private PlayStatusListener mPlayStatusListener;
    private boolean isMediaCtrlViewVisible = true;
    private String videoId = "";
    private String playUrl = "";
    private String youtubeVideoName;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.Msg.PLAY_BY_MEDIA_PLAYER:
                    if (mYouTubePlayerView != null) {
                        mYouTubePlayerView.setVisibility(View.GONE);
                    }
//                    mPlayer.playUrl(playUrl,false);
                    break;
                case Constant.Msg.PLAY_BY_YOUTUBE_VIEW:
                    if (mYouTubePlayerView != null) {
                        mYouTubePlayerView.setVisibility(View.VISIBLE);
                    }
                    play(videoId);
                    break;
            }
        }
    };
    public YoutubePlayHelper(Context context, YouTubePlayerView youTubePlayerView) {
        mContext = context;
        mYouTubePlayerView = youTubePlayerView;
    }


    public void setPlayStatusListener (PlayStatusListener listener) {
        mPlayStatusListener = listener;
    }

    public void setMediaCtrlViewVisible (int visible) {
        isMediaCtrlViewVisible = visible == View.VISIBLE;
    }

    private void initYoutubePlayer () {
        if (mYouTubePlayerView == null) {
            Log.e(TAG,"youTubePlayerView == null");
            return;
        }
        if (mYouTubePlayer != null) {
            mYouTubePlayer.cueVideo(videoId, 0);
            mYouTubePlayer.play();
            if (mYoutubePlayController != null) {
                mYoutubePlayController.setVideoName(youtubeVideoName);
            }
            return;
        }
        if (isMediaCtrlViewVisible) {
            youtubePlayerUI = mYouTubePlayerView.inflateCustomPlayerUI(R.layout.youtube_media_ctrl);
        } else {
            youtubePlayerUI = mYouTubePlayerView.inflateCustomPlayerUI(R.layout.media_ctrl_empty);
        }
        mYouTubePlayerView.initialize(new YouTubePlayerInitListener() {
            @Override
            public void onInitSuccess(final YouTubePlayer youTubePlayer) {
                Log.e(TAG,"initYoutubePlayer()......onInitSuccess()-mYouTubePlayer = " + youTubePlayer);
                if (isMediaCtrlViewVisible) {
                    mYoutubePlayController = new YoutubePlayController(mContext, youtubePlayerUI, youTubePlayer);
                    mYoutubePlayController.setPlayStatusListener(mPlayStatusListener);
                    youTubePlayer.addListener(mYoutubePlayController);
                    mYouTubePlayerView.addFullScreenListener(mYoutubePlayController);
                } else {
                    mYoutubePlayEmptyController = new YoutubePlayEmptyController();
                    mYoutubePlayEmptyController.setPlayStatusListener(mPlayStatusListener);
                    youTubePlayer.addListener(mYoutubePlayEmptyController);
                }
                youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        Log.e(TAG,"youtube player init onReady() start play videoId = " + videoId);
                        mYouTubePlayer = youTubePlayer;
                        mYouTubePlayer.cueVideo(videoId, 0);
                        mYouTubePlayer.play();
                        if (mYoutubePlayController != null) {
                            mYoutubePlayController.setVideoName(youtubeVideoName);
                        }
                    }
                });

            }
        },true);
    }

    public void seekTo (int msec) {
        if (mYouTubePlayer != null) {
            mYouTubePlayer.seekTo(msec);
        }
    }

    public void setPlayViewVisible (int visible) {
        if (mYouTubePlayerView != null) {
            mYouTubePlayerView.setVisibility(visible);
        }
    }

    public void play (String url) {
        Log.e(TAG,"play()-url = " + url);
        videoId = PlayUtil.getVideoId(url);
        initYoutubePlayer();
    }

    public boolean isPlaying() {
        if (mYoutubePlayController != null) {
            return mYoutubePlayController.isPlaying();
        }
        return false;
    }

    public void pause () {
        if (mYouTubePlayer != null) {
            mYouTubePlayer.pause();
        }
    }

    public int getCurrentPosition() {
        if (mYoutubePlayController != null) {
            return mYoutubePlayController.getCurrentPosition();
        }
        return 0;
    }

    public int getBufferPercentage() {
        return 0;
    }

    public int getDuration() {
        if (mYoutubePlayController != null) {
            return mYoutubePlayController.getDuration();
        }
        return 0;
    }

    public void setYoutubeVideoName (String videoName) {
        youtubeVideoName = videoName;
    }

    public void resume () {
        if (mYouTubePlayer != null) {
            mYouTubePlayer.play();
        }
    }

    public void onKeyDown(int keyCode, KeyEvent event) {
        if (mYoutubePlayController != null) {
            mYoutubePlayController.onKeyDown(keyCode,event);
        }
    }

    public void onKeyUp(int keyCode, KeyEvent event) {
        if (mYoutubePlayController != null) {
            mYoutubePlayController.onKeyUp(keyCode,event);
        }
    }

    public void onDestroy () {
        Log.e(TAG,"onDestroy()......");
        if (mYouTubePlayerView != null) {
            mYouTubePlayerView.release();
            mYouTubePlayerView = null;
        }
        if (mYouTubePlayer != null) {
            mYouTubePlayer = null;
        }
    }
}
