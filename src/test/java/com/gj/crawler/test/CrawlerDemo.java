package com.gj.crawler.test;

import java.util.HashMap;
import java.util.Map;
import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;
import com.gj.web.crawler.utils.InjectUtils;


public class CrawlerDemo{
	/**
	 * 编程式调用爬虫样例1:固定的入口链接（以steam为例子）
	 * 
	 * 
	 */
	public static void crawler1(){
		Crawler crawler = new Crawler();//初始化爬虫类
		crawler.setLazy(false);//设置为非懒加载（爬虫池一打开就开始）
//		crawler.setSimulate(true);
		crawler.setConnNum(5);
		crawler.setPortal("http://store.steampowered.com/search/?sort_by=Released_DESC&tags=-1");//设置入口链接
		//设置允许继续爬取的链接，用于对页面上链接进行匹配，注意为正则表达式，对特殊符号.等用\\.或者[.]
		crawler.getAllowURL().add("http[:]//store[.]steampowered[.]com/search.*");
		crawler.getAllowURL().add("http[:]//store[.]steampowered[.]com/app/.*");
		//设置需要解析的链接,这里用于对当前地址栏的链接进行匹配，也为正则表达式 
		crawler.getParseURL().add("http[:]//store[.]steampowered[.]com/app/.*");
		//设置爬虫需要带的Cookie
		crawler.getCookies().add("mature_content=1");
		crawler.getCookies().add("birthtime=824223601");
		//restrict，顾名思义，限制的意思，只提取页面上限定标签范围内的链接(使得爬虫更有针对性）
		crawler.setRestrict("*[class=search_pagination],div[id=search_result_container]");
		Parser parser = new Parser();//初始化HTML解析器
		parser.setId("steam");
		//设置解析的本地文件夹目录（用于存储需要下载的文件）
		parser.setRootDir("/usr");//根目录
		parser.setChildDir("/steam");//子目录
		/**
		 * 设置需要解析的属性，为键值对的形式,键为属性名，值为解析时候用的模式(为json),
		 * 模式（pattern）：
		 * exp:选择表达式
		 * type:属性类型
		 * download:是否下载到本地
		 * identify:是否带唯一标识（会以带唯一标识的属性名创建子文件夹，例：/usr/tmp/{name}）
		 */
		Map<String,String> patterns = new HashMap<String,String>();
//		patterns.put("description","{exp:'div[class=game_description_snippet]',type:'text'}");
//		patterns.put("picture", "{exp:'img[class=game_header_image_full]',type:'photo',download:'true'}");
//		patterns.put("configuration", "{exp:'div[class~=game_area_sys_req sysreq_content.*] div:eq(0)',type:'text'}");
		patterns.put("name", "{exp:'div[class=apphub_AppName]',identify:'false'}");
		patterns.put("description","{exp:'div[class=game_description_snippet]',type:'text'}");
		patterns.put("pub", "{exp:'a[href~=http[:]//store[.]steampowered[.]com/search/[?]publisher=.*]',type:'text'}");
		patterns.put("pub_date_str", "{exp:'div[class=release_date] span'}");
//		patterns.put("price", "{exp:'div[class=discount_prices]',type:'text'}");
		patterns.put("cn_support_str", "{exp:'b:contains(不支持简体中文)'}");
//		patterns.put("","");
//		patterns.put("content", "{exp:'div[id=highlight_strip]',type:'html',download:'true'}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put("steam", crawler);
		CrawlerThreadPoolImpl pool = (CrawlerThreadPoolImpl)CrawlerThreadPoolImpl.getInstance();
		pool.setUseMapDB(true);
		pool.setPoolSize(5);
		pool.setCrawlers(crawlers);
		pool.open();//打开爬虫池
	}
	/**
	 * 编程式调用爬虫样例2:不固定的入口链接（以BiliBili为例子）
	 * 
	 * 
	 */
	private static void crawler2(){
		Crawler crawler = new Crawler();//初始化爬虫类
		crawler.setUseParams(true);//设置使用参数
		crawler.setLazy(true);//懒加载
//		crawler.setSimulate(true);//设置为模拟浏览器形式
		//#{parameter}为需要注入的属性
		crawler.setPortal("http://search.bilibili.com/all?keyword=#{keyword}&page=#{pageNum}&order=totalrank&tids_1=4");
//		crawler.setPortal("http://space.bilibili.com/423895/?_escaped_fragment_=/index");
		crawler.getAllowURL().add("//www.bilibili.com/video/av\\w+");
		crawler.getParseURL().add("http://www.bilibili.com/video/av\\w+");
		crawler.setRestrict("ul[class=ajax-render]");
//		crawler.setRestrict("div[class=content clearfix]");
		Parser parser = new Parser();
		parser.setRootDir("/usr");//根目录
		parser.setChildDir("/bilibili");//子目录
		Map<String,String> patterns = new HashMap<String,String>();
		patterns.put("title", "{exp:'div[class=v-title] h1',type:'text'}");
		patterns.put("date", "{exp:'time i'}");
		patterns.put("playUrl","{exp:'meta[itemprop=embedURL]',attr:'content'}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put("bilibili", crawler);
		CrawlerThreadPoolImpl pool = (CrawlerThreadPoolImpl)CrawlerThreadPoolImpl.getInstance();
		pool.setCrawlers(crawlers);
		pool.open();
		for(int i = 1;i<10;i++){
			pool.execute("bilibili",new Object[]{"纯黑",i});
//			pool.execute("bilibili",new Object[]{});
		}
	}
	public static void main(String[] args) {
		crawler2();
	}
}
