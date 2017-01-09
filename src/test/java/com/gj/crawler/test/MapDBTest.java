package com.gj.crawler.test;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import com.fasterxml.jackson.databind.SerializationFeature;

public class MapDBTest {
	public static void main(String[] args) throws Exception {
		File file = new File("/usr/cache/");
		file.mkdirs();
//		if(!file.exists()){
//			file.createNewFile();
//		}
		DB db = DBMaker.newFileDB(new File("/usr/cache/mapDB"))
						.closeOnJvmShutdown()
						.transactionDisable()
						.compressionEnable().make();
		HTreeMap<String, String> b = db.createHashMap("cache")
			.keySerializer(Serializer.STRING)
			.valueSerializer(Serializer.STRING).make();
		for(int i = 0;i<10;i++){
			b.put(String.valueOf(i), String.valueOf(i));
		}
		b.close();
	}
}
