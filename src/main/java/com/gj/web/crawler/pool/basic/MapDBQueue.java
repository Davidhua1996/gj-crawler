package com.gj.web.crawler.pool.basic;

import org.mapdb.HTreeMap;

import com.gj.web.crawler.utils.MapDBContext;

public class MapDBQueue<T> extends IMQueue<T>{
	private static final String DISTINCT_NAME = "distinct";
	private HTreeMap<String,Object> distinctMap = null;
	public MapDBQueue(){
		distinctMap = MapDBContext.getDB().getHashMap(DISTINCT_NAME);
	}
	@Override
	public void pushWithKey(T t, String key) {
		if(!distinctMap.containsKey(key)){
			distinctMap.put(key, 1);//because value cannot be null,set 1 in default
			super.push(t);
		}
	}
	@Override
	public void clear() {
		distinctMap.clear();
		distinctMap.close();//for temporary collections 
	}
	
}
