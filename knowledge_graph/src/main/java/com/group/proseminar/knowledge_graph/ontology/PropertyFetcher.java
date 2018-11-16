package com.group.proseminar.knowledge_graph.ontology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Writes properties extracted from TURTLE and queried through SPARQL to a file
 *
 */
public class PropertyFetcher {
	public static void main(String[] args) {
		JSONObject root = new JSONObject();
		JSONArray jarray = new JSONArray();
		jsonFromSparql(jarray);
		jsonFromTurtle(jarray);
		root.put("properties", jarray);
		// write properties to file properties.txt
		try {
			Path path = Paths.get("src/main/resources/properties.txt");
			FileWriter writer = new FileWriter(new File(path.toAbsolutePath().toString()));
			// pretty print JSON to file
			writer.write(root.toString(4));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void jsonFromSparql(JSONArray jarray) {
		ResultSet results = queryProperties();
		while (results.hasNext()) {
			QuerySolution next = results.next();
			JSONObject jentry = new JSONObject();
			jentry.put("predicate", next.get("pred").toString());
			jentry.put("domain", next.get("domain").toString());
			// TODO: define schema
			String schema = "";
			jentry.put("schema", schema);
			JSONArray jmentions = new JSONArray();
			String predicate = next.get("pred").toString().substring("http://dbpedia.org/ontology/".length());
			String mention = extractMention(predicate).toLowerCase();
			jmentions.put(mention);
			jentry.put("mentions", jmentions);
			jarray.put(jentry);
		}
	}

	private static ResultSet queryProperties() {
		ParameterizedSparqlString qs = new ParameterizedSparqlString(
				"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
						+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
						+ "SELECT DISTINCT ?pred ?domain FROM <http://dbpedia.org/resource/classes#> "
						+ "{ ?pred a rdf:Property . ?pred rdfs:isDefinedBy <http://dbpedia.org/ontology/> . "
						+ "?pred rdfs:domain ?domain } order by ?pred");
		QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
		ResultSet results = exec.execSelect();
		return results;
	}

	private static String extractMention(String predicate) {
		String[] arr = predicate.split("(?=\\p{Lu})");
		StringBuilder builder = new StringBuilder();
		for (String s : arr) {
			builder.append(s + " ");
		}
		String str = builder.toString().substring(0, builder.toString().length() - 1);
		return str;
	}

	private static void jsonFromTurtle(JSONArray jarray) {
		Model model = ModelFactory.createDefaultModel();
		model.read("dbpedia_3Eng_property.ttl", "TURTLE");
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			JSONObject jentry = new JSONObject();
			Triple triple = iter.next().asTriple();
			String url = triple.getSubject().toString();
			jentry.put("predicate", url);
			// TODO: get domain of predicate
			jentry.put("domain", "");
			String schema = triple.getPredicate().toString();
			jentry.put("schema", schema);
			JSONArray jmentions = new JSONArray();
			String mention = triple.getObject().toString().substring(1, triple.getObject().toString().length() - 1);
			jmentions.put(mention);
			jentry.put("mentions", jmentions);
			jarray.put(jentry);
		}
	}
}
