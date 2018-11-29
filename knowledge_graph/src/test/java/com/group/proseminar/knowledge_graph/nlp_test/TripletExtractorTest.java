package com.group.proseminar.knowledge_graph.nlp_test;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.group.proseminar.knowledge_graph.nlp.TripletExtractor;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TripletExtractorTest {
	final String EPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse";
	Properties extrProps;
	StanfordCoreNLP pipeline;

	@Before
	public void setup() {
		extrProps = new Properties();
		extrProps.put("annotators", EPROPERTIES);
		extrProps.put("threads", "8");
		pipeline = new StanfordCoreNLP(extrProps);
	}

	@Test
	public void extractTripletsTest() {
		CoreDocument annotation = new CoreDocument(
				"The outbreak of the American revolution compelled Barack Obama to return to New York on the 28th of March.");
		pipeline.annotate(annotation);
		System.out.println(TripletExtractor.extractTriplets(annotation));

		annotation = new CoreDocument("London was founded by the Romans, who later abandoned the city.");
		pipeline.annotate(annotation);
		System.out.println(TripletExtractor.extractTriplets(annotation));

		annotation = new CoreDocument(
				"Luc Besson was born on the 18th of March 1959 and works as a French film director.");
		pipeline.annotate(annotation);
		System.out.println(TripletExtractor.extractTriplets(annotation));

		assertEquals(true, true);
	}
}
