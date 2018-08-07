package com.victor.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.victor.player.library.util.DensityUtil;
import com.victor.player.library.util.ScreenOrientationUtil;

public class HandleTouchHelper {
    private Activity mActivity;

    private boolean isPlayNext;//是否播放下一个节目
    private boolean isPlayPrev;//是否播放上一个节目
    private boolean isSourceNext;//是否切换下一个源
    private boolean isSourcePrev;//是否切换上一个源
    private boolean needChangeBright = false;
    private boolean needChangeVol = false;

    private float mDownX;
    private float mDownY;

    private AudioManager mAudioManager;
    private int mMaxVolume;
    public int mVolume = -1;
    public float mBrightness = -1f;


    public HandleTouchHelper(Activity activity) {
        mActivity = activity;
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public boolean handleTouchEvent (MotionEvent event) {
        int windowWidth = DensityUtil.getScreenWidth(mActivity);
        int windowHeight = DensityUtil.getScreenHeight(mActivity);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPlayNext = false;
                isPlayPrev = false;
                isSourceNext = false;
                isSourcePrev = false;
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                double distance = absDeltaX - absDeltaY;

                if (mDownX > windowWidth * 2.0 / 3 && distance <-100) {//如果是在离屏幕左侧 2/3（即 离屏幕右侧1/3）区域上下滑动则改变音量
                    onVolumeSlide((mDownY - y) / windowHeight);
                    needChangeVol = true;
                } else if (mDownX < windowWidth * 1.0 / 3 && distance <-100) {//如果是在离屏幕左侧 1/3（区域上下滑动则改变亮度
                    onBrightnessSlide((mDownY - y) / windowHeight);
                    needChangeBright = true;
                } else if (distance > 100) {//水平方向滑动
                    onSeekSlide(deltaX / windowWidth);
                    if (mDownX > x) {
                        isSourcePrev = true;
                        isSourceNext = false;
                        isPlayNext = false;
                        isPlayPrev = false;
                    } else {
                        isSourceNext = true;
                        isSourcePrev = false;
                        isPlayNext = false;
                        isPlayPrev = false;
                    }
                } else if (distance < -100){//垂直滑动
                    if (mDownY > y) {
                        isPlayNext = true;
                        isPlayPrev = false;
                        isSourceNext = false;
                        isSourcePrev = false;
                    } else {
                        isPlayPrev = true;
                        isPlayNext = false;
                        isSourceNext = false;
                        isSourcePrev = false;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (endGesture()) {
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean endGesture() {
        boolean clickEnable = false;
        mVolume = -1;
        mBrightness = -1f;
        if (isPlayNext) {
        } else if (isPlayPrev) {
        } else if (isSourceNext) {
            clickEnable = true;
        } else if (isSourcePrev) {
            clickEnable = true;
        } else if (needChangeVol) {
            clickEnable = true;
        } else if (needChangeBright) {
            clickEnable = true;
        }
        isPlayNext = false;
        isPlayPrev = false;
        isSourceNext = false;
        isSourcePrev = false;
        needChangeVol = false;
        needChangeBright = false;
        return clickEnable;
    }

    public int onVolumeSlide (float percent) {
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

    /**
     * 滑动快进快退
     * @param percent
     */
    private void onSeekSlide(float percent) {
        /*if (mPlayCtrl != null && mDuration > 0) {
            long current = mPlayCtrl.getCurrentPosition();
            long toPosition = (long) (current + mDuration * percent);
            int seekBarProgress = (int) (100 * mNewPosition / mDuration);
            mSbPlayProgress.setProgress(seekBarProgress);
            mNewPosition = (int) Math.max(0, Math.min(mDuration, toPosition));
            currentPosition = mNewPosition;
            mTvPassTime.setText(DateUtil.formatPlayTime(mNewPosition));

            mLayoutMediaCtrlBox.setVisibility(View.VISIBLE);
            mLayoutMediaVolBox.setVisibility(View.GONE);
            mLayoutMediaBrightBox.setVisibility(View.GONE);
            mLayoutMediaFastForwardBox.setVisibility(View.VISIBLE);
            mLayoutMediaError.setVisibility(View.GONE);

            mTvMediaFastForward.setText(mTvPassTime.getText() + "/" + mTvLongTime.getText());

            if (percent > 0) {
                mIvMediaFastForward.setImageResource(R.mipmap.ic_play_forward);
            } else {
                mIvMediaFastForward.setImageResource(R.mipmap.ic_play_backup);
            }

            if (!mPlayCtrl.isPlaying()) {
                mIvCenterPlay.setVisibility(View.GONE);
                mIvPlayPause.setImageResource(R.mipmap.ic_player_pause);
            }

        }*/
    }
}
