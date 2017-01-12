package com.gj.web.crawler.parse;

import java.util.List;

import org.jsoup.nodes.Document;

import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.pool.basic.URL;

public interface Parser {
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
	
	public Callback getCallback();
	
	public void setCallback(Callback callback);
	/**
	 * the root directory
	 */
	public void setRootDir(String rootDir);
	
	public String getRootDir();
	/**
	 * the child directory
	 */
	public void setChildDir(String childDir);
	
	public String getChildDir();
	
	public boolean isParsed(URL url);
}
