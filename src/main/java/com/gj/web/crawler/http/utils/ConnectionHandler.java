package com.gj.web.crawler.http.utils;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;

public class ConnectionHandler {
	private static final String DEFAULT_CONNECTION = "keep-alive";
	private static final String DEFAULT_ACCEPT_ENCODING = "gzip,deflate,sdch";
	private static final String DEFAULT_USERAGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
	private static final String DEFAULT_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
	private static final String CONTENT_TYPE = "application/x-www-form-urlencoded;charset=UTF-8";
	private static final String DEFAULT_ACCEPT_LANGUAGE = "zh-CN,zh;q=0.8";
	private ConnectionHandler(){
		
	}
	/**
	 * use default configuration to wrap up connection
	 * @param conn
	 * @return
	 */
	public static HttpURLConnection wrapper(HttpURLConnection conn){
		conn.setRequestProperty("User-Agent",DEFAULT_USERAGENT);
		conn.setRequestProperty("Accept",DEFAULT_ACCEPT);
		conn.setRequestProperty("Accept-Encoding", DEFAULT_ACCEPT_ENCODING);
		conn.setRequestProperty("Connection", DEFAULT_CONNECTION);
		conn.setRequestProperty("Content-Type",CONTENT_TYPE);
		conn.setRequestProperty("Accept-Language", DEFAULT_ACCEPT_LANGUAGE);
		return conn;
	}
	/**
	 * with host
	 * @param conn
	 * @param hostName
	 * @return
	 */
	public static HttpURLConnection wrapper(HttpURLConnection conn,String hostName){
		conn = wrapper(conn);
		conn.setRequestProperty("Host", hostName);
		return conn;
	}
	/**
	 * with specific configuration
	 * @param conn
	 * @param config
	 * @return
	 */
	public static HttpURLConnection wrapper(HttpURLConnection conn,Map<String,String> config){
		for(Entry<String,String> entry : config.entrySet()){
			conn.setRequestProperty(entry.getKey(),entry.getValue());
		}
		return conn;
	}
	/**
	 * add cookie
	 * @param conn
	 * @param hostName
	 * @return
	 */
	public static HttpURLConnection cookie(HttpURLConnection conn,String cookieValue){
		conn = wrapper(conn);
		conn.setRequestProperty("Cookie",cookieValue);
		return conn;
	}
}
