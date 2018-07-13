package com.victor.player.library.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.victor.player.library.module.PlayHelper;
import com.victor.player.library.util.AppUtil;
import com.victor.player.library.util.WindowUtil;


public class SeagullPlayeView extends FrameLayout {
    private String TAG = "SeagullPlayeView";
    protected FrameLayout playerContainer;
    protected ResizeSurfaceView mSurfaceView;
    protected ResizeTextureView mTextureView;
    private PlayHelper mPlayHelper;
    private SeagullPlayCtrl mSeagullPlayCtrl;
    private boolean isFullScreen;
    protected int mCurrentScreenScale = 0;
    public SeagullPlayeView(@NonNull Context context) {
        this(context,null);
    }

    public SeagullPlayeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SeagullPlayeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initPlayer();
    }

    /**
     * 初始化播放器视图
     */
    protected void initView() {
        playerContainer = new FrameLayout(getContext());
        playerContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(playerContainer, params);

        setVideoController(new SeagullPlayCtrl(getContext()));
    }

    private void initPlayer () {
        Log.e(TAG,"initPlayer()......");
        playerContainer.removeView(mSurfaceView);
        mSurfaceView = new ResizeSurfaceView(getContext());
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        playerContainer.addView(mSurfaceView, 0, params);
//        mPlayHelper = new PlayHelper(mSurfaceView,new Handler());
    }

    public void setVideoController(@Nullable SeagullPlayCtrl mediaPlayCtrl) {
        playerContainer.removeView(mSeagullPlayCtrl);
        mSeagullPlayCtrl = mediaPlayCtrl;
        if (mSeagullPlayCtrl != null) {
            mSeagullPlayCtrl.setPlayerView(this);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            playerContainer.addView(mSeagullPlayCtrl, params);
        }
    }

    public void release () {
        playerContainer.removeView(mTextureView);
        playerContainer.removeView(mSurfaceView);
        mPlayHelper.onDestroy();
    }

    public void toggleFullScreen () {
        Log.e(TAG,"toggleFullScreen()......");
        if (isFullScreen) {
            enterFullScreen();
        } else {
            exitFullScreen();
        }
    }

    public void enterFullScreen() {
        if (mSeagullPlayCtrl == null) return;
        Activity activity = AppUtil.scanForActivity(mSeagullPlayCtrl.getContext());
        if (activity == null) return;
        if (isFullScreen) return;
        WindowUtil.hideSystemBar(mSeagullPlayCtrl.getContext());
        this.removeView(playerContainer);
        ViewGroup contentView = activity
                .findViewById(android.R.id.content);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(playerContainer, params);
        isFullScreen = true;
    }

    public void play (String url) {
        if (mPlayHelper != null) {
            mPlayHelper.play(url);
        }
    }

    public void exitFullScreen() {
        if (mSeagullPlayCtrl == null) return;
        Activity activity = WindowUtil.scanForActivity(mSeagullPlayCtrl.getContext());
        if (activity == null) return;
        if (!isFullScreen) return;
        WindowUtil.showSystemBar(mSeagullPlayCtrl.getContext());
        ViewGroup contentView = activity
                .findViewById(android.R.id.content);
        contentView.removeView(playerContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(playerContainer, params);
        isFullScreen = false;
    }

    public void setScreenScale(int screenScale) {
        this.mCurrentScreenScale = screenScale;
        if (mSurfaceView != null) mSurfaceView.setScreenScale(screenScale);
        if (mTextureView != null) mTextureView.setScreenScale(screenScale);
    }

}
