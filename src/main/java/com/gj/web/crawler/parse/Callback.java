package com.gj.web.crawler.parse;

import java.util.List;

/**
 * callback method of parsing
 * @author David
 *
 */
public interface Callback {
	
	/**
	 * callback method
	 * @param result
	 * @return the object resolved
	 */
	Object resolve(ResultModel result);
	/**
	 * persist method,persist the object in-memory
	 * @param store
	 */
	void persist(List<Object> store);
}
