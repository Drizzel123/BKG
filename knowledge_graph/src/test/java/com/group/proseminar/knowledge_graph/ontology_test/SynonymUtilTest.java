package com.group.proseminar.knowledge_graph.ontology_test;

import org.junit.Before;
import org.junit.Test;

import com.group.proseminar.knowledge_graph.ontology.SynonymUtil;

import net.sf.extjwnl.JWNLException;

public class SynonymUtilTest {
	SynonymUtil util;
	
	@Before
	public void setup() throws JWNLException {
		util = new SynonymUtil();
	}
	
	@Test
	public void getSynonymsTest() {
		System.out.println("getSynonymsTest: ");
		System.out.println(util.getSynonyms("love"));
		System.out.println(util.getSynonyms("area"));
		System.out.println("--------------------");
	}
	
	@Test
	public void  getLemmaAndPOSTest() {
		System.out.println("getLemmaAndPOSTest: ");
		System.out.println(util.getLemmaAndPOS("precede"));
		System.out.println(util.getLemmaAndPOS("born in"));
		System.out.println("--------------------");
	}
}
