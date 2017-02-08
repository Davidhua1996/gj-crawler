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
		Crawler crawler = new Crawler();
		crawler.setUseParams(true);
		crawler.setLazy(true);
		crawler.setPortal("http://so.gamersky.com/all/news?s=#{keyword}&p=#{pageNum}");
		crawler.getAllowURL().add("http://www.gamersky.com/news/\\w+/\\w+.shtml");
		crawler.getParseURL().add("http://www.gamersky.com/news/\\w+/\\w+.shtml");
		crawler.setRestrict("div[class=Mid2_L]");
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
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put("gamersky", crawler);
		CrawlerThreadPool pool = CrawlerThreadPoolImpl.getInstance();
		pool.setCrawlers(crawlers);
		pool.open();
		for(int i = 1;i < 2;i++){
//			pool.execute("bilibili",new Object[]{"纯黑",i});
			pool.execute("gamersky",new Object[]{"饥荒：联机版",i});
		}
	}
}
