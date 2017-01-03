package com.gj.web.crawler.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.gj.web.crawler.http.utils.DataUtils;
import com.gj.web.crawler.parse.json.JsonUtils;
import com.gj.web.crawler.parse.json.Pattern;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;
import com.gj.web.crawler.pool.basic.URL;

public abstract class CrawlHTMLParser implements Parser{
	private final String TEMP = "/usr/tmp"; 
	/**
	 * whether use thread pool
	 */
	protected boolean usePool = true;
	
	/**
	 * the local path to store temp file
	 */
	protected String dir = TEMP;
	
	protected ExecutorService pool = Executors.newCachedThreadPool();
	
	protected Map<String,Object> patterns = new ConcurrentHashMap<String,Object>();
	
	public void parse(String html,URL url) {
		parse(Jsoup.parse(html),url);
	}
	public void parse(final Document doc,final URL url) {
		if(null != doc){
			if(usePool){
				pool.execute(new Runnable() {
					public void run() {
						parseMain(doc,url);
					}
				});
			}else{
				parseMain(doc,url);
			}
		}
	}
	private void parseMain(Document doc,URL url) {
		ResultModel model = new ResultModel();
		model.putAndAdd("_url", url.getUrl());//put URL to identify the result by default
		Map<String,URL> urlMap = new HashMap<String,URL>();//to store the URL from parsing which is needed to crawl
		String subDir = "/";//the subDir of medias
		for(Entry<String,Object> entry : patterns.entrySet()){
			Pattern pattern = (Pattern) entry.getValue();
			String key = entry.getKey();
			String exp = pattern.getExp();
			String type = pattern.getType() != null?pattern.getType():"text"; // do you forget the type?
			String attr = pattern.getAttr();
			Elements elements = doc.select(exp);
			if(pattern.isIdentify()){
				String value = parseElement(elements.get(0), type, attr);
				subDir += "_"+value;
				model.putAndAdd(key, value);
			}else{
				for(int i = 0;i<elements.size();i++){
					String value = parseElement(elements.get(i),type,attr);
					if(pattern.isDownload() && type.matches("(photo)|(video)")){
						URL src = new URL(url.getDomain(),value);
						src.setType(type);
						urlMap.put(key+"_"+i, src);
					}else{
						model.putAndAdd(key, value);
					}
				}
			}
		}
		for(Entry<String,URL> entry : urlMap.entrySet()){
			URL src = entry.getValue();
			if(! subDir.equals("/") && subDir.length() > 1){
				subDir += "/";
			}
			String loc = dir + subDir + DataUtils.randomName();
			new File(dir + subDir).mkdirs();
			src.setLocal(loc);
			model.putAndAdd(entry.getKey(),loc);
			CrawlerThreadPoolImpl.getInstance().execute(src);
		}
		//invoke callback method
		callback(model);
	}
	private String parseElement(Element el,String type,String attr){
		String value = null;
		if(type.matches("(photo)|(video)")){
			value = el.attr(attr!=null?attr:"src");
		}else if(type.matches("text")){
			value = el.html();//not to use method text() for keeping the HTML tag
		}else if(type.matches("html")){
			value = el.attr(attr!=null?attr:"href");
		}else{
			throw new RuntimeException("unknow type:"+type+" in pattern!");
		}
		return value;
	}
	public boolean isUsePool() {
		return usePool;
	}

	public void setUsePool(boolean usePool) {
		this.usePool = usePool;
	}

	public Map<String, Object> getPatterns() {
		return patterns;
	}

	public void setPatterns(Map<String, String> patterns) {
		this.patterns.clear();
		for(Entry<String,String> entry : patterns.entrySet()){
			String json = entry.getValue();
			Pattern pattern = JsonUtils.fromJson(json, Pattern.class);
			this.patterns.put(entry.getKey(), pattern);
		}
	}
	
}
