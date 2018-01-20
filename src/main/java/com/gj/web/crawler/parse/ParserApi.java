package com.gj.web.crawler.parse;


import org.jsoup.nodes.Document;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.store.StoreStrategy;

public interface ParserApi {
	
	String DFAULT_DOMAIN_PLACEHOLDER = "{domain}";
	
	String PARSER_PERSIST_SCHEDUAL_NAME = "schedual-persist-";
	
	int DEFAULT_TIMER_INTERVAL = 3000;
	
	String PARSER_NAME = "parse";
	
	Integer COMMIT_PER_COUNT = 200;
	
	String DB_NAME_PRFIX = "map_db_";

	String JSON_KEY = "JSON";
	/**
	 * parse HTML
	 * @param html
	 */
	 void parse(String html,URL url);
	/**
	 * as you can see,use Jsoup.parse(String) before
	 * @param doc
	 */
	 void parse(Document doc,URL url);
	/**
	 * resolve method(resolve the result from parsing)
	 * it will be invoked once parsing program ends
	 * @param result
	 */
	 Object resolve(ResultModel result);
	/**
	 * persist the collection in-memory
	 */
	 void persist();
	/**
	 * like persist method,but commit link-message also
	 */
	 void pcommit();
	
	 Callback getCallback();
	
	 String getRootDir();
	
	 String getChildDir();
	
	 boolean isParsed(URL url);
	
	 boolean isDebug();

	void setCrawlPool(CrawlerThreadPool pool);
}
