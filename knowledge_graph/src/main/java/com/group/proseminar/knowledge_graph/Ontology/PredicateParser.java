package com.group.proseminar.knowledge_graph.Ontology;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class PredicateParser {

	private Map<String, Predicate> predicateMap;

	public PredicateParser() {
		this.predicateMap = new HashMap<>();
	}

	public void parseTurtleProperties() {
		Model model = ModelFactory.createDefaultModel();
		model.read("dbpedia_3Eng_property.ttl", "TURTLE");
		StmtIterator iter = model.listStatements();

		while (iter.hasNext()) {
			Statement next = iter.next();
			Triple triple = next.asTriple();
			String url = triple.getSubject().toString();
			String label = triple.getPredicate().toString();
			String mention = triple.getObject().toString().substring(1, triple.getObject().toString().length() - 1);

			if (!predicateMap.containsKey(url)) {
				Predicate predicate = new Predicate(url, label, mention);
				predicateMap.put(url, predicate);
			} else {
				Predicate predicate = predicateMap.get(url);
				predicate.addSynonym(mention);
			}
		}
	}

	public Set<Predicate> getPredicates() {
		return predicateMap.values().stream().collect(Collectors.toSet());
	}
}
