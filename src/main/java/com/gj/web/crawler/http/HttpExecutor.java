package com.gj.web.crawler.http;

import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;

/**
 * a wrapper utility class of http's connection 
 * 
 */
public abstract class HttpExecutor 
{
	/**
	 * do response
	 * @return
	 */
	public abstract Response response();
	/**
	 * execute method
	 */
	public abstract HttpExecutor execute();
	/**
	 * 
	 * discount
	 * 
	 */
	public abstract void disconnect();
	/**
	 * 
	 * wrap up the connection and make it like a browser
	 */
	public abstract HttpExecutor wrapperConn();
	/**
	 * add cookies to request
	 * @param cookies
	 */
	public abstract HttpExecutor cookies(List<String> cookies);
	/**
	 * create new instance
	 */
    public static DefaultHttpExecutor newInstance(URL url){
    	return new DefaultHttpExecutor(url);
    }
    public static DefaultHttpExecutor newInstance(String address){
    	return new DefaultHttpExecutor(address);
    }
    public static void main(String[] args) throws Exception{
    	long current = System.currentTimeMillis();
    	URL url = new URL("http://store.steampowered.com/games/");
    	HttpExecutor executor = HttpExecutor.newInstance(url);
    	executor.wrapperConn().execute();
    	Response response = executor.response();
//    	for(Entry<String, List<String>> entry : response.headers().entrySet()){
//    		System.out.println("key:->"+entry.getKey()+" value:->"+entry.getValue());
//    	}
    	System.out.println(System.currentTimeMillis() - current);
    	System.out.println(response.body());
    	Jsoup.parse(response.body());
    	System.out.println(System.currentTimeMillis() - current);
    }
}
