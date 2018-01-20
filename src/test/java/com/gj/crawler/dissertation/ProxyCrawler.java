package com.gj.crawler.dissertation;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.gj.web.crawler.parse.DefaultCallback;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.parse.ParserApi;
import com.gj.web.crawler.parse.ResultModel;
import com.gj.web.crawler.parse.json.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyCrawler{
    public static Crawler XICI;
    public static Crawler GITHUB_PROXY;
    static{
        XICI = XICI();
        GITHUB_PROXY = GITHUB_PROXY();
    }
    private static Crawler GITHUB_PROXY(){
        Crawler crawler = custom();
        crawler.setPortal(Portal.GITHUB_PROXY.val());
        crawler.getParseURL().add(Portal.GITHUB_PROXY.val());
        Parser p = new Parser();
        p.setId("proxy-github");
        p.setRootDir("/crawlers");
        p.setChildDir("/proxy");
        p.setJson(true);
        p.setCallback(new DefaultCallback(){
            @Override
            public Object resolve(ResultModel result) {
                String json = result.getString(ParserApi.JSON_KEY);
                Pattern pattern = Pattern.compile("\\{[^}]*\\}?");
                Matcher matcher = pattern.matcher(json);
                while(matcher.find()){
                    Map<String, Object> map = JsonUtils.fromJson(matcher.group(), Map.class);
                    String country = (String)map.get("country");
                    String anonymity = (String)map.get("anonymity");
                    if((country.equalsIgnoreCase("CN") ||
                            country.equalsIgnoreCase("HK")) &&
                            anonymity.matches("(high_anonymous)|(anonymous)")){
                        String host = (String) map.get("host");
                        Object port = map.get("port");
                        ProxyUtils.addProxyIfAvailable(host,
                                port instanceof Integer?(Integer)port:Integer.valueOf((String)port));
                    }
                }
                return null;
            }

            @Override
            public synchronized void persist(List<Object> store) {
            }
        });
        p.setDebug(true);
        crawler.setParser(p);
        return crawler;
    }
    private static Crawler XICI(){
        Crawler crawler = custom();
        crawler.setPortal(Portal.XICI.val());
        crawler.getParseURL().add(Portal.XICI.val());
        Parser p = new Parser();
        p.setId("proxy-xici");
        p.setRootDir("/crawlers");
        p.setChildDir("/proxy");
        Map<String, String> patterns = new HashMap<String, String>();
        patterns.put("hostnameArr", "{exp:'table[id=ip_list] tr td:eq(1)',type:'text'}");
        patterns.put("ipArr", "{exp:'table[id=ip_list] tr td:eq(2)',type:'text'}");
        patterns.put("name","{exp:'title',type:'text'}");
        p.setPatterns(patterns);
        p.setDebug(true);
        p.setCallback(new DefaultCallback(){
            @Override
            public Object resolve(ResultModel result) {
               Object[] hostnameArr = result.getInnerMap().get("hostnameArr");
               Object[] ipArr = result.getInnerMap().get("ipArr");
               int size = hostnameArr.length;
               for(int i = 0 ; i < 20; i++){
                   try {
                       String hostname = (String)hostnameArr[i];
                       int ip = Integer.valueOf((String)ipArr[i]);
                       ProxyUtils.addProxyIfAvailable(hostname, ip);
                   }catch(Exception e){
                       continue;
                   }
               }
               return null;
            }
            @Override
            public synchronized void persist(List<Object> store) {
            }
        });
        p.setDebug(true);
        crawler.setParser(p);
        return crawler;
    }
    private static Crawler custom(){
        Crawler crawler = new Crawler();
        crawler.setLazy(false);
        crawler.setConnNum(1);
        crawler.setUseProxy(false);
        return crawler;
    }
    public enum Portal{
        XICI("http://www.xicidaili.com/nn"),
        GITHUB_PROXY("https://raw.githubusercontent.com/fate0/proxylist/master/proxy.list");
        private String url;
        Portal(String url){
            this.url = url;
        }
        public String val(){
            return url;
        }
    }
}
