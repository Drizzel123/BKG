package com.group.proseminar.knowledge_graph.ontology;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Predicate {
	private String uri;
	private String schema;
	private String origin;
	private List<String> synonyms;
	private String pos;
	private List<String> derivations;
	private float frequency;

	public Predicate(String uri, String schema, String synonym) {
		this.uri = uri;
		this.schema = schema;
		this.synonyms = Stream.of(synonym).map(x-> x.toLowerCase()).collect(Collectors.toList());
	}

	public Predicate(String uri, String schema, List<String> synonyms) {
		this.uri = uri;
		this.schema = schema;
		this.synonyms = synonyms.stream().map(x->x.toLowerCase()).collect(Collectors.toList());
	}

	public void addSynonym(String synonym) {
		this.synonyms.add(synonym.toLowerCase());
	}

	public void addAllSynonyms(Collection<String> synonyms) {
		this.synonyms.addAll(synonyms.stream().map(x->x.toLowerCase()).collect(Collectors.toList()));
	}

	public String getFirst() {
		if (!synonyms.isEmpty()) {
			return synonyms.get(0);
		}
		return null;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public List<String> getDerivations() {
		return derivations;
	}

	public void setDerivations(List<String> derivations) {
		this.derivations = derivations;
	}

	public float getFrequency() {
		return frequency;
	}

	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}

	@Override
	public String toString() {
		return "Predicate [uri=" + uri + ", schema=" + schema + ", synonyms=" + synonyms + "]";
	}
}
