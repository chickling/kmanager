package com.chickling.kmanager.model;

/**
 * @author Hulva Luva.H
 *
 */
public class ConsumerDetail {
	private String name;

	public ConsumerDetail(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ConsumerDetail [name=" + name + "]";
	}

}
