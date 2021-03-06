package com.gj.web.crawler.http.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.gj.web.crawler.pool.basic.URL;

public class DataUtils {
	private static final String DATA_PREFIX = "GJ_";
	private static SimpleDateFormat format = new SimpleDateFormat("yyyMMddHHmmssSSS");
	public static Integer BUFFER_SIZE = 131072;
	/**
	 * Join as regular expression
	 * @param allowURL
	 * @return
	 */
	public static String regexJoin(List<String> allowURL) {
		return regexJoin(allowURL,"^","$");
	}
	public static String regexJoin(List<String> allowURL, String prefix, String suffix){
		StringBuilder builder = new StringBuilder("");
		for(int i = 0;i<allowURL.size();i++){
			String tmp = allowURL.get(i);
			if(tmp.startsWith("(") 
					&& tmp.endsWith(")")){
				builder.append(tmp);
			}else{
				if(i > 0){
					builder.append("|");
				}
				builder.append("("+prefix+tmp+suffix+")");
			}
		}
		return builder.toString();
	}
	public static String randomName(){
		long current0 = System.currentTimeMillis();
		String name = null;
		try {
			name = "";
			MessageDigest digest = MessageDigest.getInstance("MD5");//MD5
			digest.digest();//reset
			int hash = Thread.currentThread().hashCode();
			String str =String.valueOf(current0)+String.valueOf(hash);//concat hash-value and thread ID
			for(byte b:digest.digest(str.getBytes())){
				int a= b&0xff;
				if(a<16){
					a+=16;
				}
				name=name+Integer.toHexString(a);
			}
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return name;
	}
	/**
	 * product a new name for file 
	 * which came from network in HTTP protocol
	 * @param name
	 * @param index distinguish between files which downloaded in the same time(nearest same),but in different index locations 
	 * @return
	 */
	public static String randomHTTPFileName(String name,int index){
		String id =String.valueOf(Thread.currentThread().getId());
		String timeS = format.format(Calendar.getInstance().getTime());
		String newName  = DATA_PREFIX+timeS+"_"+id+index;
		if(null != name && !name.trim().equals("")){
			if(name.indexOf("?")>0){//remove string after '?'
				name = name.substring(0,name.indexOf("?"));
			}
			if(name.lastIndexOf("/") > 0){
				name = name.substring(name.lastIndexOf("/"));//remove the string before '/'
			}
			if(name.lastIndexOf(".") > -1){
				String extension = name.substring(name.lastIndexOf("."));
				newName += extension;
			}
		}
		return newName;
	}
	/**
	 * format URL string by context URL
	 * @param before
	 * @param urlStr
	 * @return
	 */
	public static String formatURL(URL context, String urlStr){
		if(urlStr.startsWith("//")){
			urlStr = "http:"+urlStr;
		}else if(urlStr.startsWith("/")){
			String parent = context.getUrl();
			urlStr = parent.replaceFirst("(http|https)://([^/]+)([\\S\\s]*)", "$1://$2"+urlStr);
		}else if (urlStr.length() <= 4 ||
				!urlStr.substring(0,4).equalsIgnoreCase("http")){
			String parent = context.getUrl();
			urlStr = parent.substring(0,parent.lastIndexOf("/"))+urlStr;
		}
		return urlStr;
	}
}
