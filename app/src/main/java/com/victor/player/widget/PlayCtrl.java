package com.victor.player.widget;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.victor.player.R;
import com.victor.player.library.module.PlayHelper;
import com.victor.player.library.util.Loger;
import com.victor.player.library.util.ScreenOrientationUtil;

/**
 * @Author Victor
 * @Date Create on 2018/2/1 15:17.
 * @Describe
 */

public class PlayCtrl {
    private String TAG = "PlayCtrl";
    private Activity mActivity;

    private Animation mAnimShowCtrlLeft,mAnimHideCtrlLeft;
    private Animation mAnimShowCtrlTop,mAnimHideCtrlTop;
    private Animation mAnimShowCtrlBottom,mAnimHideCtrlBottom;
    private Animation mAnimShowCtrlRight,mAnimHideCtrlRight;

    private ScreenOrientationUtil screenOrientationUtil;
    private AudioManager mAudioManager;
    private int mMaxVolume;
    public int mVolume = -1;
    public float mBrightness = -1f;
    private PlayHelper mPlayHelper;
    private String lastPlayContentId;

    public PlayCtrl(Activity activity, SurfaceView surfaceView, Handler handler) {
        mActivity = activity;
        init(surfaceView,handler);
    }
    public PlayCtrl(Activity activity, TextureView textureView, Handler handler) {
        mActivity = activity;
        init(textureView,handler);
    }

    private void init (TextureView textureView, Handler handler) {
        screenOrientationUtil = ScreenOrientationUtil.getInstance();
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mPlayHelper = new PlayHelper(mActivity,textureView,handler);
        initAnim();
    }
    private void init (SurfaceView surfaceView, Handler handler) {
        screenOrientationUtil = ScreenOrientationUtil.getInstance();
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mPlayHelper = new PlayHelper(mActivity,surfaceView,handler);
        initAnim();
    }

    private void initAnim () {
        //显示View动画
        mAnimShowCtrlLeft = AnimationUtils.loadAnimation(mActivity, R.anim.anim_left_enter);
        mAnimShowCtrlLeft.setFillAfter(true);
        //隐藏View动画
        mAnimHideCtrlLeft = AnimationUtils.loadAnimation(mActivity, R.anim.anim_left_exit);
        mAnimHideCtrlLeft.setFillAfter(true);

        //显示View动画
        mAnimShowCtrlTop = AnimationUtils.loadAnimation(mActivity, R.anim.anim_top_enter);
        mAnimShowCtrlTop.setFillAfter(true);
        //隐藏View动画
        mAnimHideCtrlTop = AnimationUtils.loadAnimation(mActivity, R.anim.anim_top_exit);
        mAnimHideCtrlTop.setFillAfter(true);

        //显示View动画
        mAnimShowCtrlBottom = AnimationUtils.loadAnimation(mActivity, R.anim.anim_bottom_enter);
        mAnimShowCtrlBottom.setFillAfter(true);
        //隐藏View动画
        mAnimHideCtrlBottom = AnimationUtils.loadAnimation(mActivity, R.anim.anim_bottom_exit);
        mAnimHideCtrlBottom.setFillAfter(true);

        //显示View动画
        mAnimShowCtrlRight = AnimationUtils.loadAnimation(mActivity, R.anim.anim_right_enter);
        mAnimShowCtrlRight.setFillAfter(true);
        //隐藏View动画
        mAnimHideCtrlRight = AnimationUtils.loadAnimation(mActivity, R.anim.anim_right_exit);
        mAnimHideCtrlRight.setFillAfter(true);
    }

