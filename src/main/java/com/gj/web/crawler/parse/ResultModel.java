package com.gj.web.crawler.parse;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
/**
 * store the result of parsing
 * @author David
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class ResultModel implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5743183223086940895L;
	private static final Logger logger = LogManager.getLogger(ResultModel.class);
	public Map<String,Object[]> inner = new HashMap<String,Object[]>();
	protected void put(String key ,Object[] value){
		inner.put(key, value);
	}
	protected void putAndAdd(String key,Object value){
		Object[] array = inner.get(key);
		if(null == array){
			put(key,new Object[]{value});
		}else{
			int len = array.length;
			Object[] newArray = new Object[len + 1];
			System.arraycopy(array, 0, newArray, 0, len);
			newArray[len] = value;
			put(key,newArray);
		}
	}
	public Map<String,Object[]> getInnerMap(){
		return inner;
	}
	public Long getLong(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			String value = obj[0].toString();
			if(!StringUtils.isEmpty(value)){
				return new Long(value);
			}
		}
		return null;
	}
	public Integer getInteger(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0 ){
			String value = obj[0].toString();
			if(!StringUtils.isEmpty(value)){
				return new Integer(value);
			}
		}
		return null;
	}
	public Double getDouble(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			String value = obj[0].toString();
			if(!StringUtils.isEmpty(value)){
				return new Double(value);
			}
		}
		return null;
	}
	public String getString(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			if(obj[0] instanceof byte[]){
				byte[] t = (byte[])obj[0];
				return new String(t);
			}else if(obj[0] instanceof char[]){
				char[] t = (char[])obj[0];
				return new String(t);
			}
			return (String)obj[0];
		}
		return null;
	}
	public Short getShort(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			String value = obj[0].toString();
			if(!StringUtils.isEmpty(value)){
				return new Short(value);
			}
		}
		return null;
	}
	public Boolean getBoolean(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			String value = obj[0].toString();
			if(!StringUtils.isEmpty(value)){
				return new Boolean(value);
			}
		}
		return null;
	}
	public Float getFloat(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			String value = obj[0].toString();
			if(!StringUtils.isEmpty(value)){
				return new Float(value);
			}
		}
		return null;
	}
	public Long getTime(String key){//maybe it is in need
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			if(obj[0] instanceof Date){
				return ((Date)obj[0]).getTime();
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try{
				return obj[0] != null ? format.parse(obj[0].toString()).getTime():null;
			}catch (ParseException e){
				logger.error("ERROR occur method getData:"+e.getMessage());
			}
		}
		return null;
	}
	public Date getDate(String key){
		Object[] obj = inner.get(key);
		if(null != obj && obj.length > 0){
			if(obj[0] instanceof Date){
				return (Date)obj[0];
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try{
				return obj[0] != null?format.parse(obj[0].toString()):null;
			}catch (ParseException e){
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	public Object[] getValue(String key){
		Object[] value =  inner.get(key);
		if(null == value){
			return new Object[]{};
		}
		return value;
	}
}
