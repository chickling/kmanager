package com.chickling.kmonitor.model;

import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class KafkaInfo {
	private String GroupName;
	private List<BrokerInfo> brokers;
	private List<OffsetInfo> offsets;

	public KafkaInfo() {
		super();
	}

	public KafkaInfo(String name, List<BrokerInfo> brokers, List<OffsetInfo> offsets) {
		super();
		this.GroupName = name;
		this.brokers = brokers;
		this.offsets = offsets;
	}

	public String getName() {
		return GroupName;
	}

	public void setName(String name) {
		this.GroupName = name;
	}

	public List<BrokerInfo> getBrokers() {
		return brokers;
	}

	public void setBrokers(List<BrokerInfo> brokers) {
		this.brokers = brokers;
	}

	public List<OffsetInfo> getOffsets() {
		return offsets;
	}

	public void setOffsets(List<OffsetInfo> offsets) {
		this.offsets = offsets;
	}

	@Override
	public String toString() {
		return "KafkaInfo [GroupName=" + GroupName + ", brokers=" + brokers + ", offsets=" + offsets + "]";
	}

}
