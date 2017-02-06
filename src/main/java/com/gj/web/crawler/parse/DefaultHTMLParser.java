package com.gj.web.crawler.parse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gj.web.crawler.http.utils.DataUtils;
import com.gj.web.crawler.parse.json.JsonUtils;
import com.gj.web.crawler.parse.json.Pattern;
import com.gj.web.crawler.pool.CrawlerThreadPoolImpl;
import com.gj.web.crawler.pool.basic.DBType;
import com.gj.web.crawler.pool.basic.URL;
import com.gj.web.crawler.utils.MapDBContext;

public class DefaultHTMLParser implements Parser,Serializable{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8908288905050348183L;
	
	private static final String DFAULT_DB_NAME = "temp";
	
	private static  HTreeMap<String,Object> pmap = null;
	
	private Object id = null;;
	
	@JsonIgnore
	private transient DB db = null;
	
	@JsonIgnore
	private transient Callback callback = new DefaultCallback();
	
	private List<Object> store = new CopyOnWriteArrayList<Object>();
	
	private final String TEMP = "/usr/tmp"; 
	/**
	 * whether use thread pool
	 */
	protected boolean usePool = true;
	
	protected volatile int count = 0;
	
	protected String rootDir = "";
	
	protected String childDir = "";
	
	protected transient ExecutorService pool = null;
	
	protected Map<String,Object> patterns = new ConcurrentHashMap<String,Object>();
	
	public DefaultHTMLParser(){
		
	}
	
	public DefaultHTMLParser(Object id){
		init(id);
	}
	
	private synchronized void init(Object id){
		if(null == db || null == pmap){
			if(null == this.id){
				this.id = id;
			}
			db = MapDBContext.getDB(DB_NAME_PRFIX+this.id,DBType.FILE);
			pmap = db.getHashMap(PARSER_NAME);
			count = pmap.size() + COMMIT_PER_COUNT;	
		}
	}
	
