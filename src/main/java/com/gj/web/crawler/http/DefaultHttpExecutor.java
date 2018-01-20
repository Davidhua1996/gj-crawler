package com.gj.web.crawler.http;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.gj.web.crawler.http.proxy.ProxyConfig;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.gj.web.crawler.http.utils.ConnectionHandler;

public class DefaultHttpExecutor extends HttpExecutor {
	/**
	 * encoding
	 */
	private final String DEFAULT_CHARSET = "UTF-8";

	private ProxyContainer.ProxyEntity proxy;

	private HttpURLConnection con = null;

	private ProxyConfig proxyConfig = null;
	private int code = - 1;

	public DefaultHttpExecutor(String address){
		this(address, null);
	}
	public DefaultHttpExecutor(String address, ProxyContainer.ProxyEntity proxy){
		this(address, proxy, null);
	}
	public DefaultHttpExecutor(String address, ProxyContainer.ProxyEntity proxy, ProxyConfig proxyConfig){
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		this.con = openConn(url,proxy, proxyConfig);
	}
	public DefaultHttpExecutor(URL url){
		this.con = openConn(url, null, null);
	}
	public DefaultHttpExecutor(URL url,ProxyContainer.ProxyEntity proxy){
		this.con = openConn(url,proxy,null);
	}
	public DefaultHttpExecutor(URL url,ProxyContainer.ProxyEntity proxy, ProxyConfig proxyConfig){
		this.con = openConn(url, proxy, proxyConfig);
	}
	private HttpURLConnection openConn(URL url,ProxyContainer.ProxyEntity proxy, ProxyConfig proxyConfig){
		this.proxy = proxy;
		this.proxyConfig = proxyConfig;
		try {
			HttpURLConnection con = (HttpURLConnection)
					(proxy == null?url.openConnection():url.openConnection(proxy.getProxy()));
			con.setConnectTimeout(CONNECT_TIMEOUT);
			con.setReadTimeout(READ_TIMEOUT);
			con.setInstanceFollowRedirects(true);//allow the redirects with response code 3xx
			con.setDoOutput(false);//for crawler,don't use method POST or PUT
			con.setDoInput(true);
			con.setUseCaches(true);//use cache
			con.setRequestMethod("GET");
			return con;
		} catch (IOException e) {
            ProxyUtils.record(this.proxy, proxyConfig, con.getURL().toString(), -1);
			throw new RuntimeException(e);
		}
	}
	public HttpExecutor execute(){
		try {
			con.connect();
			code = con.getResponseCode();
			return this;
		} catch (IOException e) {
            ProxyUtils.record(this.proxy, proxyConfig, con.getURL().toString(), -1);
			throw new RuntimeException(e);
		}
	}

	@Override
	public int code() {
		return this.code;
	}

	@Override
	public Response response() {
		return new DefaultResponse(this.con, this.proxy, proxyConfig);
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
