package com.group.proseminar.knowledge_graph.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import net.sf.extjwnl.JWNLException;

/**
 * Links predicates to URIs.
 * 
 * @author Stefan Werner
 *
 */
public class PredicateResolver {
	private PredicateParser parser;
	private Set<Predicate> predicates;
	private SynonymUtil util;
	private Map<String, List<Predicate>> lookupMap;

	/**
	 * Initialize dependencies and add synonyms derived from a synonym database to
	 * predicates.
	 */
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

	/**
	 * Constructs a lookup-map based on a collection of predicates and their
	 * affiliated synonyms.
	 * 
	 * @param predicates - set of predicates supposed to be contained by the
	 *                   lookup-map
	 * @return lookup-map
	 */
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

	/**
	 * Links predicates to URIs based on their mentions/synonyms and their
	 * respective entry in the lookup-map.
	 */
	public String resolveToURI(String mention) {
		List<String> candidates = generateCandidates(mention);
		for (String word: candidates) {
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
		}
		return null;
	}

	public Map<String, List<Predicate>> getLookupMap() {
		return lookupMap;
	}
	
	/**
	 * Generates a list of mentions from the input string.
	 * @param mention
	 * @return
	 */
	private static List<String> generateCandidates(String mention) {
		List<String> words = new ArrayList<>(Arrays.asList(mention.split(" ")));
		List<String> result = new ArrayList<>();
		int index = words.size();
		while (index > 0) {
			int difference = words.size() - index;
			for (int i = 0; i <= difference; i++) {
				String candidate = String.join(" ", words.subList(i, i + index));
				result.add(candidate);
			}
			index--;
		}
		return result;
	}
	
	/**
	 * Gets the depending phrase of a predicate with respect to the sentence's dependency tree.
	 * @param document - annotated document (requires parse annotation)
	 * @param predicate 
	 * @return depending pharse of the predicate
	 */
	public String getVerbDependend(Annotation document, String predicate) {
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
			for (IndexedWord index: dependencies.getAllNodesByWordPattern(predicate)) {
				List<String> result = new ArrayList<>();
				for (SemanticGraphEdge e : dependencies.incomingEdgeIterable(dependencies.getNodeByIndex(index.index()))) {
					if (e.getRelation().toString().equals("cop")) {
						for (SemanticGraphEdge out: dependencies.outgoingEdgeIterable(e.getSource())) {
							if (out.getRelation().toString().equals("amod")) {
								result.add(out.getDependent().value());
							}
						}
						result.add(e.getSource().value());
						return String.join(" ", result);
					}
				}
			}
		}
		return "";
	}
}
