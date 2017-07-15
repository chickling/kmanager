/**
 Copyright 2017 2017 Hulva Luva.H
**/
package com.chickling.kmonitor.model;

import org.apache.kafka.common.protocol.types.Struct;

/**
 * @author Hulva Luva.H
 *
 */
public class MessageValueStructAndVersion {
	private Struct struct;
	private Short version;

	public MessageValueStructAndVersion() {
		super();
	}

	public Struct getStruct() {
		return struct;
	}

	public MessageValueStructAndVersion(Struct struct, Short version) {
		super();
		this.struct = struct;
		this.version = version;
	}

	public void setStruct(Struct struct) {
		this.struct = struct;
	}

	public Short getVersion() {
		return version;
	}

	public void setVersion(Short version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "MessageValueStructAndVersion [struct=" + struct + ", version=" + version + "]";
	}

}
