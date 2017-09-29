package com.chickling.kmanager.model;

import org.apache.zookeeper.data.Stat;

/**
 * @author Hulva Luva.H
 *
 */
public class ZkDataAndStat {
	private String data;
	private Stat stat;

	public ZkDataAndStat() {
		super();
	}

	public ZkDataAndStat(String data, Stat stat) {
		super();
		this.data = data;
		this.stat = stat;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Stat getStat() {
		return stat;
	}

	public void setStat(Stat stat) {
		this.stat = stat;
	}

	@Override
	public String toString() {
		return "ZkDataAndState [data=" + data + ", stat=" + stat + "]";
	}

}
