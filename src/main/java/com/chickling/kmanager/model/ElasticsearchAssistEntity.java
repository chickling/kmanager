package com.chickling.kmanager.model;

import java.util.List;
import java.util.Date;

public class ElasticsearchAssistEntity {
    private String interval;
    private List<String> indexs;
    private Date start;
    private Date end;

    public ElasticsearchAssistEntity() {
        super();
    }

    public ElasticsearchAssistEntity(String interval, List<String> indexs, Date start, Date end) {
        super();
        this.interval = interval;
        this.indexs = indexs;
        this.start = start;
        this.end = end;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getInterval() {
        return this.interval;
    }

    public void setIndexs(List<String> indexs) {
        this.indexs = indexs;
    }

    public List<String> getIndexs() {
        return this.indexs;
    }

    public void setStart(Date start){
        this.start = start;
    }

    public Date getStart(){
        return this.start;
    }
    public void setEnd(Date end){
        this.end = end;
    }

    public Date getEnd(){
        return this.end;
    }
}