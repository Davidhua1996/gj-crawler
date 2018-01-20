package com.gj.web.crawler.http;

import java.net.Proxy;
import java.net.URL;
import java.util.List;

import com.gj.web.crawler.http.proxy.ProxyConfig;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import org.jsoup.Jsoup;

/**
 * a wrapper utility class of http's connection 
 * 
 */
public abstract class HttpExecutor
{
	/**
	 * default connectTimeout
	 */
	public static final int CONNECT_TIMEOUT = 3000;
	/**
	 * default readTieout
	 */
	public static final int READ_TIMEOUT = 5000;
	/**
	 * do response
	 * @return
	 */
	public abstract Response response();
	/**
	 * execute method
	 */
	public abstract HttpExecutor execute();

	public abstract int code();
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
    public static DefaultHttpExecutor newInstance(String address, ProxyContainer.ProxyEntity proxy){
    	return new DefaultHttpExecutor(address, proxy);
	}
	public static DefaultHttpExecutor newInstance(String address, ProxyContainer.ProxyEntity proxy, ProxyConfig proxyConfig){
		return new DefaultHttpExecutor(address, proxy, proxyConfig);
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
