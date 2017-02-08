package com.gj.web.crawler.lifecycle;


public abstract class BasicLifecycle implements Lifecycle{
	
	public enum Status{
		RUNNING("running") , OPEN("open"), CLOSE("close"), INITAL("inital");
		private String name;
		Status(String name){
			this.name = name;
		}
		public String toString(){
			return name;
		}
	}
	
	private Status status = Status.INITAL;
	
	public synchronized void status(Status status){
		this.status = status;
	}
	public Status status(){
		return this.status;
	}
	public synchronized void open() {
		if(status.equals(Status.CLOSE)){
			this.initalize();
		}else if(status.equals(Status.OPEN)||status.equals(Status.RUNNING)){
			return;//have been opened
		}else if(status.equals(Status.INITAL)){
			
		}else{
			throw new RuntimeException("crawler cannot call open method with the status "+status.toString());
		}
		if(!this.status.equals(Status.INITAL)){
			throw new RuntimeException("error occurred in inital");
		}
		this.status = Status.OPEN;
		this.openInternal();
	}

	public synchronized void shutdown() {
		this.shutdownInternal();
		this.status = Status.CLOSE;
	}
	
	public synchronized void initalize() {
		if(status.equals(Status.OPEN) || status.equals(Status.RUNNING)){
			this.shutdown();
			if(!this.status.equals(Status.CLOSE)){
				throw new RuntimeException("error occurred in close");
			}
		}
		this.initalInternal();
		this.status = Status.INITAL;
	}
	
	protected abstract void openInternal();
	
	protected abstract void shutdownInternal();
	
	protected abstract void initalInternal();
}
