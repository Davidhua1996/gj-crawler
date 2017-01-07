package com.gj.web.crawler;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.pool.impl.SoftReferenceObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gj.web.crawler.htmlunit.HtmlUnitUtils;
import com.gj.web.crawler.htmlunit.WebClientPooledFactory;
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
public class Crawler implements CrawlerApi,Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -9356067239422904L;
	
	private static final int MAX_CONNECT_THREAD = 6;
	/**
	 * unique identify
	 */
	private Object id;
	
	private boolean useParams = false;
	/**
	 * simulate browser
	 */
	private boolean simulate = false;
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
	 * web portal
	 */
	private String portal = null;
	
	private volatile Integer num = 0;
	
	private ReentrantLock crawlLock = new ReentrantLock();
	
	private Condition notLimit = crawlLock.newCondition();
	
	private boolean lazy = true;
	
	/**
	 * contains the specific program of parsing HTML,
	 * such as parameter mapping
	 */
	protected Parser parser = new DefaultHTMLParser();
	
	private SoftReferenceObjectPool<WebClient> softPool = 
			new SoftReferenceObjectPool<WebClient>(new WebClientPooledFactory());
	/**
	 * crawl HTML page
	 * return the URLs crawled
	 */
	public List<URL> crawlHTML(URL url){
		begin();
		Document store = null;
		List<URL> urls = new ArrayList<URL>();
		try{
			String body = null;
			if(simulate){//simulate browser to load script,but maybe too low
				body = simulateAndResponse(url);
			}else{
				Response response = connectAndResponse(url);
				body = response.body();
			}
			if(null != body){
				Document document = Jsoup.parse(body);
				body = null;//release memory immediately
				if(isParsable(url.getUrl())){
					store = document;
				}else{
					Elements elements = null;
					if(null != restrict){
						elements = document.select(restrict);
						elements = elements.select("A[href~="+getAllowString()+"]");
					}else{
						elements = document.select("A[href~="+getAllowString()+"]");
					}
					for(int i = 0;i<elements.size();i++){
						String urlStr = elements.get(i).attr("href");
						if(urlStr.startsWith("//")){
							urlStr = "http:"+urlStr;
						}else if (urlStr.length() <= 4 ||
								!urlStr.substring(0,4).equalsIgnoreCase("http")){
							String before = url.getUrl();
							urlStr = before.substring(0,before.lastIndexOf("/"))+urlStr;
						}
						urls.add(new URL(null,urlStr));
					}
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
			HttpExecutor executor = null;
			FileOutputStream out = null;
			BufferedInputStream in = null;
			try{
				executor = HttpExecutor.newInstance(url.getUrl());
				executor.wrapperConn().cookies(cookies).execute();
				Response response = executor.response();
				out = new FileOutputStream(storePath);
				in = new 
						BufferedInputStream(response.getInputStream(),DataUtils.BUFFER_SIZE);//download from input stream
				int size = -1;
				byte[] b = new byte[DataUtils.BUFFER_SIZE];
				while((size = in.read(b)) > 0){
					out.write(b,0,size);
				}
			}catch(Exception e){
				if(e instanceof FileNotFoundException){//404 ignore
					//TODO make some logs
				}else{
					throw new RuntimeException(e);
				}
			}finally{
				try {
					executor.disconnect();
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
	private String simulateAndResponse(URL url){
		WebClient client = null;
		WebConnection webconn = null;
		try {
			client = softPool.borrowObject();
			String urlStr = url.getUrl();
			webconn = client.getWebConnection();//store temply
			HtmlUnitUtils.setWebConnection(client);
			HtmlPage page = client.getPage(urlStr);
			return page.asXml();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			if(null != client){
				client.setWebConnection(webconn);
				try {
					softPool.returnObject(client);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
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
	
	public String portal() {
		return portal;
	}
	public void setPortal(String portal) {
		this.portal = portal;
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
	public String getRestrict() {
		return restrict;
	}
	public void setRestrict(String restrict) {
		this.restrict = restrict;
	}
	public Parser getParser() {
		return this.parser;
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
	public boolean isLazy() {
		return lazy;
	}
	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}
	public Object getId() {
		return id;
	}
	public void setId(Object id) {
		this.id = id;
	}
	public boolean isUseParams() {
		return useParams;
	}
	public void setUseParams(boolean useParams) {
		this.useParams = useParams;
	}
	public boolean isSimulate() {
		return simulate;
	}
	public void setSimulate(boolean simulate) {
		this.simulate = simulate;
	}
	
}
