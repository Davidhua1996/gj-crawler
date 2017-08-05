package com.gj.crawler.test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sun.management.FileSystem;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.lifecycle.Lifecycle;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;

public class Temp {
	public static void main(String[] args) throws Exception{
//		youxiguancha();
//		steamsales();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.parse("2017-04-26T15:16:21.000Z");
//		System.out.println(format.format(new Date()));
	}
	public static void youxiguancha(){
		Crawler crawler0 = new Crawler();//初始化爬虫类
		crawler0.setUseParams(false);//设置使用参数
		crawler0.setLazy(false);//懒加载
//		crawler.setSimulate(true);//设置为模拟浏览器形式
		//#{parameter}为需要注入的属性
		crawler0.setPortal("http://www.youxiguancha.com/hangyezixun");
		crawler0.getAllowURL().add("/hangyezixun/\\w+.html");
		crawler0.getParseURL().add("http://www.youxiguancha.com/hangyezixun/\\w+.html");
		crawler0.setRestrict("ul[class=main_list]");
		crawler0.setConnNum(3);
		crawler0.setMaxDepth(2);
		Parser parser0 = new Parser();
		parser0.setId("youxiguancha-info");
		parser0.setRootDir("/usr");//根目录
		parser0.setChildDir("/youxiguancha-info");//子目录
		Map<String,String> patterns0 = new HashMap<String,String>();
		patterns0.put("title", "{exp:'h1[class=tit]'}");
		patterns0.put("date", "{exp:'div[class=info] em:eq(0)'}");
		patterns0.put("summary","{exp:'div[class=detail_c]',type:'html',download:'true'}");
		parser0.setPatterns(patterns0);
		crawler0.setParser(parser0);
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put("youxiguancha-info", crawler0);
		CrawlerThreadPoolImpl pool = (CrawlerThreadPoolImpl)CrawlerThreadPoolImpl.getInstance();
		pool.setCrawlers(crawlers);
		pool.open();
	}
	public static void steamsales(){
		Crawler crawler0 = new Crawler();//初始化爬虫类
		crawler0.setUseParams(false);//设置使用参数
		crawler0.setLazy(false);//懒加载
		crawler0.setSimulate(true);//设置为模拟浏览器形式
		//#{parameter}为需要注入的属性
		crawler0.setPortal("https://steamdb.info/sales/");
//		crawler0.getAllowURL().add("/hangyezixun/\\w+.html");
		crawler0.getParseURL().add("https://steamdb.info/sales/");
//		crawler0.setRestrict("ul[class=main_list]");
		crawler0.setConnNum(3);
		crawler0.setMaxDepth(2);
		Parser parser0 = new Parser();
		parser0.setId("steams-sales");
		parser0.setRootDir("/usr");//根目录
		parser0.setChildDir("/steams-sales");//子目录
		Map<String,String> patterns0 = new HashMap<String,String>();
		patterns0.put("content", "{exp:'div[id=sales-section-daily-deal]',type:'html'}");
		parser0.setPatterns(patterns0);
		crawler0.setParser(parser0);
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put("steams-sales", crawler0);
		CrawlerThreadPoolImpl pool = (CrawlerThreadPoolImpl)CrawlerThreadPoolImpl.getInstance();
		pool.setCrawlers(crawlers);
		pool.setPoolSize(5);
		pool.open();
	}
	public static void gamersky(){
		Crawler crawler0 = new Crawler();//初始化爬虫类
		crawler0.setUseParams(true);//设置使用参数
		crawler0.setLazy(true);//懒加载
//		crawler.setSimulate(true);//设置为模拟浏览器形式
		//#{parameter}为需要注入的属性
		crawler0.setPortal("http://search.bilibili.com/all?keyword=#{keyword}&page=#{pageNum}&order=totalrank&tids_1=4");
		crawler0.getAllowURL().add("//www.bilibili.com/video/av\\w+");
		crawler0.getParseURL().add("http://www.bilibili.com/video/av\\w+");
		crawler0.setRestrict("ul[class=ajax-render]");
		crawler0.setConnNum(1);
		Parser parser0 = new Parser();
		parser0.setId("bilibili");
		parser0.setRootDir("/usr");//根目录
		parser0.setChildDir("/bilibili");//子目录
		Map<String,String> patterns0 = new HashMap<String,String>();
		patterns0.put("title", "{exp:'div[class=v-title] h1',type:'text'}");
		patterns0.put("date", "{exp:'time i'}");
		patterns0.put("playUrl","{exp:'meta[itemprop=embedURL]',attr:'content'}");
		parser0.setPatterns(patterns0);
		crawler0.setParser(parser0);
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put("bilibili", crawler0);
		Crawler crawler = new Crawler();
		crawler.setUseParams(true);
		crawler.setLazy(true);
		crawler.setPortal("http://so.gamersky.com/all/news?s=#{keyword}&p=#{pageNum}");
		crawler.getAllowURL().add("http://www.gamersky.com/news/\\w+/\\w+.shtml");
		crawler.getParseURL().add("http://www.gamersky.com/news/\\w+/\\w+.shtml");
		crawler.setRestrict("div[class=Mid2_L]");
		crawler.setConnNum(1);
		Parser parser = new Parser();
		parser.setId("gamersky");
		parser.setRootDir("/usr");
		parser.setChildDir("/gamersky");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title","{exp:'div[class=Mid2L_tit] h1'}");
		patterns.put("details", "{exp:'div[class=detail]'}");
		patterns.put("summary", "{exp:'div[class=Mid2L_con] p:eq(0)',type:'text'}");
//		patterns.put("content", "{exp:'div[class=Mid2L_con]',type:'html'}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		crawlers.put("gamersky", crawler);
		CrawlerThreadPoolImpl pool = (CrawlerThreadPoolImpl)CrawlerThreadPoolImpl.getInstance();
		pool.setCrawlers(crawlers);
		pool.open();
		for(int i = 1;i < 2;i++){
			pool.execute("bilibili",new Object[]{"纯黑",i});
			pool.execute("gamersky",new Object[]{"饥荒：联机版",i});
		}
	}
}
