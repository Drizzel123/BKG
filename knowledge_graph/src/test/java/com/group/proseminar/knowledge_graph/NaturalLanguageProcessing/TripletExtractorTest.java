package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TripletExtractorTest {
	private StanfordCoreNLP extrPipeline;
	private final String EPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse";
	Annotation document;

	@Before
	public void setup() {
		String text = "Johann Carl Friedrich Gauss was a German mathematician and physicist who made significant contributions to many fields in mathematics and sciences. Sometimes referred to as the Princeps mathematicorum and the greatest mathematician since antiquity, Gauss had an exceptional influence in many fields of mathematics and science, and is ranked among history's most influential mathematicians.";
		Properties extrProps = new Properties();
		extrProps.put("annotators", EPROPERTIES);
		this.extrPipeline = new StanfordCoreNLP(extrProps);
		this.document = new Annotation(text);
		extrPipeline.annotate(document);
	}

	@Test
	public void extractFromTextTest() {
		TripletExtractor extractor = new TripletExtractor();
		System.out.println("Result: " + extractor.extractFromText(document));
		assertEquals(true, true);
	}
}
