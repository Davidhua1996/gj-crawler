package com.gj.web.crawler.pool.exc;

import com.gj.web.crawler.pool.basic.URL;

/**
 * <p>the report of exception</p>
 * @author David
 *
 */
public class ExcReport {
	protected String type;
	protected  String url;
	protected  String cid;
	protected  Exception excp;
	protected  byte[] payload;
	protected  String reason;
	public ExcReport(URL url, Exception excp){
		this.url = url.getUrl();
		this.excp = excp;
		this.cid = url.getCid();
		this.payload = url.getPayload();
		this.type = url.getType();
	}
	public ExcReport(){
		
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public Exception getExcp() {
		return excp;
	}
	public void setExcp(Exception excp) {
		this.excp = excp;
	}
	public byte[] getPayload() {
		return payload;
	}
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
