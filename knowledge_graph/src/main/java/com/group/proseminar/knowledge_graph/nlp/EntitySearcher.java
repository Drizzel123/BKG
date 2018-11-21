package com.group.proseminar.knowledge_graph.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EntitySearcher {

	private Collection<Entity> entities;

	public EntitySearcher(Collection<Entity> entities) {
		this.entities = entities;
	}

	/**
	 * Matches a mention to the largest entity found within.
	 * @param mention - mention to be resolved to an entity
	 * @return largest entity within mention, or null if none is found
	 */
	public Entity getLargestEntity(String mention) {
		List<String> words = new ArrayList<>(Arrays.asList(mention.split(" ")));
		int index = words.size();
		Entity largest = null;
		while (index > 0 && largest == null) {
			int difference = words.size() - index;
			for (int i = 0; i <= difference; i++) {
				String candidate = String.join(" ", words.subList(i, i + index));
				largest = entities.stream().filter(x -> x.getMentions().contains(candidate)).findFirst().orElse(null);
			}
			index--;
		}
		return largest;
	}
}
