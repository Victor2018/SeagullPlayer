package com.victor.player.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.victor.http.util.MainHandler;
import com.victor.player.R;
import com.victor.player.library.data.FacebookReq;
import com.victor.player.library.data.VimeoReq;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.interfaces.OnExtractListener;
import com.victor.player.library.module.Player;
import com.victor.player.library.util.DateUtil;
import com.victor.player.library.util.DensityUtil;
import com.victor.player.library.util.Loger;
import com.victor.player.util.Constant;


/**
 * @Author Victor
 * @Date Create on 2018/2/1 15:04.
 * @Describe
 */

public class VideoPlayCtrlView extends RelativeLayout implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener, View.OnTouchListener,OnExtractListener {
    private String TAG = "PlayCtrlView";
    private TextView mTvPassTime,mTvLongTime,mTvMediaFastForward;
    private SeekBar mSbPlayProgress;
    private MovingTextView mTvPlayName;
    private ProgressBar mPbMediaVolume,mPbMediaBright;
    private ProgressBar mPbLoading;
    private TextView mTvSubTitle;
    private LinearLayout mLayoutCtrlTop,mLayoutCtrlBottom,mLayoutMediaVolBox,
            mLayoutMediaBrightBox,mLayoutMediaFastForwardBox;
    private ImageView mIvBack,mIconMediaVol,mIvMediaFastForward,mIvPlayPause,mIvCenterPlay;
    private RelativeLayout mRlCtrl;

    private float mDownX;
    private float mDownY;

    private int mDuration = 0;//影片时长
    private boolean isDefinition = true;
    private int mNewPosition;

    private boolean isPlayNext;//是否播放下一个节目
    private boolean isPlayPrev;//是否播放上一个节目
    private boolean isSourceNext;//是否切换下一个源
    private boolean isSourcePrev;//是否切换上一个源
    private boolean isSeeking = false;//是否正在拖动进度
    private boolean needChangeBright = false;
    private boolean needChangeVol = false;
    private int currentPosition;
    private int youtubeCurrentPosition;
    private int currentPlaySortIndex;
    private int lastPlaySortIndex;

    private Context mContext;
    private AppCompatActivity mActivity;
    private PlayCtrl mPlayCtrl;

    private boolean isAirPlay;

