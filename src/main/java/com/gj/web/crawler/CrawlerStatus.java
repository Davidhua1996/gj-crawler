package com.gj.web.crawler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.gj.web.crawler.lifecycle.BasicLifecycle;
import com.gj.web.crawler.lifecycle.Lifecycle;
import com.gj.web.crawler.pool.basic.URL;

/**
 * to record the status of crawler placing in pool
 */
public class CrawlerStatus extends BasicLifecycle{
	
	private CrawlerApi crawler;
	
	public CrawlerStatus(CrawlerApi crawler){
		this.crawler = crawler;
	}
	
	/**
	 * the number of works which don't finish
	 */
	private AtomicInteger workNum = new AtomicInteger(0);
	
	public int addWork(URL url){
		return workNum.incrementAndGet();
	}
	public int addWork(List<URL> url){
		return workNum.addAndGet(url.size());
	}
	public int finish(URL url){
		return workNum.decrementAndGet();
	}
	public int workNum(){
		return workNum.get();
	}
	@Override
	protected void openInternal() {
		((Lifecycle)crawler).open();
	}
	@Override
	protected void shutdownInternal() {
		((Lifecycle)crawler).shutdown();
	}
	@Override
	protected void initalInternal() {
		((Lifecycle)crawler).initalize();
	}
	public CrawlerApi getCrawler() {
		return crawler;
	}
	public void setCrawler(CrawlerApi crawler) {
		this.crawler = crawler;
	}
}
