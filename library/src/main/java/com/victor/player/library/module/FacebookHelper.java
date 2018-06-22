//package com.victor.player.library.module;
//
//import android.util.Log;
//
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//
//import java.io.IOException;
//
//public class FacebookHelper {
//    private String TAG = "FacebookHelper";
//    public void getPageSourceAfterJs(String url) throws IOException {
//        Log.e(TAG,"getPageSourceAfterJs########################################");
//        WebClient webClient = new WebClient();
//        System.out.println("spider url= " + url);
//        HtmlPage page = webClient.getPage(url);
//        webClient.waitForBackgroundJavaScript(
//                30 * 1000);  //will wait JavaScript to execute up to 30s
//
//        String pageAsXml = page.asXml();
//        webClient.close();
//
//        getMP4Link (pageAsXml);
//    }
//
//    public void getMP4Link(String html) {
//        Log.e(TAG,"getMP4Link #################################################");
//        String[] lines = html.split("\n");
//        for (int i=0;i<lines.length;i++) {
//            if (lines[i].contains("no_ratelimit")) {
//                String[] datas = lines[i].split(",");
//                for (int j=0;j<datas.length;j++) {
//                    if (datas[j].contains("sd_src_no_ratelimit")) {
//                        String sdUrl = datas[j].split("\"")[1];
//                        Log.e(TAG,"sdUrl = " + sdUrl);
//                    }
//                    if (datas[j].contains("hd_src_no_ratelimit")) {
//                        String hddUrl = datas[j].split("\"")[1];
//                        Log.e(TAG,"hddUrl = " + hddUrl);
//                    }
//                }
//            }
//        }
//
//    }
//}
