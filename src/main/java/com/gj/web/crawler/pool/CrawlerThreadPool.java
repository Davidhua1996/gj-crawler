package com.gj.web.crawler.pool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.pool.exc.ExcReportStore;


/**
 * 
 * @author David
 *
 */
public interface CrawlerThreadPool extends ThreadPool{
	/**
	 * execute the crawler in pool whose ID is 
	 * @param cid
	 */
	void execute(String cid);
	void execute(String cid, byte[] payload);
	/**
	 * execute with params
	 * @param cid
	 * @param params
	 */
	void execute(String cid, Object[] params);
	/**
	 * execute with params
	 * @param cid
	 * @param params
	 */
	void execute(String cid,Map<String,Object> params);
	/**
	 * execute with params and payload
	 * @param cid
	 * @param params
	 * @param payload
	 */
	void execute(String cid, byte[] payload, Object[] params);
	
	void execute(String cid, byte[] payload, Map<String,Object> params);

	void executeWithKeyNot(URL url);
	/**
	 * execute the url
	 * @param url
	 */
	void execute(URL url);
	/**
	 * add crawlers from pool
	 * @return
	 */
//	public Map<String, CrawlerApi> getCrawlers();
	void addCrawler(CrawlerApi crawlerApi);

	Integer getPoolSize();
	
	Integer getMaxFree();
	
	boolean isUseMapDB();
	
	void addMonitor(Monitor monitor);
	
	/**
	 * execute URL, if URL has been crawled
	 * return the local mapping of URL else return null;
	 * @param url
	 * @return the local mapping of URL
	 */
	Object executeIfAbsent(URL url);
	
	int getMaxRetry();
	
	long getWorkQueueLen();
	
	ExcReportStore getExcReportStore();
	
	long getActiveInterval();
	
}
