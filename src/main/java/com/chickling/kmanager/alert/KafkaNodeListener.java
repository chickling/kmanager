package com.chickling.kmanager.alert;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.utils.ZKUtils;

import kafka.utils.ZkUtils;

/**
 * @author Hulva Luva.H
 * 
 *         Listening Kafka node on ZK
 *
 */
public class KafkaNodeListener {
	private static Logger LOG = LoggerFactory.getLogger(KafkaNodeListener.class);
	private static ExecutorService exec = null;
	

	public KafkaNodeListener() {
		exec = Executors.newCachedThreadPool(new DaemonThreadFactory("KafkaNodeOffLineListener"));
	}

	public void startListener() {
		LOG.info("Starting Kafka ZK node listener...");
		exec.execute(new Runnable() {

			@Override
			public void run() {
				ZKUtils.getZKClient().subscribeChildChanges(ZkUtils.BrokerIdsPath(), new IZkChildListener() {

					@Override
					public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
						
					}
					
				});
			}

		});
	}

}
