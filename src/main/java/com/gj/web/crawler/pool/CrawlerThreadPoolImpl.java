package com.gj.web.crawler.pool;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.CrawlerStatus;
import com.gj.web.crawler.lifecycle.BasicLifecycle.Status;
import com.gj.web.crawler.parse.DefaultHTMLParser;
import com.gj.web.crawler.parse.DefaultHTMLParser;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.basic.IMMultQueue;
import com.gj.web.crawler.pool.basic.IMQueue;
import com.gj.web.crawler.pool.basic.MapDBMultQueue;
import com.gj.web.crawler.pool.basic.Queue;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.utils.InjectUtils;
import com.gj.web.crawler.utils.MapDBContext;

/**
 * do make a single pool to manager the crawler threads
 * @author David
 *
 */
public class CrawlerThreadPoolImpl implements CrawlerThreadPool{
	
	private static final int MAX_POOL_SIZE = 100;
	
	private static final int MAX_FREE_TIME = 1000*16;
	
	private static final int DEFAULT_RETRY_COUNT = 6;
	
	private static final Logger logger = LogManager.getLogger(CrawlerThreadPool.class);
	
	private static final String DEFAULT_THREAD_PREFIX = "http-crawl-exec-";
	
	private static CrawlerThreadPool pool;
	
	private static volatile int threadinitTimes = 0;
	/**
	 * pool size
	 */
	private Integer poolSize = MAX_POOL_SIZE;
	/**
	 * the maximum of free time for each working thread
	 */
	private Integer maxFree = MAX_FREE_TIME;
	/**
	 * to record the number of actived thread in the pool,
	 * if the 'num' equals 0 ,it means that the pool should be closed
	 */
	private AtomicInteger num = new AtomicInteger(0);
	
	private Queue<URL> queue = new IMMultQueue<URL>();
	
	private boolean useMapDB = false;
	
	private int maxRetry = DEFAULT_RETRY_COUNT;
	/**
	 * store crawlers
	 */
	private Map<String, CrawlerApi> crawlers = new ConcurrentHashMap<String, CrawlerApi>();
	/**
	 * store status
	 */
	private Map<String, CrawlerStatus> statuses = new ConcurrentHashMap<String, CrawlerStatus>();
	/**
	 * lock
	 */
	private ReentrantLock poolLock = new ReentrantLock();
	
	private Condition notEmpty = poolLock.newCondition();
	
	/**
	 * switch
	 */
	private volatile boolean isOpen = false;
	/**
	 * monitors
	 */
	private List<Monitor> monitors = new ArrayList<Monitor>();
	
	protected CrawlerThreadPoolImpl(){
	}
	public static CrawlerThreadPool getInstance(){
		if(null == pool){
			synchronized(CrawlerThreadPool.class){
				if(null == pool){
					pool = new CrawlerThreadPoolImpl();
				}
			}
		}
		return pool;
	}
	public synchronized void open() {
		if(!isOpen){
			isOpen = true;
			if(useMapDB && !(queue instanceof MapDBMultQueue)){
				queue = new MapDBMultQueue<URL>();
			}
			for(int i = 0;i < monitors.size();i++){
				monitors.get(i).open(this);
			}
			//open threads
			int initialSize = num.get();// that means threads in pool didn't close totally before
			for(int i = initialSize ;i < poolSize;i++ ){
				Worker worker = new Worker();
				new Thread(worker,DEFAULT_THREAD_PREFIX + nextThreadId()).start();
				num.incrementAndGet();
			}
			//start to crawl
			for(Entry<String,CrawlerApi> entry : crawlers.entrySet()){
				CrawlerApi crawler = entry.getValue();
				String cid = entry.getKey();
				if(null != crawler.portal() && !crawler.isLazy()){
					String urlStr = crawler.portal();
					URL url = new URL(cid, urlStr);
					execute(url);
				}
				//set crawler status
				CrawlerStatus status = statuses.get(entry.getKey());
				if(null == status){
					status = new CrawlerStatus(entry.getValue());
					status.initalize();
					statuses.put(entry.getKey(), status);
				}
				if(status.status() != Status.OPEN){
					status.open();
				}
			}
		}
	}
	public boolean isOpen() {
		return false;
	}

