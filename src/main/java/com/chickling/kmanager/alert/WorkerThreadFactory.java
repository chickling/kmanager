
package com.chickling.kmanager.alert;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

public class WorkerThreadFactory implements ThreadFactory {
	private AtomicInteger counter = new AtomicInteger(0);
	private String prefix = "";

	public WorkerThreadFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, prefix + "-" + counter.incrementAndGet());
		t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LoggerFactory.getLogger(t.getName()).error(e.getMessage(), e);
			}
		});
		return t;
	}

}
