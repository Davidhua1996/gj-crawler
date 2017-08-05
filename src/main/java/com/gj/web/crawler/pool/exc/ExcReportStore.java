package com.gj.web.crawler.pool.exc;

import java.util.List;

/**
 * <p>store the exception report 
 * which brought from crawler pool</p>
 * @author David
 *
 */
public interface ExcReportStore {
	
	public void add(ExcReport report);
	
	public ExcReport take() throws InterruptedException;
	/**
	 * <p>take the storing reports into the list,
	 * but not more than <tt>size</tt></p>
	 * @param list report list
	 * @param size limit size of list
	 * @return
	 */
	public int takeInList(List<ExcReport> list, int size);
	/**
	 * 
	 * @return the size of store
	 */
	public int size();
}
