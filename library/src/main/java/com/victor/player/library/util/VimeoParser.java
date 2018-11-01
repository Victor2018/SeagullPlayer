package com.victor.player.library.util;

import com.victor.player.library.data.VimeoReq;
import com.victor.player.library.data.VimeoUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by longtv, All rights reserved.
 * -----------------------------------------------------------------
 * File: VimeoParser.java
 * Author: Victor
 * Date: 2018/11/1 14:10
 * Description:
 * -----------------------------------------------------------------
 */
public class VimeoParser {
    public static VimeoReq parseVimeo(String response) {
        VimeoReq vimeoReq = new VimeoReq();
        try {
            //Turn JSON string to object
            JSONObject requestJson = new JSONObject(response);

            //Access video information
            JSONObject videoInfo = requestJson.getJSONObject("video");
            vimeoReq.duration = videoInfo.getLong("duration");
            vimeoReq.title = videoInfo.getString("title");

            //Get user information
            JSONObject userInfo = videoInfo.getJSONObject("owner");
            VimeoUser vimeoUser = new VimeoUser();
            vimeoUser.accountType = userInfo.optString("account_type");
            vimeoUser.name = userInfo.optString("name");
            vimeoUser.imageUrl = userInfo.optString("img");
            vimeoUser.image2xUrl = userInfo.optString("img_2x");
            vimeoUser.url = userInfo.optString("url");
            vimeoUser.id = userInfo.optLong("id");
            vimeoReq.videoUser = vimeoUser;

            //Get thumbnail information
            JSONObject thumbsInfo = videoInfo.getJSONObject("thumbs");
            Iterator<String> iterator = thumbsInfo.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                vimeoReq.thumbs.put(key, thumbsInfo.getString(key));
            }

            //Access video stream information
            JSONArray streamArray = requestJson.getJSONObject("request")
                    .getJSONObject("files")
                    .getJSONArray("progressive");

            //Get info for each stream available
            for (int streamIndex = 0; streamIndex < streamArray.length(); streamIndex++) {
                JSONObject stream = streamArray.getJSONObject(streamIndex);
                String url = stream.getString("url");
                String quality = stream.getString("quality");
                //Store stream information
                vimeoReq.streams.put(quality, url);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vimeoReq;
    }
}
