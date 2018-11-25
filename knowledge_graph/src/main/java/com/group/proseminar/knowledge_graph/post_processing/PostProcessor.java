package com.group.proseminar.knowledge_graph.post_processing;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

public class PostProcessor {
	private Model model;
	private Property typeOf;
	private Property range;
	private Property domain;

	public PostProcessor() {
		this.model = ModelFactory.createDefaultModel();
		this.model.read("graph.ttl", "TURTLE");
		this.typeOf = model.createProperty("http://dbpedia.org/ontology/type");
		this.range = model.createProperty("http://dbpedia.org/ontology/range");
		this.domain = model.createProperty("http://dbpedia.org/ontology/domain");
	}

	public boolean checkTriplet(GraphConstructor constructor, String subject, String predicate, String object) {
		Model subjModel = ModelFactory.createDefaultModel();
		constructor.insertAncestryToModel(subjModel,subject,10);
		
		Model objModel = ModelFactory.createDefaultModel();
		constructor.insertAncestryToModel(objModel,object,10);
		
		NodeIterator types = model.listObjectsOfProperty(subjModel.getResource(subject),subjModel.getProperty("http://dbpedia.org/ontology/type"));
		
		
		System.out.println("Has next: " + types.hasNext());
		System.out.println(subjModel.getResource(subject));
		System.out.println(typeOf);
		while(types.hasNext()) {
			System.out.println(types.next());
		}
		
		ResIterator iter = model.listResourcesWithProperty(subjModel.getProperty("http://dbpedia.org/ontology/type"));
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
		
		
		Resource subj = model.createResource(subject);
		Resource pred = model.getResource(predicate);
		Resource obj = model.createResource(object);
		
		System.out.println(pred);
		if (model.contains(pred, range, subj) || model.contains(pred, domain, obj)) {
			System.out.println("True");
			return true;
		}
		System.out.println("False");
		return false;
	}
	

	public static void main(String[] args) {
		PostProcessor postProcessor = new PostProcessor();
		GraphConstructor constructor= new GraphConstructor();
		postProcessor.checkTriplet(constructor, "http://dbpedia.org/resource/Barack_Obama", "http://dbpedia.org/ontology/monarch",
				"http://dbpedia.org/resource/United_States");
		
		Model subjModel = ModelFactory.createDefaultModel();
		constructor.insertAncestryToModel(subjModel,"http://dbpedia.org/resource/Barack_Obama",10);
		subjModel.write(System.out);
	}
}
