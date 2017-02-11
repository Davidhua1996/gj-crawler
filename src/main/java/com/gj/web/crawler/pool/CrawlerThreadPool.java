package com.gj.web.crawler.pool;

import java.util.List;
import java.util.Map;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.pool.basic.URL;


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
	/**
	 * set crawlers into pool
	 * @param crawlers
	 */
	public void setCrawlers(Map<String, CrawlerApi> crawlers);
	/**
	 * set the max size of pool
	 * @param size
	 */
	public void setPoolSize(Integer size);
	
	public Integer getPoolSize();
	/**
	 * set the max free time of pool
	 * @param free
	 */
	public void setMaxFree(Integer free);
	
	public Integer getMaxFree();
	/**
	 * switch of mapDB
	 * @param DB
	 */
	public void setUseMapDB(boolean DB);
	
	public boolean isUseMapDB();
	
	public List<Monitor> getMonitors();
	
	public void setMonitors(List<Monitor> monitors);
	/**
	 * execute URL, if URL has been crawled
	 * return the local mapping of URL else return null;
	 * @param url
	 * @return the local mapping of URL
	 */
	public Object executeIfAbsent(URL url);
	
	public int getMaxRetry(); 
	
	public void setMaxRetry(int maxRetry);
}
