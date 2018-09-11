package com.gj.web.crawler.http.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * proxy container
 */
public class ProxyContainer {
    public static final int MAX_RECONNECT_COUNT = 0;
    private final List<ProxyEntity> proxyList = new ArrayList<>();
    private final Queue<ProxyEntity> candidacies = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private static final Logger logger = LogManager.getLogger(ProxyContainer.class);
    private int maxCapacity = -1;
    public ProxyContainer(){

    }
    public ProxyContainer(int maxCapacity){
        this.maxCapacity = maxCapacity;
    }
    ProxyEntity get(int i){
        return proxyList.get(i);
    }
    void remove(int i) {
        lock();
        try {
            ProxyEntity entity = proxyList.remove(i);
            logger.trace("remove proxy:"+entity.address+" rest:" + capacity() + " candidate:" + candidacies.size());
            if(!candidacies.isEmpty()){
                entity = candidacies.poll();
                if(!proxyList.contains(entity)) {
                    proxyList.add(entity);
                    logger.trace("extract candidate proxy:" + entity.address + " rest:" + capacity());
                    notEmpty();
                }
            }
        }finally{
            release();
        }
    }
    void active(int i){
        lock();
        try{
            ProxyEntity entity = proxyList.get(i);
            entity.disable = false;
            notEmpty();
        }finally{
            release();
        }
    }
    void disable(ProxyEntity entity, long time){
        if(proxyList.contains(entity)){
            lock();
            try{
                entity.disable = true;
                if(time > 0) {
                    entity.active = System.currentTimeMillis() + time;
                }
            }finally{
                release();
            }
        }
    }
    int capacity(){
        return proxyList.size();
    }
    public void remove(ProxyEntity entity){
        lock();
        try{
            proxyList.remove(entity);
        }finally{
            release();
        }
    }
    public void addProxy(String hostname, int port, int weight){
        ProxyEntity entity = new ProxyEntity(hostname, port, weight);
        addProxy(entity);
    }
    public void clear(){
        lock();
        try{
            proxyList.clear();
        }finally{
            release();
        }
    }
    public void lock(){
        this.lock.lock();
    }
    public void release(){
        this.lock.unlock();
    }
    public void empty() throws InterruptedException {
        this.notEmpty.await();
    }
    private void notEmpty(){
        this.notEmpty.signalAll();
    }
    public void addProxy(ProxyEntity entity) {
        lock();
        this.lock.lock();
        try {
            if(!proxyList.contains(entity)) {
                if(maxCapacity > 0 && proxyList.size() + 1 > maxCapacity){
                    //default, the number of candidacy  equals to maxCapacity * 5
                    if(candidacies.size()  < 5 * maxCapacity && !candidacies.contains(entity)){
                        logger.trace("add proxy:" + entity.address + " to candidacies");
                        candidacies.add(entity);
                    }
                    return;
                }
                proxyList.add(entity);
                logger.trace("add proxy:"+entity.address+" rest:" +capacity());
                notEmpty();
            }
        }finally{
            release();
            this.lock.unlock();
        }
    }

    public static class ProxyEntity{
        Proxy proxy;
        int weight;
        int cweight;
        String address;
        String hostname;
        int port;
        double rate;
        boolean disable = false;
        int reconnect = 0;
        ProxyConfig config;
        long active = -1;//time to active
        String badURL;//to record forbidden url
        String badContent;
        String badCode = "0";
        final AtomicInteger _2xx = new AtomicInteger();
        final AtomicInteger _3xx = new AtomicInteger();
        final AtomicInteger _4xx = new AtomicInteger();
        final AtomicInteger _5xx = new AtomicInteger();
        final AtomicInteger _xx = new AtomicInteger();

        public ProxyEntity(String hostname, int port, int weight){
            this.address = hostname + ":" + port;
            this.hostname =hostname;
            this.port = port;
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
            this.weight = weight;
            this.cweight = weight;
        }
        ProxyEntity(String address, int weight){
            this.address = address;
            this.weight = weight;
        }
        void clear(){
            _2xx.set(0);
            _3xx.set(0);
            _4xx.set(0);
            _5xx.set(0);
            _xx.set(0);
        }
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ProxyEntity){
                return address.equals(((ProxyEntity)obj).address);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return address.hashCode();
        }
        public Proxy getProxy(){
            return this.proxy;
        }
        public String getAddress(){
            return this.address;
        }
        public String getBadURL(){
            return badURL;
        }
    }
}
