package com.gj.web.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gj.web.crawler.http.proxy.ProxyConfig;
import com.gj.web.crawler.http.proxy.ProxyContainer;
import com.gj.web.crawler.http.proxy.ProxyUtils;
import com.gj.web.crawler.parse.json.JsonUtils;
import com.gj.web.crawler.pool.exc.ProxyForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gj.web.crawler.htmlunit.HtmlUnitUtils;
import com.gj.web.crawler.htmlunit.WebClientPooledFactory;
import com.gj.web.crawler.http.HttpExecutor;
import com.gj.web.crawler.http.Response;
import com.gj.web.crawler.http.utils.DataUtils;
import com.gj.web.crawler.lifecycle.BasicLifecycle;
import com.gj.web.crawler.lifecycle.Lifecycle;
import com.gj.web.crawler.parse.Callback;
import com.gj.web.crawler.parse.DefaultCallback;
import com.gj.web.crawler.parse.ParserApi;
import com.gj.web.crawler.pool.CrawlerThreadPool;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.store.FileStoreStrategy;
import com.gj.web.crawler.store.StoreStrategy;

/**
 * the configuration of web crawler
 * @author David
 *
 */
public class Crawler extends BasicLifecycle implements CrawlerApi,Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -9356067239422904L;
	
	private static final Logger logger = LogManager.getLogger(Crawler.class);
	/**
	 * unique identify
	 */
	protected Object id;
	
	protected boolean useParams = false;
	/**
	 * simulate browser
	 */
	protected boolean simulate = false;

	protected boolean useProxy = false;

	/**
	 * timer task for crawler
	 */
	@JsonIgnore
	private Timer timer = null;
	/**
	 * completed media URLs
	 */
	private volatile Queue<URL> medias = new LinkedBlockingDeque<URL>();
	/**
	 * for each web site,it may limit the number of connection at the moment,
	 * so we set the number by default
	 */
	private Integer connNum = MAX_CONNECT_THREAD;
	/**
	 * the depth of crawling
	 */
	protected Integer maxDepth = DEFAULT_CRAWL_DEPTH;
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
	protected List<String> cookies = new ArrayList<String>();
	/**
	 * store crawl pool address
	 */
	@JsonIgnore
	protected transient CrawlerThreadPool crawlPool = CrawlerThreadPoolImpl.getInstance();
	/**
	 * restrict the elements
	 */
	protected String restrict = null;
	
	private String allowString = null;
	
	private String parseString = null;
	
	protected long interval = DEFAULT_TIMER_INTERVAL ;
	/**
	 * web portal
	 */
	protected String portal = null;
	
	private volatile AtomicInteger num;
	
	private transient ReentrantLock crawlLock;
	
	private transient Condition notLimit;
	
	protected boolean lazy = true;
	
	/**
	 * contains the specific program of parsing HTML,
	 * such as parameter mapping
	 */
	protected ParserApi parser = null;
	
	private transient Callback callback = new  DefaultCallback();
	
	private transient StoreStrategy strategy = new FileStoreStrategy();
			
	private transient SoftReferenceObjectPool<WebClient> softPool = 
			new SoftReferenceObjectPool<WebClient>(new WebClientPooledFactory());

	private ProxyConfig proxyConfig = new ProxyConfig();
	/**
	 * you can offer a shared lock and atomic Integer obj
	 * @param shareLock
	 * @param num
	 */
	public Crawler(ReentrantLock shareLock, AtomicInteger num){
		if(null == shareLock || null == num){
			throw new IllegalArgumentException();
		}
		this.num = num;
		this.crawlLock = shareLock;
		this.notLimit = this.crawlLock.newCondition();
		this.num = num;
	}
	public Crawler(){
		this(new ReentrantLock(), new AtomicInteger());
	}
	/**
	 * crawl HTML page
	 * return the URLs crawled
	 */
	public List<URL> crawlHTML(URL url){
		List<URL> urls = new ArrayList<URL>();
		if(maxDepth > 0 && url.getDepth() > maxDepth){
			return urls;//too deep
		}
		boolean parsable = isParsable(url.getUrl());
		if(parsable && parser.isParsed(url)){
			return urls;
		}
		begin();
		if(logger.isInfoEnabled()){
			logger.info("start to crawl HTML, url=" +url.getUrl());
		}
		Document store = null;
		try{
			String body = null;
			if(simulate){//simulate browser to load script,but maybe too low
				body = simulateAndResponse(url);
			}else{
				Response response = connectAndResponse(url);
				body = response.body();
			}
			if(null != body && !body.trim().equals("")){
				Document document = Jsoup.parse(body);
//				System.out.println(document.html());
				body = null;//release memory immediately
				if(parsable){
					store = document;
				}else{
					Elements elements = null;
					if(null != restrict){
						elements = document.select(restrict);
						elements = elements.select("A[href~="+getAllowString()+"]");
					}else{
						elements = document.select("A[href~="+getAllowString()+"]");
					}
					for(int i = 0;i < elements.size();i++){
						String urlStr = elements.get(i).attr("href");
						urlStr = DataUtils.formatURL(url, urlStr);
						URL newUrl = new URL(null, urlStr, url.getDepth() + 1);
						newUrl.addHeader("Referer", url.getUrl());
						urls.add(newUrl);
					}
				}
				if(null != store && null != parser){
					parser.parse(store,url);//the entrance API of parsing
				}
			}
		}catch(Exception e){
			throw e;
		}finally{
			end();
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
			try{
				if(logger.isInfoEnabled()){
					logger.info("start to crawl Media, url=" +url.getUrl()+", local=" + url.getLocal());
				}
				executor = HttpExecutor.newInstance(url.getUrl());
				executor.wrapperConn(url.getHeaders()).cookies(cookies).execute();
				Response response = executor.response();
				File directory = new File(storePath.substring(0, storePath.lastIndexOf("/")));
				directory.mkdirs();
				strategy.localStore(response.getInputStream(), storePath);
			}catch(Exception e){
				if(e instanceof FileNotFoundException){//404 ignore
					url.setStatus(0);//unavailable
				}else{
					if(url.getRetry() >= crawlPool.getMaxRetry()){
						url.setStatus(0);
						medias.add(url);
					}
					throw new RuntimeException(e);
				}
			}finally{
				if(null != executor) executor.disconnect();
				end();
			}
			medias.add(url);
		}
	}
	public void mediaDownloaded(Queue<URL> medias0) {
		List<URL> failed = callback.mediaDownloaded(medias0);
		if(null != failed && failed.size() > 0){
			medias0.addAll(failed);//repush
		}
	}
	/**
	 * connect and do Response
	 */
	private Response connectAndResponse(URL url){
        ProxyContainer.ProxyEntity proxy = null;
        boolean hasProxy = false;
	    if(useProxy){
	        String address = url.getProxy();
	        if(!StringUtils.isEmpty(address)){
	        	hasProxy = true;//have the permanent proxy
	            String[] array = address.split(":");
	            proxy = new ProxyContainer.ProxyEntity(array[0],
                        Integer.valueOf(array[1]), 0);
            }else{
                proxy = ProxyUtils.nextProxyEntity();
                url.setProxy(proxy.getAddress());
            }
        }
		HttpExecutor executor = null;
	    int code = -1;
	    String result = "";
		try {
			if(null != proxy){
				logger.info("USE PROXY: " + proxy.getAddress());
			}
			executor = HttpExecutor.newInstance(url.getUrl(),
					proxy);
			executor.wrapperConn(url.getHeaders()).cookies(cookies).execute();// do like a browser
            code = executor.code();
			Response response = executor.response();
			result = response.body();
            if(null != proxyConfig && proxyConfig.isForbiddenContent(result)){
                code = 403;
                throw new ProxyForbiddenException("discover forbidden content:"+result.substring(0, 15)+"...");
            }
            if(null != proxyConfig && proxyConfig.isForbiddenCode(String.valueOf(code))){
				throw new ProxyForbiddenException("discover forbidden code:" + code);
			}
			result = null;
			return response;
		}catch(Exception e){
			if(!hasProxy){
				url.setProxy(null);//clear the dynamic proxy
			}
			if(!(e instanceof ProxyForbiddenException)
                    && null != proxyConfig && proxyConfig.isForbiddenCode(String.valueOf(code))){
//				url.setProxy(null);//the proxy is not available
                throw new ProxyForbiddenException("discover forbidden code:" + code);
            }
			throw e;
		}finally{
            ProxyUtils.record(proxy, proxyConfig, url.getUrl(), code, result);
			if(null != executor){
			    executor.disconnect();//don't forget that
            }
		}
	}
	private String simulateAndResponse(URL url){
		WebClient client = null;
		WebConnection webconn = null;
		String result = null;
		try {
			client = softPool.borrowObject();
			String urlStr = url.getUrl();
			webconn = client.getWebConnection();//store temply
			HtmlUnitUtils.setWebConnection(client);
			HtmlPage page = client.getPage(urlStr);
			result = page.asXml();
			page.cleanUp();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			if(null != client){
				client.setWebConnection(webconn);
				client.close();
				client.getCache().clear();
				System.gc();
				try {
					softPool.returnObject(client);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	private boolean isParsable(String pattern){
		String parseStr = getParseString();
		if(null == parseStr){
			return false;
		}
		return pattern.matches(getParseString());
	}
	
	public String getAllowString(){
		if(null == allowString){
			synchronized (allowURL) {
				allowString = DataUtils.regexJoin(allowURL);
			}
		}
		return StringUtils.isBlank(allowString.trim())?null:allowString;
	}
	public String getParseString(){
		if(null == parseString){
			synchronized (parseURL) {
				parseString = DataUtils.regexJoin(parseURL);
			}
		}
		return StringUtils.isBlank(parseString.trim())?null:parseString;
	}
	private void begin() {
		crawlLock.lock();
		try{
			while(num.get() >= connNum){
				notLimit.await();
			}
			num.incrementAndGet();
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}finally{
			crawlLock.unlock();
		}
	}
	private void end() {
		crawlLock.lock();
		try{
			num.decrementAndGet();
			notLimit.signal();
		}finally{
			crawlLock.unlock();
		}
	}
	@Override
	protected void openInternal() {
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				if(medias.size() > 0){
					mediaDownloaded(medias);
				}
			}
			
		}, interval, interval);
		if(null != parser && parser instanceof Lifecycle){
			((Lifecycle)parser).open();
		}
	}
	@Override
	protected void shutdownInternal() {
		if(null != timer){
			timer.cancel();
			timer = null;
		}
		if(null != parser && parser instanceof Lifecycle){
			((Lifecycle)parser).shutdown();
		}
		if(medias.size() > 0){
			mediaDownloaded(medias);
		}
	}
	@Override
	protected void initalInternal() {
		if(null == timer){
			timer = new Timer(CRAWLER_MEDIA_SCHEDUAL_NAME+id);
		}
		if(null != parser && parser instanceof Lifecycle){
			((Lifecycle)parser).initalize();
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
	public ParserApi getParser() {
		return this.parser;
	}
	public void setParser(ParserApi parser) {
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
	public Callback getCallback() {
		return  this.callback;
	}
	public void setCallback(Callback callback) {
		this.callback = callback;
		
	}
	public void setCrawlPool(CrawlerThreadPool pool) {
		this.crawlPool = pool;
	}
	public Integer getMaxDepth() {
		return maxDepth;
	}

	@Override
	public ProxyConfig getProxyConfig() {
		return proxyConfig;
	}

	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}
	public void setStoreStrategy(StoreStrategy strategy) {
		this.strategy = strategy;
	}

	public boolean isUseProxy() {
		return useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}
}