	public synchronized void shutdown() {
		if(num.get() == 0){
			isOpen = false;
			threadinitTimes = 0;
			queue.clear();//clear the cache,release the memory
			for(Entry<String, CrawlerStatus> entry : statuses.entrySet()){
				CrawlerStatus status = entry.getValue();
				if(status.status() != Status.CLOSE){//if status != Status.CLOSE, call close method
					status.shutdown();
				}
			}
			for(int i = 0;i < monitors.size();i++){
				monitors.get(i).close(this);
			}
		}
		System.gc();
	}
	public void close(){// close indirectly
		isOpen = false;
	}
	public void execute(String cid) {
		execute(cid, (byte[])null);
	}
	public void execute(URL url) {
		if(!isOpen) open();//open
		CrawlerStatus status = statuses.get(url.getCid());
		if(null == status){
			logger.info("couldn't find the right crawler to execute the url");
		}
		status.addWork(url);
		poolLock.lock();
		try{
			queue.pushWithKey(url,url.getUrl());//duplicate removal
			notEmpty.signal();//TODO choose to use signal() or signalAll()?
		}finally{
			poolLock.unlock();
		}
	}
	public Object executeIfAbsent(URL url){
		Object loc = null;
		if(!isOpen) open();//open
		CrawlerStatus status = statuses.get(url.getCid());
		if(null == status){
			logger.info("couldn't find the right crawler to execute the url");
		}
		status.addWork(url);
		poolLock.lock();
		try{
			loc = queue.local(url.getUrl());
			if(null == loc){
				queue.pushWithKey(url, url.getUrl());
			}
		}finally{
			poolLock.unlock();
		}
		return loc;
		
	}
	public void execute(String cid, byte[] payload) {
		CrawlerApi crawler = crawlers.get(cid);
		if(null != crawler && !crawler.isUseParams()){
			URL url = new URL(cid,crawler.portal(), payload);
			execute(url);
		}
	}
	public void execute(String cid, Object[] params) {
		execute(cid, null, params);
	}
	
	public void execute(String cid, Map<String, Object> params) {
		execute(cid, null, params);
	}
	
	public void execute(String cid, byte[] payload, Object[] params){
		CrawlerApi crawler = crawlers.get(cid);
		if(null != crawler && crawler.isUseParams()){
			String portal = InjectUtils.inject(crawler.portal(), params);
			portal = portal.replace("\"", "");
			URL url = new URL(cid, portal, payload);
			execute(url);
		}
	}
	
