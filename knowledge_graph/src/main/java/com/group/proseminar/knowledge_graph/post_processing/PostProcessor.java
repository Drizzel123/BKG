package com.group.proseminar.knowledge_graph.post_processing;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class PostProcessor {
	private Model model;
	private GraphConstructor constructor;

	public PostProcessor() {
		this.model = ModelFactory.createDefaultModel();
		this.model.read("graph.ttl", "TURTLE");
		this.constructor = new GraphConstructor();
	}

	public boolean checkTriplet(Statement statement) {
		String subject = statement.getSubject().toString();
		String predicate = statement.getPredicate().toString();
		String object = statement.getObject().toString();
		
		Model subjModel = ModelFactory.createDefaultModel();
		constructor.insertAncestryToModel(subjModel, subject, 10);
		NodeIterator tSubj = subjModel.listObjectsOfProperty(subjModel.getResource(subject), RDF.type);
		NodeIterator rSubj = subjModel.listObjectsOfProperty(subjModel.getResource(subject), RDFS.range);
		NodeIterator dSubj = subjModel.listObjectsOfProperty(subjModel.getResource(subject), RDFS.domain);
		Set<RDFNode> sSet = new HashSet<>();
		tSubj.forEachRemaining(sSet::add);
		rSubj.forEachRemaining(sSet::add);
		dSubj.forEachRemaining(sSet::add);

		Model pModel = ModelFactory.createDefaultModel();
		constructor.insertAncestryToModel(pModel, predicate, 10);
		NodeIterator dPred = pModel.listObjectsOfProperty(pModel.getResource(predicate), RDFS.domain);
		NodeIterator rPred = pModel.listObjectsOfProperty(pModel.getResource(predicate), RDFS.range);

		Set<RDFNode> pSet = new HashSet<>();
		dPred.forEachRemaining(pSet::add);
		rPred.forEachRemaining(pSet::add);
		
		// IMPORTANT: sSet is now modified
		sSet.retainAll(pSet);
		
		if (!sSet.isEmpty()) {
			// subject and predicate have a common ancestor
			return true;
		}
		
		Model oModel = ModelFactory.createDefaultModel();
		constructor.insertAncestryToModel(oModel, object, 10);
		NodeIterator oObj = oModel.listObjectsOfProperty(oModel.getResource(object), RDF.type);
		NodeIterator rObj = oModel.listObjectsOfProperty(oModel.getResource(object), RDFS.range);
		NodeIterator dObj = oModel.listObjectsOfProperty(oModel.getResource(object), RDFS.domain);

		Set<RDFNode> oSet = new HashSet<>();
		oObj.forEachRemaining(oSet::add);
		rObj.forEachRemaining(oSet::add);
		dObj.forEachRemaining(oSet::add);
		
		oSet.retainAll(pSet);
		
		if (!oSet.isEmpty()) {
			// object and predicate have a common ancestor
			return true;
		}
		return false;
	}

	public void performPostProcessing() {
		// Initialize reader model
		Path path = Paths.get("src/main/resources/nlp_result.ttl");
		URI uri = path.toUri();
		Model reader = ModelFactory.createDefaultModel();
		reader.read(uri.toString(), "Turtle");
		StmtIterator statements = reader.listStatements();
		// Initialize model for successful post-processed statements
		Model successful = ModelFactory.createDefaultModel();
		// Initialize model for unsuccessful post-processed statements
		Model unsuccessful = ModelFactory.createDefaultModel();

		List<Statement> list = new ArrayList<>();
		statements.forEachRemaining(list::add);
		list.stream().map(x-> checkTriplet(x)? successful.add(x) : unsuccessful.add(x));
		
		System.out.println("Successful:");
		successful.write(System.out,"Turtle");
		System.out.println("Unsuccessful:");
		unsuccessful.write(System.out,"Turtle");
	}
}
