package com.victor.player.library.util;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

import com.victor.player.library.data.DecipherData;
import com.victor.player.library.data.FmtStreamMap;
import com.victor.player.library.data.YoutubeHtmlData;
import com.victor.player.library.data.YoutubeReq;
import com.victor.player.library.ytparser.Format;
import com.victor.player.library.ytparser.VideoMeta;
import com.victor.player.library.ytparser.YtFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeParser {
    private static String TAG = "YoutubeParser";
    private static final Pattern patTitle = Pattern.compile("title=(.*?)(&|\\z)");
    private static final Pattern patAuthor = Pattern.compile("author=(.+?)(&|\\z)");
    private static final Pattern patChannelId = Pattern.compile("ucid=(.+?)(&|\\z)");
    private static final Pattern patLength = Pattern.compile("length_seconds=(\\d+?)(&|\\z)");
    private static final Pattern patViewCount = Pattern.compile("view_count=(\\d+?)(&|\\z)");
    private static final Pattern patStatusOk = Pattern.compile("status=ok(&|,|\\z)");

    private static final Pattern patHlsvp = Pattern.compile("hlsvp=(.+?)(&|\\z)");
    private static final Pattern patHlsItag = Pattern.compile("/itag/(\\d+?)/");

    public static final String STREAM_MAP_STRING = "url_encoded_fmt_stream_map";
    private static final Pattern patIsSigEnc = Pattern.compile("s%3D([0-9A-F|.]{10,}?)(%26|%2C)");
    private static final Pattern patDecryptionJsFile = Pattern.compile("jsbin\\\\/(player(_ias)?-(.+?).js)");
    private static final Pattern patDashManifest2 = Pattern.compile("\"dashmpd\":\"(.+?)\"");
    private static final Pattern patDashManifestEncSig = Pattern.compile("/s/([0-9A-F|.]{10,}?)(/|\\z)");
    private static final Pattern patItag = Pattern.compile("itag=([0-9]+?)([&,])");
    private static final Pattern patEncSig = Pattern.compile("s=([0-9A-F|.]{10,}?)([&,\"])");
    private static final Pattern patUrl = Pattern.compile("url=(.+?)([&,])");
    private static final Pattern patSignatureDecFunction = Pattern.compile("(\\w+)\\s*=\\s*function\\((\\w+)\\).\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;");
    private static final Pattern patVariableFunction = Pattern.compile("([{; =])([a-zA-Z$][a-zA-Z0-9$]{0,2})\\.([a-zA-Z$][a-zA-Z0-9$]{0,2})\\(");
    private static final Pattern patFunction = Pattern.compile("([{; =])([a-zA-Z$_][a-zA-Z0-9$]{0,2})\\(");

    private static boolean includeWebM = true;

    public static DecipherData parseYoutubeDecipher (String response) {
        DecipherData data = new DecipherData();
        Matcher mat = patSignatureDecFunction.matcher(response);
        if (mat.find()) {
            data.decipherFunctionName = mat.group(1);
            Log.e(TAG, "Decipher Functname: " + data.decipherFunctionName);

            Pattern patMainVariable = Pattern.compile("(var |\\s|,|;)" + data.decipherFunctionName.replace("$", "\\$") +
                    "(=function\\((.{1,3})\\)\\{)");

            String mainDecipherFunct;

            mat = patMainVariable.matcher(response);
            if (mat.find()) {
                mainDecipherFunct = "var " + data.decipherFunctionName + mat.group(2);
            } else {
                Pattern patMainFunction = Pattern.compile("function " + data.decipherFunctionName.replace("$", "\\$") +
                        "(\\((.{1,3})\\)\\{)");
                mat = patMainFunction.matcher(response);
                if (!mat.find()) {
                    return data;
                }
                mainDecipherFunct = "function " + data.decipherFunctionName + mat.group(2);
            }

            int startIndex = mat.end();

            for (int braces = 1, i = startIndex; i < response.length(); i++) {
                if (braces == 0 && startIndex + 5 < i) {
                    mainDecipherFunct += response.substring(startIndex, i) + ";";
                    break;
                }
                if (response.charAt(i) == '{')
                    braces++;
                else if (response.charAt(i) == '}')
                    braces--;
            }
            data.decipherFunctions = mainDecipherFunct;
            // Search the main function for extra functions and variables
            // needed for deciphering
            // Search for variables
            mat = patVariableFunction.matcher(mainDecipherFunct);
            while (mat.find()) {
                String variableDef = "var " + mat.group(2) + "={";
                if (data.decipherFunctions.contains(variableDef)) {
                    continue;
                }
                startIndex = response.indexOf(variableDef) + variableDef.length();
                for (int braces = 1, i = startIndex; i < response.length(); i++) {
                    if (braces == 0) {
                        data.decipherFunctions += variableDef + response.substring(startIndex, i) + ";";
                        break;
                    }
                    if (response.charAt(i) == '{')
                        braces++;
                    else if (response.charAt(i) == '}')
                        braces--;
                }
            }
            // Search for functions
            mat = patFunction.matcher(mainDecipherFunct);
            while (mat.find()) {
                String functionDef = "function " + mat.group(2) + "(";
                if ( data.decipherFunctions.contains(functionDef)) {
                    continue;
                }
                startIndex = response.indexOf(functionDef) + functionDef.length();
                for (int braces = 0, i = startIndex; i < response.length(); i++) {
                    if (braces == 0 && startIndex + 5 < i) {
                        data.decipherFunctions += functionDef + response.substring(startIndex, i) + ";";
                        break;
                    }
                    if (response.charAt(i) == '{')
                        braces++;
                    else if (response.charAt(i) == '}')
                        braces--;
                }
            }

            Log.e(TAG, "Decipher Function: " +  data.decipherFunctions);
//            decipherViaWebView(encSignatures);
            /*if (CACHING) {
                writeDeciperFunctToChache();
            }*/
        }
        return data;
    }
    public static YoutubeHtmlData parseYoutubeHtml (String response) {
        YoutubeHtmlData data = new YoutubeHtmlData();
        SparseArray<String> encSignatures = new SparseArray<>();
        SparseArray<YtFile> ytFiles = new SparseArray<>();
        String curJsFileName = null;
        Matcher mat = patDecryptionJsFile.matcher(response);
        if (mat.find()) {
            curJsFileName = mat.group(1).replace("\\/", "/");
            if (mat.group(2) != null) {
                curJsFileName.replace(mat.group(2), "");
            }
            data.decipherJsFileName = curJsFileName;
            Log.e(TAG,"parseYoutubeHtml()......curJsFileName = " + curJsFileName);
        }

        String[] streams = response.split(",|"+STREAM_MAP_STRING+"|&adaptive_fmts=");
        Log.e(TAG,"parseYoutubeHtml()......streams.length = " + streams.length);
        for (String encStream : streams) {
            encStream = encStream + ",";
            if (!encStream.contains("itag%3D")) {
                continue;
            }
            String stream;
            try {
                stream = URLDecoder.decode(encStream, "UTF-8");
                mat = patItag.matcher(stream);
                int itag;
                if (mat.find()) {
                    itag = Integer.parseInt(mat.group(1));
                    Log.e(TAG, "Itag found:" + itag);
                    if (FORMAT_MAP.get(itag) == null) {
                        Log.e(TAG, "Itag not in list:" + itag);
                        continue;
                    } else if (!includeWebM && FORMAT_MAP.get(itag).getExt().equals("webm")) {
                        continue;
                    }
                } else {
                    continue;
                }

                if (!TextUtils.isEmpty(curJsFileName)) {
                    mat = patEncSig.matcher(stream);
                    if (mat.find()) {
                        Log.e(TAG, "itag------>" + itag + "---" + mat.group(1));
                        encSignatures.append(itag, mat.group(1));
                    }
                }

                mat = patUrl.matcher(encStream);
                String url = null;
                if (mat.find()) {
                    url = mat.group(1);
                }
                if (url != null) {
                    Format format = FORMAT_MAP.get(itag);
                    String finalUrl = URLDecoder.decode(url, "UTF-8");
                    Log.e(TAG, "finalUrl------>" + finalUrl);//这里url访问会403无法播放
                    YtFile newVideo = new YtFile(format, finalUrl);
                    ytFiles.put(itag, newVideo);
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        data.ytFiles = ytFiles;
        data.encSignatures = encSignatures;
        return data;
    }
    public static boolean parseYoutubeSigEnc (String response) {
        Log.e(TAG,"parseYoutubeHtml()......");
        // "use_cipher_signature" disappeared, we check whether at least one ciphered signature
        // exists int the stream_map.
        Matcher mat;
        boolean sigEnc = true, statusFail = false;
        if(response != null && response.contains(STREAM_MAP_STRING)){
            String streamMapSub = response.substring(response.indexOf(STREAM_MAP_STRING));
            mat = patIsSigEnc.matcher(streamMapSub);
            if(!mat.find()) {
                sigEnc = false;
                if (!patStatusOk.matcher(response).find()) {
                    statusFail = true;
                }
            }
        }
        // Some videos are using a ciphered signature we need to get the
        // deciphering js-file from the youtubepage.

       return sigEnc || statusFail;

    }

    public static YoutubeReq parseYoutubeData (String response) {
        Log.e(TAG,"parseYoutubeData()......");
        YoutubeReq data = new YoutubeReq();
        try {
            HashMap<String, String> videoInfoMap = getVideoInfoMap(new Scanner(response), "utf-8");
            Matcher mat = patTitle.matcher(response);
            if (mat.find()) {
                data.title = URLDecoder.decode(mat.group(1), "UTF-8");
            }

            Matcher channelIdMat = patChannelId.matcher(response);
            if (channelIdMat.find()) {
                data.channelId = mat.group(1);
            }
            data.hlsvp = videoInfoMap.get("hlsvp");

            if (TextUtils.isEmpty(data.hlsvp)) {
                if (videoInfoMap.containsKey("player_response")) {
                    data.hlsvp = parseLiveData(videoInfoMap.get("player_response"));
                }
            }

            data.author = videoInfoMap.get("author");
            data.videoid = videoInfoMap.get("video_id");
            data.rating = videoInfoMap.get("avg_rating");
            if (videoInfoMap.containsKey("length_seconds")) {
                String lengthSec = videoInfoMap.get("length_seconds");
                if (!TextUtils.isEmpty(lengthSec)) {
                    data.length = Long.parseLong(lengthSec);
                }
            }
            if (videoInfoMap.containsKey("view_count")) {
                String viewCount = videoInfoMap.get("view_count");
                if (!TextUtils.isEmpty(viewCount)) {
                    data.viewcount = Integer.parseInt(viewCount);
                }
            }

            data.thumb = URLDecoder.decode(videoInfoMap.get("thumbnail_url"), "utf-8");
            // duration=//TODO:时长
            // TODO:解析视频格式,这货后面几个值是个啥
            // fmt_list=
            // 22/1280x720/9/0/115,
            // 43/640x360/99/0/0,
            // 18/640x360/9/0/115,
            // 5/320x240/7/0/0,
            // 36/320x240/99/1/0,
            // 17/176x144/99/1/0
//            String fmtList = videoInfoMap.get("fmt_list");
//            String[] fmtArray = fmtList.split(",");
//            for (String fmt : fmtArray) {
//                String[] format = fmt.split("/");
//            }
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

    /**
     * 解析直播m3u8播放地址
     * @param liveReponse
     * @return
     */
    private static String parseLiveData (String liveReponse) {
        Log.e(TAG,"parseLiveData()......");
        String liveUrl = "";
        if (!TextUtils.isEmpty(liveReponse)) {
            try {
                JSONObject object = new JSONObject(liveReponse);

                if (object != null) {
                    JSONObject playabilityStatus = object.getJSONObject("streamingData");
                    if (playabilityStatus != null) {
                        liveUrl = playabilityStatus.optString("hlsManifestUrl");
                        Log.e(TAG,"parseLiveData()......liveUrl= " + liveUrl);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return liveUrl;
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
//            Log.e(TAG,"key = " + name + "value = " + value);
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
        List<FmtStreamMap> streamMaps = new ArrayList<>();
        if (videoInfoMap != null && videoInfoMap.containsKey(uefsm2)) {
            String uefms2 = videoInfoMap.get(uefsm2);
            if (!TextUtils.isEmpty(uefms2)) {
                if (uefms2.contains(",")) {
                    String[] uefms2s = uefms2.split(",");
                    for (String s : uefms2s) {
                        FmtStreamMap streamMap = YoutubeParser.parseFmtStreamMap(new Scanner(s), "utf-8");
                        streamMaps.add(streamMap);
                    }
                } else {
                    FmtStreamMap streamMap = YoutubeParser.parseFmtStreamMap(new Scanner(uefms2), "utf-8");
                    streamMaps.add(streamMap);
                }
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

    public static VideoMeta parseYoutubeInfo (String identifier, String response) {
        Log.e(TAG,"parseVideoMeta()......");
        VideoMeta videoMeta = new VideoMeta();
        videoMeta.videoId = identifier;
        Matcher mat = patTitle.matcher(response);
        if (mat.find()) {
            try {
                videoMeta.title = URLDecoder.decode(mat.group(1), "UTF-8");
                mat = patHlsvp.matcher(response);
                if(mat.find()) {
                    videoMeta.isLiveStream = true;
                }
                mat = patAuthor.matcher(response);
                if (mat.find()) {
                    videoMeta.author = URLDecoder.decode(mat.group(1), "UTF-8");
                }
                mat = patChannelId.matcher(response);
                if (mat.find()) {
                    videoMeta.channelId = mat.group(1);
                }
                mat = patLength.matcher(response);
                if (mat.find()) {
                    videoMeta.videoLength = Long.parseLong(mat.group(1));
                }
                mat = patViewCount.matcher(response);
                if (mat.find()) {
                    videoMeta.viewCount = Long.parseLong(mat.group(1));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return videoMeta;
    }

    private static final SparseArray<Format> FORMAT_MAP = new SparseArray<>();

    static {
        // http://en.wikipedia.org/wiki/YouTube#Quality_and_formats

        // Video and Audio
        FORMAT_MAP.put(17, new Format(17, "3gp", 144, Format.VCodec.MPEG4, Format.ACodec.AAC, 24, false));
        FORMAT_MAP.put(36, new Format(36, "3gp", 240, Format.VCodec.MPEG4, Format.ACodec.AAC, 32, false));
        FORMAT_MAP.put(5, new Format(5, "flv", 240, Format.VCodec.H263, Format.ACodec.MP3, 64, false));
        FORMAT_MAP.put(43, new Format(43, "webm", 360, Format.VCodec.VP8, Format.ACodec.VORBIS, 128, false));
        FORMAT_MAP.put(18, new Format(18, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 96, false));
        FORMAT_MAP.put(22, new Format(22, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 192, false));

        // Dash Video
        FORMAT_MAP.put(160, new Format(160, "mp4", 144, Format.VCodec.H264, Format.ACodec.NONE, true));
        FORMAT_MAP.put(133, new Format(133, "mp4", 240, Format.VCodec.H264, Format.ACodec.NONE, true));
        FORMAT_MAP.put(134, new Format(134, "mp4", 360, Format.VCodec.H264, Format.ACodec.NONE, true));
        FORMAT_MAP.put(135, new Format(135, "mp4", 480, Format.VCodec.H264, Format.ACodec.NONE, true));
        FORMAT_MAP.put(136, new Format(136, "mp4", 720, Format.VCodec.H264, Format.ACodec.NONE, true));
        FORMAT_MAP.put(137, new Format(137, "mp4", 1080, Format.VCodec.H264, Format.ACodec.NONE, true));
        FORMAT_MAP.put(264, new Format(264, "mp4", 1440, Format.VCodec.H264, Format.ACodec.NONE, true));
        FORMAT_MAP.put(266, new Format(266, "mp4", 2160, Format.VCodec.H264, Format.ACodec.NONE, true));

        FORMAT_MAP.put(298, new Format(298, "mp4", 720, Format.VCodec.H264, 60, Format.ACodec.NONE, true));
        FORMAT_MAP.put(299, new Format(299, "mp4", 1080, Format.VCodec.H264, 60, Format.ACodec.NONE, true));

        // Dash Audio
        FORMAT_MAP.put(140, new Format(140, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 128, true));
        FORMAT_MAP.put(141, new Format(141, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 256, true));

        // WEBM Dash Video
        FORMAT_MAP.put(278, new Format(278, "webm", 144, Format.VCodec.VP9, Format.ACodec.NONE, true));
        FORMAT_MAP.put(242, new Format(242, "webm", 240, Format.VCodec.VP9, Format.ACodec.NONE, true));
        FORMAT_MAP.put(243, new Format(243, "webm", 360, Format.VCodec.VP9, Format.ACodec.NONE, true));
        FORMAT_MAP.put(244, new Format(244, "webm", 480, Format.VCodec.VP9, Format.ACodec.NONE, true));
        FORMAT_MAP.put(247, new Format(247, "webm", 720, Format.VCodec.VP9, Format.ACodec.NONE, true));
        FORMAT_MAP.put(248, new Format(248, "webm", 1080, Format.VCodec.VP9, Format.ACodec.NONE, true));
        FORMAT_MAP.put(271, new Format(271, "webm", 1440, Format.VCodec.VP9, Format.ACodec.NONE, true));
        FORMAT_MAP.put(313, new Format(313, "webm", 2160, Format.VCodec.VP9, Format.ACodec.NONE, true));

        FORMAT_MAP.put(302, new Format(302, "webm", 720, Format.VCodec.VP9, 60, Format.ACodec.NONE, true));
        FORMAT_MAP.put(308, new Format(308, "webm", 1440, Format.VCodec.VP9, 60, Format.ACodec.NONE, true));
        FORMAT_MAP.put(303, new Format(303, "webm", 1080, Format.VCodec.VP9, 60, Format.ACodec.NONE, true));
        FORMAT_MAP.put(315, new Format(315, "webm", 2160, Format.VCodec.VP9, 60, Format.ACodec.NONE, true));

        // WEBM Dash Audio
        FORMAT_MAP.put(171, new Format(171, "webm", Format.VCodec.NONE, Format.ACodec.VORBIS, 128, true));

        FORMAT_MAP.put(249, new Format(249, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 48, true));
        FORMAT_MAP.put(250, new Format(250, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 64, true));
        FORMAT_MAP.put(251, new Format(251, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 160, true));

        // HLS Live Stream
        FORMAT_MAP.put(91, new Format(91, "mp4", 144 ,Format.VCodec.H264, Format.ACodec.AAC, 48, false, true));
        FORMAT_MAP.put(92, new Format(92, "mp4", 240 ,Format.VCodec.H264, Format.ACodec.AAC, 48, false, true));
        FORMAT_MAP.put(93, new Format(93, "mp4", 360 ,Format.VCodec.H264, Format.ACodec.AAC, 128, false, true));
        FORMAT_MAP.put(94, new Format(94, "mp4", 480 ,Format.VCodec.H264, Format.ACodec.AAC, 128, false, true));
        FORMAT_MAP.put(95, new Format(95, "mp4", 720 ,Format.VCodec.H264, Format.ACodec.AAC, 256, false, true));
        FORMAT_MAP.put(96, new Format(96, "mp4", 1080 ,Format.VCodec.H264, Format.ACodec.AAC, 256, false, true));
    }

}
