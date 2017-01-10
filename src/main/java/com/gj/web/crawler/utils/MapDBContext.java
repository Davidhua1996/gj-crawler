package com.gj.web.crawler.utils;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDBContext {
	private static final String DB_ROOT_DIR = "/usr/mapDB/";
	private static final String DB_NAME = "map_crawldb";
	private static DB db = null;
	static{
		File directory = new File(MapDBContext.DB_ROOT_DIR);
		directory.mkdirs();
		db = DBMaker.newFileDB(new File(MapDBContext.DB_ROOT_DIR,DB_NAME))
				.cacheSize(100)
				.closeOnJvmShutdown()
				.compressionEnable()
				.asyncWriteFlushDelay(1000)
				.make();
	}
	public static DB getDB(){
		return db;
	}
	public static void commit(){
		db.commit();
	}
	public static void close(){
		db.close();
	}
}
