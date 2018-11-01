package com.victor.player.library.module;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.victor.player.library.data.DecipherData;
import com.victor.player.library.data.DecipherViaParm;
import com.victor.player.library.data.FmtStreamMap;
import com.victor.player.library.data.SubTitleListInfo;
import com.victor.player.library.data.YoutubeHtmlData;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.interfaces.OnHttpListener;
import com.victor.player.library.interfaces.OnYoutubeListener;
import com.victor.player.library.interfaces.OnYoutubeSubTitleListener;
import com.victor.player.library.presenter.SubTitleListPresenterImpl;
import com.victor.player.library.presenter.SubTitlePresenterImpl;
import com.victor.player.library.util.Constant;
import com.victor.player.library.util.PlayUtil;
import com.victor.player.library.util.SubTitleParser;
import com.victor.player.library.util.YoutubeAction;
import com.victor.player.library.util.YoutubeParser;
import com.victor.player.library.view.SubTitleListView;
import com.victor.player.library.view.SubTitleView;
import com.victor.player.library.ytparser.YtFile;

import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: YoutubeParserHelper.java
 * Author: Victor
 * Date: 2018/9/21 11:03
 * Description:
 * -----------------------------------------------------------------
 */
public class YoutubeParserHelper implements OnHttpListener,SubTitleListView<String>,SubTitleView<String> {
    public static final int REQUEST_YOUTUBE_INFO                    = 0x8001;
    public static final int REQUEST_YOUTUBE_HTML                    = 0x8002;
    public static final int REQUEST_YOUTUBE_DECIPHER                = 0x8003;
    public static final int REQUEST_YOUTUBE_DECIPHER_VIA           = 0x8004;

    private WeakReference<Context> refContext;
    private SparseArray<YtFile> ytFiles;
    private String youtubeUrl;
    private String identifier;
    private SubTitleListPresenterImpl subTitleListPresenter;
    private SubTitlePresenterImpl subTitlePresenter;
    private YoutubeReq youtubeReq;
    private OnYoutubeListener mOnYoutubeListener;
    private OnYoutubeSubTitleListener mOnYoutubeSubTitleListener;

    public DecipherViaParm decipherViaParm = new DecipherViaParm();
    public static void main(String[] args) {
        String youtubeUrl = "https://www.youtube.com/watch?v=ozv4q2ov3Mk";
        String videoId = "ozv4q2ov3Mk";
        System.out.println("youtubeUrl = " + youtubeUrl);
        System.out.println("videoId = " + videoId);
    }

    private String TAG = "HttpRequestHelper";
    private Handler mRequestHandler;
    private HandlerThread mRequestHandlerThread;

    public YoutubeParserHelper (Context context,OnYoutubeListener listener,OnYoutubeSubTitleListener subTitleListener) {
        refContext = new WeakReference<>(context);
        mOnYoutubeListener = listener;
        mOnYoutubeSubTitleListener = subTitleListener;
        subTitleListPresenter = new SubTitleListPresenterImpl(this);
        subTitlePresenter = new SubTitlePresenterImpl(this);
        startRequestTask ();
    }

