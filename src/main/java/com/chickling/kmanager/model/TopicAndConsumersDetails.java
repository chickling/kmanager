package com.chickling.kmanager.model;

import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class TopicAndConsumersDetails {
  private List<KafkaInfo> active;
  private List<KafkaInfo> inactive;
  private List<KafkaInfo> extra;

  public TopicAndConsumersDetails() {
    super();
  }

  public TopicAndConsumersDetails(List<KafkaInfo> active, List<KafkaInfo> inactive, List<KafkaInfo> extra) {
    super();
    this.active = active;
    this.inactive = inactive;
    this.extra = extra;
  }

  public List<KafkaInfo> getExtra() {
    return extra;
  }

  public void setExtra(List<KafkaInfo> extra) {
    this.extra = extra;
  }

  public List<KafkaInfo> getActive() {
    return active;
  }

  public void setActive(List<KafkaInfo> active) {
    this.active = active;
  }

  public List<KafkaInfo> getInactive() {
    return inactive;
  }

  public void setInactive(List<KafkaInfo> inactive) {
    this.inactive = inactive;
  }

  @Override
  public String toString() {
    return "TopicAndConsumersDetails [active=" + active + ", inactive=" + inactive + "]";
  }

}
