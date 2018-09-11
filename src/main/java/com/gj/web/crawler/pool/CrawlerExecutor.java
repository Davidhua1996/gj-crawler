package com.gj.web.crawler.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.gj.web.crawler.CrawlerApi;
import com.gj.web.crawler.CrawlerDao;
import com.gj.web.crawler.delay.CrawlerDelay;
import com.gj.web.crawler.delay.CrawlerDelayDao;
import com.gj.web.crawler.delay.CrawlerDelayTask;
import com.gj.web.crawler.delay.DelayConsumer;
import com.gj.web.crawler.delay.DelayProvider;
import com.gj.web.crawler.parse.Parser;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.pool.exc.ExcReportStore;

/**
 * the executor of all the crawlers,
 * actually, it is an extension of CrawlerThreadPoolImpl
 * and a bridge between crawler component and other framework like 'Spring'
 * @author David
 *
 */
public class CrawlerExecutor implements CrawlerThreadPool,InitializingBean{
	private static final Logger logger = LogManager.getLogger(Parser.class);
	private static final long DEFAULT_DELAY_PROVIDER_INTERVAL = 5000;
	/**
	 * Data Access Object for Crawler
	 */
	protected CrawlerDao dao = null;
	protected CrawlerDelayDao delayDao = null;
	protected DelayConsumer delayConsumer = null;
	protected DelayProvider delayProvider = null;
	protected CrawlerThreadPoolImpl pool = new CrawlerThreadPoolImpl();
	protected DelayQueue<CrawlerDelayTask> delayqueue = new DelayQueue<CrawlerDelayTask>();
	private volatile boolean closed = false;
	private Timer provider = new Timer("crawl-delay-task-provider");
	public void open() {
		pool.open();
	}

	public boolean isOpen() {
		return pool.isOpen();
	}

	public void shutdown() {
		pool.shutdown();
	}

	public void execute(String cid) {
		pool.execute(cid);
	}
	public void execute(URL url) {
		pool.execute(url);
	}

	@Override
	public void addCrawler(CrawlerApi crawlerApi) {
			pool.addCrawler(crawlerApi);
	}

	public void execute(String cid, byte[] payload) {
		pool.execute(cid, payload);
	}
	public void execute(String cid, Object[] params) {
		pool.execute(cid, params);
	}

	public void execute(String cid, Map<String, Object> params) {
		pool.execute(cid, params);
	}
	
	public void execute(String cid, byte[] payload, Object[] params) {
		pool.execute(cid, payload, params);
	}

	public void execute(String cid, byte[] payload, Map<String, Object> params) {
		pool.execute(cid, payload, params);
	}

	@Override
	public void executeWithKeyNot(URL url) {
		pool.executeWithKeyNot(url);
	}

	public Map<String, CrawlerApi> getCrawlers() {
		return pool.getCrawlers();
	}

	public void setCrawlers(Map<String, CrawlerApi> crawlers) {
		pool.setCrawlers(crawlers);
	}

	public void setPoolSize(Integer size) {
		pool.setPoolSize(size);
	}

	public Integer getPoolSize() {
		return pool.getPoolSize();
	}

	public void setMaxFree(Integer free) {
		pool.setMaxFree(free);
	}
	
	public Integer getMaxFree() {
		return pool.getMaxFree();
	}
	
	public CrawlerDao getDao() {
		return dao;
	}

	public void setDao(CrawlerDao dao) {
		this.dao = dao;
	}
	
	public CrawlerDelayDao getDelayDao() {
		return delayDao;
	}

	public void setDelayDao(CrawlerDelayDao delayDao) {
		this.delayDao = delayDao;
	}
	
	public DelayConsumer getDelayConsumer() {
		return delayConsumer;
	}

	public void setDelayConsumer(DelayConsumer delayConsumer) {
		this.delayConsumer = delayConsumer;
	}

	public List<CrawlerApi> loadAll(){
		List<CrawlerApi> crawlers = new ArrayList<CrawlerApi>();
		if(null != this.dao){
			crawlers = this.dao.loadAll();
		}
		return crawlers;
	}
	public List<CrawlerDelay> loadDelayAll(){
		List<CrawlerDelay> delays = new ArrayList<CrawlerDelay>();
		if(null != this.delayDao){
			delays = this.delayDao.loadAllDelays();
		}
		return delays;
	}
	public void setUseMapDB(boolean DB) {
		pool.setUseMapDB(DB);
	}

