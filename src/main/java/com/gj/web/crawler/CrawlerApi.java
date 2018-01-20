package com.gj.web.crawler;

import java.util.List;
import java.util.Queue;

import com.gj.web.crawler.http.proxy.ProxyConfig;
import com.gj.web.crawler.parse.Callback;
import com.gj.web.crawler.parse.ParserApi;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.store.StoreStrategy;

public interface CrawlerApi {
	
	String CRAWLER_MEDIA_SCHEDUAL_NAME ="schedual-media-download-";
	
	int DEFAULT_TIMER_INTERVAL = 5000;
	
	int MAX_CONNECT_THREAD = 10;
	/**
	 * the depth of crawling ,default no limit
	 */
	int DEFAULT_CRAWL_DEPTH = -1;
	/**
	 * HTML
	 * @param url
	 * @return
	 */
	List<URL> crawlHTML(URL url);
	/**
	 * Medium or picture
	 * @param url
	 * @param storePath
	 */
	void crawlMedia(URL url,String storePath);
	/**
	 * callback method
	 * @param medias
	 */
	void mediaDownloaded(Queue<URL> medias);
	/**
	 * return the entrance-URL if it is a crawler for searching,
	 * else return null in default
	 * @return
	 */
	String portal();
	/**
	 * return if is lazy loading
	 * @return
	 */
	boolean isLazy();
	/**
	 * return the unique identify
	 * @return
	 */
	Object getId();
	/**
	 * return if it uses parameters 
	 * @return
	 */
	boolean isUseParams();
	/**
	 * return if it use a simulate browser
	 * @return
	 */
	boolean isSimulate();
	/**
	 * return Parser
	 * @return
	 */
	ParserApi getParser();
	/**
	 * open the crawler
	 */
	void open();
	
	Callback getCallback();
	/**
	 * crawl deepth
	 * @return
	 */
	Integer getMaxDepth();

	/**
	 * get proxy configuration
	 * @return
	 */
	ProxyConfig getProxyConfig();
}
