package com.gj.web.crawler;

import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;

/**
 * the executor of all the crawlers,
 * actually, it is an extension of CrawlerThreadPoolImpl
 * and a bridge between crawler component and other framework like 'Spring'
 * TODO 和其他框架的接口做适配 
 * @author David
 *
 */
public class CrawlerExecutor{
	
	private CrawlerThreadPool pool = CrawlerThreadPoolImpl.getInstance();
}
