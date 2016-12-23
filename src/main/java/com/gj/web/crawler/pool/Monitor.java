package com.gj.web.crawler.pool;
/**
 *  register to pool 
 * @author David
 *
 */
public interface Monitor {
	/**
	 * close the pool
	 */
	public void close(CrawlerThreadPool pool);
	/**
	 * open the pool
	 */
	public void open(CrawlerThreadPool pool);
}
