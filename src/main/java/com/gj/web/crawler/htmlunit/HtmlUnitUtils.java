package com.gj.web.crawler.htmlunit;

import java.io.IOException;
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
				System.out.println(request.getUrl());
				if(null != ajax && "XMLHttpRequest".equals(ajax)){
					return super.getResponse(request);
				}
				if(request.getUrl().getPath().endsWith(".js") || 
						accept.indexOf("text/html") >= 0){
					return super.getResponse(request);
				}
				return createWebResponse(request, "", "text/html");
			}
		});
		
	}
}
