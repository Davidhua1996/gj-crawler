package com.gj.web.crawler.pool.basic;

public interface Queue<T> {
	/**
	 * initialize the queue
	 */
	public void init();
	/**
	 * poll out the element
	 * @return
	 */
	public T poll();
	/**
	 * push the element
	 * @param t
	 */
	public void push(T t);
	/**
	 * the element with key should be unique in the queue 
	 * @param t
	 * @param key
	 */
	public void pushWithKey(T t,String key);
	/**
	 * size
	 * @return
	 */
	public long size();
	/**
	 *is empty
	 */
	public boolean isEmpty();
	/**
	 * clear the cache
	 */
	public void clear();
}
