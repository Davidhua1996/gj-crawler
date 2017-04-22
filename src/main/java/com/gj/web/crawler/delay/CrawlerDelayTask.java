package com.gj.web.crawler.delay;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * crawler delay task
 * @author David
 */
public class CrawlerDelayTask implements Delayed{
	private CrawlerDelay delay;
	private long activetime;
	public CrawlerDelayTask(CrawlerDelay delayVo){
		this.delay = delayVo;
		this.activetime = System.currentTimeMillis() + delay.getDelayMinute() * 60000;
	}
	
	public int compareTo(Delayed o) {
		if(o instanceof CrawlerDelayTask){
			CrawlerDelayTask that = (CrawlerDelayTask)o;
			if(this.getDelay(TimeUnit.MILLISECONDS) > that.getExpire(TimeUnit.MILLISECONDS)){
				return 1;
			}
		}
		return (int)(getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
	}

	public long getDelay(TimeUnit unit) {
		return unit.convert(activetime - System.currentTimeMillis(),
				TimeUnit.MILLISECONDS);
	}
	public long getExpire(TimeUnit unit){
		if(null != delay.getExpire()){
			return unit.convert(delay.getExpire().getTime() - System.currentTimeMillis(),
					TimeUnit.MILLISECONDS);
		}
		return Long.MAX_VALUE;
	}
	public CrawlerDelay getDelay(){
		return delay;
	}
}
