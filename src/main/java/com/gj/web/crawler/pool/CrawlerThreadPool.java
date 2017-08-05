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
	 * @param task
	 */
	public void execute(String cid);
	public void execute(String cid, byte[] payload);
	/**
	 * execute with params
	 * @param cid
	 * @param obj
	 */
	public void execute(String cid, Object[] params);
	/**
	 * execute with params
	 * @param cid
	 * @param params
	 */
	public void execute(String cid,Map<String,Object> params);
	/**
	 * execute with params and payload
	 * @param cid
	 * @param params
	 * @param payload
	 */
	public void execute(String cid, byte[] payload, Object[] params);
	
	public void execute(String cid, byte[] payload, Map<String,Object> params);
	/**
	 * execute the url
	 * @param task
	 */
	public void execute(URL url);
	/**
	 * get crawlers from pool
	 * @return
	 */
	public Map<String, CrawlerApi> getCrawlers();
	
	public Integer getPoolSize();
	
	public Integer getMaxFree();
	
	public boolean isUseMapDB();
	
	public List<Monitor> getMonitors();
	
	/**
	 * execute URL, if URL has been crawled
	 * return the local mapping of URL else return null;
	 * @param url
	 * @return the local mapping of URL
	 */
	public Object executeIfAbsent(URL url);
	
	public int getMaxRetry(); 
	
	public long getWorkQueueLen();
	
	public ExcReportStore getExcReportStore();
	
	public long getActiveInterval();
	
}
