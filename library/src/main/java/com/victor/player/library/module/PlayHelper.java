package com.victor.player.library.module;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.TextureView;

import com.victor.player.library.data.SubTitleInfo;
import com.victor.player.library.data.SubTitleListInfo;
import com.victor.player.library.data.VimeoVideo;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.interfaces.OnHttpListener;
import com.victor.player.library.presenter.SubTitleListPresenterImpl;
import com.victor.player.library.presenter.SubTitlePresenterImpl;
import com.victor.player.library.presenter.VimeoPresenterImpl;
import com.victor.player.library.presenter.YoutubePresenterImpl;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.PlayUtil;
import com.victor.player.library.util.YoutubeParser;
import com.victor.player.library.view.SubTitleListView;
import com.victor.player.library.view.SubTitleView;
import com.victor.player.library.view.VimeoView;
import com.victor.player.library.view.YoutubeCheckView;
import com.victor.player.library.view.YoutubeView;
import com.victor.player.library.ytparser.VideoMeta;
import com.victor.player.library.ytparser.YouTubeExtractor;
import com.victor.player.library.ytparser.YtFile;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class PlayHelper implements YoutubeView<String>,VimeoView<String>,OnHttpListener<String>,
        YoutubeCheckView<String>,SubTitleListView<String>,SubTitleView<String> {
    private String TAG = "PlayHelper";
    private Context mContext;
    private SurfaceView mSurfaceView;
    private TextureView mTextureView;
    private Player mPlayer;
    private Handler mHandler;
    private YoutubePresenterImpl youtubePresenter;
    private VimeoPresenterImpl vimeoPresenter;
    private SubTitleListPresenterImpl subTitleListPresenter;
    private SubTitlePresenterImpl subTitlePresenter;
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
        subTitleListPresenter = new SubTitleListPresenterImpl(this);
        subTitlePresenter = new SubTitlePresenterImpl(this);

        mPlayer = mTextureView != null ? new Player(mTextureView,mHandler) : new Player(mSurfaceView,mHandler);
        mHttpRequestHelper = new HttpRequestHelper( this);
    }

    public synchronized void play(String url) {
        mHandler.sendEmptyMessage(Player.PLAYER_PREPARING);
        playUrl = url;
        videoType = PlayUtil.getVideoType(url);
        videoId = PlayUtil.getVideoId(url);
        switch (videoType) {
            case Constant.VideoType.YOUTUBE:
                Log.e(TAG,"playing youtube......");
                youtubePlayCount = 0;
                if (youtubePresenter != null) {
                    youtubePresenter.sendRequest(String.format(Constant.YOUTUBE_URL, videoId),null,null);
                }
                if (subTitleListPresenter != null) {
                    subTitleListPresenter.sendRequest(String.format(Constant.SUB_TITLE_LIST,videoId),null,null);
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
                if (mHttpRequestHelper != null) {
                    mHttpRequestHelper.sendRequestWithParms(Constant.Msg.REQUEST_FACEBOOK_PLAY_URL, videoId);
                }
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
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.close();
            mPlayer = null;
        }
        if (youtubePresenter != null) {
            youtubePresenter.detachView();
            youtubePresenter = null;
        }
        if (vimeoPresenter != null) {
            vimeoPresenter.detachView();
            vimeoPresenter = null;
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
            /*if (mHandler != null) {
                mHandler.sendEmptyMessage(Player.PLAYER_ERROR);
            }*/
            playYoutubeByLink(videoId);
            return;
        }
        youtubeReq = YoutubeParser.parseYoutubeData(data);

        if (youtubeReq == null) {
            Log.e(TAG,"youtube youtubeReq == null");
            playYoutubeByLink(videoId);
            return;
        }

        //如果是直播直接播放m3u8直播源地址
        if (!TextUtils.isEmpty(youtubeReq.hlsvp)) {
            mPlayer.playUrl(youtubeReq.hlsvp, true);
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
        if (mHttpRequestHelper != null) {
            mHttpRequestHelper.sendRequestWithParms(Constant.Msg.REQUEST_YOUTUBE_CHECK_PLAY_URL, youtubeReq.sm.get(0).url);
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

    private synchronized void playYoutubeCheckUrl (final String data,final String msg) {
        Log.e(TAG,"playYoutubeCheckUrl-data------------------->" + data);
        Log.e(TAG,"playYoutubeCheckUrl-msg-------------------->" + msg);
        if (data.equals("200")) {
            mPlayer.playUrl(msg,false);
        } else if (data.equals("403")) {
            playYoutubeByLink(videoId);
            mHandler.sendEmptyMessage(Constant.Msg.PLAY_BY_YOUTUBE_VIEW);
        }
    }

    private synchronized void playYoutubeByLink (String youtubeLink) {
        mHandler.sendEmptyMessage(Player.PLAYER_BUFFERING_START);
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

                YtFile ytFile = ytFiles.get(22);//720p
                if (ytFile == null) {
                    ytFile = ytFiles.get(18);//360p
                }

                if (ytFile == null) {
                    Log.e(TAG,"playYoutubeByLink()-ytFile == null");
                    playYoutubeByLink(playUrl);
                    return;
                }
                youtubePlayCount = 0;
                playUrl = ytFile.getUrl();
                Log.e(TAG,"playYoutubeByLink()-playUrl = " + playUrl);
                if (mHttpRequestHelper != null) {
                    mHttpRequestHelper.sendRequestWithParms(Constant.Msg.REQUEST_YOUTUBE_CHECK_PLAY_URL, playUrl);
                }
            }
        }.extract(youtubeLink, true, true);
    }

    @Override
    public void OnSubTitleList(String data, String msg) {
        parseSubTitleList(data);
    }

    @Override
    public void OnSubTitle(String data, String msg) {
        parseSubTitle(data);
    }

    private HashMap<Integer,SubTitleListInfo> parseSubTitleList (String result) {
        HashMap<Integer,SubTitleListInfo> datas = new HashMap<>();
        if (TextUtils.isEmpty(result)) return datas;
        SubTitleListInfo defaultLan = null;
        SAXReader reader = new SAXReader();
        Document doc = null;
        try {
            doc = reader.read(new ByteArrayInputStream(result.getBytes("utf-8")));
            Element root = doc.getRootElement();
//            System.out.println("docid = " + root.attributeValue("docid"));
            Iterator<Element> iterator = root.elementIterator("track");
            int key = 0;
            while (iterator.hasNext()){
                Element e = iterator.next();
                SubTitleListInfo data = new SubTitleListInfo();
                data.id = Integer.parseInt(e.attributeValue("id"));
                data.name = e.attributeValue("name");
                data.lang_code = e.attributeValue("lang_code");
                data.lang_original = e.attributeValue("lang_original");
                data.lang_translated = e.attributeValue("lang_translated");
                data.lang_default = Boolean.parseBoolean(e.attributeValue("lang_default"));

                if (data.lang_default) {
                    defaultLan = data;
                }
                datas.put(key, data);
                key++;
            }
            if (key == 1) {
                defaultLan = datas.get(0);
            }
            if (defaultLan != null) {
                if (subTitlePresenter != null) {
                    subTitlePresenter.sendRequest(String.format(Constant.SUB_TITLE,videoId,defaultLan.id,defaultLan.lang_code),null,null);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return datas;
    }

    private HashMap<Integer,SubTitleInfo> parseSubTitle (String result) {
        HashMap<Integer,SubTitleInfo> datas = new HashMap<>();
        if (TextUtils.isEmpty(result)) return datas;
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new ByteArrayInputStream(result.getBytes("utf-8")));
            Element root = doc.getRootElement();
            Iterator<Element> iterator = root.elementIterator("text");
            int key = 0;
            while (iterator.hasNext()){
                Element e = iterator.next();
                SubTitleInfo data = new SubTitleInfo();
                data.beginTime = (int) (Double.parseDouble(e.attributeValue("start")) * 1000);
                data.endTime = data.beginTime + (int) (Double.parseDouble(e.attributeValue("dur")) * 1000);
                String subTitle = e.getStringValue();
                if (!TextUtils.isEmpty(subTitle)) {
                    subTitle = subTitle.replaceAll("&quot;", "\"");
                    subTitle = subTitle.replaceAll("&amp;", "&");
                    subTitle = subTitle.replaceAll("&#39;", "'");
                    subTitle = subTitle.replaceAll("&lt;", "<");
                    subTitle = subTitle.replaceAll("&gt;", ">");
                    data.srtBody = subTitle;
                }
                datas.put(key, data);
                key++;
            }
            mPlayer.setSubTitle(datas);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return datas;
    }
}
