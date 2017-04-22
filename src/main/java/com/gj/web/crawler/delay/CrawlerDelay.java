package com.gj.web.crawler.delay;

import java.io.Serializable;
import java.util.Date;
/**
 * crawler delay configuration
 * @author David
 *
 */
public class CrawlerDelay implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6956540387420681235L;
	private long delayMinute;
	private Date expire;
	private Date createTime;
	private Object cid;
	public long getDelayMinute() {
		return delayMinute;
	}
	public void setDelayMinute(long delayMinute) {
		this.delayMinute = delayMinute;
	}
	public Date getExpire() {
		return expire;
	}
	public void setExpire(Date expire) {
		this.expire = expire;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Object getCid() {
		return cid;
	}
	public void setCid(Object cid) {
		this.cid = cid;
	}
}
