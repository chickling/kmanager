package com.chickling.kmanager.jmx;

import java.util.Map;

/**
 * @author Hulva Luva.H
 * @since 2017-7-22
 *
 */
public class ObjectNameHolder {
  private String metric;
  private String type;
  private String name;
  /**
   * <code>
   * kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=test ->  extra<"topic=test", JSONObject.NULL>
   * kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=test,partition=p1 -> extra<"topic=test", <"partition=p1", JSONObject.NULL>>
   * </code>
   */
  private Map<String, Object> extra;

  public ObjectNameHolder() {
    super();
  }

  public ObjectNameHolder(String metric, String type, String name, Map<String, Object> extra) {
    super();
    this.metric = metric;
    this.type = type;
    this.name = name;
    this.extra = extra;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(Map<String, Object> extra) {
    this.extra = extra;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((metric == null) ? 0 : metric.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ObjectNameHolder other = (ObjectNameHolder) obj;
    if (metric == null) {
      if (other.metric != null)
        return false;
    } else if (!metric.equals(other.metric))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

}
