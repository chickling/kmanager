package com.chickling.kmanager.model;

import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class Node {
	private String name;
	private List<Node> children;

	public Node() {
		super();
	}

	public Node(String name, List<Node> children) {
		super();
		this.name = name;
		this.children = children;
	}

	public Node(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "Node [name=" + name + ", children=" + children + "]";
	}

}
