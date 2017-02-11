package com.gj.web.crawler.pool.basic;

public class URL {
	//the depth of URL 
	protected int depth = 0;
	protected String url;
	protected String cid;
	//equals content-type
	protected String type = "html";
	//private String description; - it is not necessary
	//0 - unavailable | 1 - available
	protected int status = 1;
	//store by timestamp,used to sort
	protected long order = 0;
	//the location on disk for downloading
	protected String local = null;
	//extended data
	protected byte[] payload = null;
	//to record the retry count
	protected volatile int retry = 0;
	//attached URL, usually the parent URL
	protected URL attached;
	public URL(String cid, String url){
		this.cid = cid;
		this.url = url;
		this.order = System.currentTimeMillis();
	}
	public URL(String cid, String url, int depth){
		this(cid,url);
		this.depth = depth;
	}
	public URL(String cid, String url, byte[] payload){
		this.cid = cid;
		this.url = url;
		this.payload = payload;
	}
	
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getOrder() {
		return order;
	}
	public void setOrder(long order) {
		this.order = order;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}
	public int getRetry() {
		return retry;
	}
	public void setRetry(int retry) {
		this.retry = retry;
	}
	public byte[] getPayload() {
		return payload;
	}
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	public URL getAttached() {
		return this.attached;
	}
	public void setAttached(URL attached) {
		this.attached = attached;
	}
	
}
