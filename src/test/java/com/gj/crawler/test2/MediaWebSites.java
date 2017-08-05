package com.gj.crawler.test2;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;
import com.gj.web.crawler.store.FileXmlStoreStrategy;
/**
 * parsers:
 *  -content
 *  -summary
 *  -author
 *  -title
 * @author David
 *
 */
public class MediaWebSites {
	public static void main(String[] args){
		camecors();
//		yysaag();
//		chuapp_news();
//		chuapp_pcz();
//		chuapp_daily();
//		vgtime_news();
//		a9vg_news();
//		youxiputao_news();
//		youqudian_news();
//		appgame_news();
//		appgame_reviews();
	}
	private static Crawler prepare(){
		Crawler crawler = new Crawler();
		crawler.setLazy(false);
		crawler.setConnNum(5);
		return crawler;
	}
	private static void execute(String name, Crawler crawler){
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put(name, crawler);
		CrawlerThreadPoolImpl pool = (CrawlerThreadPoolImpl)CrawlerThreadPoolImpl.getInstance();
		pool.setUseMapDB(true);
		pool.setPoolSize(5);
		pool.setCrawlers(crawlers);
		pool.open();
	}
	public static void camecors(){
		Crawler crawler = prepare();
		crawler.setPortal("http://www.g-cores.com/categories/2/originals");
		crawler.getAllowURL().add("/categories/2/originals?page=\\w+");
		crawler.getAllowURL().add("http://www.g-cores.com/articles/\\w+");
		crawler.getParseURL().add("http://www.g-cores.com/articles/\\w+");
		crawler.setRestrict("div[class=container]");
		Parser parser = new Parser();
		parser.setId("g-cores");
		parser.setRootDir("/usr");
		parser.setChildDir("g-cores");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'h1[id=j_title_preview]',type:'text'}");
		patterns.put("summary", "{exp:'p[id=j_desc_preview]',type:'text'}");
		patterns.put("author", "{exp:'p[class=story_user_name]',type:'text'}");
		patterns.put("content", "{exp:'div[class=story]',type:'html',download:true}");
		patterns.put("pubDate", "{exp:'p[class=story_info]',date:'yyyy-MM-dd HH:mm:ss'}");
		parser.setPatterns(patterns);
		parser.setDebug(true);
		crawler.setParser(parser);
		execute("g-cores", crawler);
	}
	public static void yysaag(){
		Crawler crawler = prepare();
		crawler.setSimulate(true);
		crawler.setPortal("https://zhuanlan.zhihu.com/yysaag");
		crawler.getAllowURL().add("/p/[0-9]+");
		crawler.getParseURL().add("https://zhuanlan.zhihu.com/p/\\w+");
		crawler.setRestrict("div[class=Layout-main]");
		Parser parser = new Parser();
		parser.setId("yysaag");
		parser.setRootDir("/usr");
		parser.setChildDir("yysaag");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'h1[class~=PostIndex-title]',type:'text'}");
		patterns.put("summary", "{exp:'div[class~=RichText] u:eq(0)',type:'html'}");
		patterns.put("content", "{exp:'div[class~=RichText]',type:'html'}");
		patterns.put("pubDate", "{exp:\"time\",attr:\"datetime\",date:\"yyyy-MM-dd'T'HH:mm:ss\"}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		execute("yysaag", crawler);
	}
	public static void chuapp_news(){
		Crawler crawler = prepare();
		crawler.setPortal("http://www.chuapp.com/category/news");
		crawler.getAllowURL().add("/article/\\w+\\.html");
		crawler.getParseURL().add("http://www.chuapp.com/article/\\w+\\.html");
		crawler.setRestrict("div[class=category-left fn-left]");
		Parser parser = new Parser();
		parser.setId("chuapp-news");
		parser.setRootDir("/usr");
		parser.setChildDir("chuapp-news");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'div[class~=^single-cout] h1',type:'text'}");
		patterns.put("summary", "{exp:'p[class=(^head-excerpt$)|(^review-excerpt$)]',type:'text'}");
		patterns.put("author", "{exp:'div[class~=author] span',type:'text'}");
		patterns.put("content", "{exp:'div[class=the-content]',type:'html'}");
		patterns.put("pubDate", "{exp:\"span[class=fn-right friendly_time]\",date:\"yyyy年MM月dd日 HH时mm分\"}");
		parser.setPatterns(patterns);
		parser.setDebug(true);
		crawler.setParser(parser);
		execute("chuapp-news", crawler);
	}
	public static void chuapp_pcz(){
		Crawler crawler = prepare();
		crawler.setPortal("http://www.chuapp.com/category/pcz");
		crawler.getAllowURL().add("/article/\\w+\\.html");
		crawler.getParseURL().add("http://www.chuapp.com/article/\\w+\\.html");
		crawler.setRestrict("div[class=category-left fn-left]");
		Parser parser = new Parser();
		FileXmlStoreStrategy xmlStrategy = new FileXmlStoreStrategy();
		xmlStrategy.setIncludes(new String[]{"p","figure"});
		parser.setStoreStrategy(xmlStrategy);
		parser.setId("chuapp-pcz");
		parser.setRootDir("/usr");
		parser.setChildDir("chuapp-pcz");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'div[class~=^single-cout] h1',type:'text'}");
		patterns.put("summary", "{exp:'p[class=(^head-excerpt$)|(^review-excerpt$)]',type:'text'}");
		patterns.put("author", "{exp:'div[class~=author] h4:eq(0)',type:'text'}");
		patterns.put("content", "{exp:'div[class=the-content]',type:'html',download:'true'}");
		patterns.put("pubDate", "{exp:\"span[class=fn-right friendly_time]\",date:\"yyyy年MM月dd日 HH时mm分\"}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		parser.setDebug(true);
		execute("chuapp-pcz", crawler);
	}
	public static void chuapp_daily(){
		Crawler crawler = prepare();
		crawler.setPortal("http://www.chuapp.com/category/daily");
		crawler.getAllowURL().add("/article/\\w+\\.html");
		crawler.getParseURL().add("http://www.chuapp.com/article/\\w+\\.html");
		crawler.setRestrict("div[class=category-left fn-left]");
		Parser parser = new Parser();
		parser.setId("chuapp-daily");
		parser.setRootDir("/usr");
		parser.setChildDir("chuapp-daily");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'div[class~=^single-cont] h1',type:'text'}");
		patterns.put("summary", "{exp:'p[class~=(^head-excerpt$)|(^review-excerpt$)]',type:'text'}");
		patterns.put("author", "{exp:'div[class~=author] span:eq(0)',type:'text'}");
		patterns.put("content", "{exp:'div[class=the-content]',type:'html'}");
		patterns.put("pubDate", "{exp:\"span[class=fn-right friendly_time]\",date:\"yyyy年MM月dd日 HH时mm分\"}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		parser.setDebug(true);
		execute("chuapp-daily", crawler);
	}
	public static void vgtime_news(){
		Crawler crawler = prepare();
		crawler.setSimulate(true);
		crawler.setPortal("http://www.vgtime.com/topic/index.jhtml");
//		crawler.getAllowURL().add("/article/\\w+\\.html");
//		crawler.getParseURL().add("http://www.chuapp.com/article/\\w+\\.html");
//		crawler.setRestrict("div[class=category-left fn-left]");
//		DefaultHTMLParser parser = new DefaultHTMLParser();
//		parser.setId("chuapp-daily");
//		parser.setRootDir("/usr");
//		parser.setChildDir("chuapp-daily");
//		Map<String, String> patterns = new HashMap<String, String>();
//		patterns.put("title", "{exp:'div[class~=^single-cont] h1',type:'text'}");
//		patterns.put("summary", "{exp:'p[class~=(^head-excerpt$)|(^review-excerpt$)]',type:'text'}");
//		patterns.put("author", "{exp:'div[class~=author] span:eq(0)',type:'text'}");
//		patterns.put("content", "{exp:'div[class=the-content]',type:'html'}");
//		patterns.put("pubDate", "{exp:\"span[class=fn-right friendly_time]\",date:\"yyyy年MM月dd日 HH时mm分\"}");
//		parser.setPatterns(patterns);
//		crawler.setParser(parser);
//		parser.setDebug(true);
		execute("chuapp-daily", crawler);
	}
	public static void a9vg_news(){
		Crawler crawler = prepare();
		crawler.setPortal("http://www.a9vg.com/news/");
		crawler.getAllowURL().add("http://www.a9vg.com/\\w+/\\w+.html");
		crawler.getParseURL().add("http://www.a9vg.com/\\w+/\\w+.html");
		crawler.setRestrict("div[class=list]");
		Parser parser = new Parser();
		parser.setId("a9vg-news");
		parser.setRootDir("/usr");
		parser.setChildDir("a9vg-news");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'h1[class=news-title]',type:'text'}");
		patterns.put("summary", "{exp:'div[class=article-body] p:eq(1)',type:'text'}");
		patterns.put("author", "{exp:'span[class=editor] b',type:'text'}");
		patterns.put("content", "{exp:'div[class=article-body]',type:'html',download:true}");
		patterns.put("pubDate", "{exp:\"span[class=pos-time]\",date:\"yyyy-MM-dd HH:mm\"}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		parser.setDebug(true);
		execute("a9vg-news", crawler);
	}
	public static void youxiputao_news(){
		Crawler crawler = prepare();
		crawler.setPortal("http://youxiputao.com/article/index/id/14");
		crawler.getAllowURL().add("/articles/\\w+");
		crawler.getParseURL().add("http://youxiputao.com/articles/\\w+");
		crawler.setRestrict("ul[class=news-list]");
		Parser parser = new Parser();
		parser.setId("youxiputao-news");
		parser.setRootDir("/usr");
		parser.setChildDir("youxiputao-news");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'h2[class=title]',type:'text'}");
		patterns.put("content", "{exp:'div[class=info]',type:'html'}");
		patterns.put("pubDate", "{exp:\"div[class=title-box] div[class=time] b\",date:\"来自 游戏葡萄 yyyy-MM-dd\"}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		parser.setDebug(true);
		execute("youxiputao-news", crawler);
	}
	public static void youqudian_news(){
		Crawler crawler = prepare();
		crawler.setSimulate(true);
		crawler.setPortal("http://zhuanlan.zhihu.com/youqudian");
		crawler.getAllowURL().add("/p/[0-9]+");
		crawler.getParseURL().add("http://zhuanlan.zhihu.com/p/\\w+");
		crawler.setRestrict("div[class=Layout-main]");
		Parser parser = new Parser();
		parser.setId("youqudian");
		parser.setRootDir("/usr");
		parser.setChildDir("youqudian");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'h1[class~=PostIndex-title]',type:'text'}");
		patterns.put("summary", "{exp:'div[class~=RichText] p:eq(0)',type:'html'}");
		patterns.put("content", "{exp:'div[class~=RichText]',type:'html'}");
		patterns.put("pubDate", "{exp:\"time\",attr:\"datetime\",date:\"yyyy-MM-dd'T'HH:mm:ss\"}");
		parser.setPatterns(patterns);
		parser.setDebug(true);
		crawler.setParser(parser);
		execute("youqudian", crawler);
	}
	/**
	 * http://www.appgame.com/archives/category/apple-news
	 */
	public static void appgame_news(){
		Crawler crawler = prepare();
		crawler.setPortal("http://www.appgame.com/archives/category/apple-news");
		crawler.getAllowURL().add("http://www.appgame.com/archives/\\w+.html");
		crawler.getParseURL().add("http://www.appgame.com/archives/\\w+.html");
		crawler.setRestrict("div[class=appgame-items]");
		Parser parser = new Parser();
		FileXmlStoreStrategy xmlStrategy = new FileXmlStoreStrategy();
		xmlStrategy.setIncludes(new String[]{"p"});
		xmlStrategy.setExcludes(new String[]{"r"});
		parser.setStoreStrategy(xmlStrategy);
		parser.setId("appgame-news");
		parser.setRootDir("/usr");
		parser.setChildDir("appgame-news");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'h1[class=appgame-single-title]',type:'text'}");
		patterns.put("author", "{exp:'div[id=commentics-container]',attr:'data-author',type:'text'}");
		patterns.put("content", "{exp:'div[class=appgame-primary]',type:'html',download:'true'}");
		patterns.put("pubDate", "{exp:\"div[id=commentics-container]\",attr:'data-date',date:\"yyyy-MM-dd HH:mm\"}");
		parser.setPatterns(patterns);
		parser.setDebug(true);
		crawler.setParser(parser);
		execute("appgame-news", crawler);
	}
	public static void appgame_reviews(){
		Crawler crawler = prepare();
		crawler.setPortal("http://www.appgame.com/archives/category/game-reviews");
		crawler.getAllowURL().add("http://www.appgame.com/archives/\\w+.html");
		crawler.getParseURL().add("http://www.appgame.com/archives/\\w+.html");
		crawler.setRestrict("div[class=appgame-items]");
		Parser parser = new Parser();
		parser.setId("appgame-reviews");
		parser.setRootDir("/usr");
		parser.setChildDir("appgame-reviews");
		Map<String, String> patterns = new HashMap<String, String>();
		patterns.put("title", "{exp:'h1[class=appgame-single-title]',type:'text'}");
		patterns.put("author", "{exp:'div[id=commentics-container]',attr:'data-author',type:'text'}");
		patterns.put("content", "{exp:'div[class=appgame-primary]',type:'html'}");
		patterns.put("pubDate", "{exp:\"div[id=commentics-container]\",attr:'data-date',date:\"yyyy-MM-dd HH:mm\"}");
		parser.setPatterns(patterns);
		parser.setDebug(true);
		crawler.setParser(parser);
		execute("appgame-reviews", crawler);
	}
}
