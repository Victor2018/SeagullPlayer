package com.victor.player.library.interfaces;

import com.victor.player.library.data.SubTitleInfo;

import java.util.HashMap;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: OnYoutubeSubTitleListener.java
 * Author: Victor
 * Date: 2018/10/26 16:04
 * Description:
 * -----------------------------------------------------------------
 */
public interface OnYoutubeSubTitleListener {
    void OnYoutubeSubTitle (HashMap<Integer, SubTitleInfo> datas, String msg);
}
