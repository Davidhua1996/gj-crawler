package com.gj.crawler.dissertation;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.sun.deploy.net.proxy.ProxyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.zip.GZIPInputStream;

public class AgentUnitTest {
    private static final String DEFAULT_CONNECTION = "keep-alive";
    private static final String DEFAULT_ACCEPT_ENCODING = "deflate";
    private static final String DEFAULT_USERAGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36";
    private static final String DEFAULT_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded;charset=UTF-8";
    private static final String DEFAULT_ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9";
    private static final Logger logger = LogManager.getLogger(Crawler.class);
    public static void main(String[] argent) throws Exception{
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        for(int i = 0; i < 10; i++) {
            ProxyUtils.addProxyIfAvailable("219.149.46.151", 3129);
        }
        //"http://gz.xiaozhu.com/ajax.php?op=Ajax_GetDetailComment&lodgeId=2337298663&p=2"
//        for(int i =0; i < 10;i++) {
//            logger.info("OKOK");
//        }
//        ProxyContainer container = ProxyUtils.getContainer();
////        container.addProxy("180.106.16.132",	8118, 1);
//        Runtime.getRuntime().addShutdownHook(new Thread(){
//            @Override
//            public void run() {
//                System.out.println("hello");
//                super.run();
//            }
//        });
//        container.addProxy("113.109.52.176", 8118, 1);
//        container.addProxy("112.114.98.96", 8118, 1);
//        container.addProxy("123.56.169.22", 3128, 1);
//        container.addProxy("59.32.37.197", 8123, 1);
//        container.addProxy("110.73.7.185", 3128, 1);
//        container.addProxy("110.90.184.53", 808, 1);
//        container.addProxy("110.172.220.194", 8080, 1);
//        container.addProxy("61.135.217.7", 80, 1);
//        container.addProxy("180.121.131.176", 8118, 1);
//        container.addProxy("106.111.113.133", 47480, 1);
//        container.addProxy("121.232.146.196", 9000, 1);
//        container.addProxy("111.155.116.210", 8123, 1);
//        container.addProxy("175.15.199.53", 808, 1);
//        URL url = new URL("http://www.baidu.com");
////        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("183.232.188.27",8080));
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("113.5.170.25",8080));
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
//////        conn.setRequestProperty("Host", "gz.xiaozhu.com");
//        conn.setRequestProperty("Connection", DEFAULT_CONNECTION);
//        conn.setRequestProperty("Cache-Control","max-age=0");
//        conn.setRequestProperty("Upgrade-Insecure-Requests","1");
//        conn.setRequestProperty("User-Agent",DEFAULT_USERAGENT);
//        conn.setRequestProperty("Accept",DEFAULT_ACCEPT);
//        conn.setRequestProperty("Accept-Encoding", DEFAULT_ACCEPT_ENCODING);
////        conn.setRequestProperty("Content-Type",CONTENT_TYPE);
//        conn.setRequestProperty("Accept-Language", DEFAULT_ACCEPT_LANGUAGE);
//        conn.setRequestMethod("GET");
//        conn.setDoInput(true);
//        conn.setDoOutput(true);
//        conn.setInstanceFollowRedirects(true);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//        String str = null;
//        while(null !=(str = reader.readLine())){
//            System.out.println(str);
//        }
//        conn.disconnect();
//        System.out.println(conn.getHeaderField("Location"));
//        System.out.println(ProxyUtils.isAvailable("61.135.217.7",80));
    }
}
