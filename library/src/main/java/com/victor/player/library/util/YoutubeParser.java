package com.victor.player.library.util;

import android.text.TextUtils;
import android.util.Log;

import com.victor.player.library.data.FmtStreamMap;
import com.victor.player.library.data.YoutubeReq;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeParser {
    private static String TAG = "YoutubeParser";
    public static YoutubeReq parseYoutubeData (String response) {
        YoutubeReq data = new YoutubeReq();
        try {
            HashMap<String, String> videoInfoMap = getVideoInfoMap(new Scanner(response), "utf-8");
            String title = videoInfoMap.get("title");
            if (!TextUtils.isEmpty(title)) {
                if (videoInfoMap.get("title").contains("/")) {
                    data.title = videoInfoMap.get("title").replace("/", "-");
                }
            }
            data.author = videoInfoMap.get("author");
            data.videoid = videoInfoMap.get("video_id");
            data.rating = videoInfoMap.get("avg_rating");
            data.length = Long.parseLong(videoInfoMap.get("length_seconds"));
            data.viewcount = Integer.parseInt(videoInfoMap.get("view_count"));
            try {
                data.thumb = URLDecoder.decode(videoInfoMap.get("thumbnail_url"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // duration=//TODO:时长
            // TODO:解析视频格式,这货后面几个值是个啥
            // fmt_list=
            // 22/1280x720/9/0/115,
            // 43/640x360/99/0/0,
            // 18/640x360/9/0/115,
            // 5/320x240/7/0/0,
            // 36/320x240/99/1/0,
            // 17/176x144/99/1/0
            String fmtList = videoInfoMap.get("fmt_list");
            String[] fmtArray = fmtList.split(",");
            for (String fmt : fmtArray) {
                String[] format = fmt.split("/");
            }
//            data.keywords = videoInfoMap.get("keywords").split(",");
            data.bigthumb = videoInfoMap.get("iurlsd");
            data.bigthumbhd = videoInfoMap.get("iurlsdmaxres");
            // ciphertag//TODO:这货标识是否使用密码签名 'use_cipher_signature': ['True']
            data.ciphertag = TextUtils.equals(videoInfoMap.get("use_cipher_signature"), "True");
            // 解析流列表(这货又是个啥)
            data.sm = extractStreamMap(Constant.UEFSM, videoInfoMap, TextUtils.isEmpty(data.jsurl));
            data.asm = extractStreamMap(Constant.AF, videoInfoMap, TextUtils.isEmpty(data.jsurl));
            data.have_basic = true;
        } catch (Exception e) {
            e.printStackTrace();
            data = null;
        }

        return data;
    }

    public static HashMap<String, String> getVideoInfoMap(final Scanner scanner, final String encoding) {
        HashMap<String, String> parameters = new HashMap<>();
        scanner.useDelimiter(Constant.PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            final String[] nameValue = scanner.next().split(Constant.NAME_VALUE_SEPARATOR);
            if (nameValue.length == 0 || nameValue.length > 2) {
                throw new IllegalArgumentException("bad parameter");
            }
            final String name = decode(nameValue[0], encoding);
            String value = null;
            if (nameValue.length == 2) {
                value = decode(nameValue[1], encoding);
            }
            parameters.put(name, value);
        }
        return parameters;
    }

    public static FmtStreamMap parseFmtStreamMap(final Scanner scanner, final String encoding) {
        FmtStreamMap streamMap = new FmtStreamMap();
        scanner.useDelimiter(Constant.PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            final String[] nameValue = scanner.next().split(Constant.NAME_VALUE_SEPARATOR);
            if (nameValue.length == 0 || nameValue.length > 2)
                throw new IllegalArgumentException("bad parameter");

            final String name = decode(nameValue[0], encoding);
            String value = null;
            if (nameValue.length == 2)
                value = decode(nameValue[1], encoding);

            // fallback_host=tc.v1.cache8.googlevideo.com&
            // s=9E89E8DE8FF59D59BA5F96D9A220724C1A304F634B2C19.55E8C8A3A7C02C3FBF4D274A85A41F5F55F0401B&
            // itag=17&
            // type=video%2F3gpp%3B+codecs%3D%22mp4v.20.3%2C+mp4a.40.2%22&
            // quality=small&
            // url=http%3A%2F%2Fr20---sn-a5m7lne6.googlevideo.com%2Fvideoplayback%3Fkey%3Dyt5%26ip%3D173.254.202.174%26mt%3D1393571459%26fexp%3D936112%252C937417%252C937416%252C913434%252C936910%252C936913%252C902907%26itag%3D17%26source%3Dyoutube%26sver%3D3%26mv%3Dm%26ms%3Dau%26sparams%3Dgcr%252Cid%252Cip%252Cipbits%252Citag%252Csource%252Cupn%252Cexpire%26ipbits%3D0%26expire%3D1393597755%26gcr%3Dus%26upn%3Du-4gaUCuZCM%26id%3D782b01f5511b174f

            if (TextUtils.equals("fallback_host", name)) {
                streamMap.fallbackHost = value;
            }
            if (TextUtils.equals("s", name)) {
                streamMap.s = value;
            }
            if (TextUtils.equals("itag", name)) {
                streamMap.itag = value;
            }
            if (TextUtils.equals("type", name)) {
                streamMap.type = value;
            }
            if (TextUtils.equals("quality", name)) {
                streamMap.quality = value;
            }
            if (TextUtils.equals("url", name)) {
                streamMap.url = value;
            }
            if (TextUtils.equals("sig", name)) {
                streamMap.sig = value;
            }
        }
        return streamMap;
    }

    /** @param uefsm2
     * @param videoInfoMap
     * @param empty
     *            //这货是做啥用的 */
    public static List<FmtStreamMap> extractStreamMap(String uefsm2, HashMap<String, String> videoInfoMap, boolean empty) {
        List<FmtStreamMap> streamMaps = new ArrayList<FmtStreamMap>();
        if (videoInfoMap != null && videoInfoMap.containsKey(uefsm2)) {
            String uefms2 = videoInfoMap.get(uefsm2);
            String[] uefms2s = uefms2.split(",");
            for (String s : uefms2s) {
                FmtStreamMap streamMap = YoutubeParser.parseFmtStreamMap(new Scanner(s), "utf-8");
                streamMaps.add(streamMap);
            }
        }
        return streamMaps;
    }

    public String extractVideoId(String url) {
        Pattern p = Pattern.compile("(?:^|[^\\w-]+)([\\w-]{11})(?:[^\\w-]+|$)");
        Matcher matcher = p.matcher(url);
        // for (int i = 0; i < groupCount; i++) {
        String group = matcher.group(1);
        System.out.println(group);
        // }
        return group;
    }

    private static String decode(final String content, final String encoding) {
        try {
            return URLDecoder.decode(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
