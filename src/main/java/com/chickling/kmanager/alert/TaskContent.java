package com.chickling.kmanager.alert;

/**
 * @author Hulva Luva.H
 *
 */
public class TaskContent {
  private String group;
  private String topic;
  private Integer diapause; //
  private Integer threshold;
  private String mailTo;

  private Integer consumerAPI;

  public TaskContent() {
    super();
  }

  public TaskContent(String group, String topic, Integer diapause, Integer threshold, String mailTo, Integer consumerAPI) {
    super();
    this.group = group;
    this.topic = topic;
    this.diapause = diapause;
    this.threshold = threshold;
    this.mailTo = mailTo;
    this.consumerAPI = consumerAPI;
  }

  public Integer getDiapause() {
    return diapause;
  }

  public void setDiapause(Integer diapause) {
    this.diapause = diapause;
  }

  public Integer getThreshold() {
    return threshold;
  }

  public void setThreshold(Integer threshold) {
    this.threshold = threshold;
  }

  public String getMailTo() {
    return mailTo;
  }

  public void setMailTo(String mailTo) {
    this.mailTo = mailTo;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }


  public Integer getConsumerAPI() {
    return consumerAPI;
  }

  public void setConsumerAPI(Integer consumerAPI) {
    this.consumerAPI = consumerAPI;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + ((topic == null) ? 0 : topic.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TaskContent other = (TaskContent) obj;
    if (group == null) {
      if (other.group != null) {
        return false;
      }
    } else if (!group.equals(other.group)) {
      return false;
    }
    if (topic == null) {
      if (other.topic != null) {
        return false;
      }
    } else if (!topic.equals(other.topic)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TaskContent [group=" + group + ", topic=" + topic + ", diapause=" + diapause + ", threshold=" + threshold + ", mailTo=" + mailTo
        + "]";
  }

}
