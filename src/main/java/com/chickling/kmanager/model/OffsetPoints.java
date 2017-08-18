package com.chickling.kmanager.model;

/**
 * @author Hulva Luva.H
 *
 */
public class OffsetPoints {
  private Long timestamp;
  private Integer partition;
  private String owner;
  private Long offset;
  private Long logSize;

  public OffsetPoints() {
    super();
  }

  public OffsetPoints(Long timestamp, Integer partition, String owner, Long offset, Long logSize) {
    super();
    this.timestamp = timestamp;
    this.partition = partition;
    this.owner = owner;
    this.offset = offset;
    this.logSize = logSize;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getPartition() {
    return partition;
  }

  public void setPartition(Integer partition) {
    this.partition = partition;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
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

  @Override
  public String toString() {
    return "OffsetPoints [timestamp=" + timestamp + ", partition=" + partition + ", owner=" + owner + ", offset=" + offset + ", logSize="
        + logSize + "]";
  }


}