    public void showLeftCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.startAnimation(mAnimShowCtrlLeft);
    }

    public void hideLeftCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.GONE) return;
        view.startAnimation(mAnimHideCtrlLeft);
        view.setVisibility(View.GONE);
    }
    public void showTopCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.startAnimation(mAnimShowCtrlTop);
    }

    public void hideTopCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.GONE) return;
        view.startAnimation(mAnimHideCtrlTop);
        view.setVisibility(View.GONE);
    }

    public void showBottomCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.startAnimation(mAnimShowCtrlBottom);
    }
    public void hideBottomCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.GONE) return;
        view.startAnimation(mAnimHideCtrlBottom);
        view.setVisibility(View.GONE);
    }
    public void showRightCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.startAnimation(mAnimShowCtrlRight);
    }
    public void hideRightCtrlView (View view) {
        if (view == null) return;
        if (view.getVisibility() == View.GONE) return;
        view.startAnimation(mAnimHideCtrlRight);
        view.setVisibility(View.GONE);
    }

    public void showMediaCtrlView () {
        if (mPlayHelper != null) {
//            mPlayHelper.showPlayCtrlView();
        }
    }
    public void hideMediaCtrlView () {
        if (mPlayHelper != null) {
//            mPlayHelper.hidePlayCtrlView();
        }
    }

    public int onVolSlide (float percent) {
        if (mAudioManager == null) return 0;
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0) {
                mVolume = 0;
            }
        }
        int progress = (int) (percent * mMaxVolume) + mVolume;
        if (progress > mMaxVolume) {
            progress = mMaxVolume;
        } else if (progress < 0) {
            progress = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        return progress * 100 / mMaxVolume;
    }

    public int onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            }
            if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = mActivity.getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        mActivity.getWindow().setAttributes(lpa);

        return (int) (lpa.screenBrightness * 100);
    }

    public boolean isPortrait() {
        return screenOrientationUtil == null ? false : screenOrientationUtil.isPortrait();
    }

    public void toggleScreen () {
        if (screenOrientationUtil != null) {
            screenOrientationUtil.toggleScreen();
        }
    }

    public void setLandscape (boolean isLandscape) {
        if (screenOrientationUtil != null) {
            screenOrientationUtil.setLandscape(isLandscape);
        }
    }

    public void onSart (Activity activity) {
        screenOrientationUtil.start(mActivity);
    }

    public void onStop () {
        screenOrientationUtil.stop();
    }

    public void onDestroy () {
        screenOrientationUtil = null;
        mAudioManager = null;
        if (mPlayHelper != null) {
            mPlayHelper.onDestroy();
        }
    }

    public void play (String playUrl) {
        if (mPlayHelper == null || mPlayHelper.getPlayer() == null) {
            Loger.e(TAG,"mPlayer == null please init player first");
            return;
        }
        if (TextUtils.isEmpty(playUrl)) {
            Loger.e(TAG,"playUrl is empty");
            return;
        }
        mPlayHelper.play(playUrl);
    }

   /* public void playByYoutubeView () {
        if (mPlayHelper != null) {
            Log.e(TAG,"playByYoutubeView()......");
            mPlayHelper.playByYoutubeView();
        }
    }*/

    public int getCurrentPosition () {
        if (mPlayHelper != null && mPlayHelper.getPlayer() != null) {
            return mPlayHelper.getPlayer().getCurrentPosition();
        }
        return 0;
    }

    public int getBufferPercentage () {
        if (mPlayHelper != null) {
            return mPlayHelper.getBufferPercentage();
        }
        return 0;
    }

    public int getDuration () {
        if (mPlayHelper != null) {
            return mPlayHelper.getDuration();
        }
        return 0;
    }

    public void rePlay () {
        if (mPlayHelper != null) {
            mPlayHelper.replay();
        }
    }
    public boolean isPlaying () {
        if (mPlayHelper != null) {
            return mPlayHelper.isPlaying();
        }
        return false;
    }

    public void pause () {
        if (mPlayHelper != null) {
            mPlayHelper.pause();
        }
    }

    public void resume () {
        if (mPlayHelper != null) {
            mPlayHelper.resume();
        }
    }


    public void seekTo (int position) {
        if (mPlayHelper != null && position > 0) {
            mPlayHelper.seekTo(position);
        }
    }

    public void lockScreen (boolean isLock) {
        if (isLock) {
            screenOrientationUtil.stop();
        } else {
            screenOrientationUtil.start(mActivity);
        }
    }

    public String getCurrentPlayUrl () {
        if (mPlayHelper != null && mPlayHelper.getPlayer() != null) {
            return mPlayHelper.getPlayer().getLastPlayUrl();
        }
        return null;
    }

    public void setLastPlayContentId (String contentId) {
        lastPlayContentId = contentId;
    }

    public String getLastPlayContentId () {
        return lastPlayContentId;
    }

}
