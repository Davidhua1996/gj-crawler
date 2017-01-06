package com.gj.web.crawler.http.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentType {
	private static Map<String, String> typeExt = new ConcurrentHashMap<String, String>();
	static{
		typeExt.put("text/html", ".html");
		typeExt.put("image/jpg", ".jpg");
		typeExt.put("application/x-jpg", ".jpg");
		typeExt.put("image/png", ".png");
		typeExt.put("application/x-png", ".png");
		typeExt.put("video/mpg", ".mpg");
		typeExt.put("video/avi", ".avi");
		typeExt.put("video/x-ms-asf", ".asf");
		typeExt.put("video/x-ms-wmv", ".wmv");
	}
	public static String getExt(String type){
		return typeExt.get(type);
	}
}