	public boolean isUseMapDB() {
		return pool.isUseMapDB();
	}

	@Override
	public void addMonitor(Monitor monitor) {
		this.pool.addMonitor(monitor);
	}

	/**
	 * start to open the pool after setting the properties
	 */
	public void afterPropertiesSet() throws Exception {
		if(!pool.isOpen()){
			Map<String, CrawlerApi> map = pool.getCrawlers();
			List<CrawlerApi> crawlers = loadAll();
			for(int i = 0; i < crawlers.size(); i++){
				CrawlerApi crawler = crawlers.get(i);
				map.put(String.valueOf(crawler.getId()), crawler);
			}
			List<CrawlerDelay> delays = loadDelayAll();
			for(int i = 0; i < delays.size(); i++){
				CrawlerDelayTask delayTask = new CrawlerDelayTask(delays.get(i));
				logger.trace("delay task creating... cralwer " + delayTask.getDelay().getCid());
				delayqueue.offer(delayTask);
			}
			onDelayConsumer();
			onDelayProvider();
			open();
		}
	}
	private void onDelayConsumer(){
		if(null != delayConsumer){
			new Thread(new Runnable() {
				public void run() {
					while(!closed){
						CrawlerDelayTask delayTask;
						try {
							delayTask = delayqueue.take();
							logger.trace("delay task running... cralwer " + delayTask.getDelay().getCid());
							delayConsumer.consume(delayTask);
							if(delayTask.getExpire(TimeUnit.MINUTES) > 0){
								logger.trace("delay task creating... cralwer " + delayTask.getDelay().getCid());
								delayqueue.offer(new CrawlerDelayTask(delayTask.getDelay()));
							}
							delayTask = null;
						} catch (Exception e) {
							logger.info("exception happened in consuming crawler's delay-task queue", e);
						}
					}
				}
			},"crawl-delay-task-consumer").start();
		}
	}
	private void onDelayProvider(){
		if(null != delayProvider){
			long interval = delayProvider.interval();
			if(interval <= 0){
				interval = DEFAULT_DELAY_PROVIDER_INTERVAL;
			}
			provider.schedule(new TimerTask() {
				@Override
				public void run() {
					try{
						List<CrawlerDelay> delays = delayProvider.provide();
						if(null != delays && delays.size() > 0){
							for(int i = 0; i < delays.size(); i++){
								CrawlerDelayTask delayTask = new CrawlerDelayTask(delays.get(i));
								if(delayTask.getExpire(TimeUnit.MINUTES) > 0){
									logger.trace("delay task creating... cralwer " + delayTask.getDelay().getCid());
									delayqueue.offer(delayTask);
								}
							}
						}
					}catch(Exception e){
						logger.info("exception happened in providing crawler's delay-task queue", e);
					}
				}
			}, interval, interval);
		}
	}
	/**
	 * important to realse resource
	 */
//	@PreDestroy
	public void destory(){
		closed = true;
		provider.cancel();
	}
	public void addDelay(CrawlerDelay delay){
		delayqueue.offer( new CrawlerDelayTask(delay));
	}
	public List<Monitor> getMonitors() {
		return pool.getMonitors();
	}

	public void setMonitors(List<Monitor> monitors) {
		this.pool.setMonitors(monitors);
	}

	public Object executeIfAbsent(URL url) {
		return this.pool.executeIfAbsent(url);
	}

	public int getMaxRetry() {
		return this.pool.getMaxRetry();
	}

	public void setMaxRetry(int maxRetry) {
		this.pool.setMaxRetry(maxRetry);
	}

	public DelayProvider getDelayProvider() {
		return delayProvider;
	}

	public void setDelayProvider(DelayProvider delayProvider) {
		this.delayProvider = delayProvider;
	}

	public long getWorkQueueLen() {
		return this.pool.getWorkQueueLen();
	}
	public long getActiveInterval() {
		return this.pool.getActiveInterval();
	}

	public void setActiveInterval(long activeInterval) {
		this.pool.setActiveInterval(activeInterval);
	}

	public ExcReportStore getExcReportStore() {
		return this.pool.getExcReportStore();
	}
	
}
