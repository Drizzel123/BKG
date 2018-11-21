package com.group.proseminar.knowledge_graph.nlp;

/**
 * Generic implementation of a triplet.
 * @author Stefan Werner
 *
 * @param <T>
 * @param <U>
 * @param <V>
 */
public class Triplet<T, U, V> {

	private final T first;
	private final U second;
	private final V third;

	public Triplet(T first, U second, V third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T getFirst() {
		return first;
	}

	public U getSecond() {
		return second;
	}

	public V getThird() {
		return third;
	}

	public String toString() {
		return "<" + first + ", " + second + ", " + third + ">";
	}
}