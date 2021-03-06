package com.gj.web.crawler.utils;

import java.io.File;
import java.io.IOException;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.gj.web.crawler.pool.basic.DBType;

public class MapDBContext {
	private static final String DB_ROOT_DIR;
	static{
		 String baseDir = System.getProperty("base.dir", "/crawl");
		 DB_ROOT_DIR = baseDir + "/mapDB" + System.getProperty("namespace", "") + "/";
	}
	public static DB getDB(String dbname,DBType type){
		DB db = null;
		if(type == DBType.TEMP){
			db = configure(DBMaker.newTempFileDB()).make();
		}else if(type == DBType.FILE){
			File directory = new File(MapDBContext.DB_ROOT_DIR);
			try {
				Runtime.getRuntime().exec("chmod 700 " + DB_ROOT_DIR);
			} catch (IOException e) {
				//skip it
			}
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
