package com.victor.player.library.module;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import com.victor.player.library.data.FmtStreamMap;
import com.victor.player.library.data.VimeoVideo;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.interfaces.OnHttpListener;
import com.victor.player.library.presenter.VimeoPresenterImpl;
import com.victor.player.library.presenter.YoutubePresenterImpl;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.PlayUtil;
import com.victor.player.library.util.YoutubeParser;
import com.victor.player.library.view.VimeoView;
import com.victor.player.library.view.YoutubeView;

public class PlayHelper implements YoutubeView<String>,VimeoView<String>,OnHttpListener<String> {
    private String TAG = "PlayHelper";
    private SurfaceView mSurfaceView;
    private TextureView mTextureView;
    private Player mPlayer;
    private Handler mHandler;
    private YoutubePresenterImpl youtubePresenter;
    private VimeoPresenterImpl vimeoPresenter;
    private YoutubeReq youtubeReq;
    private int videoType;
    private HttpRequestHelper mHttpRequestHelper;

    public PlayHelper(SurfaceView surfaceView, Handler handler) {
        mSurfaceView = surfaceView;
        mHandler = handler;
        init();
    }
    public PlayHelper(TextureView textureView, Handler handler) {
        mTextureView = textureView;
        mHandler = handler;
        init();
    }

    private void init () {
        youtubePresenter = new YoutubePresenterImpl(this);
        vimeoPresenter = new VimeoPresenterImpl(this);
        mHttpRequestHelper = new HttpRequestHelper( this);
        mPlayer = mTextureView != null ? new Player(mTextureView,mHandler) : new Player(mSurfaceView,mHandler);
    }

    public synchronized void play(String url) {
        videoType = PlayUtil.getVideoType(url);
        String identifier = PlayUtil.getVideoId(url);
        switch (videoType) {
            case Constant.VideoType.YOUTUBE:
                Log.e(TAG,"playing youtube......");
                youtubePresenter.sendRequest(String.format(Constant.YOUTUBE_URL, identifier),null,null);
                break;
            case Constant.VideoType.VIMEO:
                Log.e(TAG,"playing vimeo......");
                vimeoPresenter.sendRequest(String.format(Constant.VIMEO_CONFIG_URL, identifier),Constant.getVimeoHttpHeaderParm(identifier),null);
                break;
            case Constant.VideoType.FACEBOOK:
                Log.e(TAG,"playing facebook......");
                mHttpRequestHelper.sendRequestWithParms(Constant.Msg.REQUEST_FACEBOOK_PLAY_URL, identifier);
                break;
            default:
                Log.e(TAG,"playing m3u8......");
                mPlayer.playUrl(url, false);
                break;
        }
    }

