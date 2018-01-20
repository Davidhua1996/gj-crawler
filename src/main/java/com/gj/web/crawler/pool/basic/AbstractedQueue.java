package com.gj.web.crawler.pool.basic;

public abstract class AbstractedQueue<T extends URL> implements Queue<T>{
    protected boolean dereplicate = true;
    protected long derepExpire = -1;
    public AbstractedQueue(){
        this(true);
    }
    protected AbstractedQueue(boolean dereplicate){
        this(dereplicate, -1);
    }
    protected AbstractedQueue(boolean dereplicate, long derepExpire){
        this.derepExpire = derepExpire;
        this.dereplicate = dereplicate;
    }

    public boolean isDereplicate() {
        return dereplicate;
    }

    public void setDereplicate(boolean dereplicate) {
        this.dereplicate = dereplicate;
    }

    public long getDerepExpire() {
        return derepExpire;
    }

    public void setDerepExpire(long derepExpire) {
        this.derepExpire = derepExpire;
    }
}
