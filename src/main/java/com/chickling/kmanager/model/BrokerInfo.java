package com.chickling.kmanager.model;

/**
 * @author Hulva Luva.H
 *
 */
public class BrokerInfo {
	private int bid;
	private String host;
	private int port;
	private int jmxPort;
	private long timestamp;
	private int version;

	public BrokerInfo() {

	}

	public BrokerInfo(int bid, String host, int port) {
		super();
		this.bid = bid;
		this.host = host;
		this.port = port;
	}

	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "BrokerInfo [bid=" + bid + ", host=" + host + ", port=" + port + "]";
	}

	public int getJmxPort() {
		return jmxPort;
	}

	public void setJmxPort(int jmx_port) {
		this.jmxPort = jmx_port;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
