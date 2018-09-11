package com.gj.web.crawler.parse.json;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Json转化工具类(jackson)
 * @author David
 *
 */
public class JsonUtils {
	//List前后缀
	private static final String PREFIX = "[";
	private static final String SUFFIX = "]";
	private static ObjectMapper mapper;
	private static final Logger logger = LogManager.getLogger(JsonUtils.class);
	static{
		mapper = new ObjectMapper();
		mapper.configure(Feature.ALLOW_COMMENTS, true);
		mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES,true);
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
//		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING,true);
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	private JsonUtils(){
	}
	/**
	 * 
	 * @param json json串
	 * @param clazz 反序列化模型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json,Class<?> clazz){//Deserailization
		if(!(null==json||json.trim().equals("")) && null!= clazz){
			try{
				if(json.startsWith(PREFIX)
						&&json.endsWith(SUFFIX)){
					JavaType javaType = mapper
							.getTypeFactory().constructParametricType(ArrayList.class, clazz);
					return mapper.readValue(json, javaType);
				}else{
					return (T)mapper.readValue(json,clazz);
				}
			}catch(Exception e){
				logger.info(e);
				throw new IllegalArgumentException(e.getMessage());
			}
		}
		return null;
	}
	/**
	 * 
	 * @param stream 输入流
	 * @param clazz 
	 * @return
	 */
	public static <T> T fromJson(InputStream stream,Class<?> clazz){//
		StringBuilder builder = new StringBuilder();
		String jsonStr = null;
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream,"UTF-8"));//修饰缓冲流
			while((jsonStr = reader.readLine())!= null){
				builder.append(jsonStr);
			}
//			reader.close(); 不关闭流
		}catch(Exception e){
			logger.info(e);
			throw new RuntimeException(e);
		}
		return fromJson(builder.toString(),clazz);
	}
	/**
	 * 
	 * @param obj 序列化类
	 * @param model 序列化模型
	 * @return
	 */
	public static  String toJson(Object obj,Class<?> model){//Serialization
		ObjectWriter writer = mapper.writer();
		if(null != obj){
			try{
				if(null != model){
					writer = writer.withView(model);
				}
				return writer.writeValueAsString(obj);
			}catch(JsonProcessingException e){
				logger.info(e);
				throw new IllegalArgumentException(e.getMessage());
			}
		}
		return null;
	}
}
