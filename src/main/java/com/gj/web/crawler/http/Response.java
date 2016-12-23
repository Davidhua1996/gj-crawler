package com.gj.web.crawler.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * response api
 * @author David
 */
public interface Response {
	static final Integer MAX_ATTEMPT_TIME = 3;
	/**
	 * response's body(String)
	 * @return
	 */
	public String body();
	/**
	 * transform body to bytes
	 * @return
	 */
	public byte[] bodyAsBytes();
	/**
	 * all the cookies 
	 * @return
	 */
	public Map<String,String> cookies();
	/**
	 * query the cookie's value with name 
	 * @param name
	 * @return
	 */
	public String cookie(String name);
	/**
	 * all the headers
	 * @return
	 */
	public  Map<String, List<String>> headers();
	/**
	 * query the header's value with key
	 * @param key
	 * @return
	 */
	public List<String> header(String key);
	
	public InputStream getInputStream();
}
