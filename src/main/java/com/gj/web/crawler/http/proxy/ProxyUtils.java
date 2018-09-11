package com.gj.web.crawler.http.proxy;
import com.gj.web.crawler.http.utils.ConnectionHandler;
import com.gj.web.crawler.parse.json.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * proxy tool
 */
public class ProxyUtils {
    private static final int PROXY_HEALTH_CHECK_INTERVAL = 3000;
    private static final int PROXY_PERSIST_INTERVAL = 20000;
    private static final int PROXY_AVAIL_CHECK_INTERVAL = 20000;
    private static final int PROXY_AVAIL_MAX_RECONNECT = 3;
    private static final int PROXY_AVAIL_CHECK_TIME = 1;
    private static final int PROXY_CONTAINER_MAX_SIZE = 20;
    private static final String PROXY_AVAIL_CHECK_WEBSITE = "http://httpbin.org/get";
    private static final double CONNECT_RATIO = Math.log(0.6)/ 5000;
    private static final double READ_RATIO = Math.log(0.6) / 8000;
    private static final double HEALTH_CHECK_DECAY_RATIO = 0.25;
    private static final ProxyContainer container = new ProxyContainer(PROXY_CONTAINER_MAX_SIZE);
    private static final CopyOnWriteArrayList<ProxyContainer.ProxyEntity> invalids = new CopyOnWriteArrayList<>();
    private static final Map<String, String> forbids = new ConcurrentHashMap<>();
    private static final Logger logger = LogManager.getLogger(ProxyUtils.class);
    private static String PROXY_SERVER_LIST_LOCATION;
    private static double PROXY_AVAIL_LEVEL = 0.8;
    static{
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        PROXY_SERVER_LIST_LOCATION = "proxy-server-list";
        init();
        PROXY_SERVER_LIST_LOCATION = "proxy-server-list-"+System.getProperty("proxy-list", "default");
        init();//twice init
        TimerTask persist = new TimerTask() {
            @Override
            public void run() {
                persist();
            }
        };
        TimerTask availableCheck = new TimerTask() {
            @Override
            public void run() {
                logger.trace("Start to available_check, invalids  size:"+invalids.size());
                //try to active the invalid Proxy Server
                System.out.println("invalids:"+invalids.size());
                invalids.parallelStream().forEach((entity) ->{
                    Validate validate = null;
                    if(entity.disable) {
                        //try to unblock
                        boolean unblock = false;
                        if(null != entity.config){
                            for(ProxyConfig.UnblockMethod unblockMethod : entity.config.getUnblockMethodChain()){
                                if(unblockMethod.isMatched(entity.badCode, entity.badContent)){
                                    unblock = unblockMethod.unblock(entity);
                                    break;
                                }
                            }
                        }
                        if(unblock){
                            entity.active = 0;
                        }
                        if (entity.active >= 0 && entity.active < System.currentTimeMillis()) {
                            validate = isAvailable(entity.proxy, entity.badURL, entity.config);
                        } else if (entity.active < 0) {
                            invalids.remove(entity);
                            return;
                        } else {
                            return;
                        }
                    }else{
                        validate = isAvailable(entity.proxy, entity.badURL, entity.config);
                    }
                    if(validate.isResult()){
                        entity.reconnect = 0;
                        entity.rate = validate.rate;
                        entity.badURL = null;
                        entity.badContent = null;
                        entity.badCode = "0";
                        entity.active = -1;
                        entity.disable = false;
                        entity.clear();
                        container.addProxy(entity);//if available, add to the proxy container
                        forbids.remove(entity.address);
                        invalids.remove(entity);
                    }else if(++entity.reconnect >= PROXY_AVAIL_MAX_RECONNECT){
                        forbids.remove(entity.address);
                        invalids.remove(entity);//abort it;
                    }
                });
                }
        };
        TimerTask healthCheck = new TimerTask() {
            public void run() {
                int capacity = container.capacity();
                logger.trace("Start to health_check, container size:" + capacity );
                //check the Proxy Server in use;
                int record = capacity;
                for(int i = 0; i < capacity; i ++){
                    ProxyContainer.ProxyEntity entity = container.get(i);
                    if(entity.disable){
                        forbids.put(entity.address, entity.badURL);
                    }
                    if(entity.disable || !isHealthy(entity)){
                        //if disable(be forbidden) directly add to invalid list
                        //if not healthy,may be the network fluctuation,add to de invalid list,
                        container.remove(i--);
                        capacity --;
                        invalids.add(entity);
                    }
                }
                if(record != container.capacity()){
                    logger.info("[Health_Check] container size change: "+ container.capacity());
                }
            }
        };
        Timer timer = new Timer("PROXY_CONTAINER_TIMER");
        Timer timer1 = new Timer("PROXY_INVALID_TIMER");
        timer1.schedule(availableCheck, PROXY_AVAIL_CHECK_INTERVAL, PROXY_AVAIL_CHECK_INTERVAL);
        timer.schedule(healthCheck, PROXY_HEALTH_CHECK_INTERVAL, PROXY_HEALTH_CHECK_INTERVAL);
        timer.schedule(persist, PROXY_PERSIST_INTERVAL, PROXY_PERSIST_INTERVAL);
    }
    public static void record(ProxyContainer.ProxyEntity entity, ProxyConfig proxyConfig, String url, int respCode, String content){
        if(null != entity) {
            Integer time = null != proxyConfig?proxyConfig.getForbiddenTime(String.valueOf(respCode)):null;
            if(null != time && time > 0){//when be forbidden, disable the proxy
                container.disable(entity, time);
                entity.badURL = url;
                entity.badContent = content;
                entity.config = proxyConfig;
                entity.badCode = String.valueOf(respCode);
            }else if(respCode == -1){
                entity.badURL = url;
            }
            statsRespCode(entity, respCode);
        }
    }

