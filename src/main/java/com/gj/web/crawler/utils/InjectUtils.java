package com.gj.web.crawler.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parameter inject
 * @author David
 *
 */
public class InjectUtils {
	private static final String PARAMETER_PREFIX = "#";
	private static final String ASSIGN_SYMBOL = "=";
	private static final Pattern regex = Pattern.compile(
			"("+ASSIGN_SYMBOL+"{0,1})"+
			PARAMETER_PREFIX+
			"\\{([^}]{1,})[|]{0,1}([^}]*)\\}?");
	public static String inject(String pattern, Object[] params){
		return inject(pattern,params,true);
	}
	public static String inject(String pattern,Object[] params,boolean encode){
		Matcher matcher = regex.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		int offset = 0;
		while(matcher.find()){
			if(offset<params.length){
				String value = "";
				String param = String.valueOf(params[offset]);
				if(encode){
					try {
						param = URLEncoder.encode(param,"UTF-8");
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
				if(null == matcher.group(1)
						||"".equals(matcher.group(1))){
					value = "\""+param+"\"";
				}else{
					value = matcher.group(1) + "\""+param+"\"";
				}
				value = value.replace("$", "\\$");
				matcher.appendReplacement(sb,value);
				offset++;
			}else if(null != matcher.group(3)){
				matcher.appendReplacement(sb, "\""+matcher.group(3)+"\"");
			}else{
				matcher.appendReplacement(sb, "");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	public static String inject(String pattern, Map<String,Object> params){
		return inject(pattern, params, true);
	}
	public static String inject(String pattern,Map<String,Object> params, boolean encode){
		Matcher matcher = regex.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		//will be more faster?
		Set<Entry<String,Object>> entries = params.entrySet();
		while(matcher.find()){
			String injected= matcher.group(2);
			if(null!=injected &&!"".equals(injected)){
				int flag = 0;
				for(Entry<String,Object> entry:entries){
					if(injected.equals(entry.getKey())){
						String value = "";
						String param = String.valueOf(entry.getValue());
						if(encode){
							try {
								param = URLEncoder.encode(param,"UTF-8");
							} catch (UnsupportedEncodingException e) {
								throw new RuntimeException(e);
							}
						}
						if(null == matcher.group(1)
								||"".equals(matcher.group(1))){
							value = "\""+param+"\"";
						}else{
							value = matcher.group(1) + "\""+param+"\"";
						}
						matcher.appendReplacement(sb,value);
						flag = 1;
						break;
					}
				}
				if(flag == 0){
					if(null != matcher.group(3)){
						matcher.appendReplacement(sb, "\""+matcher.group(3)+"\"");
					}else{
						matcher.appendReplacement(sb, "");
					}
				}
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
