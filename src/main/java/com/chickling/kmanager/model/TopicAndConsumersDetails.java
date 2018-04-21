package com.chickling.kmanager.model;

import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class TopicAndConsumersDetails {
	private List<KafkaInfo> zk;
	private List<KafkaInfo> broker;

	public TopicAndConsumersDetails() {
		super();
	}

	public TopicAndConsumersDetails(List<KafkaInfo> zk, List<KafkaInfo> broker) {
		super();
		this.zk = zk;
		this.broker = broker;
	}

	public List<KafkaInfo> getZk() {
		return zk;
	}

	public void setZk(List<KafkaInfo> zk) {
		this.zk = zk;
	}

	public List<KafkaInfo> getBroker() {
		return broker;
	}

	public void setBroker(List<KafkaInfo> broker) {
		this.broker = broker;
	}

	@Override
	public String toString() {
		return "TopicAndConsumersDetails [zk=" + zk + ", broker=" + broker + "]";
	}

}
