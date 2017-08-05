package com.gj.web.crawler.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * inherit FileStoreStrategy 
 * to add the exclude and include filters of tags 
 * @author David
 *
 */
public class FileXmlStoreStrategy extends FileStoreStrategy{
	/**
	 * exclude tags array 
	 */
	private String[] excludes;
	/**
	 * include tags array 
	 */
	private String[] includes;
	@Override
	public void localStore(String content, String loc) {
		Document root = Jsoup.parse(content);
		Elements doc = root.body().getAllElements();
		doc = doInFilter(doc);
		root.body().html(doc.outerHtml());
		super.localStore(root.outerHtml(), loc);
	}
	@Override
	public void localStore(InputStream in, String loc) {
		try {
			Document root = Jsoup.parse(in, "UTF-8","");
			Elements doc = root.body().getAllElements();
			doc = doInFilter(doc);
			root.body().html(doc.outerHtml());
			super.localStore(root.outerHtml(), loc);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public String[] getExcludes() {
		return excludes;
	}
	public void setExcludes(String[] excludes) {
		this.excludes = compress(excludes);
	}
	public String[] getIncludes() {
		return includes;
	}
	public void setIncludes(String[] includes) {
		this.includes = compress(includes);
	}
	/**
	 * merge the override value
	 * @param array
	 * @return
	 */
	private String[] compress(String[] array){
		Set<String> set = new HashSet<String>();
		for(int i = 0; i < array.length; i++){
			set.add(array[i]);
		}
		return set.toArray(new String[set.size()]);
	}
	private Elements doInFilter(Elements doc){
		Elements filterEl = doc;
		if(null != includes){
			filterEl = doInFilter(filterEl, includes, true);
		}
		if(null != excludes){
			filterEl = doInFilter(filterEl, excludes, false);
		}
		return filterEl;
	}
	private Elements doInFilter(Elements doc, String[] tags, boolean include){
		String select = "";
		for(int i = 0; i < tags.length; i ++){
			select += tags[i];
			if(i != tags.length -1){
				select += ",";
			}
		}
		if(include){
			Elements els = new Elements();
			els.addAll(doc.select(select));
			return els;
		}else{
			doc.select(select).remove();
			return doc;
		}
	}
}
