package com.gj.web.crawler.pool;

import java.util.Map;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.pool.basic.URL;


/**
 * 
 * @author David
 *
 */
public interface CrawlerThreadPool extends ThreadPool{
	/**
	 * add the URL to the pool's buffer queue to crawl
	 * @param task
	 */
	public void execute(URL url);
	/**
	 * get crawlers from pool
	 * @return
	 */
	public Map<String, Crawler> getCrawlers();
	/**
	 * set crawlers into pool
	 * @param crawlers
	 */
	public void setCrawlers(Map<String, Crawler> crawlers);
}
