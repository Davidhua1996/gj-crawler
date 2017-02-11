package com.gj.web.crawler.parse;

import java.util.List;
import java.util.Queue;

import com.gj.web.crawler.pool.basic.URL;

/**
 * callback method of parsing or crawling
 * @author David
 *
 */
public interface Callback {
	
	/**
	 * callback method
	 * @param result
	 * @return the object resolved
	 */
	Object resolve(ResultModel result);
	/**
	 * persist method,persist the resolved object in-memory
	 * @param store
	 */
	void persist(List<Object> store);
	/**
	 * the notice of the download medias 
	 * @param medias
	 * @return Notification failure medias
	 */
	List<URL> mediaDownloaded(Queue<URL> medias);
}
