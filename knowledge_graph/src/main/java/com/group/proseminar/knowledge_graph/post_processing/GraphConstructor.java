package com.group.proseminar.knowledge_graph.post_processing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * Creates a graph based on domain/range/type relations.
 * 
 * @author Stefan Werner
 *
 */
public class GraphConstructor {
	private Model model;

	public GraphConstructor() {
		this.model = ModelFactory.createDefaultModel();
		this.model.setNsPrefix("dbr", "http://dbpedia.org/resource/");
		this.model.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
	}

	public Model getModel() {
		return this.model;
	}

	/**
	 * Query the range of a owl target.
	 * 
	 * @param owlTarget
	 * @return
	 */
	private static ParameterizedSparqlString getRangeQuery(String owlTarget) {
		ParameterizedSparqlString qs = new ParameterizedSparqlString(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "select distinct ?range { \n"
						+ "<" + owlTarget + "> rdfs:range ?range }"

		);
		return qs;
	}

	/**
	 * Query the domain of a owl target.
	 * 
	 * @param owlTarget
	 * @return
	 */
	private static ParameterizedSparqlString getDomainQuery(String owlTarget) {
		ParameterizedSparqlString qs = new ParameterizedSparqlString("prefix owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "select distinct ?domain " + "{ <"
				+ owlTarget + "> rdfs:domain ?domain }");
		return qs;
	}

	/**
	 * Query the type of a resource target.
	 * 
	 * @param resourceTarget
	 * @return
	 */
	private static ParameterizedSparqlString getTypeQuery(String resourceTarget) {
		ParameterizedSparqlString qs = new ParameterizedSparqlString(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "select distinct ?type  { \n"
						+ "<" + resourceTarget + "> rdf:type ?type . \n"
						+ "?type rdfs:isDefinedBy <http://dbpedia.org/ontology/> }");
		return qs;
	}

	/**
	 * Executes a given query.
	 * 
	 * @param query
	 * @return
	 */
	private static ResultSet executeQuery(ParameterizedSparqlString query) {
		QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query.asQuery());
		ResultSet results = exec.execSelect();
		return results;
	}

	/**
	 * Insert the URIs forking from the initial URI with predicates
	 * type/domain/range.
	 * 
	 * @param initURI
	 * @param maxruns
	 */
	public void insertAncestryToModel(Model model, String initURI, int maxruns) {
		Stack<String> resourceURIs = new Stack<>();
		resourceURIs.add(initURI);
		int i = 0;
		while (!resourceURIs.isEmpty() && i < maxruns) {
			String resourceURI = resourceURIs.pop();
			Resource subject = model.createResource(resourceURI);
			if (resourceURI.startsWith("http://dbpedia.org/resource/")) {
				ResultSet types = executeQuery(getTypeQuery(resourceURI));
				// TODO: not sure if typeOf uri is correct
				insert(model, resourceURIs, subject, RDF.type, types, "type");
				// TODO: add subClassOf

			} else if (resourceURI.startsWith("http://dbpedia.org/ontology/")) {
				ResultSet ranges = executeQuery(getRangeQuery(resourceURI));
				insert(model, resourceURIs, subject, RDFS.range, ranges, "range");
				ResultSet domains = executeQuery(getDomainQuery(resourceURI));
				insert(model, resourceURIs, subject, RDFS.domain, domains, "domain");
			}
			i++;
		}
	}

	/**
	 * Insert results to the model and add URIs to stack for further processing.
	 * 
	 * @param model
	 * @param stack
	 * @param subject
	 * @param property
	 * @param results
	 * @param varName
	 */
	private void insert(Model model, Stack<String> stack, Resource subject, Property property, ResultSet results,
			String varName) {
		while (results.hasNext()) {
			QuerySolution next = results.next();
			Resource object = model.getResource(next.get(varName).toString());
			model.add(subject, property, object);
			if (object.getURI().startsWith("http://dbpedia.org/resource/")) {
				if (!model.contains(object, RDF.type)) {
					stack.add(object.getURI());
				}
			} else if (object.getURI().startsWith("http://dbpedia.org/ontology/")) {
				if (!model.contains(object, RDFS.range) || !model.contains(object, RDFS.domain)) {
					stack.add(object.getURI());
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		GraphConstructor graph = new GraphConstructor();

		Model reader = ModelFactory.createDefaultModel();
		reader.read("dbpedia_3Eng_property.ttl", "TURTLE");
		StmtIterator iterP = reader.listStatements();
		while (iterP.hasNext()) {
			Triple triple = iterP.next().asTriple();
			String url = triple.getSubject().toString();
			graph.insertAncestryToModel(graph.getModel(), url, 10);
			Thread.sleep(10);
		}

		reader.read("dbpedia_3Eng_class.ttl", "TURTLE");
		StmtIterator iterC = reader.listStatements();
		while (iterC.hasNext()) {
			Triple triple = iterC.next().asTriple();
			String url = triple.getSubject().toString();
			graph.insertAncestryToModel(graph.getModel(), url, 10);
			Thread.sleep(10);
		}

		Path path = Paths.get("src/main/resources/graph.ttl");
		URI uri = path.toUri();
		BufferedWriter writer = new BufferedWriter(new FileWriter(uri.getPath()));
		graph.getModel().write(writer, "Turtle");
	}

}