    private boolean pressHomePlaying;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (mActivity == null) return;
            switch (msg.what) {
                case Player.PLAYER_PREPARING:
                    mPbLoading.setVisibility(View.VISIBLE);
                    break;
                case Player.PLAYER_PREPARED:
                    hideTopBottomCtrlView();
                    mPbLoading.setVisibility(View.GONE);
                    if (mPlayCtrl != null) {
                        mDuration = mPlayCtrl.getDuration();
                        if (mDuration > 0) {
                            String longTime = DateUtil.formatPlayTime(mDuration);
                            mTvLongTime.setText(longTime);
                        }
                        //当前播放进度剩余时间必须大于10s 才seek
                        if (currentPosition > 0 && currentPosition < mDuration - 10 * 1000) {
                            mPlayCtrl.seekTo(currentPosition);
                        }
                    }
                    lastPlaySortIndex = currentPlaySortIndex;
                    break;
                case Player.PLAYER_ERROR:
                    if (mPlayCtrl != null) {
                       if (!mPlayCtrl.isPlaying()) {
                           mLayoutMediaVolBox.setVisibility(View.GONE);
                           mLayoutMediaBrightBox.setVisibility(View.GONE);
                           mLayoutMediaFastForwardBox.setVisibility(View.GONE);
                       }
                    }
                    break;
                case Player.PLAYER_BUFFERING_START:
                    mPbLoading.setVisibility(View.VISIBLE);
                    break;
                case Player.PLAYER_BUFFERING_END:
                    hideTopBottomCtrlView();
                    mPbLoading.setVisibility(View.GONE);
                    break;
                case Player.PLAYER_PROGRESS_INFO:
                    if (mPlayCtrl != null) {
                        int elapseMsec = mPlayCtrl.getCurrentPosition();
                        updateProgress(elapseMsec);
                    }
                    mTvSubTitle.setText(msg.obj.toString());
                    break;
                case Player.PLAYER_COMPLETE:
                    break;
                case Constant.Msg.HIDE_PLAY_TOP_BOTTOM_CTRL_VIEW:
                    hideTopBottomCtrlView();
                    break;
                case Constant.Msg.HIDE_PLAY_RIGHT_CTRL_VIEW:
                    break;
                case Constant.Msg.HIDE_AIRPLAY_VIEW:
                    break;
                case Constant.Msg.PREPARE_PLAY:
                    break;
            }
        }
    };

    public VideoPlayCtrlView(Context context) {
        this(context,null);
    }

    public VideoPlayCtrlView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoPlayCtrlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initialize();
    }
    public void init (AppCompatActivity activity, SurfaceView surfaceView) {
        mActivity = activity;
        mPlayCtrl = new PlayCtrl(mActivity,surfaceView,mHandler);
    }

    private void initialize () {
        initView();
        mSbPlayProgress.setOnSeekBarChangeListener(this);
    }

    private void initView () {
        LayoutInflater.from(mContext).inflate(R.layout.video_play_ctrl, this, true);
        mSbPlayProgress = findViewById(R.id.sb_play_progress);
        mTvPlayName = findViewById(R.id.mtv_play_name);
        mTvPassTime = findViewById(R.id.tv_pass_time);
        mTvLongTime = findViewById(R.id.tv_long_time);
        mLayoutCtrlTop = findViewById(R.id.ll_ctrl_top);
        mLayoutCtrlBottom = findViewById(R.id.ll_ctrl_bottom);
        mLayoutMediaVolBox = findViewById(R.id.ll_media_vol_box);
        mLayoutMediaBrightBox = findViewById(R.id.ll_media_bright_box);
        mLayoutMediaFastForwardBox = findViewById(R.id.ll_media_fastForward_box);
        mIconMediaVol = findViewById(R.id.ic_media_vol);
        mPbMediaVolume = findViewById(R.id.pb_media_vol);
        mPbMediaBright = findViewById(R.id.pb_media_bright);
        mTvMediaFastForward = findViewById(R.id.tv_media_fastForward);
        mIvPlayPause = findViewById(R.id.iv_play_pause);
        mIvCenterPlay = findViewById(R.id.iv_center_play);
        mIvMediaFastForward = findViewById(R.id.iv_media_fastforward);
        mPbLoading = findViewById(R.id.pb_loading);
        mTvSubTitle = findViewById(R.id.tv_subtitle);

        mIvBack = findViewById(R.id.iv_back);
        mRlCtrl = findViewById(R.id.rl_video_ctrl);

        mIvBack.setOnClickListener(this);
        mIvCenterPlay.setOnClickListener(this);
        mIvPlayPause.setOnClickListener(this);
        mRlCtrl.setOnClickListener(this);
        mRlCtrl.setOnTouchListener(this);
    }

    private void showTopBottomCtrlView () {
        if(mPlayCtrl == null) return;
        mPlayCtrl.showTopCtrlView(mLayoutCtrlTop);
        mPlayCtrl.showBottomCtrlView(mLayoutCtrlBottom);

        mHandler.removeMessages(Constant.Msg.HIDE_PLAY_TOP_BOTTOM_CTRL_VIEW);
        mHandler.sendEmptyMessageDelayed(Constant.Msg.HIDE_PLAY_TOP_BOTTOM_CTRL_VIEW, 8000);
    }

    public void hideTopBottomCtrlView () {
        if(mPlayCtrl == null) return;
        if (isSeeking) {
            return;
        }
        mPlayCtrl.hideTopCtrlView(mLayoutCtrlTop);
        mPlayCtrl.hideBottomCtrlView(mLayoutCtrlBottom);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && mDuration > 0) {
            int msec = progress * mDuration / 100;
            if (mPlayCtrl != null) {
                mPlayCtrl.seekTo(msec);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private boolean handleTouchEvent (MotionEvent event) {
        if (mActivity == null) return true;
        if (mPlayCtrl == null) return true;
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

    private void onVolumeSlide(float percent) {
        if(mPlayCtrl == null) return;
        mLayoutMediaVolBox.setVisibility(View.VISIBLE);
        mLayoutMediaBrightBox.setVisibility(View.GONE);
        mLayoutMediaFastForwardBox.setVisibility(View.GONE);

        int progress = mPlayCtrl.onVolSlide(percent);
        mIconMediaVol.setImageResource(progress > 0 ? R.mipmap.ic_volume_up : R.mipmap.ic_volume_off);
        mPbMediaVolume.setProgress(progress);
    }

    private void onBrightnessSlide(float percent) {
        if(mPlayCtrl == null) return;
        mLayoutMediaVolBox.setVisibility(View.GONE);
        mLayoutMediaBrightBox.setVisibility(View.VISIBLE);
        mLayoutMediaFastForwardBox.setVisibility(View.GONE);

        mPbMediaBright.setProgress(mPlayCtrl.onBrightnessSlide(percent));
    }

    /**
     * 滑动快进快退
     * @param percent
     */
    private void onSeekSlide(float percent) {
        isSeeking = true;
        if (mPlayCtrl != null && mDuration > 0) {
            long current = mPlayCtrl.getCurrentPosition();
            long toPosition = (long) (current + mDuration * percent);
            int seekBarProgress = (int) (100 * mNewPosition / mDuration);
            mSbPlayProgress.setProgress(seekBarProgress);
            mNewPosition = (int) Math.max(0, Math.min(mDuration, toPosition));
            currentPosition = mNewPosition;
            mTvPassTime.setText(DateUtil.formatPlayTime(mNewPosition));

            mLayoutMediaVolBox.setVisibility(View.GONE);
            mLayoutMediaBrightBox.setVisibility(View.GONE);
            mLayoutMediaFastForwardBox.setVisibility(View.VISIBLE);

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

        }
    }

    private boolean endGesture() {
        boolean clickEnable = false;
        isSeeking = false;
        mPlayCtrl.mVolume = -1;
        mPlayCtrl.mBrightness = -1f;

        mLayoutMediaVolBox.setVisibility(View.GONE);
        mLayoutMediaBrightBox.setVisibility(View.GONE);
        mLayoutMediaFastForwardBox.setVisibility(View.GONE);
        if (isPlayNext) {
        } else if (isPlayPrev) {
        } else if (isSourceNext) {
            clickEnable = true;
            if (mPlayCtrl != null) {
                if (!mPlayCtrl.isPlaying()) {
                    mPlayCtrl.resume();
                }
                mPlayCtrl.seekTo(mNewPosition);
            }
        } else if (isSourcePrev) {
            clickEnable = true;
            if (mPlayCtrl != null) {
                if (!mPlayCtrl.isPlaying()) {
                    mPlayCtrl.resume();
                }
                mPlayCtrl.seekTo(mNewPosition);
            }
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

    private void updateProgress (int elapseMsec) {
        if (elapseMsec > 0) {
            currentPosition = elapseMsec;
        }
        String mTimePass = DateUtil.formatPlayTime(elapseMsec);
        if (mDuration > 0 && !isSeeking) {
            mTvPassTime.setText(mTimePass);
            int progress = elapseMsec * 100 / mDuration;
            int secondaryProgress = mPlayCtrl.getBufferPercentage();
            mSbPlayProgress.setSecondaryProgress(secondaryProgress);
            mSbPlayProgress.setProgress(progress);
        }
    }


    public void onStart(Activity activity) {
        mPlayCtrl.onSart(activity);
    }

    public void onStop() {
        mPlayCtrl.onStop();
    }

    public void onDestroy () {
        if(mPlayCtrl != null){
            mPlayCtrl.onDestroy();
            mPlayCtrl = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                mActivity.finish();
                break;
            case R.id.iv_center_play:
                mPlayCtrl.resume();
                mIvPlayPause.setImageResource(R.mipmap.ic_player_pause);
                mIvCenterPlay.setVisibility(View.GONE);
                break;
            case R.id.iv_play_pause:
                if (mPlayCtrl != null) {
                    if (mPlayCtrl.isPlaying()) {
                        mPlayCtrl.pause();
                        mIvCenterPlay.setVisibility(View.VISIBLE);
                        mIvPlayPause.setImageResource(R.mipmap.ic_play);
                    } else {
                        mPlayCtrl.resume();
                        mIvCenterPlay.setVisibility(View.GONE);
                        mIvPlayPause.setImageResource(R.mipmap.ic_player_pause);
                    }
                }
                break;
            case R.id.rl_video_ctrl:
                showTopBottomCtrlView();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.rl_video_ctrl:
                return handleTouchEvent(event);
        }
        return false;
    }

    public void play (String playUrl) {
        mPbLoading.setVisibility(VISIBLE);
        if (mPlayCtrl == null) {
            Loger.e(TAG,"mPlayer == null please init player first");
            return;
        }
        if (TextUtils.isEmpty(playUrl)) {
            Loger.e(TAG,"playUrl is empty");
            return;
        }
        mPlayCtrl.play(playUrl,this);
    }

    public void pause () {
        if (mPlayCtrl != null) {
            pressHomePlaying = mPlayCtrl.isPlaying();
            mPlayCtrl.pause();
        }
    }
    public void resume () {
        if (mPlayCtrl != null) {
            if (pressHomePlaying) {
                mPlayCtrl.resume();
            }
        }
    }

    public boolean isPortrait () {
        return mPlayCtrl == null ? false : mPlayCtrl.isPortrait();
    }

    public void toggleScreen () {
        if (mPlayCtrl != null) {
            mPlayCtrl.toggleScreen();
        }
    }

    public void setLandscape (boolean isLandscape) {
        if (mPlayCtrl != null) {
            mPlayCtrl.setLandscape(isLandscape);
        }
    }

    public void setTitle (String title) {
        mTvPlayName.setText(title);
    }

    @Override
    public void OnYoutube(final YoutubeReq youtubeReq) {
        MainHandler.runMainThread(new Runnable() {
            @Override
            public void run() {
                if (youtubeReq != null) {
                    mTvPlayName.setText(youtubeReq.title);
                }
            }
        });

    }

    @Override
    public void OnVimeo(final VimeoReq vimeoReq) {
        MainHandler.runMainThread(new Runnable() {
            @Override
            public void run() {
                if (vimeoReq != null) {
                    mTvPlayName.setText(vimeoReq.title);
                }
            }
        });
    }

    @Override
    public void OnFacebook(FacebookReq facebookReq) {

    }
}
