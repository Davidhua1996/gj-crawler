package com.gj.web.crawler.pool;

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
}
