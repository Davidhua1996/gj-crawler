package com.gj.web.crawler.pool.basic;

import org.mapdb.HTreeMap;

import com.gj.web.crawler.utils.MapDBContext;

import java.util.HashMap;
import java.util.Map;

public class MapDBMultQueue<T extends URL> extends IMMultQueue<T>{
	private static final String DISTINCT_NAME = "distinct";
	private HTreeMap<String,Object> dbRecord = null;
	private Map<String,String> locals = new HashMap<String, String>();
	public MapDBMultQueue(){
		dbRecord = MapDBContext.getDB(null,DBType.TEMP).getHashMap(DISTINCT_NAME);
		dbRecord.clear();
	}
	@Override
	public void pushWithKey(T t, String key) {
		if(dereplicate){
			Long expire = (Long)dbRecord.get(key);
			if(null == expire || (expire > 0 && expire < System.currentTimeMillis())){
				dbRecord.put(key, derepExpire <= 0?derepExpire: System.currentTimeMillis() + derepExpire);
				if(null != t.getLocal()){
					locals.put(key, t.getLocal());
				}
				super.push(t);
			}
		}else if(!dereplicate){
			super.push(t);
		}
	}
	@Override
	public void clear() {
		dbRecord.clear();
		super.clear();
	}
	@Override
	public Object local(String key) {
		return locals.get(key);
	}
}
