package com.gj.web.crawler.parse;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import com.gj.web.crawler.pool.basic.URL;

public class DefaultCallback implements Callback,Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7636692919538566692L;

	/**
	 * EN:resolve the result after crawling
	 * CN:解析爬虫结果
	 */
	public Object resolve(ResultModel result) {
		Map<String,Object[]> inner = result.getInnerMap();
		System.out.println("-----------------------------");
		for(Entry<String,Object[]> entry : inner.entrySet()){
			String key = entry.getKey();
			Object[] value = entry.getValue();
			System.out.print(key +": ");
			for(int i =0 ; i < value.length; i++){
				System.out.print(value[i] + " ");
			}
			System.out.println();
		}
		System.out.println("-----------------------------");
		return result;
	}
	/**
	 * EN:suggest that if you do some time-consuming work in the method,
	 * you'd better to do in new thread 
	 * CN:持久化对象,建议如果做一些耗时的工作(在persist方法中),最好在线程中完成(为了不阻塞队列)
	 */
	public synchronized void persist(List<Object> store) {
		System.out.println("need to persisit(or wait to persist):"+store.size());
	}
	/**
	 * EN:in asynchronized way, medias like video or image have been download by background thread,
	 * when them finished,notice their attached URL
	 * CN:在异步的情况下 ,media像是video和图片是被后台线程下载的，当他们下载完的时候通知他们的关联URL（拉的方式）
	 *	注:在个别情况，由于网络等问题，被关联的URL找不到（多线程不一致性），返回失败的URL 
	 */
	public List<URL> mediaDownloaded(Queue<URL> medias) {
		
		return null;
	}

}
