package com.chickling.kmonitor.model;

import kafka.common.TopicAndPartition;

/**
 * @author Hulva Luva.H
 *
 */
public class GroupTopicPartition {
	private String group;
	private TopicAndPartition topicAndPartition;

	public GroupTopicPartition(String group, TopicAndPartition topicAndPartition) {
		super();
		this.group = group;
		this.topicAndPartition = topicAndPartition;
	}

	public GroupTopicPartition(String group, String topic, int partition) {
		this(group, new TopicAndPartition(topic, partition));
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public TopicAndPartition getTopicAndPartition() {
		return topicAndPartition;
	}

	public void setTopicAndPartition(TopicAndPartition topicAndPartition) {
		this.topicAndPartition = topicAndPartition;
	}

	@Override
	public String toString() {
		return "GroupTopicPartition [group=" + group + ", topicAndPartition=" + topicAndPartition + "]";
	}

}
