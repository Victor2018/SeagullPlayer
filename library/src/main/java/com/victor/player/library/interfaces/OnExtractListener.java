package com.victor.player.library.interfaces;

import com.victor.player.library.data.FacebookReq;
import com.victor.player.library.data.VimeoReq;
import com.victor.player.library.data.YoutubeReq;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by longtv, All rights reserved.
 * -----------------------------------------------------------------
 * File: OnExtractListener.java
 * Author: Victor
 * Date: 2018/10/26 16:39
 * Description:
 * -----------------------------------------------------------------
 */
public interface OnExtractListener {
    void OnYoutube (YoutubeReq youtubeReq);
    void OnVimeo (VimeoReq vimeoReq);
    void OnFacebook (FacebookReq facebookReq);
}
