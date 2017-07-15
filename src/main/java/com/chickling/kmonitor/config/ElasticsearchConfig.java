package com.chickling.kmonitor.config;

/**
 * @author Hulva Luva.H
 *
 */
public class ElasticsearchConfig {

	private String clusterName;
	private String hosts;
	private String index;
	private String doctype;

	public ElasticsearchConfig() {
		super();
	}

	public ElasticsearchConfig(String clusterName, String hosts, String index, String doctype) {
		super();
		this.clusterName = clusterName;
		this.hosts = hosts;
		this.index = index;
		this.doctype = doctype;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getDoctype() {
		return doctype;
	}

	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}

}
