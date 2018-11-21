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

/**
 * Parses the JSON properties and inserts them to a lookup-map.
 * 
 * @author Stefan Werner
 *
 */
public class PredicateParser {

	private Map<String, Predicate> predicateMap;

	public PredicateParser() {
		this.predicateMap = new HashMap<>();
	}

	public Set<Predicate> getPredicates() {
		return predicateMap.values().stream().collect(Collectors.toSet());
	}

	/**
	 * Creates predicates with respect to the JSON file "properties.txt" and inserts
	 * them to a lookup-map.
	 */
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
}
