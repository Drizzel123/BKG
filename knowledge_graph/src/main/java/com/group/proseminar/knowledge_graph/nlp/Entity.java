package com.group.proseminar.knowledge_graph.nlp;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class Entity {

	// TODO: select best mentions with respect to NER (mention has tag-> better
	// candidate)

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
		this.mentions.add(bestMention);
		this.bestMention = bestMention;
	}

	public void addMention(String mention) {
		this.mentions.add(mention);
		if (bestMention == null || mention.length() > bestMention.length()) {
			bestMention = mention;
		}
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
		if (!mentions.contains(bestMention)) {
			mentions.add(bestMention);
		}
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
		// TODO: Check this!
		switch (label) {
		case "PERSON":
		case "TITLE":
			this.label = "Person";
			break;
		case "LOCATION":
		case "CITY":
			this.label = "Place";
			break;
		case "NUMBER":
		case "DATE":
		default:
			this.label = "Literal";
		}
	}

	public String toString() {
		return "URI: " + uri + ", Label: " + label + ", Best Mention: " + bestMention + ", Mentions: " + mentions;
	}
}
