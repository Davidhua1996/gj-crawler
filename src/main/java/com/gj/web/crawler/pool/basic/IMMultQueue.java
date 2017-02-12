package com.gj.web.crawler.pool.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gj.web.crawler.pool.basic.QueueWeightPoll.Segment;

/**
 * contain multiple queue (dived by URL)
 * @author David
 *
 */
public class IMMultQueue<T extends URL> implements Queue<T>{
	private QueueWeightPoll<T> weightPoll = QueueWeightPoll.getInstance();
	/**
	 * each segment contains a subQueue
	 */
	private Segment<T>[] segments = null;
	private ConcurrentHashMap<String, Integer> locations = new ConcurrentHashMap<String, Integer>();
	private ReentrantReadWriteLock segmentlock = new ReentrantReadWriteLock();
	public void init() {
		if(null != segments){
			for(int i = 0 ; i <segments.length ; i++){
				Queue<? extends URL> queue = segments[i].getQueue();
				if(null != queue) queue.init();
			}
		}
	}
	
	public T poll() {
		Segment<T> segment = null;
		T result = null;
		segmentlock.writeLock().lock();
		try{
			segment = select();
		}finally{
			segmentlock.writeLock().unlock();
		}
		if(null != segment){
			result = segment.getQueue().poll();
		}
		return result;
	}

	public void push(T t) {
		URL url = (URL)t;
		Segment<T> segment = getSegment(url.getCid()+"-"+url.getType());
		if(null == segment){
			segment = segment(url.getCid() + "-" + url.getType(), 1, new IMQueue<T>());
		}
		segment.getQueue().push(t);//maybe sub queue can solve the concurrent problem?
	}
	private Segment<T> getSegment(String type){
		Integer index = locations.get(type);
		Segment<T> segment = null;
		if(null != index){
			segment = segments[index];
			if(null != segment && !segment.getType().equals(type)){ //conflict
				throw new RuntimeException("error in getting segment");
			}
		}
		return segment;
	}
	public Segment<T> segment(String type, int weight, Queue<T> queue){
		Segment<T> segment = null;
		segmentlock.writeLock().lock();
		try{
			segment = new Segment<T>(type, weight, queue);
			segment(segment);
		}finally{
			segmentlock.writeLock().unlock();
		}
		return segment;
	}
	@SuppressWarnings("unchecked")
	private void segment(Segment<T> segment){
		if(segments == null){
			segments = new Segment[1];
			segments[0] = segment;
			locations.put(segment.getType(), 0);
		}
		Segment<T>[] segs = new Segment[segments.length + 1];
		System.arraycopy(segments, 0, segs, 0, segments.length);
		segs[segments.length] = segment;
		locations.put(segment.getType(), segments.length);
		System.out.println("segment:"+segment.getType());
		segments = segs;
	}
	public void pushWithKey(T t, String key) {
		URL url = (URL)t;
		Segment<T> segment = getSegment(url.getCid()+"-"+url.getType());
		if(null == segment){
			segment = segment(url.getCid() + "-" +url.getType(), 1, new IMQueue<T>());
		}
		segment.getQueue().pushWithKey(t, key);
	}

	public long size() {
		long size = 0;
		if(null != segments){
			for(int i = 0 ; i < segments.length ;i ++){
				size += segments[i].getQueue().size();
			}
		}
		return size;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public void clear() {
		if(null != segments){
			for(int i = 0 ; i < segments.length ;i ++){
				segments[i].getQueue().clear();
			}
		}
	}
	private Segment<T> select(){
		Segment<T> segment = null;
		if(null != segments){
			int index = weightPoll.pollSel(segments);
			segments[index].setCwt(segments[index].getCwt() - 1);
			segment = segments[index];
		}
		return segment;
	}

	public Object local(String key) {
		Object local = null;
		if(null != segments){
			for(int i = 0 ; i < segments.length ;i ++){
				local = segments[i].getQueue().local(key);
				if(null != local){
					break;
				}
			}
		}
		return local;
	}
	public static void main(String[] args) {
		IMMultQueue<URL> queue = new IMMultQueue<URL>();
		queue.push(new URL("gamersky-news","http://www.news"));
//		queue.push(new URL("gamersky-game","http://www.game"));
		queue.push(new URL("gamersky-news","http://www.news.img"));
//		queue.push(new URL("gamersky-scane","http://www.scane"));
		while(!queue.isEmpty()){
			URL url = queue.poll();
			System.out.println(url.getUrl());
		}
	}
}
