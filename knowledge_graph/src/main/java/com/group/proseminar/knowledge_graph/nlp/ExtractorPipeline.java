package com.group.proseminar.knowledge_graph.nlp;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;

public class ExtractorPipeline {

	private StanfordCoreNLP corefPipeline;
	private StanfordCoreNLP extrPipeline;
	private final String CPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse,dcoref";
	private final String EPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse";
	CoreferenceResoluter resoluter;
	TripletExtractor extractor;

	public ExtractorPipeline() {
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
		this.extrPipeline = new StanfordCoreNLP(extrProps);
	}

	public void processArticle(String article) throws Exception {
		CoreDocument doc = new CoreDocument(article);
		corefPipeline.annotate(doc);
		String resoluted = resoluter.coreferenceResolution(doc);
		if (resoluted == null) {
			resoluted = article;
		}
		Set<Entity> entities = resoluter.linkEntitiesToMentions(doc);
		Annotation res = new Annotation(resoluted);
		extrPipeline.annotate(res);
		Set<Triplet<Tree, Tree, Tree>> triplets = extractor.extractFromText(res);
		EntityLinker linker = new EntityLinker();

		for (Triplet<Tree, Tree, Tree> triplet : triplets) {
			String subject = toMention(triplet.getFirst());
			String object = toMention(triplet.getThird());
			Entity sEntity = entities.stream().filter(x -> x.getMentions().contains(subject)).findAny().orElse(null);
			Entity oEntity = entities.stream().filter(x -> x.getMentions().contains(object)).findAny().orElse(null);
			// if sEntity or oEntity is null create a literal entity
			if (sEntity == null) {
				sEntity = new Entity(subject);
				sEntity.setLabel("Literal");
				entities.add(sEntity);
			}
			if (oEntity == null) {
				oEntity = new Entity(object);
				oEntity.setLabel("Literal");
				entities.add(oEntity);
			}
			Set<Entity> set = Stream.of(sEntity, oEntity).collect(Collectors.toSet());
			linker.resolveURIs(set);

			System.out.println("Subject: " + subject + ", Object: " + object);
			// Print out progress
			// Remember: not every entity might have been resolved to an URI
			if (sEntity.getUri() != null) {
				System.out.println("Subject (URI): " + sEntity.getUri() + ", Label: " + sEntity.getLabel());
			} else {
				System.out.println("Subject: " + sEntity.getBestMention());
			}

			if (oEntity.getUri() != null) {
				System.out.println("Object (URI): " + oEntity.getUri() + ", Label: " + oEntity.getLabel());
			} else {
				System.out.println("Object: " + oEntity.getBestMention());
			}
		}
		// TODO: Write triplets to file
	}

	private String toMention(Tree root) {
		String s = "";
		for (Tree leaf : root.getLeaves()) {
			s += leaf.value() + " ";
		}
		return s.substring(0, s.length() - 1);
	}
}
