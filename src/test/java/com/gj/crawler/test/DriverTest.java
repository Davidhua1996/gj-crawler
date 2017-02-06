package com.gj.crawler.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
//
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.htmlunit.HtmlUnitDriver;
//import org.openqa.selenium.remote.DesiredCapabilities;
//import org.openqa.selenium.remote.RemoteWebDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebRequest;

public class DriverTest {

	public static void main(String[] args) throws Exception{
//		WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME,true);
//		driver.get("http://www.bilibili.com/video/av7691117/");
//		System.out.println(driver.getPageSource());
		URL url = new URL("https://www.zhihu.com/question/51690276/answer/131152090");
		Socket socket = new Socket("171.39.80.247", 8123);
		SocketAddress address = socket.getRemoteSocketAddress();
		Proxy proxy = new Proxy(Type.HTTP, address);
		while(!socket.isClosed()){
			HttpURLConnection con = (HttpURLConnection) url.openConnection(proxy);;
			con.setRequestMethod("GET");
			con.connect();
			System.out.println(con.getHeaderField("Content-Type"));
			System.out.println(con.getLastModified());
			con.disconnect();
		}
		socket.close();
	}
	public List<String> getProxys() throws Exception{
		List<String> proxys = new ArrayList<String>();
		URL url = new URL("http://www.xicidaili.com/nn/1");
		return null;
	}
}
