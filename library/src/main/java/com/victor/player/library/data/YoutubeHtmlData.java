package com.victor.player.library.data;

import android.util.SparseArray;

import com.victor.player.library.ytparser.YtFile;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by longtv, All rights reserved.
 * -----------------------------------------------------------------
 * File: YoutubeHtmlData.java
 * Author: Victor
 * Date: 2018/9/25 14:25
 * Description:
 * -----------------------------------------------------------------
 */
public class YoutubeHtmlData {
    public SparseArray<String> encSignatures;
    public String decipherJsFileName;
    public SparseArray<YtFile> ytFiles;
}
