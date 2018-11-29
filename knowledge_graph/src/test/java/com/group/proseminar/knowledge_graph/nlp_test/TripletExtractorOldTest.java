package com.group.proseminar.knowledge_graph.nlp_test;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.group.proseminar.knowledge_graph.nlp.TripletExtractorOld;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TripletExtractorOldTest {
	private StanfordCoreNLP extrPipeline;
	private final String EPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse";
	private TripletExtractorOld extractor;

	@Before
	public void setup() {
		Properties extrProps = new Properties();
		extrProps.put("annotators", EPROPERTIES);
		extrProps.put("threads", "8");
		this.extrPipeline = new StanfordCoreNLP(extrProps);
		this.extractor = new TripletExtractorOld();
	}

//	@Test
//	public void extractFromTextTest() {
//		String text = "Londinium was founded by the Romans, who later abandoned the city.";
//		Annotation document = new Annotation(text);
//		extrPipeline.annotate(document);
//		System.out.println("Result: " + extractor.extractFromText(document));
//		assertEquals(true, true);
//	}

	@Test
	public void extractFromTest() {
		String text = "The outbreak of the American revolution compelled Barack Obama to return to New York on the 28th of March.";
		Annotation document = new Annotation(text);
		extrPipeline.annotate(document);
		System.out.println("Result: " + extractor.extractFromText(document));
		assertEquals(true, true);
	}
}
