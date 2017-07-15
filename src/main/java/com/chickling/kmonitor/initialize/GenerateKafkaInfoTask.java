package com.chickling.kmonitor.initialize;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.model.KafkaInfo;

/**
 * @author Hulva Luva.H
 * @since 2017-07-13
 *
 */
public class GenerateKafkaInfoTask implements Runnable {
	private static Logger LOG = LoggerFactory.getLogger(GenerateKafkaInfoTask.class);

	private String group;

	public GenerateKafkaInfoTask(String _group) {
		this.group = _group;
	}

	@Override
	public void run() {
		KafkaInfo kafkaInfo = null;
		try {
			kafkaInfo = SystemManager.og.getInfo(group, new ArrayList<String>());
			if (SystemManager.getConfig().getIsAlertEnabled() && !SystemManager.offsetInfoCacheQueue.offer(kafkaInfo))
				LOG.warn("Offer kafkaInfo into offsetInfoCacheQueue faild..Queue zise: "
						+ SystemManager.getConfig().getOffsetInfoCacheQueue() + ".Current cached: "
						+ SystemManager.offsetInfoCacheQueue.size());
			SystemManager.db.batchInsert(kafkaInfo.getOffsets());
		} catch (Exception e) {
			LOG.warn("GenerateKafkaInfoTask for group: " + this.group + " failed.", e);
		}
	}

}
