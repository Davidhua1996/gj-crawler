package com.gj.web.crawler;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.InitializingBean;

import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;
import com.gj.web.crawler.pool.basic.URL;

/**
 * the executor of all the crawlers,
 * actually, it is an extension of CrawlerThreadPoolImpl
 * and a bridge between crawler component and other framework like 'Spring'
 * TODO 和其他框架的接口做适配 
 * @author David
 *
 */
public class CrawlerExecutor implements CrawlerThreadPool,InitializingBean{
	
	private CrawlerThreadPool pool = CrawlerThreadPoolImpl.getInstance();

	public void open() {
		pool.open();
	}

	public boolean isOpen() {
		return pool.isOpen();
	}

	public void shutdown() {
		pool.shutdown();
	}

	public void execute(URL url) {
		pool.execute(url);
	}

	public Map<String, CrawlerApi> getCrawlers() {
		return pool.getCrawlers();
	}

	public void setCrawlers(Map<String, CrawlerApi> crawlers) {
		pool.setCrawlers(crawlers);
	}

	public void setPoolSize(Integer size) {
		pool.setPoolSize(size);
	}

	public Integer getPoolSize() {
		return pool.getPoolSize();
	}

	public void setMaxFree(Integer free) {
		pool.setMaxFree(free);
	}
	
	public Integer getMaxFree() {
		return pool.getMaxFree();
	}
	/**
	 * start to open the pool after setting the properties
	 */
	public void afterPropertiesSet() throws Exception {
		if(!pool.isOpen()){
			pool.open();
			Map<String,CrawlerApi> crawlers = pool.getCrawlers();
			for(Entry<String,CrawlerApi> entry : crawlers.entrySet()){
				CrawlerApi crawler = entry.getValue();
				String domain = entry.getKey();
				if(null != crawler.entrance() &&
						!crawler.entrance().trim().equals("")){
					String urlStr = crawler.entrance();
					URL url = new URL(domain, urlStr);
					pool.execute(url);
				}
			}
		}
	}
}
