package com.gj.web.crawler.pool;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.gj.web.crawler.Crawler;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.parse.DefaultHTMLParser;
import com.gj.web.crawler.parse.DefaultHTMLParser;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.basic.IMQueue;
import com.gj.web.crawler.pool.basic.Queue;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.utils.InjectUtils;

/**
 * do make a single pool to manager the crawler threads
 * @author David
 *
 */
public class CrawlerThreadPoolImpl implements CrawlerThreadPool{
	
	private static final int MAX_POOL_SIZE = 100;
	
	private static final int MAX_FREE_TIME = 1000*15;
	
	private static CrawlerThreadPool pool;
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
	
	private Queue<URL> queue = new IMQueue<URL>();
	
	private boolean useMongoQueue = false;
	/**
	 * store crawlers
	 */
	private Map<String,CrawlerApi> crawlers = new ConcurrentHashMap<String,CrawlerApi>();
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
			if(useMongoQueue){
				//TODO make a queue to connect MongoDB;
			}
			//open threads
			isOpen = true;
			for(int i = 0;i < monitors.size();i++){
				monitors.get(i).open(this);
			}
			for(int i = 0;i < poolSize;i++ ){
				Worker worker = new Worker();
				new Thread(worker).start();
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
			}
		}
	}
	public boolean isOpen() {
		return false;
	}

	public synchronized void shutdown() {
		if(num.get() == 0){
			isOpen = false;
			queue.clear();//clear the cache,release the memory
			for(int i = 0;i < monitors.size();i++){
				monitors.get(i).close(this);
			}
		}
	}
	public void execute(String cid) {
		CrawlerApi crawler = crawlers.get(cid);
		if(null != crawler && !crawler.isUseParams()){
			URL url = new URL(cid,crawler.portal());
			execute(url);
		}
	}
	public void execute(URL url) {
		poolLock.lock();
		try{
			queue.pushWithKey(url,url.getUrl());//duplicate removal
			fixCapacity();
			notEmpty.signal();//TODO choose to use signal() or signalAll()?
		}finally{
			poolLock.unlock();
		}
	}
	public void execute(String cid, Object... params) {
		CrawlerApi crawler = crawlers.get(cid);
		if(null != crawler && crawler.isUseParams()){
			String portal = InjectUtils.inject(crawler.portal(), params);
			URL url = new URL(cid, portal);
			execute(url);
		}
	}
	public void execute(String cid, Map<String, Object> params) {
		CrawlerApi crawler = crawlers.get(cid);
		if(null != crawler && crawler.isUseParams()){
			String portal = InjectUtils.inject(crawler.portal(), params);
			URL url = new URL(cid, portal);
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
			fixCapacity();
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
		if(actNum < poolSize && queueNum > 0 && actNum/queueNum < 1){
			Worker worker = new Worker();
			new Thread(worker).start();
			num.incrementAndGet();
		}
	}
	private class Worker implements Runnable {
		//start to run
		public void run() {
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
					}
				}finally{
					poolLock.unlock();
				}
				if(!bool || url == null){//cannot get the URL from queue,destroy the thread automatically
					break;
				}
				//find the crawler from 'crawlers' by cid
				CrawlerApi crawler = crawlers.get(url.getCid());
				if(null != crawler){
					//the specific crawl process is in class Crawler
					try{
						if(null != url.getType()){
							if(url.getType().matches("html")){
								List<URL> urls = crawler.crawlHTML(url);
								for(int i = 0;i<urls.size();i++){
									URL tmp = urls.get(i);
									tmp.setCid(url.getCid());//set cid tag
									execute(tmp);
								}
							}else if(url.getType().matches("(photo)|(video)")){
								crawler.crawlMedia(url, url.getLocal());
							}
						}
					}catch(Exception ex){
						Throwable root = ex;
						while( root.getCause() != null){
							root = root.getCause();						}
						if(root instanceof SocketTimeoutException){
							System.out.println("timeout error occured: url:->"+url.getUrl()+" local:"+url.getLocal());
							executeWithKeyNot(url);
						}else if(root instanceof IOException){
							System.out.println("io error occured: url:->"+url.getUrl()+" local:"+url.getLocal()+" \nmessage:"+root.getMessage());
							executeWithKeyNot(url);
						}else{
							ex.printStackTrace();
						}
					}
				}
				if(!isOpen){//check available at last
					break;
				}
			}
			num.decrementAndGet();//exists!
			if(num.get() == 0){
				shutdown();
			}
		}
	}
	public boolean isUseMongoQueue() {
		return useMongoQueue;
	}
	
	public void setUseMongoQueue(boolean useMongoQueue) {
		this.useMongoQueue = useMongoQueue;
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
}
