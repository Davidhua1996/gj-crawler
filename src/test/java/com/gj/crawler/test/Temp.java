package com.gj.crawler.test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import sun.management.FileSystem;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.lifecycle.Lifecycle;
import com.gj.web.crawler.parse.DefaultHTMLParser;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;

public class Temp {
	public static void main(String[] args) {
		gamersky();
		
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
		DefaultHTMLParser parser0 = new DefaultHTMLParser();
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
		DefaultHTMLParser parser = new DefaultHTMLParser();
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
		CrawlerThreadPool pool = CrawlerThreadPoolImpl.getInstance();
		pool.setCrawlers(crawlers);
		pool.open();
		for(int i = 1;i < 2;i++){
			pool.execute("bilibili",new Object[]{"纯黑",i});
			pool.execute("gamersky",new Object[]{"饥荒：联机版",i});
		}
	}
}
