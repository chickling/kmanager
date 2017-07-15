package com.chickling.kmonitor.model;

import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class OffsetHistory {
	private String group;
	private String topic;
	private List<OffsetPoints> offsets;

	public OffsetHistory(String group, String topic, List<OffsetPoints> offsets) {
		super();
		this.group = group;
		this.topic = topic;
		this.offsets = offsets;
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

	public List<OffsetPoints> getOffsets() {
		return offsets;
	}

	public void setOffsets(List<OffsetPoints> offsets) {
		this.offsets = offsets;
	}

	@Override
	public String toString() {
		return "OffsetHistory [group=" + group + ", topic=" + topic + ", offsets=" + offsets + "]";
	}

}