    public Player getPlayer () {
        return mPlayer;
    }
    public void onDestroy() {
        Log.e(TAG,"onDestroy()......");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.close();
            mPlayer = null;
        }
        if (youtubePresenter != null) {
            youtubePresenter.detachView();
        }
        if (youtubeReq != null) {
            youtubeReq = null;
        }
    }

    @Override
    public void OnYoutube(String data, String msg) {
        if (mPlayer == null) {
            if (mHandler != null) {
                Log.e(TAG,"mPlayer == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG,"youtube response data == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        youtubeReq = YoutubeParser.parseYoutubeData(data);

        if (youtubeReq != null) {
//            Log.e(TAG,"OnYoutube-author" + youtubeReq.author);
//            Log.e(TAG,"OnYoutube-bigthumb" + youtubeReq.bigthumb);
//            Log.e(TAG,"OnYoutube-bigthumbhd" + youtubeReq.bigthumbhd);
//            Log.e(TAG,"OnYoutube-category" + youtubeReq.category);
//            Log.e(TAG,"OnYoutube-description" + youtubeReq.description);
//            Log.e(TAG,"OnYoutube-duration" + youtubeReq.duration);
//            Log.e(TAG,"OnYoutube-formats" + youtubeReq.formats);
//            Log.e(TAG,"OnYoutube-jsurl" + youtubeReq.jsurl);
//            Log.e(TAG,"OnYoutube-published" + youtubeReq.published);
//            Log.e(TAG,"OnYoutube-rating" + youtubeReq.rating);
//            Log.e(TAG,"OnYoutube-thumb" + youtubeReq.thumb);
//            Log.e(TAG,"OnYoutube-title" + youtubeReq.title);
//            Log.e(TAG,"OnYoutube-videoid" + youtubeReq.videoid);
//            Log.e(TAG,"OnYoutube-ciphertag" + youtubeReq.ciphertag);
//            Log.e(TAG,"OnYoutube-have_basic" + youtubeReq.have_basic);
//            Log.e(TAG,"OnYoutube-have_gdata" + youtubeReq.have_gdata);
//            Log.e(TAG,"OnYoutube-keywords" + youtubeReq.keywords);
//            Log.e(TAG,"OnYoutube-length" + youtubeReq.length);
//            Log.e(TAG,"OnYoutube-viewcount" + youtubeReq.viewcount);
            if (youtubeReq.sm != null && youtubeReq.sm.size() > 0) {
                mPlayer.playUrl(youtubeReq.sm.get(0).url,false);
//                for (FmtStreamMap info : youtubeReq.sm) {
//                    Log.e(TAG,"OnYoutube-fallbackHost" + info.fallbackHost);
//                    Log.e(TAG,"OnYoutube-itag" + info.itag);
//                    Log.e(TAG,"OnYoutube-quality" + info.quality);
//                    Log.e(TAG,"OnYoutube-s" + info.s);
//                    Log.e(TAG,"OnYoutube-sig" + info.sig);
//                    Log.e(TAG,"OnYoutube-type" + info.type);
//                    Log.e(TAG,"OnYoutube-url" + info.url);
//                    Log.e(TAG,"OnYoutube-url" + info);
//                }
            }
        }
    }

    @Override
    public void OnVimeo(String data, String msg) {
        if (mPlayer == null) {
            if (mHandler != null) {
                Log.e(TAG,"mPlayer == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (data == null) {
            Log.e(TAG,"vimeo response data == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        VimeoVideo vimeoVideo = new VimeoVideo(data);
        if (vimeoVideo == null) {
            Log.e(TAG,"parse vimeo response data error");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        String quality = "1080p";
        String playUrl = vimeoVideo.getStreams().get("1080p");
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoVideo.getStreams().get("720p");
            quality = "720p";
        }
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoVideo.getStreams().get("540p");
            quality = "540p";
        }
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoVideo.getStreams().get("360p");
            quality = "360p";
        }
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = vimeoVideo.getStreams().get("270p");
            quality = "270p";
        }
        Log.e(TAG, "OnVimeo-vimeoVideo.getStreams()------>" + vimeoVideo.getStreams().toString());
        Log.e(TAG, "OnVimeo-quality------>" + quality);
        Log.e(TAG, "OnVimeo-playUrl------>" + playUrl);
        mPlayer.playUrl(playUrl, false);
    }

    @Override
    public void onComplete(int videoType, String data, String msg) {
        Log.e(TAG,"onComplete()......msg = " + msg);
        if (mPlayer == null) {
            if (mHandler != null) {
                Log.e(TAG,"mPlayer == null");
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG,"response data == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        switch (videoType) {
            case Constant.VideoType.VIMEO:
                break;
            case Constant.VideoType.YOUTUBE:
                break;
            case Constant.VideoType.FACEBOOK:
                playFacebook(data);
                break;
            default:
                break;
        }
    }

    private synchronized void playFacebook (String data) {
        Log.e(TAG, "playFacebook-data = " + data);
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG,"facebook response data == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (!data.contains(",") || data.length() == 1) {
            Log.e(TAG,"facebook response data error!");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
        }
        String[] urls = data.split(",");
        String playUrl = "";
        if (urls.length > 1) {
            playUrl = urls[0];
        }
        if (TextUtils.isEmpty(playUrl) && urls.length >= 2) {
            playUrl = urls[1];
        }
        if (TextUtils.isEmpty(playUrl)) {
            Log.e(TAG,"facebook response playUrl is empty!");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        mPlayer.playUrl(playUrl,false);
    }

    public YoutubeReq getYoutubeReq () {
        return youtubeReq;
    }
}
