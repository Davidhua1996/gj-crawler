package com.gj.web.crawler.http;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.gj.web.crawler.http.proxy.ProxyConfig;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.gj.web.crawler.http.utils.DataUtils;

public class DefaultResponse implements Response {
	/**
	 * cannot store as String,
	 * because the process of encrypt cost much time
	 */
	private ByteBuffer body = null;
	private String charset = "UTF-8";
	private HttpURLConnection con = null;
	private Map<String,List<String>> headers = null;
	private Map<String,String> cookies = new HashMap<String,String>();
	private ProxyContainer.ProxyEntity proxy;
	/**
	 * record the time of attempting to read from input stream 
	 */
	private int attemptTime = 0;
	protected DefaultResponse(HttpURLConnection con, ProxyContainer.ProxyEntity proxy){
		this.con = con;
		this.proxy = proxy;
		resolveHeaders(con);
	}
	protected void setBody(ByteBuffer body){
		this.body = body;
	}
	public String body() {
		if(null == body){
			try {
				InputStream in = con.getInputStream();
				if(null != con.getHeaderField("Content-Encoding") 
						&& con.getHeaderField("Content-Encoding").equals("gzip")){
					in = new GZIPInputStream(in);
				}
				body = streamToByte(in);
			} catch (IOException e){
				if(e instanceof FileNotFoundException){//404 ignore
					//TODO make some logs
					return null;
				}
				throw new RuntimeException("error happened when create input stream in connection",e);
			}
		}
		String result = Charset.forName(charset).decode(body).toString();
		body.flip();
		return result;
	}

	public byte[] bodyAsBytes() {
		try {
			return body().getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, String> cookies() {
		return cookies;
	}

	public String cookie(String name) {
		return cookies.get(name);
	}

	public Map<String, List<String>> headers() {
		return headers;
	}

	public List<String> header(String key) {
		return headers.get(key);
	}
	
	private ByteBuffer streamToByte(InputStream in){
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(DataUtils.BUFFER_SIZE);
			byte[] buffer = new byte[DataUtils.BUFFER_SIZE];
			int len = -1;
			while((len = in.read(buffer)) > -1){
				out.write(buffer,0,len);
			}
			return ByteBuffer.wrap(out.toByteArray());
		} catch(IOException ie){
			if(ie.getCause() instanceof SocketTimeoutException && attemptTime < MAX_ATTEMPT_TIME){
				return streamToByte(in);
			}else{
				if(ie.getCause() instanceof SocketTimeoutException){
					throw new RuntimeException("Read time out and have been tried "+attemptTime+" times",ie);
				}else{
					throw new RuntimeException(ie);
				}
			}
		}
	}
	private void resolveHeaders(HttpURLConnection con){
		this.headers = con.getHeaderFields();
		List<String> cookieList = this.headers.get("Set-Cookie");
		if(null != cookieList){
			for(String cookie : cookieList){
				//only to resolve the name of cookie,because other key-value pairs are not necessary
				String key = cookie.substring(0,cookie.indexOf("="));
				cookies.put(key, cookie);
			}
		}
	}
	public InputStream getInputStream() {
		try {
			InputStream in = this.con.getInputStream();
			if(null != con.getHeaderField("Content-Encoding") 
					&& con.getHeaderField("Content-Encoding").equals("gzip")){
				in = new GZIPInputStream(in);
			}
			return in;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
