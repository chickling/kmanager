package com.chickling.kmanager.alert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.email.EmailSender;
import com.chickling.kmanager.email.Template;
import com.chickling.kmanager.initialize.SystemManager;
import com.chickling.kmanager.model.KafkaInfo;
import com.chickling.kmanager.model.OffsetInfo;

/**
 * @author Hulva Luva.H
 *
 */
public class TaskHandler implements Runnable {
  private static Logger LOG = LoggerFactory.getLogger(TaskHandler.class);

  @Override
  public void run() {
    while (true) {
      try {
        KafkaInfo kafkaInfo = SystemManager.offsetInfoCacheQueue.take();
        List<OffsetInfo> offsetInfoList = kafkaInfo.getOffsets();
        TaskContent task = null;
        Set<TaskContent> tasks = new HashSet<TaskContent>();
        for (OffsetInfo offsetInfo : offsetInfoList) {
          task = TaskManager.getTask(offsetInfo.getGroup(), offsetInfo.getTopic());
          if (task == null) {
            continue;
          }
          if (offsetInfo.getLag() > task.getThreshold()) {
            tasks.add(task);
            if (TaskManager.cachedTriggeredOffsetInfo.containsKey(offsetInfo.getTopic())) {
              TaskManager.cachedTriggeredOffsetInfo.get(offsetInfo.getTopic()).add(offsetInfo);
            } else {
              Set<OffsetInfo> offsetInfos = new HashSet<OffsetInfo>();
              offsetInfos.add(offsetInfo);
              TaskManager.cachedTriggeredOffsetInfo.put(offsetInfo.getTopic(), offsetInfos);
            }
          }
        }
        for (TaskContent _task : tasks) {
          Long lastSendTime = TaskManager.cachedLastSendTime.get(_task.getGroup() + "@" + _task.getTopic());
          // if alerting task keeping triggered, that make sure that
          // we do not keep on receiving email in a short time
          if (lastSendTime != null) {
            if ((System.currentTimeMillis() - lastSendTime) > _task.getDiapause() * 60 * 1000) {
              generateEmailContentAndSend(_task, TaskManager.cachedTriggeredOffsetInfo.get(_task.getTopic()));
            }
          } else {
            TaskManager.cachedLastSendTime.put(_task.getGroup() + "@" + _task.getTopic(), System.currentTimeMillis());
            generateEmailContentAndSend(_task, TaskManager.cachedTriggeredOffsetInfo.get(_task.getTopic()));
          }
        }
      } catch (Exception e) {
        LOG.error("Something went wrong when handle alert task...", e);
      }
    }
  }

  private void generateEmailContentAndSend(TaskContent _task, Set<OffsetInfo> offsetInfos) throws Exception {
    try {
      LOG.info("generateEmailContentAndSend...");
      TaskManager.cachedLastSendTime.put(_task.getGroup() + "@" + _task.getTopic(), System.currentTimeMillis());
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
      EmailSender.sendEmail(template.getContent(), _task.getMailTo(), _task.getGroup() + "@" + _task.getTopic());
    } catch (Exception e) {
      throw new RuntimeException("generateEmailContentAndSendException: ", e);
    }
  }

}