    public static void removeProxy(String address){
        container.remove(new ProxyContainer.ProxyEntity(address, 1));
    }

    public static void addProxyIfAvailable(String hostname, Integer port){
        double rate = 0.0;
        for(int i = 0;i < PROXY_AVAIL_CHECK_TIME; i++){
            Validate validate = isAvailable(hostname, port);
            if(!validate.isResult()){
                return;
            }
            rate += validate.rate;
        }
        ProxyContainer.ProxyEntity entity = new ProxyContainer.ProxyEntity(hostname, port, 1);
        entity.rate = rate / PROXY_AVAIL_CHECK_TIME;
        container.addProxy(entity);
    }
    /**
     * check the proxy server if is available
     * @return
     */
    public static Validate isAvailable(String hostname, int port){
        if(forbids.get(hostname +":"+port) == null) {
            return isAvailable(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port)),
                    null , null);
        }else{
            return new Validate();
        }
    }
    public static Validate isAvailable(String hostname, int port, String url){
        return isAvailable(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port)), url, null);
    }
    public static Validate isAvailable(Proxy proxy){
        return isAvailable(proxy, null, null);
    }
    public static Validate isAvailable(Proxy proxy, String dist, ProxyConfig config){
        boolean result = true;
        URL url = null;
        long record = -1;
        long connectTime = -1;
        long readTime = -1;
        int code = -1;
        Validate validate = new Validate();
        try {
            url = new URL(dist == null?PROXY_AVAIL_CHECK_WEBSITE:dist);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection(proxy);
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout((int)(Math.log(PROXY_AVAIL_LEVEL)/CONNECT_RATIO));
            conn.setReadTimeout((int)(Math.log(PROXY_AVAIL_LEVEL)/READ_RATIO));
            conn.setInstanceFollowRedirects(true);//allow the redirects with response code 3xx
            conn.setDoOutput(false);//for crawler,don't use method POST or PUT
            conn.setDoInput(true);
            conn.setUseCaches(true);//use cache
            conn.setRequestMethod("GET");
            ConnectionHandler.wrapper(conn);
            record = System.currentTimeMillis();
            conn.connect();
            connectTime = System.currentTimeMillis() - record;
            record = System.currentTimeMillis();
            code = conn.getResponseCode();
            StringBuffer buffer = new StringBuffer();
            String tmp = null;
            InputStream in = conn.getInputStream();
            if(null != conn.getHeaderField("Content-Encoding")
                    && conn.getHeaderField("Content-Encoding").equals("gzip")){
                in = new GZIPInputStream(in);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                   in,"UTF-8"));
            while((tmp = reader.readLine())!= null){
                buffer.append(tmp);
            }
            readTime = System.currentTimeMillis() - record;
            conn.disconnect();
            if(dist == null) {
                String json = buffer.toString().trim();
                if (json.startsWith("{") && json.endsWith("}")) {
                    Map<String, Object> jsonMap = JsonUtils.fromJson(buffer.toString(), Object.class);
                    if (!jsonMap.get("url").equals(PROXY_AVAIL_CHECK_WEBSITE)) {
                        result = false;
                    }
                } else {
                    result = false;
                }
            }else{
                validate.content = buffer.toString();
            }
        }catch(Exception e){
           result = false;
        }
        validate.code = code;
        validate.connectTime  = connectTime;
        validate.readTime = readTime;
        if(null != config){
            if(config.isForbiddenContent(validate.content)){
                result = false;
            }
            if(result && config.isForbiddenCode(String.valueOf(code))){
                result = false;
            }
        }
        validate.result = result;
        if(result) {
            validate.rate = (Math.pow(Math.E, CONNECT_RATIO * connectTime) +
                    Math.pow(Math.E, READ_RATIO * readTime)) / 2.0;
        }
        logger.trace("result:"+result +",validate:"+ JsonUtils.toJson(validate, Validate.class)+",url="+dist);
        return validate;
    }
    /**
     * get proxy by round robin
     * @return
     */
    public static ProxyContainer.ProxyEntity nextProxyEntity(){
        container.lock();
        try{
            if(container.capacity() <= 0){
                container.empty();
            }
            int u = 0;
            int reset = -1;
            int avalid = -1;
            while(true){
                for(int i = 0; i < container.capacity(); i++){
                    if(container.get(i).cweight <= 0 || container.get(i).disable){
                        continue;
                    }
                    u = i;
                    while(i < container.capacity() - 1) {
                        i++;
                        if (container.get(i).cweight <= 0 || container.get(i).disable) {
                            continue;
                        }
                        if ((container.get(u).weight * 1000 / container.get(i).weight <
                                container.get(u).cweight * 1000 / container.get(i).weight)) {
                            return container.get(u);
                        }
                        u = i;
                    }
                    ProxyContainer.ProxyEntity entity = container.get(u);
                    entity.cweight --;
                    logger.trace("use proxy:" + entity.address);
                    return entity;
                }
                if(reset ++ > 0){
                    if(avalid < 0){
                        container.empty();
                    }
                    for(int i = 0; i < container.capacity(); i ++) {
                        if(!container.get(i).disable) {
                            ProxyContainer.ProxyEntity entity = container.get(0);
                            entity.cweight--;
                            logger.trace("use proxy:" + entity.address);
                            return entity;
                        }
                    }
                    throw new RuntimeException("Unexpected proxy container statement!");
                }
                for(int i = 0 ; i < container.capacity(); i++){
                    ProxyContainer.ProxyEntity entity = container.get(i);
                    entity.cweight = entity.weight;
                    if(avalid < 0 && !entity.disable){
                        avalid = i;
                    }
                }
            }
        } catch (InterruptedException e) {
           logger.error(e);
           return null;
        } finally{
            container.release();
        }
    }

    /**
     *
     * @param entity
     */
    private static boolean isHealthy(ProxyContainer.ProxyEntity entity){
        double rate = entity.rate;
        int _2xx =  entity._2xx.getAndSet(0);
        int _3xx =  entity._3xx.getAndSet(0);
        int _4xx = entity._4xx.getAndSet(0);
        int _5xx =  entity._5xx.getAndSet(0);
        int _xx  =  entity._xx.getAndSet(0);
        int sum = _2xx + _3xx + _4xx + _5xx + _xx;
        int fail = _4xx + _5xx + _xx;
        if(sum > 0) {
//            System.out.println(HEALTH_CHECK_DECAY_RATIO * ((double)fail / (double)sum));
            rate -= (HEALTH_CHECK_DECAY_RATIO * ((double)fail / (double)sum));
        }
        entity.rate = rate;
        logger.info("[Health Check]:, rate:" + rate + ", fail:" + fail + ", sum:"+sum+", address:" + entity.getAddress());
        return rate > 0.5;
    }
    private static void statsRespCode(ProxyContainer.ProxyEntity entity, int respCode){
        if(respCode >= 200 && respCode < 300){
            entity._2xx.incrementAndGet();
        }else if(respCode >= 300 && respCode < 400){
            entity._3xx.incrementAndGet();
        }else if(respCode >= 400 && respCode < 500){
            entity._4xx.incrementAndGet();
        }else if(respCode >= 500 && respCode < 600){
            entity._5xx.incrementAndGet();
        }else{
            entity._xx.incrementAndGet();
        }
    }
    private static void persist(){
        BufferedWriter writer = null;
        try{
            logger.trace("Start to persist ProxyUtils.Proxy");
            List<String> addrList = new ArrayList<String>();
            container.lock();
            try {
                logger.trace("Get address list from ProxyUtils.ProxyContainer,size:"+container.capacity());
                for (int i = 0; i < container.capacity(); i++){
                    addrList.add(container.get(i).address);
                }
            }finally {
                container.release();
            }
            logger.trace("Write address list to location:"+PROXY_SERVER_LIST_LOCATION);
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream((ProxyUtils.class.getClassLoader().getResource("").getPath()+PROXY_SERVER_LIST_LOCATION)),
                    "UTF-8"));
            for(int i = 0; i < addrList.size(); i ++){
                writer.write(addrList.get(i));
                writer.newLine();
            }
            writer.flush();
            logger.trace("End to persist ProxyUtils.ProxyContainer");
        } catch (FileNotFoundException e) {
            logger.error(e);
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }finally{
            if(null!= writer){
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.error("OutputStream is null");
                }
            }
        }
    }
    private static void init(){
        if("true".equals(System.getProperty("debug"))){
            return;
        }
        BufferedReader reader = null;
        try {
            logger.info("Start to init ProxyUtils.ProxyContainer, location="+PROXY_SERVER_LIST_LOCATION);
            File file = new File(ProxyUtils.class.getClassLoader().getResource("").toURI().getPath()+PROXY_SERVER_LIST_LOCATION);
            if(!file.exists()){
                file.createNewFile();
                return;
            }
            reader = new BufferedReader(new InputStreamReader
                    (ProxyUtils.class.getClassLoader().getResourceAsStream(PROXY_SERVER_LIST_LOCATION), "UTF-8"));
            String address = null;
            while((address = reader.readLine())!= null){
                String[] item = address.split(":");
                if(item.length == 2){
                    addProxyIfAvailable(item[0], Integer.valueOf(item[1]));
                }
            }
            logger.info("End to init ProxyUtils.ProxyContainer");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (URISyntaxException e) {
            logger.error(e);
        } finally{
            if(null!= reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("InputStream is null");
                }
            }
        }
    }

    public static class Validate implements Serializable{
        private boolean result = false;
        private int code = -1;
        private double rate = 0d;
        private double connectTime = -1;
        private String content;
        private double readTime = -1;

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public double getConnectTime() {
            return connectTime;
        }

        public void setConnectTime(double connectTime) {
            this.connectTime = connectTime;
        }

        public double getReadTime() {
            return readTime;
        }

        public void setReadTime(double readTime) {
            this.readTime = readTime;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
