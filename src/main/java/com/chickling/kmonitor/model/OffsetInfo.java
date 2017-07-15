package com.chickling.kmonitor.model;

/**
 * @author Hulva Luva.H
 *
 */
public class OffsetInfo {
	private String group;
	private String topic;
	private Integer partition;
	private Long offset;
	private Long logSize;
	private String owner;
	private Long creation;
	private Long modified;
	private Long lag;

	public OffsetInfo() {
		super();
	}

	public OffsetInfo(String group, String topic, Integer partition, Long offset, Long logSize, String owner,
			Long creation, Long modified) {
		super();
		this.group = group;
		this.topic = topic;
		this.partition = partition;
		this.offset = offset;
		this.logSize = logSize;
		this.owner = owner;
		this.creation = creation;
		this.modified = modified;
		this.setLag();
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

	public Integer getPartition() {
		return partition;
	}

	public void setPartition(Integer partition) {
		this.partition = partition;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public Long getLogSize() {
		return logSize;
	}

	public void setLogSize(Long logSize) {
		this.logSize = logSize;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getCreation() {
		return creation;
	}

	public void setCreation(Long creation) {
		this.creation = creation;
	}

	public Long getModified() {
		return modified;
	}

	public void setModified(Long modified) {
		this.modified = modified;
	}

	public Long getLag() {
		return lag;
	}

	public void setLag() {
		this.lag = logSize - offset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + partition;
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
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
		OffsetInfo other = (OffsetInfo) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (partition != other.partition)
			return false;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OffsetInfo [group=" + group + ", topic=" + topic + ", partition=" + partition + ", offset=" + offset
				+ ", logSize=" + logSize + ", creation=" + creation + ", modified=" + modified + ", lag=" + lag + "]";
	}

}
