package com.chickling.kmanager.model;

import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class TopicDetails {
	private List<ConsumerDetail> consumers;

	public TopicDetails(List<ConsumerDetail> consumers) {
		super();
		this.consumers = consumers;
	}

	public List<ConsumerDetail> getConsumers() {
		return consumers;
	}

	public void setConsumers(List<ConsumerDetail> consumers) {
		this.consumers = consumers;
	}

}
