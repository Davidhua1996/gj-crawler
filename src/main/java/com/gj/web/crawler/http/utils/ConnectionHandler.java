package com.gj.web.crawler.http.utils;

import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Map.Entry;

public class ConnectionHandler {
	private static final String DEFAULT_CONNECTION = "keep-alive";//keep-alive
	private static final String DEFAULT_ACCEPT_ENCODING = "gzip,deflate,sdch";
	private static final String[] DEFAULT_USER_AGENTS =
			new String[] {
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36",
					"Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.119 Safari/537.36",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 MicroMessenger/6.5.2.501 NetType/WIFI WindowsWechat QBCore/3.43.691.400 QQBrowser/9.0.2524.400"
			};
	private static final String DEFAULT_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/apng,*/*;q=0.8";
	private static final String CONTENT_TYPE = "application/x-www-form-urlencoded;charset=UTF-8";
	private static final String DEFAULT_ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9";
	private static SecureRandom random = new SecureRandom();
	static{
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
	}
	private ConnectionHandler(){
		
	}
	/**
	 * use default configuration to wrap up connection
	 * @param conn
	 * @return
	 */
	public static HttpURLConnection wrapper(HttpURLConnection conn){
		conn.setRequestProperty("Connection", DEFAULT_CONNECTION);
		int l = DEFAULT_USER_AGENTS.length;
		conn.setRequestProperty("User-Agent",DEFAULT_USER_AGENTS[random.nextInt(l)]);
		conn.setRequestProperty("Accept",DEFAULT_ACCEPT);
		conn.setRequestProperty("Host", conn.getURL().getHost());
		conn.setRequestProperty("Accept-Encoding", DEFAULT_ACCEPT_ENCODING);
//		conn.setRequestProperty("Content-Type",CONTENT_TYPE);
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
	 * @param cookieValue
	 * @return
	 */
	public static HttpURLConnection cookie(HttpURLConnection conn,String cookieValue){
		conn = wrapper(conn);
		conn.setRequestProperty("Cookie",cookieValue);
		return conn;
	}
}
