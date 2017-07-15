package com.chickling.kmonitor.model;

import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class TopicAndConsumersDetails {
	private List<KafkaInfo> active;
	private List<KafkaInfo> inactive;

	public TopicAndConsumersDetails() {
		super();
	}

	public TopicAndConsumersDetails(List<KafkaInfo> active, List<KafkaInfo> inactive) {
		super();
		this.active = active;
		this.inactive = inactive;
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
