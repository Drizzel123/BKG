package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;

public class ExtractorPipeline {

	private StanfordCoreNLP corefPipeline;
	private StanfordCoreNLP extrPipeline;
	private final String CPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse";
	private final String EPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse,dcoref";
	CoreferenceResoluter resoluter;
	TripletExtractor extractor;

	ExtractorPipeline() {
		this.resoluter = new CoreferenceResoluter();
		this.extractor = new TripletExtractor();
		// Initialize corefPipeline
		Properties corefProps = new Properties();
		corefProps.put("annotators", CPROPERTIES);
		corefProps.put("dcoref.score", true);
		this.corefPipeline = new StanfordCoreNLP(corefProps);
		// Initialize extrPipeline
		Properties extrProps = new Properties();
		extrProps.put("annotators", EPROPERTIES);
		extrProps.put("dcoref.score", true);
		this.extrPipeline = new StanfordCoreNLP(corefProps);
	}

	public void startPipeline(String path) throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				Annotation doc = new Annotation(line);
				corefPipeline.annotate(doc);
				String resoluted = resoluter.coreferenceResolution(doc);
				if (resoluted == null) {
					resoluted = line;
				}
				Annotation res = new Annotation(resoluted);
				extrPipeline.annotate(res);
				Set<Triplet<Tree, Tree, Tree>> triplets = extractor.extractFromText(res);
				// TODO: Write triplets to file
				System.out.println("TRIPLETS: " + triplets);
			}
		}
	}
}
