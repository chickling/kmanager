package com.chickling.kmonitor.config;

/**
 * @author Hulva Luva.H
 *
 */
public class ZookeeperConfig {

	private String zkHosts;
	private int zkSessionTimeout;
	private int zkConnectionTimeout;

	public ZookeeperConfig() {
		super();
	}

	public ZookeeperConfig(String zkHosts, int zkSessionTimeout, int zkConnectionTimeout) {
		super();
		this.zkHosts = zkHosts;
		this.zkSessionTimeout = zkSessionTimeout;
		this.zkConnectionTimeout = zkConnectionTimeout;
	}

	public String getZkHosts() {
		return zkHosts;
	}

	public void setZkHosts(String zkHosts) {
		this.zkHosts = zkHosts;
	}

	public int getZkSessionTimeout() {
		return zkSessionTimeout;
	}

	public void setZkSessionTimeout(int zkSessionTimeout) {
		this.zkSessionTimeout = zkSessionTimeout;
	}

	public int getZkConnectionTimeout() {
		return zkConnectionTimeout;
	}

	public void setZkConnectionTimeout(int zkConnectionTimeout) {
		this.zkConnectionTimeout = zkConnectionTimeout;
	}

	@Override
	public String toString() {
		return "KafkaConsumerConfig [zkHosts=" + zkHosts + ", zkSessionTimeout=" + zkSessionTimeout
				+ ", zkConnectionTimeout=" + zkConnectionTimeout + "]";
	}

}
