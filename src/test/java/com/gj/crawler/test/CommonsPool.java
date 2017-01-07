package com.gj.crawler.test;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gj.web.crawler.htmlunit.HtmlUnitUtils;
import com.gj.web.crawler.htmlunit.WebClientPooledFactory;

public class CommonsPool {
	public static void main(String[] args) {
		SoftReferenceObjectPool<WebClient> softPool = 
				new SoftReferenceObjectPool<WebClient>(new WebClientPooledFactory());
		WebClient client = null;
		WebConnection webconn = null;
		try {
			client = softPool.borrowObject();
			String url = "http://www.bilibili.com/video/av2035575/";
			webconn = client.getWebConnection();//store temply
			HtmlUnitUtils.setWebConnection(client);
			HtmlPage page = client.getPage(url);
			System.out.println(page.asXml());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != client){
				client.setWebConnection(webconn);
				try {
					softPool.returnObject(client);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
