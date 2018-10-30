package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CoreferenceResoluter {
	
	public static void main (String[] args) {
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
		props.put("dcoref.score", true);
		StanfordCoreNLP corefPipeline = new StanfordCoreNLP(props);
		String text = "Johann Carl Friedrich Gauss was born on 30 April 1777 in Brunswick, in the Duchy of Brunswick-Wolfenbüttel, to poor, working-class parents. His mother was illiterate and never recorded the date of his birth, remembering only that he had been born on a Wednesday, eight days before the Feast of the Ascension. Gauss later solved this puzzle about his birthdate in the context of finding the date of Easter, deriving methods to compute the date in both past and future years. He was christened and confirmed in a church near the school he attended as a child.";
		System.out.println(text);
		
		Annotation document = new Annotation(text);
		corefPipeline.annotate(document);
		CoreferenceResoluter res = new CoreferenceResoluter();
		System.out.println(res.coreferenceResolution(document));
	}

	public String coreferenceResolution(Annotation document) {
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		Map<Integer, CorefChain> corefChainMap = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
		if (corefChainMap == null) {
			return null;
		}
		List<String> punctutations = Arrays.asList(". , : ;".split(" "));

		String resoluted = "";
		for (int i = 0; i < sentences.size(); i++) {
			CoreMap sentence = sentences.get(i);
			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

			for (int j = 0; j < tokens.size(); j++) {
				CoreLabel token = tokens.get(j);
				Integer id = tokens.get(j).get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
				CorefChain corefChain = corefChainMap.get(id);
				String word = token.word();

				// If token refers to a pronoun
				if (corefChain != null && (token.tag().equals("PRP") || token.tag().equals("PRP$"))) {
					// Substitute original word by its best mention
					CorefMention representation = corefChain.getRepresentativeMention();
					String coref = representation.toString();
					String bestMention = coref.substring(1, coref.lastIndexOf("\""));
					
					if (token.tag().equals("PRP$")) {
						// discriminate between singular and plural
						 String lemma = token.lemma();
			                if (lemma != null) {
			                    String originalText = token.originalText();
			                    if (lemma.equalsIgnoreCase(originalText)) {
			                        // Is singular
			                    	bestMention +="'s";
			                    } else {
			                    	// Is plural
			                    	if (!bestMention.endsWith("s")) {
			                    		bestMention +="s";
			                    	}
			                    	bestMention +="'";
			                    }
			                }
					}
					
					System.out.println("Substituting: " + word + " through " + corefChain.getRepresentativeMention());
	
					resoluted += " " + bestMention;
				} else {
					if (!punctutations.contains(word)) {
						resoluted += " ";
					}
					resoluted += word;
				}

			}
		}
		return resoluted.trim();
	}
}
