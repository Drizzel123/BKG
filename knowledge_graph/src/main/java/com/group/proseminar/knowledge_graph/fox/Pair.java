package com.group.proseminar.knowledge_graph.fox;

public class Pair {
	String text;
	String type;

	public Pair(String text, String type) {
		this.text = text;
		this.type = type;
	}

	public String toString() {

		return "[" + this.text + ", " + this.type + "]";

	}
}