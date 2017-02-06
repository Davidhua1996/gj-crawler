package com.gj.web.crawler.htmlunit;

import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;

public class HtmlUnitUtils {
	public static void setWebConnection(WebClient client){
		client.setWebConnection(new FalsifyingWebConnection(client) {
			@Override
			public WebResponse getResponse(WebRequest request)
					throws IOException {
				String ajax = request.getAdditionalHeaders().get("X-Requested-With");
				String accept = request.getAdditionalHeaders().get("Accept");
				if(null != ajax && "XMLHttpRequest".equals(ajax)){
					return super.getResponse(request);
				}
				if(request.getUrl().getPath().endsWith(".js") || 
						accept.indexOf("text/html") >= 0){
					System.out.println(request.getUrl());
					return super.getResponse(request);
				}
				return createWebResponse(request, "", "text/html");
			}
			
		});
	}
}
