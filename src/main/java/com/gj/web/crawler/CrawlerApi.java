package com.gj.web.crawler;

import java.util.List;
import java.util.Queue;

import com.gj.web.crawler.parse.Callback;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.store.StoreStrategy;

public interface CrawlerApi {
	
	public static final int DEFAULT_TIMER_INTERVAL = 5000;
	
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
	 * callback method
	 * @param medias
	 */
	public void mediaDownloaded(Queue<URL> medias);
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
	/**
	 * open the crawler
	 */
	public void open();
	
	public Callback getCallback();
	
	public void setCallback(Callback callback);
	
	public void setCrawlPool(CrawlerThreadPool pool);
	/**
	 * crawl deepth
	 * @return
	 */
	public Integer getMaxDepth();
	
	public void setMaxDepth(Integer maxDepth);
	
	public void setStoreStrategy(StoreStrategy strategy);
}
