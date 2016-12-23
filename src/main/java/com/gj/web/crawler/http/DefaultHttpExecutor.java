package com.gj.web.crawler.http;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import com.gj.web.crawler.http.utils.ConnectionHandler;

public class DefaultHttpExecutor extends HttpExecutor {
	/**
	 * default connectTimeout 
	 */
	private final int CONNECT_TIMEOUT = 5000;
	/**
	 * default readTieout
	 */
	private final int READ_TIMEOUT = 10000;
	/**
	 * encoding
	 */
	private final String DEFAULT_CHARSET = "UTF-8";
	
	private HttpURLConnection con = null;
	
	public DefaultHttpExecutor(String address){
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		this.con = openConn(url,null);
	}
	public DefaultHttpExecutor(URL url){
		this.con = openConn(url,null);
	}
	public DefaultHttpExecutor(URL url,Proxy proxy){
		this.con = openConn(url,proxy);
	}
	private HttpURLConnection openConn(URL url,Proxy proxy){
		try {
			HttpURLConnection con = (HttpURLConnection)
					(proxy == null?url.openConnection():url.openConnection(proxy));
			con.setConnectTimeout(CONNECT_TIMEOUT);
			con.setReadTimeout(READ_TIMEOUT);
			con.setInstanceFollowRedirects(true);//allow the redirects with response code 3xx
			con.setDoOutput(false);//for crawler,don't use method POST or PUT
			con.setDoInput(true);
			con.setRequestMethod("GET");
			return con;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public HttpExecutor execute(){
		try {
			con.connect();
			return this;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public Response response() {
		return new DefaultResponse(this.con);
	}
	@Override
	public HttpExecutor wrapperConn() {
		ConnectionHandler.wrapper(this.con);
		return this;
	}
	@Override
	public void disconnect() {
		this.con.disconnect();
	}
	@Override
	public HttpExecutor cookies(List<String> cookies) {
		StringBuilder cookieValue = new StringBuilder("");
		for(String cookie : cookies){
			cookieValue.append(cookie);
			if(!cookie.endsWith(";")){
				cookieValue.append(";");
			}
		}
		String value = cookieValue.toString();
		if(null != value && !value.trim().equals("")){
			ConnectionHandler.cookie(this.con, value);
		}
		return this;
	}
}
