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
	 void close(CrawlerThreadPool pool);
	/**
	 * <p>monitor the active method which called in a certain interval of time</p>
	 * @param pool
	 */
	 void active(CrawlerThreadPool pool);
	/**
	 * open the pool
	 */
	 void open(CrawlerThreadPool pool);
}
