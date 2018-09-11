package com.gj.crawler.dissertation;

import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.gj.web.crawler.parse.json.JsonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Temporary{
    public static void main(String[] args){
        //119.27.177.169:80
//        for(int i = 0; i < 10; i ++){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for(int i = 0 ;i < 3;i ++) {
//                        System.out.println(JsonUtils.toJson(ProxyUtils.isAvailable("125.62.26.75", 3128), null));
                        System.setProperty("debug", "true");
//                        ProxyUtils.addProxyIfAvailable("61.135.217.7",80);
                        System.out.println(JsonUtils.toJson(ProxyUtils.isAvailable("182.74.200.201",80
                        ,"http://www.baidu.com"), null));
//                    }
//                }
//            }).start();
//        }
//        Pattern pattern = Pattern.compile("(?<=www)[\\s\\S]+");
//        Matcher matcher = pattern.matcher("www.baidu.com");
//        matcher.find();
//        System.out.println(matcher.group());
//        System.out.println(JsonUtils.toJson(ProxyUtils.isAvailable("219.149.46.151",3129, "http://zhuhai.xiaozhu.com/fangzi/22909800.html"), null));
//        String side = "http://gz.xiaozhu.com/ajax.php?op=Ajax_GetDetailComment&lodgeId=2337298663&p=2";
//        System.out.println(side.substring(side.indexOf("lodgeId=")+8, side.lastIndexOf("&")));
    }
}
