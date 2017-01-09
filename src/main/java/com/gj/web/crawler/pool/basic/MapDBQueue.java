package com.gj.web.crawler.pool.basic;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class MapDBQueue<T> extends IMQueue<T>{
	private static final String QUEUE_DISTINC_DIR = "/usr/queue/";
	private static final String DISTINC_NAME = "distinc";
	private HTreeMap<String,Object> mapDB = null;
	public MapDBQueue(){
		this(QUEUE_DISTINC_DIR);
	}
	public MapDBQueue(String dir){
		File file = new File(dir);
		file.mkdirs();
		DB db = DBMaker.newFileDB(new File(dir,DISTINC_NAME))
				.cacheSize(100)
				.closeOnJvmShutdown()
				.compressionEnable()
				.make();
		mapDB = db.getHashMap(DISTINC_NAME);
	}
	@Override
	public void pushWithKey(T t, String key) {
		if(!mapDB.containsKey(key)){
			mapDB.put(key, 1);//because value cannot be null,set 1 in default
			super.push(t);
		}
	}
	@Override
	public void clear() {
		mapDB.clear();
		mapDB.close();//need to close;
	}
	
}
