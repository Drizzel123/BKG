package com.group.proseminar.knowledge_graph.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.Pair;

/**
 * Substitutes entities of a coreference-chain by their respective mention.
 * 
 * @author Stefan Werner
 *
 */
public class CoreferenceResolver {
	/**
	 * Substitutes certain entities of a coreference-chain in an annotated document
	 * by their respective mention.
	 * 
	 * @param document - annotated text
	 * @return text with resolved coreferences
	 */
	public String resolveCoreferences(CoreDocument document) {
		String result = document.text();
		List<Pair<String, Pair<Integer, Integer>>> substitutionList = new ArrayList<>();
		Map<Integer, CorefChain> corefChainMap = document.corefChains();
		for (CoreSentence sentence : document.sentences()) {
			for (CoreEntityMention entityMention : sentence.entityMentions()) {
				List<CoreLabel> tokens = entityMention.tokens();
				if (!tokens.isEmpty()) {
					Integer id = null;
					Iterator<CoreLabel> iter = tokens.iterator();
					while (id == null && iter.hasNext()) {
						CoreLabel nextToken = iter.next();
						id = nextToken.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
					}
					String substitute = entityMention.toString();
					if (id != null) {
						CorefChain cc = corefChainMap.get(id);
						substitute = substitutePronoun(cc, entityMention);
						Pair<String, Pair<Integer, Integer>> offsetPair = new Pair<>(substitute,
								entityMention.charOffsets());
						substitutionList.add(offsetPair);
					}
				}
			}
		}
		result = substituteByOffset(result, substitutionList);
		return result;
	}

	/**
	 * Returns the substitution of a pronoun with respect to its coreference-chain
	 * and mention.
	 * 
	 * @param cc            - coreference-chain that contains the token
	 * @param entityMention - respective mention of the pronoun
	 * @return the substitution
	 */
	private String substitutePronoun(CorefChain cc, CoreEntityMention entityMention) {
		List<CoreLabel> tokens = entityMention.tokens();
		if (tokens.size() > 0) {
			// Get first token
			CoreLabel token = tokens.get(0);

			if (cc != null && (token.tag().equals("PRP") || token.tag().equals("PRP$"))) {
				// Substitute original word by its best mention
				CorefMention representation = cc.getRepresentativeMention();
				String coref = representation.toString();
				String bestMention = coref.substring(1, coref.lastIndexOf("\""));
				// TODO: replace substring, ... by mentionSpan
				// String mentionSpan = cc.getRepresentativeMention().mentionSpan;

				if (token.tag().equals("PRP$")) {
					// discriminate between singular and plural
					String lemma = token.lemma();
					if (lemma != null) {
						String originalText = token.originalText();
						if (lemma.equalsIgnoreCase(originalText)) {
							// Is singular
							bestMention += "'s";
						} else {
							// Is plural
							if (!bestMention.endsWith("s")) {
								bestMention += "s";
							}
							bestMention += "'";
						}
					}
				}
				return bestMention;
			}
		}
		return entityMention.toString();
	}

	/**
	 * Substitutes characters in the text by given strings at their target position.
	 * 
	 * @param text     - target text
	 * @param mentions - list of strings associated with their target substitution
	 *                 range
	 * @return
	 */
	private static String substituteByOffset(String text, List<Pair<String, Pair<Integer, Integer>>> mentions) {
		int offset = 0;
		for (Pair<String, Pair<Integer, Integer>> entry : mentions) {
			String subst = entry.first;
			Pair<Integer, Integer> offsets = entry.second;
			Integer lower = offsets.first;
			Integer upper = offsets.second;
			text = text.substring(0, lower + offset) + subst + text.substring(upper + offset, text.length());
			offset = subst.length() - (upper - lower) + offset;
		}
		return text;
	}

	/**
	 * Identifies entities in an annotated document.
	 * 
	 * @param document - the annotated text
	 * @return a set of found entities
	 */
	public Set<Entity> linkEntitiesToMentions(CoreDocument document) {
		Map<Integer, CorefChain> corefChainMap = document.corefChains();
		Map<CorefChain, Entity> entities = new HashMap<>();
		for (CoreSentence sentence : document.sentences()) {
			for (CoreEntityMention entityMention : sentence.entityMentions()) {
				List<CoreLabel> tokens = entityMention.tokens();
				if (!tokens.isEmpty()) {
					Integer id = null;
					Iterator<CoreLabel> iter = tokens.iterator();
					while (id == null && iter.hasNext()) {
						CoreLabel nextToken = iter.next();
						id = nextToken.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
					}
					if (id != null) {
						CorefChain cc = corefChainMap.get(id);
						Entity entity;
						// Check if an entity for the respective corefchain exists, else create an
						// entity
						if (entities.containsKey(cc)) {
							entity = entities.get(cc);
							entity.addMention(entityMention.text());
							if (entity.getLabel() == null) {
								entity.setLabel(entityMention.entityType());
							}
						} else {
							entity = new Entity(entityMention.text());
							entity.setLabel(entityMention.entityType());
						}
						entities.put(cc, entity);
					}
				}
			}
		}
		return entities.values().stream().collect(Collectors.toSet());
	}

}
