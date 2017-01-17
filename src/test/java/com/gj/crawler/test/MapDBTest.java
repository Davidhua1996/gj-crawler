package com.gj.crawler.test;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.gj.web.crawler.utils.MapDBContext;

public class MapDBTest {
	public static void main(String[] args) throws Exception {
		File directory = new File("/usr/mapDB/");
		directory.mkdirs();
		DB db = DBMaker.newFileDB(new File("/usr/mapDB/","map_db_temp"))
				.cacheSize(100)
				.closeOnJvmShutdown()
				.compressionEnable()
				.asyncWriteFlushDelay(1000).make();
		HTreeMap<String, String> b = db.getHashMap("parse");
		long current = System.currentTimeMillis();
		System.out.println(b.size());
		System.out.println(System.currentTimeMillis() - current);
		current = System.currentTimeMillis();
		b.containsKey("http:steam.cseradf");
		System.out.println(System.currentTimeMillis() - current);
	}
}
