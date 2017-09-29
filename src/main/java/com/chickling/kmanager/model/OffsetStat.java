package com.chickling.kmanager.model;

public class OffsetStat {
    private Long timestamp;
    private String group;
    private String topic;
    private Long offset;
    private Long lag;

    public OffsetStat() {
        super();
    }

    public OffsetStat(Long timestamp, String group, String topic, Long offset, Long lag) {
        super();
        this.timestamp = timestamp;
        this.group = group;
        this.topic = topic;
        this.offset = offset;
        this.lag = lag;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return this.topic;
    }

    public Long getOffset() {
        return this.offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public void setLag(Long lag) {
        this.lag = lag;
    }

    public Long getLag() {
        return this.lag;
    }
}