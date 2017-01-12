package com.gj.web.crawler.pool.basic;

import org.mapdb.HTreeMap;

import com.gj.web.crawler.utils.MapDBContext;

public class MapDBQueue<T> extends IMQueue<T>{
	private static final String DISTINCT_NAME = "distinct";
	private HTreeMap<String,Object> dMap = null;
	public MapDBQueue(){
		dMap = MapDBContext.getDB(null,DBType.TEMP).getHashMap(DISTINCT_NAME);
		dMap.clear();
		
	}
	@Override
	public void pushWithKey(T t, String key) {
		if(!dMap.containsKey(key)){
			dMap.put(key, 1);//because value cannot be null,set 1 in default
			super.push(t);
		}
	}
	@Override
	public void clear() {
		dMap.clear();
	}
	
}
