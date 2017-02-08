package com.gj.web.crawler.lifecycle;
/**
 * simple lifecycle component
 * @author David
 *
 */
public interface Lifecycle {
	
	public void initalize();
	
	public void open();
	
	public void shutdown();
}
