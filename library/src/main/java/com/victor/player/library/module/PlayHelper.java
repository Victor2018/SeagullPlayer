package com.victor.player.library.module;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import com.victor.player.library.data.FacebookReq;
import com.victor.player.library.data.SubTitleInfo;
import com.victor.player.library.data.VimeoReq;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.interfaces.OnExtractListener;
import com.victor.player.library.interfaces.OnYoutubeListener;
import com.victor.player.library.interfaces.OnYoutubeSubTitleListener;
import com.victor.player.library.presenter.FacebookPresenterImpl;
import com.victor.player.library.presenter.VimeoPresenterImpl;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.PlayUtil;
import com.victor.player.library.view.FacebookView;
import com.victor.player.library.view.VimeoView;

import java.util.HashMap;

public class PlayHelper implements VimeoView<VimeoReq>,FacebookView<FacebookReq>,
        OnYoutubeListener,OnYoutubeSubTitleListener {
    private String TAG = "PlayHelper";
    private Context mContext;
    private SurfaceView mSurfaceView;
    private TextureView mTextureView;
    private Player mPlayer;
    private Handler mHandler;
    private YoutubeParserHelper youtubeParserHelper;
    private VimeoPresenterImpl vimeoPresenter;
    private FacebookPresenterImpl facebookPresenter;
    private YoutubeReq youtubeReq;
    private FacebookReq facebookReq;
    private VimeoReq vimeoReq;
    private int videoType;
    private String videoId;
    private OnExtractListener mOnExtractListener;


    public PlayHelper(Context context,SurfaceView surfaceView, Handler handler) {
        mContext = context;
        mSurfaceView = surfaceView;
        mHandler = handler;
        init();
    }
    public PlayHelper(Context context,TextureView textureView, Handler handler) {
        mContext = context;
        mTextureView = textureView;
        mHandler = handler;
        init();
    }

    private void init () {
        youtubeParserHelper = new YoutubeParserHelper(mContext,this,this);
        vimeoPresenter = new VimeoPresenterImpl(this);
        facebookPresenter = new FacebookPresenterImpl(this);

        mPlayer = mTextureView != null ? new Player(mTextureView,mHandler) : new Player(mSurfaceView,mHandler);
    }

    public synchronized void play(String url,OnExtractListener listener) {
        retData();
        mOnExtractListener = listener;
        mHandler.sendEmptyMessage(Player.PLAYER_PREPARING);
        videoType = PlayUtil.getVideoType(url);
        videoId = PlayUtil.getVideoId(url);
        switch (videoType) {
            case Constant.VideoType.YOUTUBE:
                Log.e(TAG,"playing youtube......");
                if (youtubeParserHelper != null) {
                    youtubeParserHelper.sendRequestWithParms(YoutubeParserHelper.REQUEST_YOUTUBE_INFO, videoId);
                }
                break;
            case Constant.VideoType.VIMEO:
                Log.e(TAG,"playing vimeo......");
                if (vimeoPresenter != null) {
                    vimeoPresenter.sendRequest(String.format(Constant.VIMEO_CONFIG_URL, videoId),Constant.getVimeoHttpHeaderParm(videoId),null);
                }
                break;
            case Constant.VideoType.FACEBOOK:
                Log.e(TAG,"playing facebook......");
                if (facebookPresenter != null) {
                    facebookPresenter.sendRequest(videoId,null, null);
                }
                break;
            case Constant.VideoType.M3U8:
                Log.e(TAG,"playing m3u8......");
                mPlayer.playUrl(url, false);
                break;
            default:
                Log.e(TAG,"playing m3u8......");
                mPlayer.playUrl(url, false);
                break;
        }
    }

    private void retData () {
        youtubeReq = null;
        vimeoReq = null;
        facebookReq = null;
    }

    public Player getPlayer () {
        return mPlayer;
    }

    public void pause () {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    public void resume () {
        if (mPlayer != null) {
            mPlayer.resume();
        }
    }

    public void replay () {
        if (mPlayer != null) {
            mPlayer.replay();
        }
    }

    public int getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }
    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return 0;
    }
    public int getBufferPercentage() {
        if (mPlayer != null) {
            return mPlayer.getBufferPercentage();
        }
        return 0;
    }
    public void seekTo(int msec) {
        if (mPlayer != null) {
            mPlayer.seekTo(msec);
        }
    }

    public boolean isPlaying () {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public void onDestroy() {
        Log.e(TAG,"onDestroy()......");
        retData();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.close();
            mPlayer = null;
        }
        if (youtubeParserHelper != null) {
            youtubeParserHelper.onDestroy();
            youtubeParserHelper = null;
        }
        if (vimeoPresenter != null) {
            vimeoPresenter.detachView();
            vimeoPresenter = null;
        }
        if (facebookPresenter != null) {
            facebookPresenter.detachView();
            facebookPresenter = null;
        }
    }

    @Override
    public void OnVimeo(VimeoReq vimeoReq, String msg) {
        this.vimeoReq = vimeoReq;
        if (mOnExtractListener != null) {
            mOnExtractListener.OnVimeo(vimeoReq);
        }
        if (mPlayer == null) {
            if (mHandler != null) {
                Log.e(TAG,"mPlayer == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (vimeoReq == null) {
            Log.e(TAG,"vimeo response data == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (vimeoReq.streams == null || vimeoReq.streams.size() == 0) {
            Log.e(TAG,"vimeo response data == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        String quality = "1080p";
        String playUrl = vimeoReq.streams.get("1080p");
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoReq.streams.get("720p");
            quality = "720p";
        }
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoReq.streams.get("540p");
            quality = "540p";
        }
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoReq.streams.get("360p");
            quality = "360p";
        }
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoReq.streams.get("270p");
            quality = "270p";
        }
        Log.e(TAG, "OnVimeo-quality------>" + quality);
        Log.e(TAG, "OnVimeo-playUrl------>" + playUrl);
        mPlayer.playUrl(playUrl, false);
    }

    public YoutubeReq getYoutubeReq () {
        return youtubeReq;
    }
    public VimeoReq getVimeoReq () {
        return vimeoReq;
    }
    public FacebookReq getFacebookReq () {
        return facebookReq;
    }

    @Override
    public void OnYoutube(YoutubeReq youtubeReq, String msg) {
        this.youtubeReq = youtubeReq;
        if (mOnExtractListener != null) {
            mOnExtractListener.OnYoutube(youtubeReq);
        }
        if (mPlayer == null) {
            if (mHandler != null) {
                Log.e(TAG,"mPlayer == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }

        if (youtubeReq == null) {
            Log.e(TAG,"youtube youtubeReq == null");
            if (mHandler != null) {
                Log.e(TAG,"youtubeReq == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }

        if (TextUtils.isEmpty(youtubeReq.hlsvp)) {
            if (youtubeReq.sm == null || youtubeReq.sm.size() == 0) {
                if (mHandler != null) {
                    Log.e(TAG,"youtubeReq.sm.size() == 0");
                    mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
                }
                return;
            }
            Log.e(TAG,"OnYoutube-url =" + youtubeReq.sm.get(0).itag + "--->" + youtubeReq.sm.get(0).url);
            mPlayer.playUrl(youtubeReq.sm.get(0).url,false);
        } else {
            Log.e(TAG,"OnYoutube-hlsvp = " + youtubeReq.hlsvp);
            mPlayer.playUrl(youtubeReq.hlsvp,true);
        }
    }

    @Override
    public void OnYoutubeSubTitle(HashMap<Integer, SubTitleInfo> datas, String msg) {
        if (mPlayer != null) {
            mPlayer.setSubTitle(datas);
        }
    }

    @Override
    public void OnFacebook(FacebookReq facebookReq, String msg) {
        this.facebookReq = facebookReq;
        if (mOnExtractListener != null) {
            mOnExtractListener.OnFacebook(facebookReq);
        }
        if (mPlayer == null) {
            if (mHandler != null) {
                Log.e(TAG,"mPlayer == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (facebookReq == null) {
            if (mHandler != null) {
                Log.e(TAG,"facebookReq == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        mPlayer.playUrl(facebookReq.playUrl,false);
    }
}
