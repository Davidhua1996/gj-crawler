package com.gj.web.crawler;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.gj.web.crawler.http.HttpExecutor;
import com.gj.web.crawler.http.Response;
import com.gj.web.crawler.http.utils.DataUtils;
import com.gj.web.crawler.parse.DefaultHTMLParser;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.basic.URL;

/**
 * the configuration of web crawler
 * @author David
 *
 */
public class Crawler implements CrawlerApi{
	private static final int MAX_CONNECT_THREAD = 6;
	/**
	 * for each web site,it may limit the number of connection at the moment,
	 * so we set the number by default
	 */
	private Integer connNum = MAX_CONNECT_THREAD;
	/**
	 * regular expression for crawl-allowed URLs
	 */
	private List<String> allowURL = new ArrayList<String>();
	/**
	 * regular expression for URLs in need of parsing
	 */
	private List<String> parseURL = new ArrayList<String>();
	/**
	 * some web page need special cookie
	 */
	private List<String> cookies = new ArrayList<String>();
	/**
	 * restrict the elements
	 */
	private String restrict = null;
	
	private String allowString = null;
	
	private String parseString = null;
	/**
	 * entrance-URL
	 */
	private String entrance = null;
	
	private volatile Integer num = 0;
	
	private ReentrantLock crawlLock = new ReentrantLock();
	
	private Condition notLimit = crawlLock.newCondition();
	/**
	 * contains the specific program of parsing HTML,
	 * such as parameter mapping
	 */
	private Parser parser = new DefaultHTMLParser();
	/**
	 * crawl HTML page
	 * return the URLs crawled
	 */
	public List<URL> crawlHTML(URL url){
		begin();
		Document store = null;
		List<URL> urls = new ArrayList<URL>();
		try{
			Response response = connectAndResponse(url);
			String body = response.body();
			if(null != body){
				Document document = Jsoup.parse(response.body());
				if(isParsable(url.getUrl())){
					store = document;
				}
				Elements elements = null;
				if(null != restrict){
					elements = document.select(restrict);
					elements = elements.select("A[href~="+getAllowString()+"]");
				}else{
					elements = document.select("A[href~="+getAllowString()+"]");
				}
				for(int i = 0;i<elements.size();i++){
					urls.add(new URL(null,elements.get(i).attr("href")));
				}
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			end();
		}
		if(null != store){
			parser.parse(store,url);//the entrance API of parsing
		}
		return urls;
	}
	/**
	 * crawl multiply Medium,like photo or video
	 * @param url
	 * @param storePath local space to store medium
	 */
	public void crawlMedia(URL url,String storePath){
		if(null != storePath && !storePath.trim().equals("")){
			begin();
			HttpExecutor executor = HttpExecutor.newInstance(url.getUrl());
			executor.wrapperConn().cookies(cookies).execute();
			Response response = executor.response();
			FileOutputStream out = null;
			BufferedInputStream in = null;
			try{
				out = new FileOutputStream(storePath);
				in = new 
						BufferedInputStream(response.getInputStream(),DataUtils.BUFFER_SIZE);//download from input stream
				int size = -1;
				byte[] b = new byte[DataUtils.BUFFER_SIZE];
				while((size = in.read(b)) > 0){
					out.write(b,0,size);
				}
			}catch(IOException e){
				if(e instanceof FileNotFoundException){//404 ignore
					//TODO make some logs
				}else{
					throw new RuntimeException(e);
				}
			}finally{
				executor.disconnect();
				try {
					if(null != in)in.close();
					if(null != out)out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}finally{
					end();
				}
			}
		}
	}
	/**
	 * connect and do Response
	 */
	private Response connectAndResponse(URL url){
		HttpExecutor executor = HttpExecutor.newInstance(url.getUrl());
		executor.wrapperConn().cookies(cookies).execute();// do like a browser
		Response response = executor.response();
		response.body();
		executor.disconnect();//don't forget that
		return response;
	}
	
	private boolean isParsable(String pattern){
		return pattern.matches(getParseString());
	}
	
	public String getAllowString(){
		if(null == allowString){
			synchronized (allowURL) {
				allowString = DataUtils.regexJoin(allowURL);
			}
		}
		return allowString;
	}
	public String getParseString(){
		if(null == parseString){
			synchronized (parseURL) {
				parseString = DataUtils.regexJoin(parseURL);
			}
		}
		return parseString;
	}
	private void begin() {
		crawlLock.lock();
		try{
			while(num >= connNum){
				notLimit.await();
			}
			num++;
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}finally{
			crawlLock.unlock();
		}
	}
	private void end() {
		crawlLock.lock();
		try{
			num--;
			notLimit.signal();
		}finally{
			crawlLock.unlock();
		}
	}
	public void setEntrance(String entrance){
		this.entrance = entrance;
	}
	public String entrance() {
		return entrance;
	}
	public Integer getConnNum() {
		return connNum;
	}
	public void setConnNum(Integer connNum) {
		this.connNum = connNum;
	}
	public List<String> getAllowURL() {
		return allowURL;
	}
	public void setAllowURL(List<String> allowURL) {
		this.allowURL = allowURL;
	}
	public void setAllowString(String allowString) {
		this.allowString = allowString;
	}
	public String getRestrict() {
		return restrict;
	}
	public void setRestrict(String restrict) {
		this.restrict = restrict;
	}
	public Parser getParser() {
		return parser;
	}
	public void setParser(Parser parser) {
		this.parser = parser;
	}
	public List<String> getParseURL() {
		return parseURL;
	}
	public void setParseURL(List<String> parseURL) {
		this.parseURL = parseURL;
	}
	public List<String> getCookies() {
		return cookies;
	}
	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}
	
}
