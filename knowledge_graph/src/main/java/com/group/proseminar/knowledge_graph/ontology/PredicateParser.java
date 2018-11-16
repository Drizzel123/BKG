package com.group.proseminar.knowledge_graph.ontology;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class PredicateParser {

	private Map<String, Predicate> predicateMap;

	public PredicateParser() {
		this.predicateMap = new HashMap<>();
	}

//	public void parseTurtleProperties() {
//		Model model = ModelFactory.createDefaultModel();
//		model.read("dbpedia_3Eng_property.ttl", "TURTLE");
//		StmtIterator iter = model.listStatements();
//
//		while (iter.hasNext()) {
//			Statement next = iter.next();
//			Triple triple = next.asTriple();
//			String url = triple.getSubject().toString();
//			String label = triple.getPredicate().toString();
//			String mention = triple.getObject().toString().substring(1, triple.getObject().toString().length() - 1);
//
//			if (!predicateMap.containsKey(url)) {
//				Predicate predicate = new Predicate(url, label, mention);
//				predicateMap.put(url, predicate);
//			} else {
//				Predicate predicate = predicateMap.get(url);
//				predicate.addSynonym(mention);
//			}
//		}
//	}

	public Set<Predicate> getPredicates() {
		return predicateMap.values().stream().collect(Collectors.toSet());
	}

	public void parseJSONProperties() {
		try {
			Path path = Paths.get("src/main/resources/properties.txt");
			URI uri = path.toUri();
			JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
			JSONObject root = new JSONObject(tokener);
			JSONArray properties = (JSONArray) root.get("properties");
			Iterator<?> iter = properties.iterator();
			while (iter.hasNext()) {
				JSONObject jentry = (JSONObject) iter.next();
				String url = jentry.getString("predicate");
				String domain = jentry.getString("domain");
				String schema = jentry.getString("schema");
				JSONArray mentions = (JSONArray) jentry.get("mentions");
				List<String> list = new ArrayList<>();
				Iterator<?> mIter = mentions.iterator();
				while (mIter.hasNext()) {
					list.add((String) mIter.next());
				}
				if (!predicateMap.containsKey(url)) {
					Predicate predicate = new Predicate(url, schema, domain, list);
					predicateMap.put(url, predicate);
				} else {
					Predicate predicate = predicateMap.get(url);
					predicate.addAllSynonyms(list);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		PredicateParser parser = new PredicateParser();
		parser.parseJSONProperties();

		System.out.println(parser.getPredicates());
	}
}
