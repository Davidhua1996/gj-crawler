package com.gj.web.crawler.pool.basic;

public class URL {
	//maybe it is in needed in MongoDB?
	private String _id;
	private String url;
	private String cid;
	//equals content-type
	private String type = "html";
	//private String description; - it is not necessary
	//0 - push | 1 - poll
	private int status = 0;
	//store by timestamp,used to sort
	private long order = 0;
	//the location on disk for downloading
	private String local = null;
	public URL(String cid, String url){
		this.cid = cid;
		this.url = url;
		this.order = System.currentTimeMillis();
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
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
	
}
