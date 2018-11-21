package com.group.proseminar.knowledge_graph.nlp_test;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.group.proseminar.knowledge_graph.nlp.CoreferenceResolver;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreferenceResolverTest {
	CoreferenceResolver resolver;
	private StanfordCoreNLP corefPipeline;
	private final String CPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse,dcoref";
	CoreDocument documentA;
	CoreDocument documentB;

	@Before
	public void setup() {
		this.resolver = new CoreferenceResolver();
		String textA = "Johann Carl Friedrich Gauss was a German mathematician and physicist who made significant contributions to many fields in mathematics and sciences.";
		String textB = "Barack Obama is the president of America.";
		Properties corefProps = new Properties();
		corefProps.put("annotators", CPROPERTIES);
		this.corefPipeline = new StanfordCoreNLP(corefProps);
		this.documentA = new CoreDocument(textA);
		this.documentB = new CoreDocument(textB);
		corefPipeline.annotate(documentA);
		corefPipeline.annotate(documentB);
	}

//	@Test
//	public void extractFromTextTest() {
//		System.out.println("Result test A: " + resolver.resolveCoreferences(documentA));
//	}
	
	@Test 
	public void linkEntitiesToMentionsTest() {
		resolver.resolveCoreferences(documentB);
		System.out.println("Result test B: " + resolver.linkEntitiesToMentions(documentB));
	}
}
