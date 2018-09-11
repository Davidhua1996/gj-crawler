package com.gj.web.crawler.pool;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gj.web.crawler.pool.basic.*;
import com.gj.web.crawler.pool.exc.ProxyForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.CrawlerStatus;
import com.gj.web.crawler.lifecycle.BasicLifecycle.Status;
import com.gj.web.crawler.pool.exc.ExcReport;
import com.gj.web.crawler.pool.exc.ExcReportStore;
import com.gj.web.crawler.pool.exc.IMExcReportStore;
import com.gj.web.crawler.utils.InjectUtils;

import javax.swing.table.AbstractTableModel;

/**
 * do make a single pool to manager the crawler threads
 * @author David
 *
 */
public class CrawlerThreadPoolImpl implements CrawlerThreadPool{

	private static final String DEFAULT_POOL_ID = "default";

	private static final int MAX_POOL_SIZE = 100;
	
	private static final int MAX_FREE_TIME = 1000*30;
	
	private static final int DEFAULT_RETRY_COUNT = 10;
	
	private static final long DEFAULT_ACTIVE_INTERVAL = 60*5;
	
	private static final Logger logger = LogManager.getLogger(CrawlerThreadPool.class);
	
	private static final String DEFAULT_THREAD_PREFIX = "http-crawl-exec-";
	
	private static final String DEFUALT_ACTIVE_NAME = "http-crawl-active-";
	
	private static CrawlerThreadPool pool;

	private String id = DEFAULT_POOL_ID;

	private volatile int threadinitTimes = 0;
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
	
	private boolean useMapDB = true;

	private boolean dereplicate = true;

	private int derepExpire = -1;

	private AbstractedQueue<URL> queue = new IMMultQueue<>();

	private int maxRetry = DEFAULT_RETRY_COUNT;
	
	private long activeInterval = DEFAULT_ACTIVE_INTERVAL;
	
	private Thread activeRun = null;
	/**
	 * store crawlers
	 */
	private Map<String, CrawlerApi> crawlers = new ConcurrentHashMap<>();
	/**
	 * store status
	 */
	private Map<String, CrawlerStatus> statuses = new ConcurrentHashMap<>();
	/**
	 * lock
	 */
	private ReentrantLock poolLock = new ReentrantLock();
	
	private Condition notEmpty = poolLock.newCondition();
	/**
	 * to store exception 
	 */
	private ExcReportStore excReportStore = new IMExcReportStore();
	/**
	 * switch
	 */
	private volatile boolean isOpen = false;
	/**
	 * monitors
	 */
	private List<Monitor> monitors = new ArrayList<>();
	
	protected CrawlerThreadPoolImpl(){
		this(true, MAX_POOL_SIZE);
	}
	protected CrawlerThreadPoolImpl(boolean useMapDB, int poolSize){
		this.useMapDB = useMapDB;
		this.poolSize = poolSize;
	}
	public static CrawlerThreadPool getInstance(){
		return getInstance(true, MAX_POOL_SIZE);
	}