	private synchronized void openPool(){
		if(null == pool){
			pool = new ThreadPoolExecutor(2, 5, 30, 
					TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10), 
					new ThreadPoolExecutor.CallerRunsPolicy());
		}
	}
	
	public void parse(String html,URL url) {
		parse(Jsoup.parse(html),url);
	}
	public void parse(final Document doc,final URL url) {
		if(null == db || null == pmap){
			init(this.id);
		}
		if(null != doc && null == pmap.putIfAbsent(url.getUrl(),1)){
			try{
				if(usePool){
					if(null == pool){
						openPool();
					}
					pool.execute(new Runnable() {
						public void run() {
							parseMain(doc,url);
						}
					});
				}else{
					parseMain(doc,url);
				}
			}catch(Exception e){//catch the exception thrown from parsing process
				//TODO Logger
			}
		}
	}
	private void parseMain(Document doc,URL url) {
		ResultModel model = new ResultModel();
		String path = path();
		model.putAndAdd("_url", url.getUrl());//put URL to identify the result by default
		model.putAndAdd("_payload", url.getPayload());//add payload message
		Map<String,URL> download = new HashMap<String,URL>();//to store the URL from parsing which is needed to download
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
				String tmp = value.replaceAll("[\\/:*?\"<>|]", " ").trim();
				subDir += (subDir.equals("/")?tmp:"_"+tmp); 
				model.putAndAdd(key, value);
			}else{
				for(int i = 0;i<elements.size();i++){
					String value = parseElement(elements.get(i),type,attr);
					if(pattern.isDownload() && type.matches("(photo)|(video)|(html)|(text)")){
						URL src = new URL(url.getCid(),value);
						src.setType(type);
						download.put(key+"_"+i, src);
					}else{
						model.putAndAdd(key, value);
					}
				}
			}
		}
		if(! subDir.equals("/") && subDir.length() > 1){
			subDir += "/";
			File parent = new File(path + subDir);
			deleteRec(parent);
			parent.mkdirs();
		}
		int index = 0;
		List<Entry<String,URL>> entrys = 
				new ArrayList<Entry<String,URL>>(download.entrySet());
		for(int i = 0;i < entrys.size(); i++){
			Entry<String,URL> entry = entrys.get(i);
			URL src = entry.getValue();
			String value = src.getUrl();
			String loc = null,name = null;
			String tmpKey = entry.getKey();
			String key = tmpKey.substring(0,tmpKey.lastIndexOf("_"));
			if(src.getType().matches("html")){//for type 'html',parse again
				Document htmlNode = Jsoup.parse(value);
				Elements imgs = htmlNode.select("img");//search all 'img' elements
				for(int j = 0;j<imgs.size();j++){
					Element el = imgs.get(j);
					URL imgurl = new URL(url.getCid(),el.attr("src"));
					imgurl.setType("photo");
					name = DataUtils.randomHTTPFileName(imgurl.getUrl(), entrys.size());
					loc = path + subDir + name;
					imgurl.setLocal(loc);
					el.attr("src", childDir+ subDir + name);
					Entry<String,URL> newEntry = 
								new AbstractMap.SimpleEntry<String, URL>(tmpKey+"_img_"+j, imgurl);
					entrys.add(newEntry);
				}
				src.setUrl(htmlNode.html());
				src.setType("text");//change to 'text'
				entry = new AbstractMap.SimpleEntry<String, URL>(tmpKey,src);
				entrys.add(entry);
				continue;
			}else if(src.getType().matches("text")){// for type 'text',save 'value' as 'content' directly
				name = DataUtils.randomHTTPFileName(null, index);
				loc = path + subDir + name;
				localStore(value,loc);
			}else{//else put into the pool to crawler
				if(null == (loc = src.getLocal())){
					name = DataUtils.randomHTTPFileName(value, index);
					loc = path + subDir + name;
					src.setLocal(loc);
				}else{
					name = loc.substring(loc.lastIndexOf("/")+1);
				}
				CrawlerThreadPoolImpl.getInstance().execute(src);
			}
			model.putAndAdd(key,childDir + subDir + name);
			index ++;
		}
		doc.empty();
		//invoke resolve method in callback
		Object resolvedObj = resolve(model);
		model.inner.clear();
		if(null != resolvedObj){//store in-memory in temporary
			store.add(resolvedObj);
		}
		pcommit();
		System.gc();
	}
	private String parseElement(Element el,String type,String attr){
		String value = null;
		if(type.matches("(photo)|(video)")){
			value = el.attr(attr!=null?attr:"src");
		}else if(type.matches("text")){
			value = attr!=null?el.attr(attr):el.text();//not to use method text() for keeping the HTML tag
		}else if(type.matches("html")){
			value = attr!=null?el.attr(attr):el.html();
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
	public String path(){//concat(rootDir,childDir)
		String path = "";
		if(rootDir.trim().equals("") 
				&& childDir.trim().equals("")){
			path = TEMP;
		}else if(!rootDir.endsWith("/") && !childDir.startsWith("/")){
			path = rootDir + "/" + childDir;
		}else{
			path = rootDir + childDir;
		}
		return path;
	}
	private void pcommit(){
		if(pmap.size() >= count){
			pcommit0();
		}
	}
	private synchronized void pcommit0(){
		if(pmap.size() >= count){
			try{
				db.commit();
				if(store.size() > 0){
					callback.persist(store);
				}
				store.clear();
				count += COMMIT_PER_COUNT;
			}catch(Exception e){
				db.rollback();
				//TODO Logger
			}finally{
				System.gc();
			}
		}
	}
	private void deleteRec(final File parent){
		if(parent.exists() && parent.isDirectory()){
			File[] childs = parent.listFiles();
			for(int i = 0;i < childs.length; i++){
				deleteRec(childs[i]);
			}
		}else if(parent.exists()){
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run() {
					parent.delete();
					return null;
				}
			});
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
	
	public void setPatterns(Map<String, String> patterns) {
		this.patterns.clear();
		for(Entry<String,String> entry : patterns.entrySet()){
			String json = entry.getValue();
			Pattern pattern = JsonUtils.fromJson(json, Pattern.class);
			this.patterns.put(entry.getKey(), pattern);
		}
	}
	public Object resolve(ResultModel result) {
		return callback.resolve(result);
	}
	public void persist(){
		if(store.size() > 0){
			callback.persist(store);
		}
	}
	public boolean isParsed(URL url) {
		if(null == pmap){
			init(this.id);
		}
		return pmap.containsKey(url.getUrl());
	}
	public Callback getCallback() {
		return callback;
	}
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	public void setRootDir(String rootDir) {
		if(null == rootDir){
			throw new IllegalArgumentException("rootDir cannot be null");
		}
		this.rootDir = rootDir;
	}
	public String getRootDir() {
		return rootDir;
	}
	public void setChildDir(String childDir) {
		if(null == childDir){
			throw new IllegalArgumentException("childDir cannot be null");
		}
		this.childDir = childDir;
	}
	public String getChildDir() {
		return childDir;
	}
	public Object getId() {
		return id;
	}
	public void setId(Object id) {
		this.id = id;
	}
	
}
