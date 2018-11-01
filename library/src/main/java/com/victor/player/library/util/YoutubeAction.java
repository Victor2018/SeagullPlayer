package com.victor.player.library.util;

import android.content.Context;
import android.util.Log;

import com.victor.player.library.data.DecipherViaParm;
import com.victor.player.library.interfaces.OnHttpListener;
import com.victor.player.library.module.YoutubeParserHelper;
import com.victor.player.library.ytparser.jsevaluator.JsEvaluator;
import com.victor.player.library.ytparser.jsevaluator.interfaces.JsCallback;

import java.net.SocketTimeoutException;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by longtv, All rights reserved.
 * -----------------------------------------------------------------
 * File: YoutubeAction.java
 * Author: Victor
 * Date: 2018/9/21 11:21
 * Description:
 * -----------------------------------------------------------------
 */
public class YoutubeAction {
    private static String TAG = "YoutubeAction";
    public synchronized static void requestYoutubeInfo (String identifier,OnHttpListener listener){
        Log.e(TAG,"requestYoutubeInfo()......identifier = " + identifier);
        String msg = "";
        String data = "";
        try {
            String requestUrl  = String.format(Constant.YOUTUBE_URL, identifier);
            data = HttpUtil.HttpGetRequest(requestUrl,"");
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onComplete(YoutubeParserHelper.REQUEST_YOUTUBE_INFO,data, msg);
        }
    }
    public synchronized static void requestYoutubeHtml (String identifier,OnHttpListener listener){
        Log.e(TAG,"requestYoutubeHtml()......identifier = " + identifier);
        String msg = "";
        String data = "";
        try {
            String requestUrl = String.format(Constant.WATCHV_HTTPS, identifier);
            Log.e(TAG,"requestYoutubeHtml()......requestUrl = " + requestUrl);
            data = HttpUtil.HttpGetRequest(requestUrl,"");
            data = data.replace("\\u0026", "&");
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onComplete(YoutubeParserHelper.REQUEST_YOUTUBE_HTML,data, msg);
        }
    }

    public synchronized static void requestYoutubeDecipher (String decipherJsFileName,OnHttpListener listener){
        Log.e(TAG,"requestYoutubeDecipher()......decipherJsFileName = " + decipherJsFileName);
        String msg = "";
        String data = "";
        try {
            String requestUrl = String.format(Constant.DECIPHER_URL,decipherJsFileName);
            Log.e(TAG,"requestYoutubeDecipher()......requestUrl = " + requestUrl);
            data = HttpUtil.HttpGetRequest(requestUrl,"");
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onComplete(YoutubeParserHelper.REQUEST_YOUTUBE_DECIPHER,data, msg);
        }
    }

    public synchronized static void decipherViaWebView (Context context, DecipherViaParm decipherViaParm, final OnHttpListener listener){
        Log.e(TAG,"decipherViaWebView()......");
        if (decipherViaParm == null) return;

        final StringBuilder stb = new StringBuilder(decipherViaParm.decipherFunctions + " function decipher(");
        stb.append("){return ");

        for (int i = 0; i < decipherViaParm.encSignatures.size(); i++) {
            int key = decipherViaParm.encSignatures.keyAt(i);
            if (i < decipherViaParm.encSignatures.size() - 1)
                stb.append(decipherViaParm.decipherFunctionName).append("('").append(decipherViaParm.encSignatures.get(key)).
                        append("')+\"\\n\"+");
            else
                stb.append(decipherViaParm.decipherFunctionName).append("('").append(decipherViaParm.encSignatures.get(key)).
                        append("')");
        }
        stb.append("};decipher();");
        Log.e(TAG,"decipherViaWebView()......jsCode = " + stb.toString());
        new JsEvaluator(context).evaluate(stb.toString(), new JsCallback() {
            @Override
            public void onResult(String result) {
                Log.e(TAG,"decipherViaWebView()......JsEvaluator-onResult = " + result);
                if (listener != null) {
                    listener.onComplete(YoutubeParserHelper.REQUEST_YOUTUBE_DECIPHER_VIA,result, "");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, errorMessage);
                if (listener != null) {
                    listener.onComplete(YoutubeParserHelper.REQUEST_YOUTUBE_DECIPHER_VIA,null, errorMessage);
                }
            }
        });
    }
}
