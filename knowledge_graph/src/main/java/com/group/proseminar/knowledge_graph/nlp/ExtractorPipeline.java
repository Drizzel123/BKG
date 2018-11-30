package com.group.proseminar.knowledge_graph.nlp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import com.group.proseminar.knowledge_graph.ontology.PredicateResolver;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

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
	private PredicateResolver predResolver;
	private BufferedWriter writer;
	private Model resultModel;

	/**
	 * Initialize dependent classes and setup annotators.
	 * 
	 * @throws IOException
	 */
	public ExtractorPipeline() throws IOException {
		this.corefResolver = new CoreferenceResolver();
		// Initialize corefPipeline
		Properties corefProps = new Properties();
		corefProps.put("annotators", CPROPERTIES);
		corefProps.put("dcoref.score", true);
		corefProps.put("threads", "8");
		this.corefPipeline = new StanfordCoreNLP(corefProps);
		// Initialize extrPipeline
		Properties extrProps = new Properties();
		extrProps.put("annotators", EPROPERTIES);
		extrProps.put("threads", "8");
		this.extrPipeline = new StanfordCoreNLP(extrProps);
		this.predResolver = new PredicateResolver();
		// Initialize writer and model for output
		Path path = Paths.get("src/main/resources/nlp_results.ttl");
		this.writer = new BufferedWriter(new FileWriter(path.toUri().getPath()));
		this.resultModel = ModelFactory.createDefaultModel();
		this.resultModel.setNsPrefix("dbr", "http://dbpedia.org/resource/");
		this.resultModel.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
	}

	/**
	 * Executes pipeline approach on a given article.
	 * 
	 * @param article
	 * @throws Exception
	 */
	public void processArticle(String article) throws Exception {
		CoreDocument doc = new CoreDocument(article);
		corefPipeline.annotate(doc);
		String resolved = corefResolver.resolveCoreferences(doc);
		if (resolved == null) {
			resolved = article;
		}
		Set<Entity> entities = corefResolver.linkEntitiesToMentions(doc);
		CoreDocument res = new CoreDocument(resolved);
		extrPipeline.annotate(res);
		Map<CoreSentence, Set<Triplet<String, String, String>>> tripletMap = TripletExtractor.extractTriplets(res);
		// link entities to URIs
		EntityLinker linker = new EntityLinker();
		linker.resolveURIs(entities);
		Collection<Triplet<String, String, String>> result = new HashSet<>();

		System.out.println("Triplets: " + tripletMap);

		for (Entry<CoreSentence, Set<Triplet<String, String, String>>> entry : tripletMap.entrySet()) {
			for (Triplet<String, String, String> triplet : entry.getValue()) {
				// handle subject and object
				String subject = triplet.getFirst();
				String predicate = triplet.getSecond();
				String object = triplet.getThird();
				Entity sEntity = linker.getLargestEntity(entities, subject);
				Entity oEntity = linker.getLargestEntity(entities, object);
				if (sEntity != null && oEntity != null) {
					// write to triplet
					String subjURI = sEntity.getUri();
					String predURI = predResolver.resolveToURI(predicate);
					String objURI = oEntity.getUri();

					if (predURI == null) {
						Entry<String, String> pDependent = TripletExtractor.getDependentPredicate(entry.getKey(),
								predicate);
						Entry<String, String> oDependent = TripletExtractor.getDependentObject(entry.getKey(),
								pDependent.getKey());

						if (pDependent != null && oDependent != null) {
							predURI = predResolver.resolveToURI(pDependent.getValue());
							oEntity = linker.getLargestEntity(entities, oDependent.getValue());
							objURI = oEntity.getUri();
						}
					}

					Triplet<String, String, String> uriTriplet = null;
					if (subjURI != null && predURI != null && objURI != null) {
						uriTriplet = new Triplet<>(subjURI, predURI, objURI);
					}
					if (uriTriplet != null) {
						result.add(uriTriplet);
					}
				}
			}
		}
		// Insert results to model
		result.stream().forEach(x -> System.out.println(x));
		result.stream().forEach(x -> insertTripletToModel(x.getFirst(), x.getSecond(), x.getThird()));
	}

	/**
	 * Inserts triplets of format subject, predicate, object to a jena-model.
	 * 
	 * @param sURI - URI of subject
	 * @param pURI - URI of predicate
	 * @param oURI - URI of object
	 */
	private void insertTripletToModel(String sURI, String pURI, String oURI) {
		Resource sResource = this.resultModel.createResource(sURI);
		Property property = this.resultModel.createProperty(pURI);
		Resource oResource = this.resultModel.createResource(oURI);
		this.resultModel.add(sResource, property, oResource);
	}

	/**
	 * Write model to file.
	 */
	public void writeResultToFile() {
		this.resultModel.write(writer, "Turtle");
	}
}
