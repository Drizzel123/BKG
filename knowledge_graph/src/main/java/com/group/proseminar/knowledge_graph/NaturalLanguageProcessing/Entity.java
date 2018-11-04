package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class Entity {

	private TreeSet<String> mentions = new TreeSet<>(new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			if (o1.length() > o2.length()) {
				return 1;
			} else if (o1.length() < o2.length()) {
				return -1;
			} else {
				return 0;
			}
		}
	});
	private String bestMention;
	private String uri;
	private String label;

	public Entity(Collection<String> mentions, String bestMention) {
		this.mentions.addAll(mentions);
		this.bestMention = bestMention;
	}

	public Entity(Collection<String> mentions) {
		this.mentions.addAll(mentions);
		// If no bestMention is set specifically choose the longest string
		this.bestMention = this.mentions.first();
	}

	public Entity(String bestMention) {
		this.bestMention = bestMention;
	}

	public void addMention(String mention) {
		this.mentions.add(mention);
	}

	public TreeSet<String> getMentions() {
		return mentions;
	}

	public void setMentions(TreeSet<String> mentions) {
		this.mentions = mentions;
	}

	public String getBestMention() {
		return bestMention;
	}

	public void setBestMention(String bestMention) {
		this.bestMention = bestMention;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
