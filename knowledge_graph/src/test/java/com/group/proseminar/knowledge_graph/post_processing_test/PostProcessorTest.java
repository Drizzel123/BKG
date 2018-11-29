package com.group.proseminar.knowledge_graph.post_processing_test;

import static org.junit.Assert.assertEquals;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;

import com.group.proseminar.knowledge_graph.post_processing.PostProcessor;

public class PostProcessorTest {
	private PostProcessor processor;
	private Model model;

	@Before
	public void setup() {
		this.processor = new PostProcessor();
		this.model = ModelFactory.createDefaultModel();
	}

	@Test
	public void postProcessingTest() {
		String sub_1 = "http://dbpedia.org/resource/Barack_Obama";
		String pred_1 = "http://dbpedia.org/ontology/birthPlace";
		String obj_1 = "http://dbpedia.org/resource/Berlin";
		Resource s_1 = model.createResource(sub_1);
		Property p_1 = model.createProperty(pred_1);
		Resource o_1 = model.createResource(obj_1);
		Statement statement_1 = model.createStatement(s_1, p_1, o_1);
		assertEquals(true, processor.checkTriplet(statement_1));

		String pred_2 = "http://dbpedia.org/ontology/birthYear";
		Property p_2 = model.createProperty(pred_2);
		Statement statement_2 = model.createStatement(o_1, p_2, s_1);
		assertEquals(true, processor.checkTriplet(statement_2));

		String sub_2 = "http://dbpedia.org/resource/Roger_Federer";
		String pred_3 = "http://dbpedia.org/ontology/champion";
		String obj_2 = "http://dbpedia.org/resource/Wimbledon";
		Resource s_2 = model.createResource(sub_2);
		Property p_3 = model.createProperty(pred_3);
		Resource o_2 = model.createResource(obj_2);
		Statement statement_3 = model.createStatement(s_2, p_3, o_2);
		assertEquals(true, processor.checkTriplet(statement_3));
		
		String pred_4 = "http://dbpedia.org/ontology/cites";
		Property p_4 = model.createProperty(pred_4);
		Statement statement_4 = model.createStatement(s_2, p_4, o_2);
		assertEquals(false,processor.checkTriplet(statement_4));

	}
}
