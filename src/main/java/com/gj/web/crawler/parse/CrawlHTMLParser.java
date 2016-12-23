package com.gj.web.crawler.parse;

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
	protected String tmpDir = TEMP;
	
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
		model.putAndAdd("_url", url.getUrl());//put URL to identify the result
		for(Entry<String,Object> entry : patterns.entrySet()){
			Pattern pattern = (Pattern) entry.getValue();
			String key = entry.getKey();
			String exp = pattern.getExp();
			String type = pattern.getType();
			if(null == type){ // do you forget the type?
				type = "text";
			}
			Elements elements = doc.select(exp);
			for(int i = 0 ;i<elements.size(); i++){
				Element el = elements.get(i);
				String value = null;
				if(type.matches("(photo)|(video)")){
					value = el.attr("src");
					if(pattern.isDownload()){
						URL src = new URL(url.getDomain(), value);//dont't forget domain
						src.setType(type);
//						src.setLocal(tmpDir+"/"+value.substring(value.indexOf("apps/")+5,value.lastIndexOf("/")-1));
						value = tmpDir+"/"+DataUtils.randomName();//randomly create file path for downloading
						src.setLocal(value);
//						crawler.crawlMedium(src, value);
						CrawlerThreadPoolImpl.getInstance().execute(src);//throw into crawler queue 
					}
				}else if(type.matches("text")){
					value = el.html();//not to use method text() for keeping the HTML tag
				}else if(type.matches("html")){
					value = el.attr("href");
					if(pattern.isDownload()){
						URL href = new URL(url.getDomain(), value);//dont't forget domain
						href.setType(type);
						CrawlerThreadPoolImpl.getInstance().execute(href);
					}
				}else{
					throw new RuntimeException("unknow type:"+type+" in pattern!");
				}
				model.putAndAdd(key,value);
			}
		}
		//invoke callback method
		callback(model);
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
