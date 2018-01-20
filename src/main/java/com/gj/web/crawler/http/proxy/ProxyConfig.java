package com.gj.web.crawler.http.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * proxy config for each crawler
 */
public class ProxyConfig {

    private final Map<String, Integer> codeForbiddenTimes = new ConcurrentHashMap<>();

    public void addForbiddenTime(String code, Integer time){
        codeForbiddenTimes.put(code, time);
    }
    public Integer getForbiddenTime(String code){
        return codeForbiddenTimes.get(code);
    }
}
