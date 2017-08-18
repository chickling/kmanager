package com.chickling.kmanager.model;

/**
 * @author Hulva Luva.H
 *
 */

public class OffsetHistoryQueryParams {
	private String interval; // 5m, 30m, 1h, 1d
	private String range; // 8h, 16h, 1d, 2d, 1w
	private String rangeto;
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

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getRangeto() {
		return rangeto;
	}

	public void setRangeto(String rangeto) {
		this.rangeto = rangeto;
	}

	@Override
	public String toString() {
		return "LagRequestParams [interval=" + interval + ", range=" + range + ", rangeTo=" + rangeto + ", topic="
				+ topic + ", group=" + group + "]";
	}
}
