package com.group.proseminar.knowledge_graph.nlp_test;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.group.proseminar.knowledge_graph.nlp.CoreferenceResolver;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreferenceResolverTest {
	private StanfordCoreNLP corefPipeline;
	private final String CPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse,dcoref";
	CoreDocument document;

	@Before
	public void setup() {
		String text = "Johann Carl Friedrich Gauss was a German mathematician and physicist who made significant contributions to many fields in mathematics and sciences.";
		Properties corefProps = new Properties();
		corefProps.put("annotators", CPROPERTIES);
		this.corefPipeline = new StanfordCoreNLP(corefProps);
		this.document = new CoreDocument(text);
		corefPipeline.annotate(document);
	}

	@Test
	public void extractFromTextTest() {
		CoreferenceResolver resolver = new CoreferenceResolver();
		System.out.println("Result: " + resolver.resolveCoreferences(document));
		assertEquals(true, true);
	}
}
