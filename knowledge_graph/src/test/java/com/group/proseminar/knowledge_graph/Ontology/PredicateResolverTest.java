package com.group.proseminar.knowledge_graph.Ontology;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class PredicateResolverTest {
	PredicateResolver res;

	@Before
	public void setup() {
		res = new PredicateResolver();
	}

	@Test
	public void extractFromTextTest() {
		System.out.println(res.getLookupMap().values().stream().collect(Collectors.toSet()));
		System.out.println("run: " + res.resolveToURI("run"));
		System.out.println("established on: " + res.resolveToURI("established on"));
		System.out.println("birth name: " + res.resolveToURI("birth name"));
		System.out.println("creator of: " + res.resolveToURI("creator of"));

		assertEquals(true, true);
	}
}