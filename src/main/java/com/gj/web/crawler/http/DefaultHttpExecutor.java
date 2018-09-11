package com.gj.web.crawler.http;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gj.web.crawler.http.proxy.ProxyConfig;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.gj.web.crawler.http.utils.ConnectionHandler;

public class DefaultHttpExecutor extends HttpExecutor {
	/**
	 * encoding
	 */
	private final String DEFAULT_CHARSET = "UTF-8";
	private final String DATA_PARAM_SEPERATOR = "&";

	private ProxyContainer.ProxyEntity proxy;

	private HttpURLConnection con = null;

	private int code = - 1;

	public DefaultHttpExecutor(String address){
		this(address, null);
	}
	public DefaultHttpExecutor(String address, ProxyContainer.ProxyEntity proxy){
		this(address, "GET", proxy);
	}
	public DefaultHttpExecutor(String address, String method, ProxyContainer.ProxyEntity proxy){
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		this.con = openConn(url,method, proxy);
	}
	public DefaultHttpExecutor(URL url){
		this.con = openConn(url, "GET", null);
	}
	public DefaultHttpExecutor(URL url,ProxyContainer.ProxyEntity proxy){
		this.con = openConn(url,"GET", proxy);
	}
	private HttpURLConnection openConn(URL url,String method, ProxyContainer.ProxyEntity proxy){
		this.proxy = proxy;
		try {
			HttpURLConnection con = (HttpURLConnection)
					(proxy == null?url.openConnection():url.openConnection(proxy.getProxy()));
			con.setConnectTimeout(CONNECT_TIMEOUT);
			con.setReadTimeout(READ_TIMEOUT);
			con.setInstanceFollowRedirects(true);//allow the redirects with response code 3xx
			con.setDoOutput(true);//for crawler,don't use method POST or PUT
			con.setDoInput(true);
			con.setUseCaches(true);//use cache
			con.setRequestMethod(method);
			return con;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public HttpExecutor execute(){
		try {
			con.connect();
			code = con.getResponseCode();
			return this;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public HttpExecutor data(Map<String, String> data) {
		try {
			OutputStream out = this.con.getOutputStream();
			StringBuilder builder = new StringBuilder();
			Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String, String> entry = iterator.next();
				builder.append(entry.getKey()).append("=").append(entry.getValue());
				if(iterator.hasNext()){
					builder.append(DATA_PARAM_SEPERATOR);
				}
			}
			byte[] b = builder.toString().getBytes();
			out.write(b, 0, b.length);
			return this;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public int code() {
		return this.code;
	}

	@Override
	public Response response() {
		return new DefaultResponse(this.con, this.proxy);
	}
	@Override
	public HttpExecutor wrapperConn() {
		ConnectionHandler.wrapper(this.con);
		return this;
	}

    @Override
    public HttpExecutor wrapperConn(Map<String, String> headers) {
	    ConnectionHandler.wrapper(this.con);
	    headers.forEach((key, value) -> this.con.setRequestProperty(key, value));
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
