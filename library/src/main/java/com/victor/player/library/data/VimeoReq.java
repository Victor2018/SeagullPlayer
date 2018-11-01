package com.victor.player.library.data;

import java.util.HashMap;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by longtv, All rights reserved.
 * -----------------------------------------------------------------
 * File: VimeoReq.java
 * Author: Victor
 * Date: 2018/10/26 16:42
 * Description:
 * -----------------------------------------------------------------
 */
public class VimeoReq {
    public String title;
    public long duration;
    public HashMap<String,String> streams = new HashMap<> ();
    public HashMap<String,String> thumbs = new HashMap<> ();
    public VimeoUser videoUser;
}
