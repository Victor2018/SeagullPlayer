package com.victor.player.library.module;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.victor.player.library.data.FmtStreamMap;
import com.victor.player.library.data.VimeoVideo;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.presenter.VimeoPresenterImpl;
import com.victor.player.library.presenter.YoutubePresenterImpl;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.PlayUtil;
import com.victor.player.library.util.YoutubeParser;
import com.victor.player.library.view.VimeoView;
import com.victor.player.library.view.YoutubeView;

public class PlayHelper implements YoutubeView<String>,VimeoView<String>{
    private String TAG = "PlayHelper";
    private SurfaceView mSurfaceView;
    private Player mPlayer;
    private Handler mHandler;
    private YoutubePresenterImpl youtubePresenter;
    private VimeoPresenterImpl vimeoPresenter;
    private YoutubeReq youtubeReq;
    private int videoType;

    public PlayHelper(SurfaceView surfaceView, Handler handler) {
        mSurfaceView = surfaceView;
        mHandler = handler;
        init();
    }

    private void init () {
        youtubePresenter = new YoutubePresenterImpl(this);
        vimeoPresenter = new VimeoPresenterImpl(this);
        mPlayer = new Player(mSurfaceView,mHandler);
    }

    public void play(String url) {
        videoType = PlayUtil.getVideoType(url);
        String identifier = PlayUtil.getVideoId(url);
        switch (videoType) {
            case Constant.VideoType.YOUTUBE:
                Log.e(TAG,"playing youtube......");
                youtubePresenter.sendRequest(String.format(Constant.VIDINFO, identifier),null,null);
                break;
            case Constant.VideoType.VIMEO:
                Log.e(TAG,"playing vimeo......");
                vimeoPresenter.sendRequest(String.format(Constant.VIMEO_CONFIG_URL, identifier),Constant.getVimeoHttpHeaderParm(identifier),null);
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
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG,"youtube response data == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        youtubeReq = YoutubeParser.parseYoutubeData(data);

        if (youtubeReq != null) {
            Log.e(TAG,"OnYoutube-author" + youtubeReq.author);
            Log.e(TAG,"OnYoutube-bigthumb" + youtubeReq.bigthumb);
            Log.e(TAG,"OnYoutube-bigthumbhd" + youtubeReq.bigthumbhd);
            Log.e(TAG,"OnYoutube-category" + youtubeReq.category);
            Log.e(TAG,"OnYoutube-description" + youtubeReq.description);
            Log.e(TAG,"OnYoutube-duration" + youtubeReq.duration);
            Log.e(TAG,"OnYoutube-formats" + youtubeReq.formats);
            Log.e(TAG,"OnYoutube-jsurl" + youtubeReq.jsurl);
            Log.e(TAG,"OnYoutube-published" + youtubeReq.published);
            Log.e(TAG,"OnYoutube-rating" + youtubeReq.rating);
            Log.e(TAG,"OnYoutube-thumb" + youtubeReq.thumb);
            Log.e(TAG,"OnYoutube-title" + youtubeReq.title);
            Log.e(TAG,"OnYoutube-videoid" + youtubeReq.videoid);
            Log.e(TAG,"OnYoutube-ciphertag" + youtubeReq.ciphertag);
            Log.e(TAG,"OnYoutube-have_basic" + youtubeReq.have_basic);
            Log.e(TAG,"OnYoutube-have_gdata" + youtubeReq.have_gdata);
            Log.e(TAG,"OnYoutube-keywords" + youtubeReq.keywords);
            Log.e(TAG,"OnYoutube-length" + youtubeReq.length);
            Log.e(TAG,"OnYoutube-viewcount" + youtubeReq.viewcount);
            if (youtubeReq.sm != null && youtubeReq.sm.size() > 0) {
                mPlayer.playUrl(youtubeReq.sm.get(0).url,false);
                for (FmtStreamMap info : youtubeReq.sm) {
                    Log.e(TAG,"OnYoutube-fallbackHost" + info.fallbackHost);
                    Log.e(TAG,"OnYoutube-itag" + info.itag);
                    Log.e(TAG,"OnYoutube-quality" + info.quality);
                    Log.e(TAG,"OnYoutube-s" + info.s);
                    Log.e(TAG,"OnYoutube-sig" + info.sig);
                    Log.e(TAG,"OnYoutube-type" + info.type);
                    Log.e(TAG,"OnYoutube-url" + info.url);
                    Log.e(TAG,"OnYoutube-url" + info);
                }
            }
        }
    }

    @Override
    public void OnVimeo(String data, String msg) {
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
        if (mPlayer != null) {
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
    }
}
