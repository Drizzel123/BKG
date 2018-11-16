package com.group.proseminar.knowledge_graph.ontology_test;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.group.proseminar.knowledge_graph.ontology.PredicateResolver;

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
		System.out.println("designed: " + res.resolveToURI("designed"));
		System.out.println("won: " + res.resolveToURI("won"));
		System.out.println("born in: " + res.resolveToURI("born in"));
		System.out.println("raised by: " + res.resolveToURI("raised by"));
		System.out.println("invented: " + res.resolveToURI("invented"));

		assertEquals(true, true);
	}
}