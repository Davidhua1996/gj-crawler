package com.gj.crawler.test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xml.sax.InputSource;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.parse.DefaultHTMLParser;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;


public class CrawlerDemo{
	/**
	 * 编程式调用爬虫样例（以steam为例子）
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		Crawler crawler = new Crawler();//初始化爬虫类
		crawler.setLazy(false);//设置为非懒加载（爬虫池一打开就开始）
		crawler.setPortal("http://store.steampowered.com/search/?sort_by=Released_DESC&tags=-1");//设置入口链接
		//设置允许继续爬取的链接，用于对页面上链接进行匹配，注意为正则表达式，对特殊符号.等用\\.或者[.]
		crawler.getAllowURL().add("http[:]//store[.]steampowered[.]com/search.*");
		crawler.getAllowURL().add("http[:]//store[.]steampowered[.]com/app/.*");
		//设置需要解析的链接，用于对页面上链接进行匹配,也为正则表达式 
		crawler.getParseURL().add("http[:]//store[.]steampowered[.]com/app/.*");
		//设置爬虫需要带的Cookie
		crawler.getCookies().add("mature_content=1");
		crawler.getCookies().add("birthtime=824223601");
		//restrict，顾名思义，限制的意思，只提取页面上限定标签范围内的链接(使得爬虫更有针对性）
		crawler.setRestrict("*[class=search_pagination],div[id=search_result_container]");
		DefaultHTMLParser parser = new DefaultHTMLParser();//初始化HTML解析器
		parser.setDir("/usr/tmp");//设置解析的本地文件夹（用于存储需要下载的文件）
		/**
		 * 设置需要解析的属性，为键值对的形式,键为属性名，值为解析时候用的模式(为json),
		 * 模式（pattern）：
		 * exp:选择表达式
		 * type:属性类型
		 * download:是否下载到本地
		 * identify:是否带唯一标识（会以带唯一标识的属性名创建子文件夹，例：/usr/tmp/{name}）
		 */
		Map<String,String> patterns = new HashMap<String,String>();
		patterns.put("description","{exp:'div[class=game_description_snippet]',type:'text'}");
		patterns.put("picture", "{exp:'img[class=game_header_image_full]',type:'photo',download:'true'}");
		patterns.put("configuration", "{exp:'div[class~=game_area_sys_req sysreq_content.*] div:eq(0)',type:'text'}");
		patterns.put("name", "{exp:'div[class=apphub_AppName]',identify:'true'}");
		patterns.put("description","{exp:'div[class=game_description_snippet]',type:'text',download:'true'}");
		parser.setPatterns(patterns);
		crawler.setParser(parser);
		Map<String,CrawlerApi> crawlers = new HashMap<String,CrawlerApi>();
		crawlers.put("steam", crawler);
		CrawlerThreadPool pool = CrawlerThreadPoolImpl.getInstance();
		pool.setCrawlers(crawlers);
		pool.open();//打开爬虫池
	}
}
