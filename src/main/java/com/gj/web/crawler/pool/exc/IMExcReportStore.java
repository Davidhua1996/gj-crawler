package com.gj.web.crawler.pool.exc;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.gj.web.crawler.pool.basic.URL;

/**
 * <p>store the exception report in memory</p>
 * @author David
 *
 */
public class IMExcReportStore extends ExcReportAnalysisStore{
	private BlockingQueue<ExcReport> excpQueue = new LinkedBlockingQueue<ExcReport>();
	public void add(ExcReport report) {
		super.add(report);
		excpQueue.add(report);
	}

	public ExcReport take() throws InterruptedException {
		return excpQueue.take();
	}

	public int takeInList(List<ExcReport> list, int size) {
		return excpQueue.drainTo(list, size);
	}
	public int size(){
		return excpQueue.size();
	}
}
