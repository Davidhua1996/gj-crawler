package com.gj.crawler.test;

import java.io.IOException;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import net.sourceforge.htmlunit.corejs.javascript.Script;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowImpl;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;
import com.gargoylesoftware.htmlunit.util.NameValuePair;


public class HtmlUnitTest {
	public static void main(String[] args) throws Exception{
		try{
			WebClient client = new WebClient(BrowserVersion.CHROME);
			JavaScriptEngine engine = client.getJavaScriptEngine();
			client.getOptions().setCssEnabled(false);
			client.getOptions().setJavaScriptEnabled(true);
			client.getOptions().setActiveXNative(false);
			client.getOptions().setThrowExceptionOnScriptError(false);
			client.getOptions().setAppletEnabled(false);
			client.getOptions().setThrowExceptionOnFailingStatusCode(false);
			client.getOptions().setDoNotTrackEnabled(true);
			client.getOptions().setThrowExceptionOnFailingStatusCode(false);
			client.waitForBackgroundJavaScript(1000);
			client.getOptions().setTimeout(1200);
			client.setAjaxController(new NicelyResynchronizingAjaxController());
			client.waitForBackgroundJavaScriptStartingBefore(1000);
			final String title = "http://www.bilibili.com/video/av2035575/";
			client.setWebConnection(new FalsifyingWebConnection(client) {
				@Override
				public WebResponse getResponse(WebRequest request)
						throws IOException {
					String ajax = request.getAdditionalHeaders().get("X-Requested-With");
					if(null != ajax && "XMLHttpRequest".equals(ajax)){
						System.out.println(request.getUrl().getPath());
						return super.getResponse(request);
					}
					if(request.getUrl().getPath().endsWith(".js") || 
							request.getUrl().toString().equals(title)){
						System.out.println(request.getUrl().getPath());
						return super.getResponse(request);
					}
	//				createWebResponse(wr, content, contentType)
					return createWebResponse(request, "", "application/javascript");
				}
				
			});
			String result = "";
			for(int i = 0;i<5;i++){
				long current = System.currentTimeMillis();
				HtmlPage page = client.getPage(title);
	//			ScriptableObject object = page.getScriptObject();
				System.out.println(page.asXml());
				Document doc = Jsoup.parse(page.asXml());
				result += System.currentTimeMillis() - current+"_";
			}
			System.out.println(result);
		}catch(Exception e){
			
		}
	}
}
