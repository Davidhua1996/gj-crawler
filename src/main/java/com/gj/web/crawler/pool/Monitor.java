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
	 * <p>monitor the active method which called in a certain interval of time</p>
	 * @param pool
	 */
	public void active(CrawlerThreadPool pool);
	/**
	 * open the pool
	 */
	public void open(CrawlerThreadPool pool);
}
