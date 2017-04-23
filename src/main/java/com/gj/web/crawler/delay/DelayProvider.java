package com.gj.web.crawler.delay;

import java.util.List;

/**
 * the provider of delay queue
 * @author David
 *
 */
public interface DelayProvider {
	/**
	 * the interval of calling 
	 * @return
	 */
	public long interval();
	/**
	 * provide method
	 * @return
	 */
	public List<CrawlerDelay> provide();
}
