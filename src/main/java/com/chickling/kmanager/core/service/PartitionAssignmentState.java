/**
 * 
 */
package com.chickling.kmanager.core.service;

import java.util.Optional;

import org.apache.kafka.common.Node;

/**
 * @author Hulva Luva.H
 * @since 2018年4月23日
 */
public class PartitionAssignmentState {
	private String group;
	private Optional<Node> coordinator;
	private Optional<String> topic;
	private Optional<Integer> partition;
	private Optional<Long> offset;
	private Optional<Long> lag;
	private Optional<String> consumerId;
	private Optional<String> host;
	private Optional<String> clientId;
	private Optional<Long> logEndOffset;
	public PartitionAssignmentState(String group, Optional<Node> coordinator, Optional<String> topic,
			Optional<Integer> partition, Optional<Long> offset, Optional<Long> lag, Optional<String> consumerId,
			Optional<String> host, Optional<String> clientId, Optional<Long> logEndOffset) {
		super();
		this.group = group;
		this.coordinator = coordinator;
		this.topic = topic;
		this.partition = partition;
		this.offset = offset;
		this.lag = lag;
		this.consumerId = consumerId;
		this.host = host;
		this.clientId = clientId;
		this.logEndOffset = logEndOffset;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public Optional<Node> getCoordinator() {
		return coordinator;
	}
	public void setCoordinator(Optional<Node> coordinator) {
		this.coordinator = coordinator;
	}
	public Optional<String> getTopic() {
		return topic;
	}
	public void setTopic(Optional<String> topic) {
		this.topic = topic;
	}
	public Optional<Integer> getPartition() {
		return partition;
	}
	public void setPartition(Optional<Integer> partition) {
		this.partition = partition;
	}
	public Optional<Long> getOffset() {
		return offset;
	}
	public void setOffset(Optional<Long> offset) {
		this.offset = offset;
	}
	public Optional<Long> getLag() {
		return lag;
	}
	public void setLag(Optional<Long> lag) {
		this.lag = lag;
	}
	public Optional<String> getConsumerId() {
		return consumerId;
	}
	public void setConsumerId(Optional<String> consumerId) {
		this.consumerId = consumerId;
	}
	public Optional<String> getHost() {
		return host;
	}
	public void setHost(Optional<String> host) {
		this.host = host;
	}
	public Optional<String> getClientId() {
		return clientId;
	}
	public void setClientId(Optional<String> clientId) {
		this.clientId = clientId;
	}
	public Optional<Long> getLogEndOffset() {
		return logEndOffset;
	}
	public void setLogEndOffset(Optional<Long> logEndOffset) {
		this.logEndOffset = logEndOffset;
	}
	@Override
	public String toString() {
		return "PartitionAssignmentState [group=" + group + ", coordinator=" + coordinator + ", topic=" + topic
				+ ", partition=" + partition + ", offset=" + offset + ", lag=" + lag + ", consumerId=" + consumerId
				+ ", host=" + host + ", clientId=" + clientId + ", logEndOffset=" + logEndOffset + "]";
	}
}
