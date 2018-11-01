package com.victor.player.library.util;

import android.text.TextUtils;

import com.victor.player.library.data.FacebookReq;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by longtv, All rights reserved.
 * -----------------------------------------------------------------
 * File: FacebookParser.java
 * Author: Victor
 * Date: 2018/11/1 14:52
 * Description:
 * -----------------------------------------------------------------
 */
public class FacebookParser {
    public static FacebookReq pareFacebook (String result) {
        FacebookReq facebookReq = new FacebookReq();
        if (!TextUtils.isEmpty(result)) {
            if (result.contains("\n")) {
                String[] items = result.split("\n");
                for (int i=0;i<items.length;i++) {
                    if (items[i].contains("no_ratelimit")) {
                        String[] datas = items[i].split(",");
                        for (int j=0;j<datas.length;j++) {
                            if (datas[j].contains("hd_src_no_ratelimit") && datas[j].split("\"").length >= 2) {
                                facebookReq.hdPlayUrl = datas[j].split("\"")[1];
                            }
                            if (datas[j].contains("sd_src_no_ratelimit") && datas[j].split("\"").length >= 2) {
                                facebookReq.sdPlayUrl = datas[j].split("\"")[1];
                            }
                        }
                    }
                }
            }
        }
        facebookReq.playUrl = facebookReq.hdPlayUrl;
        if (TextUtils.isEmpty(facebookReq.playUrl)) {
            facebookReq.playUrl = facebookReq.sdPlayUrl;
        }
        return facebookReq;
    }
}
