package com.gj.web.crawler.parse;

import java.util.Map;
import java.util.Map.Entry;

import com.gj.web.crawler.Crawler;

public class DefaultHTMLParser extends CrawlHTMLParser {
	public DefaultHTMLParser(){
		
	}
	/**
	 * EN:suggest that if you do some time-consuming work in the method,
	 * you'd better to do in new thread
	 * CN:建议如果做一些耗时的工作(在callback方法中),最好在线程中完成(为了不阻塞队列)
	 */
	public void callback(ResultModel result) {
		Map<String,Object[]> inner = result.getInnerMap();
		if(result.getString("picture") == null){
//			System.out.println(result.getString("_url"));
		}
		System.out.println(result.getString("_url"));
		for(Entry<String,Object[]> entry : inner.entrySet()){
//			System.out.println(entry.getKey()+":  "+entry.getValue()[0].toString());
		}
	}
	public void setCrawler(Crawler crawler) {
		
	}
}
