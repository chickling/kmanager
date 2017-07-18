package com.chickling.kmonitor.alert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.email.EmailSender;
import com.chickling.kmonitor.email.Template;
import com.chickling.kmonitor.initialize.SystemManager;
import com.chickling.kmonitor.model.KafkaInfo;
import com.chickling.kmonitor.model.OffsetInfo;

/**
 * @author Hulva Luva.H
 *
 */
public class TaskHandler implements Runnable {
	private static Logger LOG = LoggerFactory.getLogger(TaskHandler.class);

	@Override
	public void run() {
		try {
			while (true) {
				KafkaInfo kafkaInfo = SystemManager.offsetInfoCacheQueue.take();

				Map<String, Set<OffsetInfo>> cachedTriggeredOffsetInfo = new HashMap<String, Set<OffsetInfo>>();

				List<OffsetInfo> offsetInfoList = kafkaInfo.getOffsets();
				TaskContent task = null;
				Set<TaskContent> tasks = new HashSet<TaskContent>();
				for (OffsetInfo offsetInfo : offsetInfoList) {
					task = TaskManager.getTask(offsetInfo.getGroup(), offsetInfo.getTopic());
					if (task == null) {
						continue;
					}
					tasks.add(task);
					if (offsetInfo.getLag() > task.getThreshold()) {
						if (cachedTriggeredOffsetInfo.containsKey(offsetInfo.getTopic())) {
							cachedTriggeredOffsetInfo.get(offsetInfo.getTopic()).add(offsetInfo);
						} else {
							Set<OffsetInfo> offsetInfos = new HashSet<OffsetInfo>();
							offsetInfos.add(offsetInfo);
							cachedTriggeredOffsetInfo.put(offsetInfo.getTopic(), offsetInfos);
						}
					}
				}
				for (TaskContent _task : tasks) {
					Long lastSendTime = TaskManager.cachedLastSendTime.get(_task.getGroup() + "_" + _task.getTopic());
					// if alerting task keeping triggered, that make sure that
					// we do not keep on receiving email in a short time
					if (lastSendTime != null) {
						if ((System.currentTimeMillis() - lastSendTime) > _task.getDiapause() * 60 * 1000) {
							generateEmailContentAndSend(_task, cachedTriggeredOffsetInfo.get(_task.getTopic()));
						}
					} else {
						TaskManager.cachedLastSendTime.put(_task.getGroup() + "_" + _task.getTopic(),
								System.currentTimeMillis());
						generateEmailContentAndSend(_task, cachedTriggeredOffsetInfo.get(_task.getTopic()));
					}
				}
			}
		} catch (InterruptedException e) {
			LOG.error("Something went wrong when handle alert task...", e);
		}
	}

	private void generateEmailContentAndSend(TaskContent _task, Set<OffsetInfo> offsetInfos) {
		TaskManager.cachedLastSendTime.put(_task.getGroup() + "_" + _task.getTopic(), System.currentTimeMillis());
		Template template = new Template();
		StringBuilder blabla;
		blabla = new StringBuilder();
		for (OffsetInfo offsetInfo : offsetInfos) {
			blabla.append("<tr><td style=\"border: 1px solid #ddd;\">" + offsetInfo.getGroup() + "</td>");
			blabla.append("<td style=\"border: 1px solid #ddd;\">" + offsetInfo.getTopic() + "</td>");
			blabla.append("<td style=\"border: 1px solid #ddd;\">" + offsetInfo.getPartition() + "</td>");
			blabla.append("<td style=\"border: 1px solid #ddd;\">" + offsetInfo.getLag() + "</td><tr>");
		}
		template.insertTr(blabla.toString());
		EmailSender.sendEmail(template.getContent(), _task.getMailTo(), _task.getGroup() + "_" + _task.getTopic());
	}

}
