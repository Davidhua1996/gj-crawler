package com.gj.crawler.dissertation;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.gj.web.crawler.parse.DefaultCallback;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.parse.ResultModel;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;
import com.gj.web.crawler.pool.basic.URL;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * xiaozhu网租房爬虫单元测试（xiaozhu网）
 */
public class RentUnitTest {
    //xiaozhu网爬虫公用一个请求锁,防止并发被ban
    private static final ReentrantLock shareLock = new ReentrantLock();
    private static final AtomicInteger num = new AtomicInteger();
    private static CrawlerThreadPool pool;
    public static void main(String[] args){
        initProxyPool();
        pool = CrawlerThreadPoolImpl.getInstance(true , 3);
        Map<String,CrawlerApi> crawlers = pool.getCrawlers();
        crawlers.put("xiaozhu-fangzi", performRent());
        crawlers.put("xiaozhu-fangdong", performOwn());
        crawlers.put("xiaozhu-cmt", performCmt());
//        pool.setDereplicate(false);//关闭url去重，即允许url重复
        pool.open();
//        pool.execute(new URL("xiaozhu-fangzi", "http://gz.xiaozhu.com/fangzi/21994257701.html"));
        pool.execute(new URL("xiaozhu-cmt", "http://gz.xiaozhu.com/ajax.php?op=Ajax_GetDetailComment&lodgeId=2337298663&p=2"));
    }
    private static void initProxyPool(){
        final CrawlerThreadPool proxyPool = CrawlerThreadPoolImpl.newInstance("proxy", true, 5);
        proxyPool.getCrawlers().put("proxy-xici", ProxyCrawler.XICI);
        proxyPool.getCrawlers().put("proxy-github", ProxyCrawler.GITHUB_PROXY);
        Timer timer = new Timer("PROXY_CRAWL_TIMER");
        TimerTask xici = new TimerTask() {
            @Override
            public void run() {
                if(!proxyPool.isOpen()){
                    proxyPool.open();
                    return;
                }
                proxyPool.execute("proxy-xici");
            }
        };
        TimerTask github = new TimerTask() {
            @Override
            public void run() {
                if(!proxyPool.isOpen()){
                    proxyPool.open();
                    return;
                }
                proxyPool.execute("proxy-github");
            }
        };
        proxyPool.open();
        timer.schedule(github, 60000*15, 60000*15);
        timer.schedule(xici, 60000*15, 60000*15);
    }
    //房客评论信息爬虫
    public static Crawler performCmt(){
        Crawler cmt = custom();
        cmt.setUseParams(true);
        cmt.getParseURL().add("http://\\w+\\.xiaozhu.com/ajax.php[?]op=Ajax_GetDetailComment&lodgeId=\\w+&p=\\w+");
        Parser p = new Parser();
        p.setId("xiaozhu-cmt");
        p.setRootDir("/crawlers");
        p.setChildDir("/zhuzhu");
        Map<String, String> patterns = new HashMap<String, String>();
        patterns.put("ids", "{exp:'div[class=dp_con] h6',attr:'data-id',type:'text'}");
        patterns.put("urls", "{exp:'div[class=dp_box clearfix mt_10] > a',attr:'href',type:'text'}");
        patterns.put("names", "{exp:'div[class=dp_con] h6 a span',type:'text'}");
        patterns.put("contents", "{exp:'div[class=dp_con]',type:'text'}");
        patterns.put("next", "{exp:'span[class=active_link]+a', type:'text'}");
        p.setPatterns(patterns);
        p.setCallback(new DefaultCallback(){
            @Override
            public Object resolve(ResultModel result) {
                try {
                    byte[] payload = (byte[])result.getInnerMap().get("_payload")[0];
                    String  url = result.getString("_url");
                    String next = result.getString("next");
                    Object[] urls = result.getInnerMap().get("urls");
                    if(null != next){
                        //因为评论抓取的html里面不带有纯链接，所以编码驱动评论爬虫的进一步抓取
                        pool.execute(new URL("xiaozhu-cmt",
                                url.substring(0, url.lastIndexOf("&")) +"&p="+next,payload));
                    }
                    if(null != urls){
                        //将房客主页链接交给房东信息爬虫去爬取探测
                        for(int i = 0; i < urls.length; i ++){
                            pool.execute(new URL("xiaozhu-fangdong",
                                    String.valueOf(urls[i]),payload));
//                            System.out.println("这里:"+String.valueOf(urls[i]));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return super.resolve(result);
            }

            @Override
            public synchronized void persist(List<Object> store) {
//                super.persist(store);
            }
        });
        p.setDebug(true);
        cmt.setParser(p);
        return cmt;
    }
    //租房信息爬虫
    public static Crawler performRent(){
        Crawler rent = custom();
        rent.setUseParams(true);
        rent.getAllowURL().add("http://\\w+\\.xiaozhu.com/fangzi/\\w+\\.html");
        rent.getParseURL().add("http://\\w+\\.xiaozhu.com/fangzi/\\w+\\.html");
        Parser p = new Parser();
        p.setId("xiaozhu-fangzi");
        p.setRootDir("/crawlers");
        p.setChildDir("/zhuzhu");
        Map<String, String> patterns = new HashMap<String, String>();
        patterns.put("title", "{exp:'div[class=pho_info] h4:eq(0) em',type:'text'}");
        patterns.put("labels", "{exp:'div[class=labels] span',type:'text'}");
        patterns.put("price", "{exp:'div[class=day_l] span', type:'text'}");
        patterns.put("houseInfo", "{exp:'ul[class~=house_info] li p', type:'text'}");
        patterns.put("info","{exp:'div[class=intro_item_content] p', type:'text'}");
        patterns.put("owner", "{exp:'a[class=lorder_name]', type:'text'}");
        patterns.put("ownerURL", "{exp:'a[class=lorder_name]', attr:'href', type:'text'}");
        patterns.put("id", "{exp:'input[id=lodgeUnitId]', attr:'value', type:'text'}");
        p.setPatterns(patterns);
        p.setCallback(new DefaultCallback(){
            @Override
            public Object resolve(ResultModel result) {
                try {
                    byte[] payload = (byte[])result.getInnerMap().get("_payload")[0];
                    String prev = null != payload?new String(payload):null;
                    payload = result.getString("_url").getBytes("UTF-8");
                    String ownerURL = result.getString("ownerURL");
                    if(null != ownerURL && (null == prev || !prev.equals(ownerURL))){
                        pool.execute(new URL("xiaozhu-fangdong", ownerURL,
                                payload));//添加入房东信息的爬虫队列
                     }
                    pool.execute(new URL("xiaozhu-cmt",
                            "http://gz.xiaozhu.com/ajax.php?op=Ajax_GetDetailComment&lodgeId="+result.getString("id")+"&p=1", payload));//添加入抓取评论信息的爬虫队列
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return super.resolve(result);
            }

            @Override
            public synchronized void persist(List<Object> store) {
//                super.persist(store);
            }
        });
        p.setDebug(true);
        rent.setParser(p);
        return rent;
    }
    //房东信息爬虫
    public static Crawler performOwn(){
        Crawler own = custom();
        own.getAllowURL().add("http://www.xiaozhu.com/fangdong/\\w+/");
        own.getParseURL().add("http://www.xiaozhu.com/fangdong/\\w+/");
        Parser p = new Parser();
        p.setId("xiaozhu-fangdong");
        p.setRootDir("/crawlers");
        p.setChildDir("/zhuzhu");
        Map<String, String> patterns = new HashMap<String, String>();
        patterns.put("name", "{exp:'div[class=fd_infor] h1:eq(0)',type:'text'}");
        patterns.put("source", "{exp:'div[class=fd_infor] ul li:eq(0) span',type:'text-all'}");
        patterns.put("reply", "{exp:'div[class=fd_infor] ul li:eq(1) span',type:'text-all'}");
        patterns.put("comment", "{exp:'div[class=fd_infor] ul li:eq(2) span',type:'text-all'}");
        patterns.put("time", "{exp:'div[class=fd_infor] ul li:eq(3) span',type:'text-all'}");
        patterns.put("order", "{exp:'div[class=fd_infor] ul li:eq(4) span',type:'text-all'}");
        patterns.put("order_", "{exp:'div[class=fd_infor] ul li:eq(5) span',type:'text-all'}");
        patterns.put("rooms", "{exp:'dl[class=fd_room] a', attr:'href', type:'text'}");
        p.setPatterns(patterns);
        p.setDebug(true);
        p.setCallback(new DefaultCallback(){
            @Override
            public Object resolve(ResultModel result) {
                try {
                    byte[] payload = (byte[])result.getInnerMap().get("_payload")[0];
                    String prev = null != payload?new String(payload):null;
                    Object[] rooms= result.getInnerMap().get("rooms");
                    payload = result.getString("_url").getBytes("UTF-8");
                    if(null != rooms) {
                        for (int i = 0; i < rooms.length; i++) {
                            if (null != prev && prev.equals(rooms[i])) {
                                continue;
                            }
                            //添加入租房信息的爬虫队列
                            pool.execute(new URL("xiaozhu-fangzi", String.valueOf(rooms[i]), payload));
//                            System.out.println("这里房:"+String.valueOf(rooms[i]));
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch(Exception e){
                    e.printStackTrace();
                }
                return super.resolve(result);
            }

            @Override
            public synchronized void persist(List<Object> store) {
//                super.persist(store);
            }
        });
        own.setParser(p);
        return own;
    }
    private static Crawler custom(){
        Crawler crawler = new Crawler(shareLock, num);
        crawler.setLazy(true);
        crawler.setConnNum(3);
        crawler.setUseProxy(true);
        crawler.getProxyConfig().addForbiddenTime("403", 60000*40);
        crawler.getProxyConfig().addForbiddenTime("503", 60000*40);
        return crawler;
    }
}
