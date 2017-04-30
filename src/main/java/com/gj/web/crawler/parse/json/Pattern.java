package com.gj.web.crawler.parse.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * the pattern of parsing
 * @author David
 */
@JsonInclude(Include.NON_EMPTY)
public class Pattern implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4274958096429127834L;
	
	private String exp;
	/**
	 * could be "photo","video"
	 */
	private String type = "text";
	/**
	 * whether should download to local disk 
	 */
	private boolean download = false;
	/**
	 * if is an identify value
	 */
	private boolean identify = false;
	/**
	 * the attribute of element to crawl,
	 * if type equals "photo|video" the value is "src" default
	 * else is "null"(to crawl the text content),
	 * you can set as you like
	 */
	private String attr = null;
	/**
	 * date pattern
	 */
	private String date;
	
	public String getExp() {
		return exp;
	}
	public void setExp(String exp) {
		this.exp = exp;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isDownload() {
		return download;
	}
	public void setDownload(boolean download) {
		this.download = download;
	}
	public String getAttr() {
		return attr;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}
	public boolean isIdentify() {
		return identify;
	}
	public void setIdentify(boolean identify) {
		this.identify = identify;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
}
