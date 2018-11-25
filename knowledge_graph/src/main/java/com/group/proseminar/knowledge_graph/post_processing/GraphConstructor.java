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

/**
 * Creates a graph based on domain/range/type relations.
 * 
 * @author Stefan Werner
 *
 */
public class GraphConstructor {
	private Model model;
	private Property typeOf;
	private Property range;
	private Property domain;

	public GraphConstructor() {
		this.model = ModelFactory.createDefaultModel();
		this.typeOf = model.createProperty("http://dbpedia.org/ontology/type");
		this.range = model.createProperty("http://dbpedia.org/ontology/range");
		this.domain = model.createProperty("http://dbpedia.org/ontology/domain");
	}

	public Model getModel() {
		return this.model;
	}

	private static ParameterizedSparqlString getRangeQuery(String owlTarget) {
		ParameterizedSparqlString qs = new ParameterizedSparqlString(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "select distinct ?range { \n"
						+ "<" + owlTarget + "> rdfs:range ?range }"

		);
		return qs;
	}

	private static ParameterizedSparqlString getDomainQuery(String owlTarget) {
		ParameterizedSparqlString qs = new ParameterizedSparqlString("prefix owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "select distinct ?domain " + "{ <"
				+ owlTarget + "> rdfs:domain ?domain }");
		return qs;
	}

	private static ParameterizedSparqlString getTypeQuery(String resourceTarget) {
		ParameterizedSparqlString qs = new ParameterizedSparqlString(
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "select distinct ?type  { \n"
						+ "<" + resourceTarget + "> rdf:type ?type . \n"
						+ "?type rdfs:isDefinedBy <http://dbpedia.org/ontology/> }");
		return qs;
	}

	private static ResultSet executeQuery(ParameterizedSparqlString query) {
		QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query.asQuery());
		ResultSet results = exec.execSelect();
		return results;
	}

	private void insertAncestryToGraph(String initURI, int maxruns) {
		Stack<String> resourceURIs = new Stack<>();
		resourceURIs.add(initURI);
		int i = 0;
		while (!resourceURIs.isEmpty() && i < maxruns) {
			String resourceURI = resourceURIs.pop();
			Resource subject = model.createResource(resourceURI);
			if (resourceURI.startsWith("http://dbpedia.org/resource/")) {
				ResultSet types = executeQuery(getTypeQuery(resourceURI));
				// TODO: not sure if typeOf uri is correct
				insert(model, resourceURIs, subject, typeOf, types, "type");

			} else if (resourceURI.startsWith("http://dbpedia.org/ontology/")) {
				ResultSet ranges = executeQuery(getRangeQuery(resourceURI));
				insert(model, resourceURIs, subject, range, ranges, "range");
				ResultSet domains = executeQuery(getDomainQuery(resourceURI));
				insert(model, resourceURIs, subject, domain, domains, "domain");
			}
			i++;
		}
	}

	private void insert(Model model, Stack<String> stack, Resource subject, Property property, ResultSet results,
			String varName) {
		while (results.hasNext()) {
			QuerySolution next = results.next();
			Resource object = model.getResource(next.get(varName).toString());
			model.add(subject, property, object);
			if (object.getURI().startsWith("http://dbpedia.org/resource/")) {
				if (!model.contains(object, typeOf)) {
					stack.add(object.getURI());
				}
			} else if (object.getURI().startsWith("http://dbpedia.org/ontology/")) {
				if (!model.contains(object, range) || !model.contains(object, domain)) {
					stack.add(object.getURI());
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		GraphConstructor postProcessing = new GraphConstructor();
		
		Model reader = ModelFactory.createDefaultModel();
		reader.read("dbpedia_3Eng_property.ttl", "TURTLE");
		StmtIterator iterP = reader.listStatements();
		while (iterP.hasNext()) {
			Triple triple = iterP.next().asTriple();
			String url = triple.getSubject().toString();
			postProcessing.insertAncestryToGraph(url, 10);
			Thread.sleep(10);
		}
		
		reader.read("dbpedia_3Eng_class.ttl", "TURTLE");
		StmtIterator iterC = reader.listStatements();
		while(iterC.hasNext()) {
			Triple triple = iterC.next().asTriple();
			String url = triple.getSubject().toString();
			postProcessing.insertAncestryToGraph(url, 10);
			Thread.sleep(10);
		}
		
		Path path = Paths.get("src/main/resources/graph.ttl");
		URI uri = path.toUri();
		BufferedWriter writer = new BufferedWriter(new FileWriter(uri.getPath()));
		postProcessing.getModel().write(writer, "Turtle");
	}

}
