package com.victor.player.library.util;import java.io.IOException;import java.net.SocketTimeoutException;import org.jsoup.Jsoup;import org.jsoup.nodes.Document;import android.content.Context;import android.text.TextUtils;import android.util.Log;import com.victor.player.library.interfaces.OnHttpListener;/** * Created by victor on 2016/1/21. */public class HttpActions {    private static String TAG = "HttpActions";    public synchronized static void requestVimeoPlayUrl (String url,OnHttpListener listener){		String requestUrl = String.format(Constant.VIMEO_CONFIG_URL, url);		Log.e(TAG,"requestVimeoPlayUrl()......requestUrl = " + requestUrl);		String msg = "";		String data = "";		try {			String res = HttpUtil.HttpGetRequest(requestUrl,url);			if (!TextUtils.isEmpty(res)) {				Log.e(TAG, "requestVimeoPlayUrl-res = " + res);				data = res;			} else {				msg = "vimeo server no play data response error!";			}		} catch (SocketTimeoutException e) {			// TODO Auto-generated catch block			e.printStackTrace();			msg = "connect vimeo server timeout error!";		}        if (listener != null) {			listener.onComplete(Constant.VideoType.VIMEO,data, msg);		}    }        public synchronized static void requestYoutbePlayUrl (String url,OnHttpListener listener){        Log.e(TAG,"requestYoutbePlayUrl()......url = " + url);        String msg = "";        String data = "";		String requestUrl = String.format(Constant.YOUTUBE_URL, url);		try {			String res = HttpUtil.HttpGetRequest(requestUrl,url);			if (!TextUtils.isEmpty(res)) {				Log.e(TAG, "requestYoutbePlayUrl-res = " + res);				data = res;			} else {				msg = "youtube server no play data response error!";			}		} catch (SocketTimeoutException e) {			// TODO Auto-generated catch block			e.printStackTrace();			msg = "connect youtube server timeout error!";		}        if (listener != null) {			listener.onComplete(Constant.VideoType.YOUTUBE,data, msg);		}    }    public synchronized static void requestFacebookPlayUrl (String url,OnHttpListener listener){    	Log.e(TAG,"requestFacebookPlayUrl()......url = " + url);    	String msg = "";    	String hdUrl = "";    	String sdUrl = "";    	String data = "";    			Document document;		try {			document = Jsoup.connect(url)					  .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")					  .timeout(10000).get();			if (document != null) {				String result = document.toString();				if (!TextUtils.isEmpty(result)) {					if (result.contains("\n")) {						String[] items = document.toString().split("\n");						for (int i=0;i<items.length;i++) {							if (items[i].contains("no_ratelimit")) {								String[] datas = items[i].split(",");								for (int j=0;j<datas.length;j++) {									if (datas[j].contains("hd_src_no_ratelimit") && datas[j].split("\"").length >= 2) {										hdUrl = datas[j].split("\"")[1];									}									if (datas[j].contains("sd_src_no_ratelimit") && datas[j].split("\"").length >= 2) {										sdUrl = datas[j].split("\"")[1];									}								}							}						}					}				}			}		} catch (IOException e) {			// TODO Auto-generated catch block			e.printStackTrace();			msg = e.getMessage();		}    	data = hdUrl + "," + sdUrl;    	if (listener != null) {    		listener.onComplete(Constant.VideoType.FACEBOOK,data, msg);    	}    }	public synchronized static void requestCheckYoutubePlayUrl (String url,OnHttpListener listener){		String responseCode = "";		String msg = url;		try {			responseCode = HttpUtil.GetReponseCode(url) + "";		} catch (SocketTimeoutException e) {			e.printStackTrace();			msg = e.getMessage();		}		if (listener != null) {			listener.onComplete(Constant.VideoType.YOUTUBE_CHECK,responseCode, msg);		}	}}