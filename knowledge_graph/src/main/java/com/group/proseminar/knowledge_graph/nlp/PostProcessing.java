package com.group.proseminar.knowledge_graph.nlp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.group.proseminar.knowledge_graph.ontology.Predicate;

public class PostProcessing {

	public static boolean predicateIsDomainOfSubjectOrObject(String subject, Predicate predicate, String object) {
		Set<String> subjectDomains = executeDomainQuery(subject);
		Set<String> objectDomains = executeDomainQuery(object);
		// TODO: check current rule
		// accept if subject OR object have a domain matching the domain of the
		// predicate
		if (subjectDomains.contains(predicate.getDomain()) || objectDomains.contains(predicate.getDomain())) {
			return true;
		}

		return false;
	}

	// TODO: works only with owl classes (example:
	// http://dbpedia.org/ontology/Person) but not with resources (example:
	// http://dbpedia.org/resource/Berlin)
	// VERY IMPORTANT !!!!
	private static Set<String> executeDomainQuery(String target) {
		ParameterizedSparqlString qs = new ParameterizedSparqlString("prefix owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "select distinct ?domain "
				+ "{ ?domain rdfs:domain " + "<" + target + "> }");
		QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
		ResultSet results = exec.execSelect();
		Set<String> domains = new HashSet<>();
		results.forEachRemaining(x -> domains.add(x.get("domain").toString()));
		return domains;

	}

	public static void main(String[] args) {
		System.out.println(executeDomainQuery("http://dbpedia.org/resource/Berlin"));
		Predicate predGauss = new Predicate("http://dbpedia.org/ontology/birthPlace", "",
				"http://dbpedia.org/ontology/Person", "");
		System.out.println(predicateIsDomainOfSubjectOrObject("http://dbpedia.org/resource/Carl_Friedrich_Gauss",
				predGauss, "http://dbpedia.org/resource/Berlin"));

	}
}
