package com.gj.web.crawler.pool.basic;

import org.mapdb.HTreeMap;

import com.gj.web.crawler.utils.MapDBContext;

public class MapDBMultQueue<T extends URL> extends IMMultQueue<T>{
	private static final String DISTINCT_NAME = "distinct";
	private HTreeMap<String,Object> dMap = null;
	public MapDBMultQueue(){
		dMap = MapDBContext.getDB(null,DBType.TEMP).getHashMap(DISTINCT_NAME);
		dMap.clear();
		
	}
	@Override
	public void pushWithKey(T t, String key) {
		if(!dMap.containsKey(key)){
			if(null != ((URL)t).getLocal()){
				dMap.put(key, ((URL)t).getLocal());
			}else{
				dMap.put(key, 0);//because value cannot be null,set 0 in default
			}
			super.push(t);
		}
	}
	@Override
	public void clear() {
		dMap.clear();
		super.clear();
	}
	@Override
	public Object local(String key) {
		return dMap.get(key);
	}
	
}
