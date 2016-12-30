package com.gj.web.crawler;

import java.util.List;

import com.gj.web.crawler.pool.basic.URL;

public interface CrawlerApi {
	/**
	 * HTML
	 * @param url
	 * @return
	 */
	public List<URL> crawlHTML(URL url);
	/**
	 * Medium or picture
	 * @param url
	 * @param storePath
	 */
	public void crawlMedium(URL url,String storePath);
	/**
	 * return the entrance-URL if it is a crawler for searching,
	 * else return null in default
	 * @return
	 */
	public String entrance();
}
