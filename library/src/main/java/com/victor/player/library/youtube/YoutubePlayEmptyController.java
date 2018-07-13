package com.victor.player.library.youtube;

import android.util.Log;

import com.victor.player.library.module.Player;

public class YoutubePlayEmptyController extends AbstractYouTubePlayerListener  {
    private String TAG = "YoutubePlayController";

    private PlayStatusListener mPlayStatusListener;

    public YoutubePlayEmptyController() {
    }

    @Override
    public void onReady() {
    }

    @Override
    public void onCurrentSecond(float second) {
        super.onCurrentSecond(second);
        if (mPlayStatusListener != null) {
            mPlayStatusListener.onPlayStatus(Player.PLAYER_PROGRESS_INFO, (int) (second * 1000));
        }
    }

    @Override
    public void onVideoDuration(float duration) {
        super.onVideoDuration(duration);
        if (mPlayStatusListener != null) {
            mPlayStatusListener.onPlayStatus(Player.PLAYER_PREPARED, (int) (duration * 1000));
        }
    }

    @Override
    public void onStateChange(@PlayerConstants.PlayerState.State int state) {
        if (mPlayStatusListener != null) {
            mPlayStatusListener.onPlayStatus(getPlayStatus(state),0);
        }
    }

    public void setPlayStatusListener (PlayStatusListener listener) {
        Log.e(TAG,"setPlayStatusListener()......");
        mPlayStatusListener = listener;
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

}
