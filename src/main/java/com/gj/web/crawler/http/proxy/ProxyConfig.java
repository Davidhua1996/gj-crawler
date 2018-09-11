package com.gj.web.crawler.http.proxy;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * proxy config for each crawler
 */
public class ProxyConfig {

    private final Map<String, Integer> codeForbiddenTimes = new ConcurrentHashMap<>();

    private final List<UnblockMethod> unblockMethods = new ArrayList<>();

    private final List<String> forbiddenContent = new ArrayList<>();
    public void addForbiddenTime(String code, Integer time){
        codeForbiddenTimes.put(code, time);
    }
    public Integer getForbiddenTime(String code){
        Integer time = codeForbiddenTimes.get(code);
        if(null == time){
            for(Map.Entry<String, Integer> entry : codeForbiddenTimes.entrySet()){
                String key = entry.getKey();
                if(code.matches(key)){
                    return entry.getValue();
                }
            }
        }
        return time;
    }
    public void addUnblockMethod(UnblockMethod method){
        unblockMethods.add(method);
    }

    public List<UnblockMethod> getUnblockMethodChain(){
        return Collections.unmodifiableList(unblockMethods);
    }
    public void addForbiddenContent(String content){
        this.forbiddenContent.add(content);
    }
    public boolean isForbiddenContent(String body){
        if(null != body) {
            body = body.trim();
            for (String content : forbiddenContent) {
                if (body.startsWith(content)) {
                    return true;
                }
            }
            //not html or json document
            if (!((body.startsWith("<") && body.endsWith(">")) ||
                    (body.startsWith("{") && body.endsWith("}")))) {
                return true;
            }
        }
        return false;
    }

    public boolean isForbiddenCode(String code){

        return null != getForbiddenTime(code);
    }

    public interface UnblockMethod{

        boolean isMatched(String code, String content);

        boolean unblock(ProxyContainer.ProxyEntity proxyEntity);

    }
}
