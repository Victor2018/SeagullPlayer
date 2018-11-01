package com.victor.player.library.util;

import android.text.TextUtils;

import com.victor.player.library.data.SubTitleInfo;
import com.victor.player.library.data.SubTitleListInfo;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by longtv, All rights reserved.
 * -----------------------------------------------------------------
 * File: SubTitleParser.java
 * Author: Victor
 * Date: 2018/10/26 16:17
 * Description:
 * -----------------------------------------------------------------
 */
public class SubTitleParser {
    public static SubTitleListInfo parseSubTitleList (String result) {
        HashMap<Integer,SubTitleListInfo> datas = new HashMap<>();
        if (TextUtils.isEmpty(result)) return null;
        SubTitleListInfo defaultLan = null;
        SAXReader reader = new SAXReader();
        Document doc = null;
        try {
            doc = reader.read(new ByteArrayInputStream(result.getBytes("utf-8")));
            Element root = doc.getRootElement();
//            System.out.println("docid = " + root.attributeValue("docid"));
            Iterator<Element> iterator = root.elementIterator("track");
            int key = 0;
            while (iterator.hasNext()){
                Element e = iterator.next();
                SubTitleListInfo data = new SubTitleListInfo();
                data.id = Integer.parseInt(e.attributeValue("id"));
                data.name = e.attributeValue("name");
                data.lang_code = e.attributeValue("lang_code");
                data.lang_original = e.attributeValue("lang_original");
                data.lang_translated = e.attributeValue("lang_translated");
                data.lang_default = Boolean.parseBoolean(e.attributeValue("lang_default"));

                if (data.lang_default) {
                    defaultLan = data;
                }
                datas.put(key, data);
                key++;
            }
            if (key == 1) {
                defaultLan = datas.get(0);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return defaultLan;
    }

    public static HashMap<Integer,SubTitleInfo> parseSubTitle (String result) {
        HashMap<Integer,SubTitleInfo> datas = new HashMap<>();
        if (TextUtils.isEmpty(result)) return datas;
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new ByteArrayInputStream(result.getBytes("utf-8")));
            Element root = doc.getRootElement();
            Iterator<Element> iterator = root.elementIterator("text");
            int key = 0;
            while (iterator.hasNext()){
                Element e = iterator.next();
                SubTitleInfo data = new SubTitleInfo();
                data.beginTime = (int) (Double.parseDouble(e.attributeValue("start")) * 1000);
                data.endTime = data.beginTime + (int) (Double.parseDouble(e.attributeValue("dur")) * 1000);
                String subTitle = e.getStringValue();
                if (!TextUtils.isEmpty(subTitle)) {
                    subTitle = subTitle.replaceAll("&quot;", "\"");
                    subTitle = subTitle.replaceAll("&amp;", "&");
                    subTitle = subTitle.replaceAll("&#39;", "'");
                    subTitle = subTitle.replaceAll("&lt;", "<");
                    subTitle = subTitle.replaceAll("&gt;", ">");
                    data.srtBody = subTitle;
                }
                datas.put(key, data);
                key++;
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return datas;
    }
}
