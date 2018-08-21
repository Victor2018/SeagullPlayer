package com.victor.player.library.module;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.victor.player.library.data.SubTitleInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author Victor
 * @Date Create on 2018/1/18 15:32.
 * @Describe
 */

public class Player implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener,
        SurfaceHolder.Callback, MediaPlayer.OnVideoSizeChangedListener{

    public static final int PLAYER_PREPARING =				0xf100;
    public static final int PLAYER_PREPARED =				0xf101;
    public static final int PLAYER_BUFFERING_START = 		0xf102;
    public static final int PLAYER_BUFFERING_END = 		0xf103;
    public static final int PLAYER_ERROR = 					0xf104;
    public static final int PLAYER_SEEK_END =				0xf105;
    public static final int PLAYER_PROGRESS_INFO = 		0xf106;
    public static final int PLAYER_COMPLETE = 				0xf107;
    public static final int PLAYER_CAN_NOT_SEEK = 			0xf108;
    public static final int HIDE_PLAY_CTRL_VIEW = 			0xf109;
    public static final int PLAYER_PLAYING    = 			0xf110;
    public static final int PLAYER_PAUSE       = 			0xf111;

    private static final String TAG = "Player";
    private int videoWidth;
    private int videoHeight;
    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private SurfaceView mSurfaceView; // 绘制View
    private SurfaceHolder mSurfaceHolder = null; // 显示一个surface的抽象接口，使你可以控制surface的大小和格式
    private Handler mNotifyHandler;

    private String mPlayUrl = null;
    private boolean mIsLive;
    private int replayCount;//重播次数

    Timer mTimer = new Timer();
    TimerTask mTimerTask = new PlayTimerTask();
    private boolean isTimerRun;

    private int mBufferPercentage;
    private HashMap<Integer,SubTitleInfo> subTitleInfos;

    private Player(Handler handler) {
        mNotifyHandler = handler;
    }
    public Player(TextureView textureView, Handler handler) {
        this(handler);
        mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(this);
        createMediaPlayer();
    }
    public Player(SurfaceView surfaceView, Handler handler) {
        this(handler);
        if (surfaceView != null) {
            open(surfaceView);
        }
        createMediaPlayer();
    }

    public void open(SurfaceView surfaceView) {
        Log.e(TAG, "open()......");
        mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // 视频缓冲完就开始播放，不使用SurfaceView的缓冲区
    }

    public void close() {
        Log.d(TAG, "close()......");
        if (mNotifyHandler != null) {
            mNotifyHandler.removeCallbacks(mRunable);
        }
        releaseMediaPlayer();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//        Log.e(TAG, "onBufferingUpdate()......mBufferPercentage = " + percent);
        mBufferPercentage = percent;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mIsLive) {
            replay();
            return;
        }
        if (mNotifyHandler != null) {
            mNotifyHandler.sendEmptyMessage(PLAYER_COMPLETE);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError()......");
        replay();
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onInfo()......");

        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                // replay every 10s
                startTimer();
                Log.e(TAG, "onInfo-buffer start......");
                if (mNotifyHandler != null) {
                    // here we delay send message to resume play.
                    mNotifyHandler.removeMessages(PLAYER_BUFFERING_START);
                    mNotifyHandler.sendEmptyMessage(PLAYER_BUFFERING_START);
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                stopTimer();
                Log.e(TAG, "onInfo-buffer end......");

                if (mNotifyHandler != null) {
                    // here we delay send message to resume play.
                    mNotifyHandler.removeMessages(PLAYER_BUFFERING_END);
                    mNotifyHandler.sendEmptyMessage(PLAYER_BUFFERING_END);
                }

                break;
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                if (mNotifyHandler != null) {
                    mNotifyHandler.sendEmptyMessage(PLAYER_CAN_NOT_SEEK);
                }
                break;
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                Log.e(TAG, "UNKNOWN...");
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e(TAG, "onPrepared()......");
        stopTimer();
        videoWidth = mMediaPlayer.getVideoWidth();
        videoHeight = mMediaPlayer.getVideoHeight();

        Log.e(TAG, "onPrepared()......videoWidth = " + videoWidth);
        Log.e(TAG, "onPrepared()......videoHeight = " + videoHeight);

        if (videoHeight != 0 && videoWidth != 0) {
            if (mNotifyHandler != null) {
                mNotifyHandler.sendEmptyMessage(PLAYER_PREPARED);
                mNotifyHandler.sendEmptyMessage(PLAYER_PREPARED);
            }
            mp.start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.e(TAG, "onSeekComplete()......");
        if (mNotifyHandler != null) {
            mNotifyHandler.sendEmptyMessage(PLAYER_SEEK_END);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
//        createMediaPlayer();// 如果surfaceview被添加到其他view中会导致正在播放的影片重新播放

        // //////////////////
        // because maybe surface view is not ready when first play,
        // but application already call playUrl,
        // then we need to play again.
        // /////////////////
//        openVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(mSurfaceHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        releaseMediaPlayer();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surfaceTexture;
            createMediaPlayer();
            openVideo();
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void createMediaPlayer() {
        Log.e(TAG, "createMediaPlayer()......");
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.reset();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnCompletionListener(this);

            if (mSurfaceTexture != null) {
                mSurface = new Surface(mSurfaceTexture);
                mMediaPlayer.setSurface(mSurface);
            }

            if (mSurfaceHolder != null) {
                mMediaPlayer.setDisplay(mSurfaceHolder); // 设置画面输出
            }

            mMediaPlayer.setScreenOnWhilePlaying(true); // 保持屏幕高亮

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void openVideo() {
        Log.e(TAG, "openVideo()......");
        if (mNotifyHandler != null) {
            mNotifyHandler.removeMessages(PLAYER_BUFFERING_END);
        }
        if (TextUtils.isEmpty(mPlayUrl)) {
            Log.d(TAG, "mPlayUrl or is empty");
            return;
        }
        if (mSurface == null && mSurfaceHolder == null) {
            Log.d(TAG, "mSurface or mSurfaceHolder is null");
            return;
        }

        synchronized (this) {
            try {
                if (mNotifyHandler != null) {
                    mNotifyHandler.sendEmptyMessage(PLAYER_PREPARING);
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(mPlayUrl);
                    mMediaPlayer.prepareAsync();
                    startNotify();
                } else {
                    Log.e(TAG,"mMediaPlayer == null");
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void startNotify() {
        Log.e(TAG, "startNotify()......");
        if (mNotifyHandler != null) {
            mNotifyHandler.post(mRunable);
        }
    }

    Runnable mRunable = new Runnable() {

        public void run() {
            Message msg = new Message();
            msg.obj = showSubTitle();
            msg.what = PLAYER_PROGRESS_INFO;
            if (mNotifyHandler != null) {
                mNotifyHandler.sendMessage(msg);
                mNotifyHandler.postDelayed(mRunable, 500);
            }

        }
    };

    public int getBufferPercentage () {
        return mBufferPercentage;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public boolean isPlaying () {
        return mMediaPlayer == null ? false : mMediaPlayer.isPlaying();
    }

    public void playUrl(String videoUrl,boolean isLive) {
        Log.e(TAG, "playUrl()......" + videoUrl);
        mPlayUrl = videoUrl;
        mIsLive = isLive;
        openVideo();
    }

    public void pause() {
        Log.e(TAG, "pause()......");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                if (mNotifyHandler != null) {
                    mNotifyHandler.sendEmptyMessage(PLAYER_PAUSE);
                }
            }
        }
    }

    public void resume() {
        Log.e(TAG, "resume()......");
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
                if (mNotifyHandler != null) {
                    mNotifyHandler.sendEmptyMessage(PLAYER_PLAYING);
                }
            }
        }
    }

    public void replay() {
        replayCount++;
        stopTimer();
        Log.e(TAG, "replay()......replayCount = " + replayCount);
        if (replayCount >= 3) {
            replayCount = 0;
            if (!mIsLive) {
                stop();
                if (mNotifyHandler != null) {
                    mNotifyHandler.removeMessages(PLAYER_ERROR);
                    mNotifyHandler.sendEmptyMessage(PLAYER_ERROR);
                }
                return;
            }
        }
        if (!TextUtils.isEmpty(mPlayUrl)) {
            playUrl(mPlayUrl,mIsLive);
        }
    }

    public void stop() {
        Log.e(TAG, "stop()......");
        if (mMediaPlayer != null) {
            mPlayUrl = null;
            mMediaPlayer.stop();
        }
    }

    public void seekTo(int msec) {
        Log.e(TAG, "seekTo()......");
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
            if (mNotifyHandler != null) {
                mNotifyHandler.sendEmptyMessage(HIDE_PLAY_CTRL_VIEW);
            }
        }
    }

    public int getCurrentPosition() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer == null ? 0: mMediaPlayer.getDuration();
    }

    private void releaseMediaPlayer() {
        Log.e(TAG, "releaseMediaPlayer()......");
        synchronized (this) {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop(); // mMediaPlayer.reset();
                mMediaPlayer.release();
            }
            mMediaPlayer = null;
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    public String getLastPlayUrl () {
        return mPlayUrl;
    }

    class PlayTimerTask extends TimerTask {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            replay();
        }

    }

    private void startTimer() {
        Log.e(TAG, "startTimer()......");
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new PlayTimerTask();
        }

        if (mTimer != null && mTimerTask != null && isTimerRun == false) {
            mTimer.schedule(mTimerTask, 10000, 10000);
            isTimerRun = true;
        }

    }

    private void stopTimer() {
        Log.e(TAG, "stopTimer()......");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

        isTimerRun = false;
    }

    public void setSubTitle (HashMap<Integer,SubTitleInfo> datas) {
        subTitleInfos = datas;
    }

    public String showSubTitle () {
        String subTitle = "";
        if (subTitleInfos == null) {
            return subTitle;
        }
        if (subTitleInfos.size() == 0) {
            return subTitle;
        }
        int currentPosition = getCurrentPosition();
        Iterator<Integer> keys = subTitleInfos.keySet().iterator();
        while (keys.hasNext()) {
            Integer key = keys.next();
            SubTitleInfo bean = subTitleInfos.get(key);
            if (currentPosition > bean.beginTime && currentPosition < bean.endTime) {
                subTitle = bean.srtBody;
                break;
            }
        }
        return subTitle;
    }
}
