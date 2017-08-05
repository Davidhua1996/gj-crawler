package com.gj.web.crawler.parse;


import org.jsoup.nodes.Document;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.store.StoreStrategy;

public interface ParserApi {
	
	public static final String DFAULT_DOMAIN_PLACEHOLDER = "{domain}";
	
	public static final String PARSER_PERSIST_SCHEDUAL_NAME = "schedual-persist-";
	
	public static final int DEFAULT_TIMER_INTERVAL = 3000;
	
	public static final String PARSER_NAME = "parse";
	
	public static final Integer COMMIT_PER_COUNT = 200;
	
	public static final String DB_NAME_PRFIX = "map_db_";
	/**
	 * parse HTML
	 * @param html
	 */
	public void parse(String html,URL url);
	/**
	 * as you can see,use Jsoup.parse(String) before
	 * @param doc
	 */
	public void parse(Document doc,URL url);
	/**
	 * resolve method(resolve the result from parsing)
	 * it will be invoked once parsing program ends
	 * @param result
	 */
	public Object resolve(ResultModel result);
	/**
	 * persist the collection in-memory
	 * @param store
	 */
	public void persist();
	/**
	 * like persist method,but commit link-message also
	 */
	public void pcommit();
	
	public Callback getCallback();
	
	public String getRootDir();
	
	public String getChildDir();
	
	public boolean isParsed(URL url);
	
	public boolean isDebug();
	
}