	/**
	 * when you want multi-instances
	 * @return
	 */
	public static CrawlerThreadPool newInstance(String id, boolean useMapDB, int poolSize){
		return newInstance(id, useMapDB, poolSize, -1);
	}
	public static CrawlerThreadPool newInstance(String id, boolean useMapDB, int poolSize, int derepExpire){
		CrawlerThreadPoolImpl newPool = new CrawlerThreadPoolImpl(useMapDB, poolSize);
		newPool.id = id;
		newPool.derepExpire = derepExpire;
		return newPool;
	}
	public static CrawlerThreadPool getInstance(boolean useMapDB, int poolSize){
		if(null == pool){
			synchronized(CrawlerThreadPool.class){
				if(null == pool){
					pool = new CrawlerThreadPoolImpl(useMapDB, poolSize);
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
				queue.setDerepExpire(derepExpire);
				queue.setDereplicate(dereplicate);
			}
			try{
				for(int i = 0;i < monitors.size();i++){
					monitors.get(i).open(this);
				}
			}catch(Exception e){
				logger.error("monitor open() method occurred error ,msg = " + e.getMessage());
			}
			//open threads
			//check if the "active thread" is still running
			if(activeRun != null && activeRun.isAlive()){
				activeRun.interrupt();//destroy it
				activeRun = null;
			}
			activeRun = new Thread(new ActiveRunner(this), DEFUALT_ACTIVE_NAME + id);
			activeRun.start();
			int initialSize = num.get();// that means threads in pool didn't close totally before
			for(int i = initialSize ;i < poolSize;i++ ){
				Worker worker = new Worker();
				new Thread(worker,DEFAULT_THREAD_PREFIX + id + "-" +nextThreadId()).start();
				num.incrementAndGet();
			}
			//start to crawl
			for(Entry<String,CrawlerApi> entry : crawlers.entrySet()){
				CrawlerApi crawler = entry.getValue();
				if(null != crawler.getParser()){
					crawler.getParser().setCrawlPool(this);
				}
				String cid = entry.getKey();
				//set crawler status
				CrawlerStatus status = statuses.get(entry.getKey());
				if(null == status){
					status = new CrawlerStatus(entry.getValue());
					status.initalize();
					statuses.put(entry.getKey(), status);
				}
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
			threadinitTimes = 0;
			queue.clear();//clear the cache,release the memory
			for(Entry<String, CrawlerStatus> entry : statuses.entrySet()){
				CrawlerStatus status = entry.getValue();
				if(status.status() != Status.CLOSE){//if status != Status.CLOSE, call close method
					status.shutdown();
				}
			}
			try{
				for(int i = 0;i < monitors.size();i++){
					monitors.get(i).close(this);
				}
			}catch(Exception e){
				logger.error("monitor close() method occurred error ,msg = " + e.getMessage());
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
			return;
		}
		if(status.status() != Status.OPEN){
			status.open();
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

	@Override
	public void addCrawler(CrawlerApi crawlerApi) {
		this.crawlers.put(String.valueOf(crawlerApi.getId()), crawlerApi);
	}

	public Object executeIfAbsent(URL url){
		Object loc = null;
		if(!isOpen) open();//open
		CrawlerStatus status = statuses.get(url.getCid());
		if(null == status){
			logger.info("couldn't find the right crawler to execute the url");
		}
		if(status.status() != Status.OPEN){
			status.open();
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
			executeWithKeyNot(url);
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
			executeWithKeyNot(url);
		}
	}
	
	public void execute(String cid, byte[] payload, Map<String, Object> params) {
		CrawlerApi crawler = crawlers.get(cid);
		if(null != crawler && crawler.isUseParams()){
			String portal = InjectUtils.inject(crawler.portal(), params);
			portal = portal.replace("\"", "");
			URL url = new URL(cid, portal, payload);
			executeWithKeyNot(url);
		}
	}
	/**
	 * @param url
	 */
	public void executeWithKeyNot(URL url){
		Object loc = null;
		if(!isOpen) open();//open
		CrawlerStatus status = statuses.get(url.getCid());
		if(null == status){
			logger.info("couldn't find the right crawler to execute the url");
		}
		if(status.status() != Status.OPEN){
			status.open();
		}
		status.addWork(url);
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
			int min = (int)Math.min(queueNum - actNum, poolSize - actNum);
			logger.trace("create new thread:"+ min);
			for(int i = 0;i< min && num.get() <= poolSize;i++){
				Worker worker = new Worker();
				new Thread(worker,DEFAULT_THREAD_PREFIX + id + "-" + nextThreadId()).start();
				num.incrementAndGet();
			}
		}
	}
	private synchronized int nextThreadId(){
		return ++threadinitTimes;
	}
	private class ActiveRunner implements Runnable{
		private CrawlerThreadPool pool;
		public ActiveRunner(CrawlerThreadPool pool){
			this.pool = pool;
		}
		public void run() {
			while(true){
				try{
					TimeUnit.SECONDS.sleep(activeInterval);
					if(!isOpen){
						break;
					}
					for(int i = 0;i < monitors.size(); i ++){
						monitors.get(i).active(pool);
					}
				}catch(Exception e){
					logger.error(Thread.currentThread().getName() + " interrupted while running ,msg = " + e.getMessage());
				}
			}
		}
		
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
						long executeTime = System.currentTimeMillis();
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
								executeTime = System.currentTimeMillis() - executeTime;
								//limited the speed of thread and release the cpu source
								long sleep = executeTime > 1000?50:1000 - executeTime;
								try {
									Thread.sleep(sleep);
								} catch (InterruptedException e) {
									logger.error(e);
								}
							}
						}catch(Exception ex){
							Throwable root = ex;
							if(ex instanceof ProxyForbiddenException){
//								ex.printStackTrace();
								logger.info(ex.getMessage() + " PROXY:" + url.getProxy());
								if(StringUtils.isEmpty(url.getProxy())){
									//if use the dynamic proxy, try again
									executeWithKeyNot(url);
								}
							}else if(url.getRetry() >= maxRetry){
								logger.error("retry limited!\n"+root.getMessage());
								excReportStore.add(new ExcReport(url, ex));
							}else{
								url.setRetry(url.getRetry() + 1);//increase simplify
								while( root.getCause() != null){
									root = root.getCause();
								}
								if(root instanceof SocketTimeoutException){
									logger.info("timeout exception occurred: url:->"+url.getUrl()+" local:"+url.getLocal()+" msg:"+root.getMessage());
									executeWithKeyNot(url);
								}else if(root instanceof ProtocolException){
									logger.info("protocol exception : \n"+root.getMessage());
								}else if(root instanceof IOException){
									logger.info("io error occurred: url:->"+url.getUrl()+" local:"+url.getLocal()+" msg:"+root.getMessage());
									executeWithKeyNot(url);
								}else{
									logger.error("unknown error occurred: " + ex.getMessage());
									excReportStore.add(new ExcReport(url, ex));
								}
							}
						}
					}
					if(!isOpen){//check available at last
						if(logger.isInfoEnabled()){
							logger.info("the pool is ready to close,queue size is "+queue.size());
						}
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

	@Override
	public void addMonitor(Monitor monitor) {
		this.monitors.add(monitor);
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
	public long getWorkQueueLen() {
		return queue.size();
	}
	public ExcReportStore getExcReportStore(){
		return excReportStore;
	}
	public long getActiveInterval() {
		return activeInterval;
	}
	public void setActiveInterval(long activeInterval) {
		this.activeInterval = activeInterval;
	}

	public boolean isDereplicate() {
		return dereplicate;
	}

	public void setDereplicate(boolean dereplicate) {
		this.dereplicate = dereplicate;
	}

	public int getDerepExpire() {
		return derepExpire;
	}

	public void setDerepExpire(int derepExpire) {
		this.derepExpire = derepExpire;
	}
}
