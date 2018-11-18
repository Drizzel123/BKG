package com.group.proseminar.knowledge_graph.nlp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.group.proseminar.knowledge_graph.ontology.PredicateResolver;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;

/**
 * Controls pipeline features of the natural language processing package.
 * 
 * @author Stefan Werner
 *
 */
public class ExtractorPipeline {

	private StanfordCoreNLP corefPipeline;
	private StanfordCoreNLP extrPipeline;
	private final String CPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse,dcoref";
	private final String EPROPERTIES = "tokenize,ssplit,pos,lemma,ner,parse";
	private CoreferenceResolver corefResolver;
	private TripletExtractor extractor;
	private PredicateResolver predResolver;

	/**
	 * Initialize dependent classes and setup annotators.
	 */
	public ExtractorPipeline() {
		this.corefResolver = new CoreferenceResolver();
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
		this.predResolver = new PredicateResolver();
	}

	/**
	 * Executes pipeline approach on a given article.
	 * 
	 * @param article
	 * @return a collection of found triplets, specified by URIs.
	 * @throws Exception
	 */
	public Collection<Triplet<String, String, String>> processArticle(String article) throws Exception {
		CoreDocument doc = new CoreDocument(article);
		corefPipeline.annotate(doc);
		String resoluted = corefResolver.resolveCoreferences(doc);
		if (resoluted == null) {
			resoluted = article;
		}
		Set<Entity> entities = corefResolver.linkEntitiesToMentions(doc);
		Annotation res = new Annotation(resoluted);
		extrPipeline.annotate(res);
		Set<Triplet<Tree, Tree, Tree>> triplets = extractor.extractFromText(res);
		EntityLinker linker = new EntityLinker();

		Collection<Triplet<String, String, String>> result = new HashSet<>();

		for (Triplet<Tree, Tree, Tree> triplet : triplets) {
			// handle subject and object
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

			// handle predicate
			String predicate = toMention(triplet.getSecond());

			// write to triplet
			String subjURI = sEntity.getUri();
			String predURI = predResolver.resolveToURI(predicate);
			String objURI = oEntity.getUri();

			Triplet<String, String, String> uriTriplet = null;
			if (subjURI != null && predURI != null && objURI != null) {
				uriTriplet = new Triplet<>(subjURI, predURI, objURI);
			} else if (subjURI != null && predURI != null && oEntity.getLabel() != null) {
				uriTriplet = new Triplet<>(subjURI, predURI, oEntity.getLabel());
			}
			if (uriTriplet != null) {
				result.add(uriTriplet);
			}

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
			System.out.println("Predicate: " + predicate);
			if (predURI != null) {
				System.out.println("URI: " + predURI);
			}

			// handle predicate
			// TODO: handle predicate
		}
		System.out.println("Processing finished.");
		System.out.println("Result: " + result);
		return result;
	}

	/**
	 * Transforms the string representation of the given tree to a mention.
	 * 
	 * @param root - root node of the tree
	 * @return mention contained by the tree
	 */
	private String toMention(Tree root) {
		String s = "";
		for (Tree leaf : root.getLeaves()) {
			s += leaf.value() + " ";
		}
		return s.substring(0, s.length() - 1);
	}

}
