package com.gj.web.crawler.store;

import java.io.InputStream;

/**
 * define the store strategy in crawler
 * @author David
 *
 */
public interface StoreStrategy {
	
	public void localStore(String content, String loc);
	
	public void localStore(InputStream in, String loc);
}
