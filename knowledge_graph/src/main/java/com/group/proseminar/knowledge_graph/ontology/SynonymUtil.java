package com.group.proseminar.knowledge_graph.ontology;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * Constructs a dictionary for synonym-lookup.
 * 
 * @author Stefan Werner
 *
 */
public class SynonymUtil {
	private Dictionary dictionary;
	private final String PROPERTIES = "tokenize,ssplit,pos,lemma";
	private StanfordCoreNLP pipeline;

	public SynonymUtil() throws JWNLException {
		this.dictionary = Dictionary.getDefaultResourceInstance();
		Properties properties = new Properties();
		properties.put("annotators", PROPERTIES);
		this.pipeline = new StanfordCoreNLP(properties);
	}

	/**
	 * Returns a list of synonyms ordered by their likelihood.
	 * 
	 * @param lemma of the target
	 * @param pos   of the target
	 * @return a list of synonyms
	 */
	public List<String> getSynonyms(String word) {
		List<String> synonyms = new ArrayList<>();
		List<String> result = new ArrayList<>();
		try {
			Entry<String, String> lemmaPOS = getLemmaAndPOS(word);
			String lemma = lemmaPOS.getKey();
			String pos = lemmaPOS.getValue();
			IndexWord indexWord = dictionary.getIndexWord(translatePOS(pos), lemma);
			List<Synset> synsets = indexWord.getSenses();
			for (Synset syn : synsets) {
				for (Word w : syn.getWords()) {
					synonyms.add(w.getLemma());
				}
			}
			// remove duplicates while retaining order
			Set<String> set = new HashSet<>();
			for (String w : synonyms) {
				if (set.add(w)) {
					result.add(w);
				}
			}
		} catch (Exception e) {
			result.clear();
			return result;
		}
		return result;
	}

	/**
	 * Returns a (lemma, part of speech) entry for a word.
	 * 
	 * @param word
	 * @return
	 */
	public Entry<String, String> getLemmaAndPOS(String word) {
		Annotation tokenAnnotation = new Annotation(word);
		this.pipeline.annotate(tokenAnnotation);
		List<CoreMap> list = tokenAnnotation.get(SentencesAnnotation.class);
		CoreLabel label = list.get(0).get(TokensAnnotation.class).get(0);
		String lemma = label.get(LemmaAnnotation.class);
		String pos = label.get(PartOfSpeechAnnotation.class);
		Entry<String, String> entry = new AbstractMap.SimpleEntry<>(lemma, pos);
		return entry;
	}

	private POS translatePOS(String corePOS) {
		switch (corePOS) {
		case "NN":
		case "NNS":
		case "NNP":
		case "NNPS":
			return POS.NOUN;
		case "VB":
		case "VBD":
		case "VBG":
		case "VBN":
		case "VBP":
		case "VBZ":
			return POS.VERB;
		case "RB":
		case "RBR":
		case "RBS":
			return POS.ADVERB;
		case "JJ":
		case "JJR":
		case "JJS":
			return POS.ADJECTIVE;
		}
		return null;
	}
}
