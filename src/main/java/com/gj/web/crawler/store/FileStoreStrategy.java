package com.gj.web.crawler.store;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gj.web.crawler.http.utils.DataUtils;
/**
 * store in File-System
 * @author David
 *
 */
public class FileStoreStrategy implements StoreStrategy{

	public void localStore(String content, String loc) {
		FileOutputStream out = null;
		ByteBuffer buf = null;
		try{
			byte[] b = content.getBytes("UTF-8");
			buf = ByteBuffer.wrap(b);
			File directory = new File(loc.substring(0,loc.lastIndexOf("/")));
			directory.mkdirs();
			out = new FileOutputStream(loc);
			out.getChannel().write(buf);
			buf.clear();
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			if(null != out)
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			buf.clear();
		}
	}

	public void localStore(InputStream in, String loc) {
		FileOutputStream out = null;
		BufferedInputStream bfIn = null;
		try{
			out = new FileOutputStream(loc);
			bfIn = new 
					BufferedInputStream(in,DataUtils.BUFFER_SIZE);//download from input stream
			int size = -1;
			byte[] b = new byte[DataUtils.BUFFER_SIZE];
			while((size = bfIn.read(b)) > 0){
				out.write(b,0,size);
			}
			out.flush();
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			try {
				if(null != bfIn)bfIn.close();
				if(null != out)out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
