package com.gj.web.crawler.pool.basic;

import java.util.HashMap;
import java.util.Map;

/**
 * in-memory queue
 * @author David
 *
 * @param <T>
 */
public class IMQueue<T extends URL> extends AbstractedQueue<T>{
	private volatile long size = 0;
	private Node<T> head = new Node<T>();
	private Node<T> tail = new Node<T>();
	private Map<String,Object> record = new HashMap<String,Object>();
	private Map<String,String> locals = new HashMap<String, String>();
	public IMQueue(){

	}
	@SuppressWarnings("hiding")
	private class Node<T>{
		T data;
		Node<T> next;
		public Node(T data,Node<T> next){
			this.data = data;
			this.next = next;
		}
		public Node(){
		}
	}
	public void init() {
		size = 0;
		head = new Node<T>(null,null);
		tail = new Node<T>(null,null);
	}

	public T poll() {
		if(this.size <= 0){
			throw new NullPointerException("the queue is empty!");
		}
		Node<T> next = this.head.next;
		T result = next.data;
		if(next.equals(tail.next)){// if the last element
			tail.next = null;
		}
		this.head.next = next.next;
		size--;
		return result;
	}


	public void push(T t) {
		Node<T> node = new Node<T>(t,null);
		Node<T> next = tail.next;
		if(null == next){//if the first element;
			tail.next = node;
			head.next = node;
		}else{
			next.next = node;
			tail.next = node;
		}
		size++;
	}

	public void pushWithKey(T t, String key) {
		if(dereplicate){
//			System.out.println(key);
			Long expire = (Long)record.get(key);
			if(null == expire || (expire > 0 && expire < System.currentTimeMillis())) {
				record.put(key, derepExpire <= 0 ? derepExpire : System.currentTimeMillis() + derepExpire);
				String local = t.getLocal();
				if (null != local) {
					locals.put(key, local);
				}
				push(t);
			}
		}else if(!dereplicate){
			push(t);
		}
	}
	
	public long size() {
		return size;
	}

	public boolean isEmpty() {
		return size <=0;
	}
	public void clear() {
		this.record.clear();
	}
	public static void main(String[] args) {
		
	}

	public Object local(String key) {
		return locals.get(key);
	}
}
