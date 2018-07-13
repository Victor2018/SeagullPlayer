package com.victor.player.library.youtube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.victor.player.library.R;
import com.victor.player.library.module.Player;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.DateUtil;

public class YoutubePlayController extends AbstractYouTubePlayerListener implements
        YouTubePlayerFullScreenListener,View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,View.OnFocusChangeListener {
    private String TAG = "YoutubePlayController";
    private Context mContext;
    private final View playerUI;

    private YouTubePlayer youTubePlayer;

    // panel is used to intercept clicks on the WebView, I don't want the user to be able to click the WebView directly.
    private SeekBar mSbMediaProgress;
    private TextView mTvVideoName,mTvTimePass,mTvVideoDuration;
    private LinearLayout mLayoutMediaCtrl;

    private boolean isPlaying = false;
    private boolean showUI = true;
    private boolean canFadeControls = false;
    private int videoDuration;
    private boolean isBackPlay;
    private boolean isFastforwardPlay;
    private int currentPosition;
    private Animation mAnimShowCtrl,mAnimHideCtrl;
    private PlayStatusListener mPlayStatusListener;
    private String youtubeVideoName;

    public YoutubePlayController(Context context, View customPlayerUI, YouTubePlayer youTubePlayer) {
        this.playerUI = customPlayerUI;
        this.mContext = context;
        this.youTubePlayer = youTubePlayer;

        initViews(customPlayerUI);
        initAnim();
    }

    @SuppressLint("WrongViewCast")
    private void initViews(View playerUI) {
        mLayoutMediaCtrl = playerUI.findViewById(R.id.ll_media_ctrl);
        mSbMediaProgress = playerUI.findViewById(R.id.sb_media_progress);
        mTvVideoName = playerUI.findViewById(R.id.tv_video_name);
        mTvTimePass = playerUI.findViewById(R.id.tv_time_pass);
        mTvVideoDuration = playerUI.findViewById(R.id.tv_video_duration);

        mSbMediaProgress.setOnSeekBarChangeListener(this);
    }

    private void initAnim () {
        //显示View动画
        mAnimShowCtrl = AnimationUtils.loadAnimation(mContext, R.anim.anim_bottom_enter);
        mAnimShowCtrl.setFillAfter(true);
        //隐藏View动画
        mAnimHideCtrl = AnimationUtils.loadAnimation(mContext, R.anim.anim_bottom_exit);
        mAnimHideCtrl.setFillAfter(true);
    }

    @Override
    public void onReady() {
    }

    @Override
    public void onStateChange(@PlayerConstants.PlayerState.State int state) {
//        if (state == PlayerConstants.PlayerState.PLAYING) {
//            mIbPlayPause.setImageResource(R.mipmap.ic_vidcontrol_pause_play_00);
//        } else {
//            mIbPlayPause.setImageResource(R.mipmap.ic_vidcontrol_pause_play_11);
//        }
        updateControlsState(state);

        if(state == PlayerConstants.PlayerState.PLAYING || state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED) {

            canFadeControls = true;
            boolean playing = state == PlayerConstants.PlayerState.PLAYING;

            if (playing) {
                mHandler.removeMessages(Constant.Msg.HIDE_PLAY_CTRL_VIEW);
                mHandler.sendEmptyMessageDelayed(Constant.Msg.HIDE_PLAY_CTRL_VIEW,8000);

            } else {
                mHandler.removeMessages(Constant.Msg.HIDE_PLAY_CTRL_VIEW);
            }

        } else {
            showMediaCtrlView();
            if(state == PlayerConstants.PlayerState.BUFFERING) {
                canFadeControls = false;
            }

            if(state == PlayerConstants.PlayerState.UNSTARTED) {
                canFadeControls = false;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCurrentSecond(float second) {
        if (mPlayStatusListener != null) {
            mPlayStatusListener.onPlayStatus(Player.PLAYER_PROGRESS_INFO, (int) (second * 1000));
        }
        currentPosition = (int) second;
        if (videoDuration > 0) {
            int progress = currentPosition * 100 / videoDuration;
            mSbMediaProgress.setProgress(progress);
        }
        String longTime = DateUtil.formatPlayTime(currentPosition * 1000);
        mTvTimePass.setText(longTime);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onVideoDuration(float duration) {
        if (mPlayStatusListener != null) {
            mPlayStatusListener.onPlayStatus(Player.PLAYER_PREPARED, (int) (duration * 1000));
        }

        videoDuration = (int) (duration);
        String longTime = DateUtil.formatPlayTime(videoDuration * 1000);
        mTvVideoDuration.setText(longTime);
    }

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        ViewGroup.LayoutParams viewParams = playerUI.getLayoutParams();
        viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        playerUI.setLayoutParams(viewParams);
    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        ViewGroup.LayoutParams viewParams = playerUI.getLayoutParams();
        viewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        playerUI.setLayoutParams(viewParams);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ib_play_pause) {
            if (isPlaying) {
                youTubePlayer.pause();
            } else {
                youTubePlayer.play();
            }
            isPlaying = !isPlaying;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && videoDuration > 0) {
            int msec = progress * videoDuration / 100;
            if (youTubePlayer != null) {
                youTubePlayer.seekTo(msec);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void showMediaCtrlView () {
        if (mLayoutMediaCtrl == null) return;
        if (mLayoutMediaCtrl.getVisibility() == View.VISIBLE) {
            mHandler.removeMessages(Constant.Msg.HIDE_PLAY_CTRL_VIEW);
            mHandler.sendEmptyMessageDelayed(Constant.Msg.HIDE_PLAY_CTRL_VIEW,8000);
            return;
        }
        mLayoutMediaCtrl.setVisibility(View.VISIBLE);
        mLayoutMediaCtrl.startAnimation(mAnimShowCtrl);
        mHandler.sendEmptyMessageDelayed(Constant.Msg.HIDE_PLAY_CTRL_VIEW,8000);
    }

    private void hideMediaCtrlView () {
        if(!canFadeControls || !showUI) return;

        if (mLayoutMediaCtrl == null) return;
        if (mLayoutMediaCtrl.getVisibility() == View.GONE) return;
        mLayoutMediaCtrl.startAnimation(mAnimHideCtrl);
        mLayoutMediaCtrl.setVisibility(View.GONE);
    }

    public void setPlayStatusListener (PlayStatusListener listener) {
        Log.e(TAG,"setPlayStatusListener()......");
        mPlayStatusListener = listener;
    }

    private void updateControlsState(int state) {
        if (!TextUtils.isEmpty(youtubeVideoName)) {
            mTvVideoName.setText(youtubeVideoName);
        }
        if (mPlayStatusListener != null) {
            mPlayStatusListener.onPlayStatus(getPlayStatus(state),0);
        }
        switch (state) {
            case PlayerConstants.PlayerState.ENDED:
                isPlaying = false;
                break;
            case PlayerConstants.PlayerState.PAUSED:
                isPlaying = false;
                break;
            case PlayerConstants.PlayerState.PLAYING:
                isPlaying = true;
                break;
            case PlayerConstants.PlayerState.UNSTARTED:
                break;
            default:
                break;
        }
    }

    public int getPlayStatus (int youtuebStatus) {
        int status = 0;
        switch (youtuebStatus) {
            case PlayerConstants.PlayerState.BUFFERING:
                status= Player.PLAYER_BUFFERING_START;
                break;
            case PlayerConstants.PlayerState.PLAYING:
                status= Player.PLAYER_PREPARED;
                break;
            case PlayerConstants.PlayerState.PAUSED:
                status= Player.PLAYER_PAUSE;
                break;
            case PlayerConstants.PlayerState.ENDED:
                status= Player.PLAYER_COMPLETE;
                break;
            case PlayerConstants.PlayerState.UNKNOWN:
            case PlayerConstants.PlayerState.UNSTARTED:
                status = Player.PLAYER_ERROR;
                break;
        }
        return status;
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.Msg.HIDE_PLAY_CTRL_VIEW:
                    hideMediaCtrlView();
                    break;
            }
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int i = v.getId();
        if (i == R.id.ib_back) {
            isBackPlay = hasFocus;
        } else if (i == R.id.ib_forward) {
            isFastforwardPlay = hasFocus;
        }
    }

    public void seekTo (boolean seekEnd) {
        if (mSbMediaProgress == null) return;
        if (!isBackPlay && ! isFastforwardPlay) return;
        if (videoDuration <= 0) return;
        if (isFastforwardPlay) {
            currentPosition = currentPosition + videoDuration * 1 / 100;
        } else if (isBackPlay){
            currentPosition = currentPosition - videoDuration * 1 / 100;
        }
        if (currentPosition < 0) {
            currentPosition = 0;
        }
        if (currentPosition > videoDuration) {
            currentPosition = videoDuration;
        }
        if (youTubePlayer != null) {
            int progress = currentPosition * 100 / videoDuration;
            youTubePlayer.seekTo(currentPosition);
            mSbMediaProgress.setProgress(progress);
        }
    }

    public void onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                seekTo(false);
                break;
            case KeyEvent.KEYCODE_BACK:
                break;
        }
    }
    public void onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                showMediaCtrlView();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                showMediaCtrlView();
                seekTo(true);
                break;
        }
    }

    public boolean isPlaying () {
        return isPlaying;
    }

    public int getCurrentPosition () {
        return currentPosition * 1000;
    }
    public int getDuration () {
        return videoDuration * 1000;
    }

    public void setVideoName (String videoName) {
        youtubeVideoName = videoName;
        if (mTvVideoName != null) {
            mTvVideoName.setText(videoName);
        }
    }

}
