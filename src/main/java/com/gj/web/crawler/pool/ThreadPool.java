package com.gj.web.crawler.pool;


/**
 * thread pool 's API
 * @author David
 *
 */
public interface ThreadPool {
	/**
	 * create work threads
	 */
	 void open();
	/**
	 * if the pool has been open
	 * @return
	 */
	 boolean isOpen();
	/**
	 * close the pool,the method will not return until 
	 * all the work threads have been destroyed
	 */
	 void shutdown();
}
