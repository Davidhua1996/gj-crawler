package com.gj.web.crawler.pool.basic;
/**
 * queue weight poll
 * (imitate nginx)
 * @author David
 *
 */
public class QueueWeightPoll<T extends URL> {
	public static class Segment<T extends URL>{
		private String type;
		private int wt = -1;
		private int cwt = -1;
		private Queue<T> queue = null;
		public Segment(String type, int weight, Queue<T> queue){
			this.type = type;
			this.wt = weight;
			this.cwt = wt;
			this.queue = queue;
		}
		public int getWt() {
			return wt;
		}
		public void setWt(int wt) {
			this.wt = wt;
		}
		public int getCwt() {
			return cwt;
		}
		public void setCwt(int cwt) {
			this.cwt = cwt;
		}
		public Queue<T> getQueue() {
			return queue;
		}
		public void setQueue(Queue<T> queue) {
			this.queue = queue;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
	}
	@SuppressWarnings("rawtypes")
	private static QueueWeightPoll whtpoll = null;
	private QueueWeightPoll(){
		
	}
	@SuppressWarnings("unchecked")
	public static <T extends URL> QueueWeightPoll<T> getInstance(){
		if(whtpoll == null){
			synchronized(QueueWeightPoll.class){
				if(whtpoll == null){
					whtpoll = new QueueWeightPoll<T>();
				}
			}
		}
		return whtpoll;
	}
	public int pollSel(Segment<T>[] segments){
		int u = 0;
		int reset = -1;
		while(true){
			for(int i = 0; i < segments.length; i++){
				if(segments[i].cwt <= 0 || segments[i].getQueue().size() <=0){
					continue;
				}
				u = i;
				while( i < segments.length - 1){
					i++;
					if(segments[i].cwt <= 0 || segments[i].getQueue().size() <= 0 ){
						continue;
					}
					if((segments[u].wt * 1000 / segments[i].wt < 
							segments[u].cwt * 1000 / segments[i].cwt)){
						return u;
					}
					u = i;
				}
				return u;
			}
			if(reset++ > 0){
				return 0;
			}
			for(int i = 0 ; i < segments.length; i++){
				segments[i].cwt = segments[i].wt;
			}
		}
	}
	public static void main(String[] args) {
	}
}