    private void startRequestTask (){
        mRequestHandlerThread = new HandlerThread("YoutubeParserHelper");
        mRequestHandlerThread.start();
        mRequestHandler = new Handler(mRequestHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                HashMap<Integer,Object> parmMap;
                switch (msg.what) {
                    case REQUEST_YOUTUBE_INFO:
                        retData();
                        parmMap = (HashMap<Integer, Object>) msg.obj;
                        if (parmMap == null) return;
                        youtubeUrl = (String) parmMap.get(REQUEST_YOUTUBE_INFO);
                        identifier = PlayUtil.getVideoId(youtubeUrl);
                        YoutubeAction.requestYoutubeInfo(identifier,YoutubeParserHelper.this);
                        if (subTitleListPresenter != null) {
                            subTitleListPresenter.sendRequest(String.format(Constant.SUB_TITLE_LIST,identifier),null,null);
                        }
                        break;
                    case REQUEST_YOUTUBE_HTML:
                        parmMap = (HashMap<Integer, Object>) msg.obj;
                        if (parmMap == null) return;
                        youtubeUrl = (String) parmMap.get(REQUEST_YOUTUBE_HTML);
                        identifier = PlayUtil.getVideoId(youtubeUrl);
                        YoutubeAction.requestYoutubeHtml(identifier,YoutubeParserHelper.this);
                        break;
                    case REQUEST_YOUTUBE_DECIPHER:
                        parmMap = (HashMap<Integer, Object>) msg.obj;
                        if (parmMap == null) return;
                        String decipherJsFileName = (String) parmMap.get(REQUEST_YOUTUBE_DECIPHER);
                        YoutubeAction.requestYoutubeDecipher(decipherJsFileName,YoutubeParserHelper.this);
                        break;
                    case REQUEST_YOUTUBE_DECIPHER_VIA:
                        parmMap = (HashMap<Integer, Object>) msg.obj;
                        if (parmMap == null) return;
                        DecipherViaParm parm = (DecipherViaParm) parmMap.get(REQUEST_YOUTUBE_DECIPHER_VIA);
                        YoutubeAction.decipherViaWebView(refContext.get(),parm,YoutubeParserHelper.this);
                        break;
                }
            }
        };
    }

    private void retData () {
        ytFiles = null;
        youtubeReq = null;
        youtubeUrl = null;
        identifier = null;
    }

    public void sendRequestWithParms (int Msg,Object requestData) {
        if (requestData == null) {
            Log.e(TAG,"sendRequestWithParms()-requestData == null");
            return;
        }
        HashMap<Integer, Object> requestMap = new HashMap<Integer, Object>();
        requestMap.put(Msg, requestData);
        Message msg = mRequestHandler.obtainMessage(Msg,requestMap);
        mRequestHandler.sendMessage(msg);
    }

    public void sendRequest (int msg) {
        mRequestHandler.sendEmptyMessage(msg);
    }

    public void onDestroy () {
        if (mRequestHandlerThread != null) {
            mRequestHandlerThread.quit();
            mRequestHandlerThread = null;
        }
        if (subTitleListPresenter != null) {
            subTitleListPresenter.detachView();
            subTitleListPresenter = null;
        }
        if (subTitlePresenter != null) {
            subTitlePresenter.detachView();
            subTitlePresenter = null;
        }
    }

    @Override
    public void onComplete(int videoType, Object data, String msg) {
        switch (videoType) {
            case REQUEST_YOUTUBE_INFO:
                if (data != null) {
                    youtubeReq = YoutubeParser.parseYoutubeData(data.toString());
                }
                boolean sigEnc = YoutubeParser.parseYoutubeSigEnc(data.toString());
                if (sigEnc) {
                    Log.e(TAG, "onComplete-REQUEST_YOUTUBE_INFO-youtubeUrl = " + youtubeUrl);
                    sendRequestWithParms(REQUEST_YOUTUBE_HTML, youtubeUrl);
                } else {
                    if (mOnYoutubeListener != null) {
                        mOnYoutubeListener.OnYoutube(youtubeReq,"start play youtube...");
                    }
                }
                break;
            case REQUEST_YOUTUBE_HTML:
                YoutubeHtmlData youtubeHtmlData = YoutubeParser.parseYoutubeHtml(data.toString());
                if (youtubeHtmlData != null) {
                    ytFiles = youtubeHtmlData.ytFiles;
                    decipherViaParm.encSignatures = youtubeHtmlData.encSignatures;
                    sendRequestWithParms(REQUEST_YOUTUBE_DECIPHER,youtubeHtmlData.decipherJsFileName);
                }
                break;
            case REQUEST_YOUTUBE_DECIPHER:
                DecipherData decipherData = YoutubeParser.parseYoutubeDecipher(data.toString());
                if (decipherData != null) {
                    decipherViaParm.decipherFunctionName = decipherData.decipherFunctionName;
                    decipherViaParm.decipherFunctions = decipherData.decipherFunctions;
                    Log.e(TAG,"onComplete-decipherData.decipherFunctionName = " + decipherData.decipherFunctionName);
                    Log.e(TAG,"onComplete-decipherData.decipherFunctions = " + decipherData.decipherFunctions);
                    sendRequestWithParms(REQUEST_YOUTUBE_DECIPHER_VIA,decipherViaParm);
                }
                break;
            case REQUEST_YOUTUBE_DECIPHER_VIA:
                String signature = data.toString();
                Log.e(TAG,"onComplete-jsResult = " + signature);
                if (!TextUtils.isEmpty(signature)) {
                    List<FmtStreamMap> urls = new ArrayList<>();
                    String[] sigs = signature.split("\n");
                    for (int i = 0; i < decipherViaParm.encSignatures.size() && i < sigs.length; i++) {
                        int key = decipherViaParm.encSignatures.keyAt(i);
                        if (key == 0) {
//                            dashMpdUrl = dashMpdUrl.replace("/s/" + encSignatures.get(key), "/signature/" + sigs[i]);
//                            Log.e(TAG,"getStreamUrls()......dashMpdUrl = " + dashMpdUrl);
                        } else {
                            if (ytFiles != null && ytFiles.size() > 0) {
                                String url = ytFiles.get(key).getUrl();
                                url += "&signature=" + sigs[i];
                                Log.e(TAG,"getStreamUrls()......url = " + key + "------>" +url);
                                if (youtubeReq != null) {
                                    if (youtubeReq.sm != null && youtubeReq.sm.size() > 0) {
                                        if (i < youtubeReq.sm.size()) {
                                            if (key == 22 || key == 18) {
                                                FmtStreamMap streamMap = new FmtStreamMap();
                                                streamMap.itag = key + "";
                                                streamMap.url = url;
                                                urls.add(streamMap);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (youtubeReq != null) {
                        youtubeReq.sm = urls;
                        mOnYoutubeListener.OnYoutube(youtubeReq,"youtube no data response!");
                    }
                }
                break;
        }
    }

    @Override
    public void OnSubTitleList(String data, String msg) {
        Log.e(TAG,"OnSubTitleList()-data = " + data);
        if (data == null) {
            return;
        }
        SubTitleListInfo subTitleListInfo = SubTitleParser.parseSubTitleList(data);
        if (subTitleListInfo != null) {
            if (subTitlePresenter != null) {
                subTitlePresenter.sendRequest(String.format(Constant.SUB_TITLE,identifier,subTitleListInfo.id, subTitleListInfo.lang_code),null,null);
            }
        }
    }

    @Override
    public void OnSubTitle(String data, String msg) {
        if (mOnYoutubeSubTitleListener != null) {
            mOnYoutubeSubTitleListener.OnYoutubeSubTitle(SubTitleParser.parseSubTitle(data.toString()),msg);
        }
    }
}
