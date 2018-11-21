package com.group.proseminar.knowledge_graph.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.extjwnl.JWNLException;

public class PredicateResolver {
	private PredicateParser parser;
	private Set<Predicate> predicates;
	private SynonymUtil util;
	private Map<String, List<Predicate>> lookupMap;

	public PredicateResolver() {
		this.parser = new PredicateParser();
		this.parser.parseJSONProperties();
		this.predicates = this.parser.getPredicates();
		try {
			this.util = new SynonymUtil();
			// Extend predicates by possible synonyms
			// TODO: pick number of synonym based on the difficulty of the word
			for (Predicate pred : predicates) {
				List<String> list = util.getSynonyms(pred.getFirst());
				Collection<String> firstThree = new ArrayList<>();
				// get the first num synonyms
				int num = 3;
				int i = 0;
				while (i < num && i < list.size()) {
					firstThree.add(list.get(i));
					i++;
				}
				pred.addAllSynonyms(firstThree);
			}
			this.lookupMap = fillLookupMap(predicates);
		} catch (JWNLException e) {
			this.util = null;
		}
	}

	private static Map<String, List<Predicate>> fillLookupMap(Set<Predicate> predicates) {
		// Map synonyms to their predicate - a word might occur in multiple Predicates
		// If a word is a synonym of two predicates, place the predicate where the
		// Synonym has a lower index first in the list
		Map<String, List<Predicate>> map = new HashMap<>();
		// Set is the collection of predicates that still have a synonym at the i-th
		// iteration
		Set<Predicate> set = new HashSet<>();
		set.addAll(predicates);
		int i = 0;
		while (!set.isEmpty()) {
			// tmp is the collection of predicates that add a synonym to the map at this run
			Set<Predicate> tmp = new HashSet<>();
			for (Predicate predicate : set) {
				List<String> synonyms = predicate.getSynonyms();
				if (i < synonyms.size()) {
					// This predicate adds a synonym to the map
					tmp.add(predicate);
					String synonym = synonyms.get(i);
					List<Predicate> p = map.get(synonym);
					if (p == null) {
						p = new ArrayList<>();
						p.add(predicate);
						map.put(synonym, p);
					} else {
						p.add(predicate);
					}
					;
				}
			}
			i++;
			set = tmp;
		}
		return map;
	}

	public String resolveToURI(String word) {
		word = word.toLowerCase();
		// Try with lemma and original word
		String lemma = util.getLemmaAndPOS(word).getKey();
		// Gets candidates for word and lemma respectively (sorted by likelihood)
		List<Predicate> predWord = lookupMap.get(word);
		List<Predicate> predLemma = lookupMap.get(lemma);
		// TODO: pick the best predicate for the word not just the first with an URI
		// (return if a match is found with the normal word, then look at the lemma)
		if (predWord != null) {
			Predicate bestPredWord = predWord.get(0);

			if (bestPredWord.getUri() != null) {
				System.out.println(bestPredWord);
				return bestPredWord.getUri();
			}
		}
		if (predLemma != null) {
			Predicate bestPredLemma = predLemma.get(0);
			
			if (bestPredLemma.getUri() != null) {
				System.out.println(bestPredLemma);
				return bestPredLemma.getUri();
			}
		}
		if (predWord != null) {
			Predicate bestPredWord = predWord.get(0);

			if (bestPredWord.getUri() != null) {
				System.out.println(bestPredWord);
				return bestPredWord.getUri();
			}
		}
		return null;
	}

	public Map<String, List<Predicate>> getLookupMap() {
		return lookupMap;
	}
}
