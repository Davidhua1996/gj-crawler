package com.gj.web.crawler;

import java.util.List;

/**
 * Data Access Object,
 * users can manage Crawler's configurations by operating Database  
 * @author David
 *
 */
public interface CrawlerDao {
	/**
	 * load all crawlers
	 * @return
	 */
	public <T extends CrawlerApi> List<T> loadAll();
	/**
	 * load crawler by ID
	 * @param crawlerId
	 * @return
	 */
	public <T extends CrawlerApi> T load(Object cid);
}
