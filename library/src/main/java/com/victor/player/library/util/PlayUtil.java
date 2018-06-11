package com.victor.player.library.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Victor
 * @Date Create on 2018/5/18 10:47.
 * @Describe
 */

public class PlayUtil {

    public static String getVideoId (String url) {
        String videoId = url;
        if (TextUtils.isEmpty(videoId)) {
            return "";
        }
        Uri uri = Uri.parse(videoId);
        String host = uri.getHost();

        if (!TextUtils.isEmpty(host)) {
            if (host.equals(Constant.YOUTUBE_HOST)) {
                videoId =  uri.getQueryParameter("v");
                if (TextUtils.isEmpty(videoId)) {
                    videoId = uri.getLastPathSegment();
                }
            } else if (host.equals(Constant.YOUTUBE_HOST2)) {
                videoId = uri.getLastPathSegment();
            } else if (host.equals(Constant.VIMEO_HOST)) {
                videoId = uri.getLastPathSegment();
            }
        }
        return videoId;
    }

    public static int getVideoType (String url) {
        int videoType = 0;
        if (TextUtils.isEmpty(url)) return videoType;
        //播放url纯数字为vimeo video id
        if (isDigit(url)) {
            videoType = Constant.VideoType.VIMEO;
        } else {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (!TextUtils.isEmpty(host)) {
                if (host.equals(Constant.YOUTUBE_HOST) || host.equals(Constant.YOUTUBE_HOST2)) {
                    videoType = Constant.VideoType.YOUTUBE;
                } else if (host.equals(Constant.VIMEO_HOST)) {
                    videoType = Constant.VideoType.VIMEO;
                } else {
                    videoType = Constant.VideoType.SDMC;
                }
            } else {
                //host 为空说明是youtube video id
                videoType = Constant.VideoType.YOUTUBE;
            }
        }
        return videoType;
    }

    // 判断一个字符串是否都为数字
    private static boolean isDigit(String strNum) {
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher((CharSequence) strNum);
        return matcher.matches();
    }

}
