package com.gj.web.crawler.delay;
/**
 * the consumer of delay queue
 * @author David
 *
 */
public interface DelayConsumer {
	/**
	 * consume method
	 * @param delay
	 */
	public void consume(CrawlerDelayTask delayTask);
}
