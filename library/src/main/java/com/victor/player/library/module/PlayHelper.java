package com.victor.player.library.module;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.victor.player.library.data.VimeoVideo;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.interfaces.OnHttpListener;
import com.victor.player.library.presenter.VimeoPresenterImpl;
import com.victor.player.library.presenter.YoutubePresenterImpl;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.PlayUtil;
import com.victor.player.library.util.YoutubeParser;
import com.victor.player.library.view.VimeoView;
import com.victor.player.library.view.YoutubeCheckView;
import com.victor.player.library.view.YoutubeView;
import com.victor.player.library.youtube.PlayStatusListener;
import com.victor.player.library.youtube.YouTubePlayerView;
import com.victor.player.library.ytparser.VideoMeta;
import com.victor.player.library.ytparser.YouTubeExtractor;
import com.victor.player.library.ytparser.YtFile;

public class PlayHelper implements YoutubeView<String>,VimeoView<String>,OnHttpListener<String>,YoutubeCheckView<String> {
    private String TAG = "PlayHelper";
    private Context mContext;
    private SurfaceView mSurfaceView;
    private TextureView mTextureView;
    private Player mPlayer;
    private Handler mHandler;
    private YoutubePresenterImpl youtubePresenter;
    private VimeoPresenterImpl vimeoPresenter;
    private YoutubeReq youtubeReq;
    private int videoType;
    private String videoId;
    private String playUrl;
    private HttpRequestHelper mHttpRequestHelper;
    private int youtubePlayCount;

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
        youtubePresenter = new YoutubePresenterImpl(this);
        vimeoPresenter = new VimeoPresenterImpl(this);

        mPlayer = mTextureView != null ? new Player(mTextureView,mHandler) : new Player(mSurfaceView,mHandler);
        mHttpRequestHelper = new HttpRequestHelper( this);
    }

    public synchronized void play(String url) {
        youtubePlayCount = 0;
        playUrl = url;
        videoType = PlayUtil.getVideoType(url);
        videoId = PlayUtil.getVideoId(url);
        switch (videoType) {
            case Constant.VideoType.YOUTUBE:
                Log.e(TAG,"playing youtube......");
                youtubePresenter.sendRequest(String.format(Constant.YOUTUBE_URL, videoId),null,null);
                break;
            case Constant.VideoType.VIMEO:
                Log.e(TAG,"playing vimeo......");
                vimeoPresenter.sendRequest(String.format(Constant.VIMEO_CONFIG_URL, videoId),Constant.getVimeoHttpHeaderParm(videoId),null);
                break;
            case Constant.VideoType.FACEBOOK:
                Log.e(TAG,"playing facebook......");
                mHttpRequestHelper.sendRequestWithParms(Constant.Msg.REQUEST_FACEBOOK_PLAY_URL, videoId);
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
        if (mHttpRequestHelper != null) {
            mHttpRequestHelper.onDestroy();
            mHttpRequestHelper = null;
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

        if (youtubeReq == null) {
            playYoutubeByLink(videoId);
            return;
        }
        if (youtubeReq.sm == null) {
            playYoutubeByLink(videoId);
            return;
        }
        if (youtubeReq.sm.size() == 0) {
            playYoutubeByLink(videoId);
            return;
        }
//        youtubeCheckPresenter.sendRequest(youtubeReq.sm.get(0).url,null,null);
//        mPlayer.playUrl(youtubeReq.sm.get(0).url,false);
        mHttpRequestHelper.sendRequestWithParms(Constant.Msg.REQUEST_YOUTUBE_CHECK_PLAY_URL, youtubeReq.sm.get(0).url);
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
        Log.e(TAG,"onComplete()......data = " + data);
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
            case Constant.VideoType.YOUTUBE_CHECK:
                playYoutubeCheckUrl(data,msg);
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

    @Override
    public void OnYoutubeCheck(String data, String msg) {
        Log.e(TAG,"data------------------->" + data);
        Log.e(TAG,"msg-------------------->" + msg);
    }

    private void playYoutubeCheckUrl (final String data,final String msg) {
        Log.e(TAG,"playYoutubeCheckUrl-data------------------->" + data);
        Log.e(TAG,"playYoutubeCheckUrl-msg-------------------->" + msg);
        if (data.equals("200")) {
            mPlayer.playUrl(msg,false);
        } else if (data.equals("403")) {
            playYoutubeByLink(videoId);
            mHandler.sendEmptyMessage(Constant.Msg.PLAY_BY_YOUTUBE_VIEW);
        }
    }

    private void playYoutubeByLink (String youtubeLink) {
        youtubePlayCount++;
        Log.e(TAG,"playYoutubeByLink()-youtubeLink = " + youtubeLink);
        Log.e(TAG,"playYoutubeByLink()-youtubePlayCount = " + youtubePlayCount);
        if (mContext == null) {
            Log.e(TAG,"playYoutubeByLink()-mContext == null");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        if (youtubePlayCount > 3) {
            Log.e(TAG,"playYoutubeByLink()-youtubePlayCount > 3");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }
            return;
        }
        new YouTubeExtractor(mContext) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles == null) {
                    Log.e(TAG,"playYoutubeByLink()-ytFiles == null");
                    playYoutubeByLink(playUrl);
                    return;
                }
                if (ytFiles.size() == 0) {
                    Log.e(TAG,"playYoutubeByLink()-ytFiles.size() == 0");
                    playYoutubeByLink(playUrl);
                    return;
                }

                YtFile ytFile = ytFiles.get(22);
                if (ytFile == null) {
                    Log.e(TAG,"playYoutubeByLink()-ytFile == null");
                    playYoutubeByLink(playUrl);
                    return;
                }
                youtubePlayCount = 0;
                playUrl = ytFile.getUrl();
                Log.e(TAG,"playYoutubeByLink()-playUrl = " + playUrl);
                mHttpRequestHelper.sendRequestWithParms(Constant.Msg.REQUEST_YOUTUBE_CHECK_PLAY_URL, playUrl);
            }
        }.extract(youtubeLink, true, true);
    }
}
