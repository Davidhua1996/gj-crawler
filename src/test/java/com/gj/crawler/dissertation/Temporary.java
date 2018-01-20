package com.gj.crawler.dissertation;

import com.gj.web.crawler.parse.json.JsonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

public class Temporary{
    public static void main(String[] args){
//        String json = "{\"ERRORCODE\":\"0\",\"RESULT\":{\"total\":38668,\"rows\":[{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049569,\"ip\":\"180.115.7.138\",\"port\":\"24920\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国江苏省常州市 电信\",\"anony\":\"高匿\",\"responsetime\":\"1.28\",\"validatetime\":\"1分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049568,\"ip\":\"223.214.249.206\",\"port\":\"26078\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国安徽省宣城市 电信\",\"anony\":\"高匿\",\"responsetime\":\"3.93\",\"validatetime\":\"3分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049567,\"ip\":\"115.202.228.22\",\"port\":\"45835\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国浙江省台州市 电信\",\"anony\":\"高匿\",\"responsetime\":\"3.17\",\"validatetime\":\"3分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049566,\"ip\":\"125.126.166.6\",\"port\":\"20463\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国浙江省台州市 电信\",\"anony\":\"高匿\",\"responsetime\":\"2.19\",\"validatetime\":\"2分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049565,\"ip\":\"120.42.120.61\",\"port\":\"39221\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国福建省厦门市 电信\",\"anony\":\"高匿\",\"responsetime\":\"4.76\",\"validatetime\":\"4分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049564,\"ip\":\"115.217.255.75\",\"port\":\"43020\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国浙江省宁波市 电信\",\"anony\":\"高匿\",\"responsetime\":\"1.27\",\"validatetime\":\"1分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049563,\"ip\":\"183.144.207.187\",\"port\":\"27800\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国浙江省绍兴市 电信\",\"anony\":\"高匿\",\"responsetime\":\"3.91\",\"validatetime\":\"3分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049562,\"ip\":\"223.244.127.178\",\"port\":\"32802\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国安徽省淮南市 电信\",\"anony\":\"高匿\",\"responsetime\":\"0.40\",\"validatetime\":\"刚刚\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049561,\"ip\":\"1.199.192.153\",\"port\":\"40063\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国河南省洛阳市 电信\",\"anony\":\"高匿\",\"responsetime\":\"2.22\",\"validatetime\":\"2分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"},{\"page\":0,\"rows\":0,\"startindex\":0,\"sort\":null,\"order\":null,\"sortsql\":null,\"id\":1049560,\"ip\":\"122.4.41.137\",\"port\":\"37774\",\"createTime\":1514000710000,\"invalid\":0,\"position\":\"中国山东省济南市 电信\",\"anony\":\"高匿\",\"responsetime\":\"4.28\",\"validatetime\":\"4分钟前\",\"type\":\"HTTP/HTTPS\",\"post\":\"GET/POST\"}]}}";
//        Map<String, Object> result = JsonUtils.fromJson(json, Map.class);
//        System.out.println(((Map)result.get("RESULT")).get("rows").getClass());
        double ratio = Math.log(0.6)/15000.0;
        double rate = 0.96;
        int count = 0;
        while(rate > 0.5) {
            rate = rate * 0.75;
            count ++;
            System.out.println(rate);
        }
//        System.out.println(x);
//        System.out.println(Math.pow(Math.E , x * 5000));
    }
}
