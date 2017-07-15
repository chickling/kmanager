package com.chickling.kmonitor.model;

/**
 * @author Hulva Luva.H
 *
 */
public class TopicAndGroup {
	private String topic;
	private String group;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public String toString() {
		return "TopicAndGroup [topic=" + topic + ", group=" + group + "]";
	}

}
