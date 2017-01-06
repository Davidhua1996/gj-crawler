package com.gj.web.crawler.parse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
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

public class DefaultHTMLParser implements Parser,Serializable{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8908288905050348183L;

	private Callback callback = new DefaultCallback();
	
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
		Map<String,URL> urlMap = new HashMap<String,URL>();//to store the URL from parsing which is needed to download
		String subDir = "/";//the subDir of medias
		for(Entry<String,Object> entry : patterns.entrySet()){
			Pattern pattern = (Pattern) entry.getValue();
			String key = entry.getKey();
			String exp = pattern.getExp();
			String type = pattern.getType() != null?pattern.getType():"text"; // do you forget the type?
			String attr = pattern.getAttr();
			Elements elements = doc.select(exp);
			if(pattern.isIdentify() && elements.size() > 0){
				String value = parseElement(elements.get(0), type, attr);
				value = value.replaceAll("[\\/:*?\"<>|]", " ");
				subDir += (subDir.equals("/")?value:"_"+value); 
				model.putAndAdd(key, value);
			}else{
				for(int i = 0;i<elements.size();i++){
					String value = parseElement(elements.get(i),type,attr);
					if(pattern.isDownload() && type.matches("(photo)|(video)|(html)|(text)")){
						URL src = new URL(url.getCid(),value);
						src.setType(type);
						urlMap.put(key+"_"+i, src);
					}else{
						model.putAndAdd(key, value);
					}
				}
			}
		}
		int index = 0;
		for(Entry<String,URL> entry : urlMap.entrySet()){
			URL src = entry.getValue();
			String value = src.getUrl();
			if(! subDir.equals("/") && subDir.length() > 1){
				subDir += "/";
			}
			String loc = null;
			new File(dir + subDir).mkdirs();
			String key = entry.getKey();
			key = key.substring(0,key.indexOf("_"));
			if(src.getType().matches("text")){// for type 'text',save 'value' as 'content' directly
				loc = dir + subDir +DataUtils.randomHTTPFileName(null, index);
				localStore(value,loc);
			}else{//else put into the pool to crawler
				loc = dir + subDir + DataUtils.randomHTTPFileName(value, index);
				src.setLocal(loc);
				CrawlerThreadPoolImpl.getInstance().execute(src);
			}
			model.putAndAdd(key,loc);
			index ++;
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
	private void localStore(String content,String loc){//save 'content' directly
		FileOutputStream out = null;
		ByteBuffer buf = null;
		try{
			byte[] b = content.getBytes("UTF-8");
			buf = ByteBuffer.wrap(b);
			File file = new File(loc);
			file.createNewFile();
			out = new FileOutputStream(file);
			out.getChannel().write(buf);
			buf.clear();
		}catch(IOException e){
			System.out.println(loc);
			throw new RuntimeException(e);
		}finally{
			if(null != out)
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			buf.clear();
		}
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
	
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public void setPatterns(Map<String, String> patterns) {
		this.patterns.clear();
		for(Entry<String,String> entry : patterns.entrySet()){
			String json = entry.getValue();
			Pattern pattern = JsonUtils.fromJson(json, Pattern.class);
			this.patterns.put(entry.getKey(), pattern);
		}
	}
	public void callback(ResultModel result) {
		callback.callback(result);
	}
	
}
