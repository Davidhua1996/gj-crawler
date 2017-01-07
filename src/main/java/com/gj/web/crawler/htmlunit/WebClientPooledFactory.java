package com.gj.web.crawler.htmlunit;



import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;

public class WebClientPooledFactory extends BasePoolableObjectFactory<WebClient>{
	static{
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log","org.apache.commons.logging.impl.NoOpLog");
	}
	@Override
	public WebClient makeObject() throws Exception {
		WebClient client = new WebClient(BrowserVersion.CHROME);
		client.setJavaScriptEngine(new JScriptEngine(client));
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
		return client;
	}
	

}
