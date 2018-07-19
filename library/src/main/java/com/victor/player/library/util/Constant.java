package com.victor.player.library.util;

import java.util.HashMap;

/**
 * Created by victor on 2017/9/26.
 */

public class Constant {
    public static final boolean MODEL_DEBUG = true;
    public static final boolean MODEL_ONLINE = true;
    public static final int BUILD_CODE = 0;

    public static final String PARAMETER_SEPARATOR = "&";
    public static final String NAME_VALUE_SEPARATOR = "=";
    public static final String GDATA = "http://gdata.youtube.com/feeds/api/videos/%s?v=2";
    public static final String WATCHV = "http://www.youtube.com/watch?v=%s";
    public static final String YOUTUBE_URL = "http://www.youtube.com/get_video_info?video_id=%s&asv=3&el=detailpage&hl=en_US";
    public static final String PLAYLIST = "http://www.youtube.com/list_ajax?style=json&action_get_list=1&list=%s";
    public static final String USERAGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
    public static final String UEFSM = "url_encoded_fmt_stream_map";
    public static final String AF = "adaptive_fmts";
    public static final String JSPLAYER = "ytplayer\\.config\\s*=\\s*([^\\n]+);";

    public static final String PLAY_URL = "PLAY_URL";


    public static final String YOUTUBE_HOST = "www.youtube.com";
    public static final String YOUTUBE_HOST2 = "youtu.be";

    //Base URL for Vimeo videos
    public static final String VIMEO_URL = "https://vimeo.com/%s";
    public static final String VIMEO_HOST = "vimeo.com";
    public static final String FACEBOOK_HOST = "www.facebook.com";
    //Config URL containing video information
    public static final String VIMEO_CONFIG_URL = "https://player.vimeo.com/video/%s/config";

    public static class VideoType {
        public static final int YOUTUBE                                        = 0x601;
        public static final int VIMEO                                          = 0x602;
        public static final int SDMC                                           = 0x603;
        public static final int FACEBOOK                                       = 0x604;
        public static final int YOUTUBE_CHECK                                 = 0x605;
    }

    public static class Msg {
        public static final int HIDE_PLAY_CTRL_VIEW                            = 0x201;
        public static final int REQUEST_VIMEO_PLAY_URL                         = 0x202;
        public static final int REQUEST_YOUTUBE_PLAY_URL                       = 0x203;
        public static final int REQUEST_FACEBOOK_PLAY_URL                      = 0x204;
        public static final int REQUEST_YOUTUBE_CHECK_PLAY_URL                = 0x205;
        public static final int PLAY_BY_YOUTUBE_VIEW                           = 0x206;
        public static final int PLAY_BY_MEDIA_PLAYER                           = 0x207;
        public static final int PLAY_VIDEO                                       = 0x208;
    }

    public static HashMap<String,String> getVimeoHttpHeaderParm (String identifier) {
        HashMap<String,String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Referer", String.format(VIMEO_URL, identifier));
        return header;
    }
}
