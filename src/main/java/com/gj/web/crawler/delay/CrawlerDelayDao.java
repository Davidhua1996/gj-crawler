package com.gj.web.crawler.delay;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * Data Access Object,
 * users can manage Crawler Delay task's configurations by operating Database
 * @author David
 *
 */
public interface CrawlerDelayDao {
	/**
	 * load all crawler's delayed tasks
	 * @return
	 */
	public <T extends CrawlerDelay> List<T> loadAllDelays();

}