	public void execute(String cid, byte[] payload, Map<String, Object> params) {
		CrawlerApi crawler = crawlers.get(cid);
		if(null != crawler && crawler.isUseParams()){
			String portal = InjectUtils.inject(crawler.portal(), params);
			portal = portal.replace("\"", "");
			URL url = new URL(cid, portal, payload);
			execute(url);
		}
	}
	/**
	 * @param url
	 */
	private void executeWithKeyNot(URL url){
		poolLock.lock();
		try{
			queue.push(url);//retry
			notEmpty.signal();//TODO choose to use signal() or signalAll()?
		}finally{
			poolLock.unlock();
		}
	}
	/**
	 * fix the pool's capacity
	 */
	private void fixCapacity(){
		int actNum = num.get();
		long queueNum = this.queue.size();
		if(queueNum < 0 || actNum <0){
			this.isOpen = false;
			//TODO LOGGER
			logger.trace("error occured in pool: actNum:"+actNum+" queueNum:"+queueNum);
		}
		if(actNum < poolSize && actNum >0 & queueNum > 0 && actNum/queueNum < 1){
			logger.trace("create new thread:"+(queueNum - actNum));
			for(int i = 0;i<queueNum - actNum;i++){
				Worker worker = new Worker();
				new Thread(worker,DEFAULT_THREAD_PREFIX+nextThreadId()).start();
				num.incrementAndGet();
			}
		}
	}
	private synchronized int nextThreadId(){
		return ++threadinitTimes;
	}
	private class Worker implements Runnable {
		//start to run
		public void run() {
			try{
				while(true){
					URL url = null;
					poolLock.lock();
					boolean bool = true;
					try{
						while(queue.size() <= 0){
							try {
								bool =  notEmpty.await(maxFree,TimeUnit.MILLISECONDS);
								if(!bool){
									break;
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if(bool){
							url = queue.poll();
							fixCapacity();
						}
					}finally{
						poolLock.unlock();
					}
					if(!bool || url == null){//cannot get the URL from queue,destroy the thread automatically
						break;
					}
					//find the crawler's status from 'statuses' by cid
					CrawlerStatus status = statuses.get(url.getCid());
					if(null != status){
						if(status.status() != Status.RUNNING){
							status.status(Status.RUNNING);
						}
						CrawlerApi crawler = status.getCrawler();
						try{
							//the specific crawl process is in class Crawler
							if(null != url.getType()){
								if(url.getType().matches("html")){
									List<URL> urls = crawler.crawlHTML(url);
									for(int i = 0;i<urls.size();i++){
										URL tmp = urls.get(i);
										tmp.setCid(url.getCid());//set cid tag
										tmp.setPayload(url.getPayload());//add payload message
										execute(tmp);
									}
								}else if(url.getType().matches("(photo)|(video)")){
									crawler.crawlMedia(url, url.getLocal());
								}else{
									throw new RuntimeException("wrong");
								}
								status.finish(url);//finish the work
							}
						}catch(Exception ex){
							Throwable root = ex;
							if(url.getRetry() >= maxRetry){
								logger.info("retry limited!\n"+root.getMessage());
							}else{
								url.setRetry(url.getRetry() + 1);//increase simplify
								while( root.getCause() != null){
									root = root.getCause();
								}
								if(root instanceof SocketTimeoutException){
									logger.info("timeout exception occured: url:->"+url.getUrl()+" local:"+url.getLocal());
									executeWithKeyNot(url);
								}else if(root instanceof ProtocolException){
									logger.info("protocal exception : \n"+root.getMessage());
								}else if(root instanceof IOException){
									logger.info("io error occured: url:->"+url.getUrl()+" local:"+url.getLocal());
									executeWithKeyNot(url);
								}else{
									ex.printStackTrace();
								}
							}
						}
					}
					if(!isOpen){//check available at last
						System.out.println("队列里的值:"+queue.size());
						break;
					}
				}
			}finally{
				num.decrementAndGet();//exists!
				if(num.get() == 0){
					shutdown();
				}
			}
		}
	}
	public Object crawled(String url) {
		return queue.local(url);
	}
	public boolean isUseMapDB() {
		return useMapDB;
	}
	public void setUseMapDB(boolean useMapDB) {
		this.useMapDB = useMapDB;
	}
	public Map<String, CrawlerApi> getCrawlers() {
		return crawlers;
	}
	public void setCrawlers(Map<String, CrawlerApi> crawlers) {
		this.crawlers = crawlers;
	}
	
	public Integer getPoolSize() {
		return poolSize;
	}
	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}
	public Integer getMaxFree() {
		return maxFree;
	}
	public void setMaxFree(Integer maxFree) {
		this.maxFree = maxFree;
	}
	public List<Monitor> getMonitors() {
		return monitors;
	}
	public void setMonitors(List<Monitor> monitors) {
		this.monitors = monitors;
	}
	public int getMaxRetry() {
		return maxRetry;
	}
	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}
	
}
