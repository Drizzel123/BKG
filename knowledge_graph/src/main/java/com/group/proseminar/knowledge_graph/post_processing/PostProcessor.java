package com.group.proseminar.knowledge_graph.post_processing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * Checks what statements (triplets) in a collection are to be considered valid.
 * 
 * @author Stefan Werner
 *
 */
public class PostProcessor {
	private GraphConstructor constructor;
	private Model successful;
	private Model unsuccessful;

	public PostProcessor() {
		this.constructor = new GraphConstructor();
		this.successful = ModelFactory.createDefaultModel();
		this.successful.setNsPrefix("dbr", "http://dbpedia.org/resource/");
		this.successful.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		this.unsuccessful = ModelFactory.createDefaultModel();
		this.unsuccessful.setNsPrefix("dbr", "http://dbpedia.org/resource/");
		this.unsuccessful.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
	}

	/**
	 * Checks if a Statement is likely to be valid.
	 * 
	 * @param statement
	 * @return true if the post-processing accepts the statement, false otherwise
	 */
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

	/**
	 * Separates the triplets in file with path {@code originPath } with respect to
	 * the post-processing. Triplets with successful post-processing will be added
	 * to the model successful, the remaining to the model unsuccessful.
	 * 
	 * @param originPath
	 * @param successfulPath
	 * @param unsuccessfulPath
	 * @throws IOException
	 */
	private void performPostProcessing(URI originURI) {
		// Initialize reader model
		Model reader = ModelFactory.createDefaultModel();
		reader.read(originURI.toString(), "Turtle");
		// Iterate over statements and check them
		StmtIterator statements = reader.listStatements();
		// Add statements to successful/unsuccessful model
		while (statements.hasNext()) {
			Statement next = statements.next();
			if (checkTriplet(next)) {
				successful.add(next);
			} else {
				unsuccessful.add(next);
			}
		}
	}

	/**
	 * Write a given model to a file at the specified path.
	 * 
	 * @param model
	 * @param path
	 * @throws IOException
	 */
	private void writeModelToFile(Model model, URI uri) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(uri.getPath()));
		model.write(writer, "Turtle");
	}

	/**
	 * Performs post-processing and separates the statements in successful and
	 * unsuccessful.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public void performPostProcessing() throws IOException{
		// Create uris for results (successful and unsuccessful post-processing)
		URI successfulUri = Paths.get("src/main/resources/successful.ttl").toUri();
		URI unsuccessfulUri = Paths.get("src/main/resources/unsuccessful.ttl").toUri();

		// Perform post-processing for nlp package
		URI nlpUri = Paths.get("src/main/resources/nlp_results.ttl").toUri();
		performPostProcessing(nlpUri);

		// TODO: Create model reading results of FOX

		// Write results to file
		writeModelToFile(this.successful, successfulUri);
		writeModelToFile(this.unsuccessful, unsuccessfulUri);
	}

	public static void main(String[] args) {
		PostProcessor processor = new PostProcessor();
		try {
			processor.performPostProcessing();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
