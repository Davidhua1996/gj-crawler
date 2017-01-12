package com.gj.web.crawler.utils;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.gj.web.crawler.pool.basic.DBType;

public class MapDBContext {
	private static final String DB_ROOT_DIR = "/usr/mapDB/";
	public static DB getDB(String dbname,DBType type){
		DB db = null;
		if(type == DBType.TEMP){
			db = configure(DBMaker.newTempFileDB()).make();
		}else if(type == DBType.FILE){
			File directory = new File(MapDBContext.DB_ROOT_DIR);
			directory.mkdirs();
			db = configure(DBMaker.newFileDB(new File(MapDBContext.DB_ROOT_DIR,dbname))).make();
		}
		return db;
	}
	@SuppressWarnings("rawtypes")
	private static DBMaker configure(DBMaker maker){
		return maker.cacheSize(100)
		.closeOnJvmShutdown()
		.compressionEnable()
		.asyncWriteFlushDelay(1000);
	}
}
