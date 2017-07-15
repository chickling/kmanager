package com.chickling.kmonitor.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年7月5日
 * @description
 *
 */
public class ScheduleTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				System.out.println(System.currentTimeMillis());
				try {
					Thread.sleep(2100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}, 0, 1 * 1000, TimeUnit.MILLISECONDS);
	}

}
