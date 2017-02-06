package com.gj.web.crawler;

import java.util.List;

import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.basic.URL;

public interface CrawlerApi {
	
	public static final int MAX_CONNECT_THREAD = 10;
	/**
	 * the depth of crawling ,default no limit
	 */
	public static final int DEFAULT_CRAWL_DEPTH = -1;
	/**
	 * HTML
	 * @param url
	 * @return
	 */
	public List<URL> crawlHTML(URL url);
	/**
	 * Medium or picture
	 * @param url
	 * @param storePath
	 */
	public void crawlMedia(URL url,String storePath);
	/**
	 * return the entrance-URL if it is a crawler for searching,
	 * else return null in default
	 * @return
	 */
	public String portal();
	/**
	 * return if is lazy loading
	 * @return
	 */
	public boolean isLazy();
	/**
	 * return the unique identify
	 * @return
	 */
	public Object getId();
	/**
	 * return if it uses parameters 
	 * @return
	 */
	public boolean isUseParams();
	/**
	 * return if it use a simulate browser
	 * @return
	 */
	public boolean isSimulate();
	/**
	 * return Parser
	 * @return
	 */
	public Parser getParser();
}
