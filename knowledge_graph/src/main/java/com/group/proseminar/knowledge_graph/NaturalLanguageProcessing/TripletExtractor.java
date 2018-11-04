package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class TripletExtractor {

	/**
	 * Extracts a set of triplets (subject, predicate, object) given an annotated
	 * text.
	 * 
	 * @param annotation annotated text
	 * @return set of triplets
	 */
	public Set<Triplet<Tree, Tree, Tree>> extractFromText(Annotation annotation) {
		Set<Triplet<Tree, Tree, Tree>> tripletSet = new HashSet<>();
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			System.out.println("Sentence: " + sentence);

			Tree parseTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
			// Start extraction on every "S" node
			for (Tree node : parseTree) {
				if (node.label().toString().equals("S")) {
					tripletSet.addAll(extractTripletFromS(node));
				}
			}
		}
		return tripletSet;
	}

	/**
	 * Extracts a set of triplets (subject, predicate, object) given the root of a
	 * sentence.
	 * 
	 * @param sroot root of a sentence with label "S"
	 * @return set of triplets derived from the dependency tree of the sentence
	 */
	public Set<Triplet<Tree, Tree, Tree>> extractTripletFromS(Tree sroot) {
		Set<Triplet<Tree, Tree, Tree>> tripletSet = new HashSet<>();
		Tree subj = extractSubjectFromS(sroot);
		Tree pred = extractPredicateFromS(sroot);
		if (pred == null || subj == null) {
			return tripletSet;
		}
		Tree obj = extractObjectOrigin(sroot, pred);
		if (obj == null) {
			return tripletSet;
		}
		for (Tree candidate : objectPostProcessing(obj)) {
			tripletSet.add(new Triplet<Tree, Tree, Tree>(subj, pred, candidate));
		}
		return tripletSet;
	}

	/**
	 * Derives the subject of a sentence given the root.
	 * 
	 * @param sroot root of a sentence with label "S"
	 * @return subject of the sentence
	 */
	public Tree extractSubjectFromS(Tree sroot) {
		// Return null if S has no child with label NP
		// Take subject from previous triplet???
		if (!sroot.getChildrenAsList().stream().anyMatch(x -> x.label().toString().equals("NP"))) {
			return null;
		}
		Set<String> types = new HashSet<>();
		types.add("NP");
		Tree treeNP = findFirst(sroot, types);
		return treeNP;
	}

	/**
	 * Finds the first occurrence of a node with a label matching a string in types
	 * by performing a Breadth First approach.
	 * 
	 * @param root  root of the node
	 * @param types set of labels for the possible result
	 * @return the first node whose label matches any string in types
	 */
	public Tree findFirst(Tree root, Set<String> types) {
		Map<Tree, Boolean> visited = new HashMap<>();
		LinkedList<Tree> queue = new LinkedList<Tree>();
		visited.put(root, true);
		queue.add(root);
		Tree node;
		while (queue.size() != 0) {
			node = queue.poll();
			// Get type of node
			String type = node.label().toString();
			if (types.contains(type)) {
				return node;
			}
			for (Tree child : node.children()) {
				visited.putIfAbsent(child, false);
				if (!visited.get(child)) {
					visited.put(child, true);
					queue.add(child);
				}
			}
		}
		return null;
	}

	/**
	 * Determines the deepest verb type of the sentence.
	 * 
	 * @param sroot root of the sentence
	 * @return verb type of the predicate, null if no predicate is found
	 */
	public Tree extractPredicateFromS(Tree sroot) {
		// Find VP child from root S
		Tree nodeVP = null;
		List<Tree> listVP = sroot.getChildrenAsList().stream().filter(x -> x.label().toString().equals("VP"))
				.collect(Collectors.toList());
		if (listVP.size() > 0) {
			nodeVP = listVP.get(0);
		} else {
			return null;
		}

		// DFS to find verb type nodes
		String types = "VB, VBD, VBG, VBN, VBP, VBZ";
		Set<String> matchSet = new HashSet<>(Arrays.asList(types.split(", ")));
		Set<String> pruneSet = new HashSet<>();
		pruneSet.add("S");
		pruneSet.add("SBAR");
		Set<Tree> result = new HashSet<>();
		depthFirstSearch(nodeVP, matchSet, pruneSet, result);

		// Determine verb type with largest depth
		Map<Tree, Integer> depthMap = determineDepth(nodeVP, result);
		int max = -1;
		Tree deepestVT = null;
		for (Tree candidate : result) {
			int depth = depthMap.get(candidate);
			if (depth > max) {
				max = depth;
				deepestVT = candidate;
			}
		}
		return deepestVT;
	}

	/**
	 * Extracts the origin of the object with respect to the node of the predicate.
	 * 
	 * @param sroot root of the sentence with label "S"
	 * @param vt    node of the predicate (verb type)
	 * @return origin of the object
	 */
	public Tree extractObjectOrigin(Tree sroot, Tree vt) {
		List<Tree> siblings = vt.siblings(sroot);
		String types = "NP,PP,ADJP";
		Set<String> typeSet = new HashSet<>(Arrays.asList(types.split(",")));
		// Candidates are nodes of a type contained in typeSet
		Set<Tree> candidates = siblings.stream().filter(tree -> typeSet.contains(tree.label().toString()))
				.collect(Collectors.toSet());

		// Find object in candidates
		Tree result = null;
		for (Tree candidate : candidates) {
			String type = candidate.label().toString();
			if (type.equals("NP") || type.equals("PP") || type.equals("S")) {
				// Search for first noun
				Set<String> nTypeSet = new HashSet<>();
				nTypeSet.add("NP");
				result = findFirst(candidate, nTypeSet);
			} else {
				// Find first adjective
				String adjectives = "JJ, JJR, JJS";
				Set<String> adjSet = new HashSet<>(Arrays.asList(adjectives.split(", ")));
				result = findFirst(candidate, adjSet);
			}
			if (result != null) {
				return result;
			}
		}
		return result;
	}

	/**
	 * Performs post-processing on the object, which might be split into more than
	 * one part.
	 * 
	 * @param object - object to be post-processed
	 * @return set of objects, which are derived from the original object
	 */
	public Set<Tree> objectPostProcessing(Tree origin) {
		Set<Tree> objects = new HashSet<>();
		objects.add(origin);
		boolean changed = true;

		while (changed) {
			changed = false;
			Set<Tree> iter = new HashSet<>();
			for (Tree object : objects) {
				List<Entry<Tree, String>> labels = object.getChildrenAsList().stream()
						.map(child -> new AbstractMap.SimpleEntry<Tree, String>(child, child.label().toString()))
						.collect(Collectors.toList());

				if (labels.stream().anyMatch(x -> x.getValue().equals("CC"))) {
					Set<String> separators = new HashSet<>();
					separators.add("CC");
					Set<Tree> split = splitObjectsByCC(labels, separators);
					if (!split.isEmpty()) {
						iter.addAll(split);
						changed = true;
					}
				} else if (labels.stream().anyMatch(x -> x.getValue().equals("SBAR"))) {
					// Choose first children as successor
					// TODO: Check if first child of "SBAR" fork is always "NP"
					Tree firstNP = object.firstChild();
					if (firstNP != null) {
						iter.add(firstNP);
						changed = true;
					}
				} else if (labels.stream().anyMatch(x -> x.getValue().equals("DT"))) {
					// Declare labels to be filtered
					// TODO: Think about what tags should be added
					Set<String> filter = new HashSet<>();
					filter.add("DT");
					filter.add("JJ");
					// Remove nodes with a specific set of labels from the tree
					List<Tree> filtered = labels.stream().filter(x -> !filter.contains(x.getValue()))
							.map(entry -> entry.getKey()).collect(Collectors.toList());
					// TODO: Check if "NP" is always the case
					Tree artNode = new LabeledScoredTreeNode(new StringLabel("NP"), filtered);
					iter.add(artNode);
					changed = true;
				} else {
					// No changes on object
					iter.add(object);
				}
			}
			// Apply the changes of this iteration
			if (changed) {
				objects = iter;
			}
		}
		return objects;
	}

	/**
	 * Splits a sentence into parts by nodes with label contained by the set separator.
	 * @param labels set of entries describing trees and their attached labels
	 * @param separator set of labels to split by
	 * @return set of trees, split by a separator 
	 */
	public Set<Tree> splitObjectsByCC(List<Entry<Tree, String>> labels, Set<String> separator) {
		Set<Tree> result = new HashSet<>();
		Set<List<Tree>> candidates = new HashSet<>();
		List<Tree> current = new ArrayList<>();
		for (Entry<Tree, String> entry : labels) {

			if (separator.contains(entry.getValue())) {
				// Do not add tree with label contained by separator
				candidates.add(current);
				current = new ArrayList<>();
			} else {
				current.add(entry.getKey());
			}
		}
		candidates.add(current);

		for (List<Tree> list : candidates) {
			// Create an artificial parent for the nodes contained in the list
			Tree artNode = new LabeledScoredTreeNode(new StringLabel("NP"), list);
			result.add(artNode);
		}
		return result;
	}

	/**
	 * Depth First Search starting from the root, where nodes whose label matches a
	 * string in the set match will be added to the result set. Furthermore the tree
	 * will be pruned at nodes with labels that do match a string in the set prune.
	 * 
	 * @param root   root of the tree
	 * @param match  set of labels for the result
	 * @param prune  labels of nodes whose children will not be considered
	 * @param result nodes whose labels do match a string of the set match
	 */
	public void depthFirstSearch(Tree root, Set<String> match, Set<String> prune, Set<Tree> result) {
		Map<Tree, Boolean> visited = new HashMap<>();
		Stack<Tree> stack = new Stack<>();
		stack.push(root);

		while (!stack.empty()) {
			Tree node = stack.pop();
			visited.putIfAbsent(node, false);
			// Get type of node
			String type = node.label().toString();
			if (match.contains(type)) {
				result.add(node);
			}
			if (!visited.get(node)) {
				// Mark node as visited
				visited.put(node, true);
				// Only push children on stack if the tree is not to be pruned
				// Do not prune at the root
				if ((!prune.contains(type)) || node.equals(root)) {
					List<Tree> children = node.getChildrenAsList();
					// Ensure the right order of the children on the stack
					Collections.reverse(children);
					for (Tree child : children) {
						stack.push(child);
					}
				}
			}
		}
	}

	/**
	 * Determines the depth of nodes contained in the set with respect to the given
	 * root.
	 * 
	 * @param root  the root of the tree
	 * @param nodes nodes, whose depth is to be determined
	 * @return map of (node, depth) entries
	 */
	private Map<Tree, Integer> determineDepth(Tree root, Set<Tree> nodes) {
		Map<Tree, Integer> depthMap = new HashMap<>();
		Map<Tree, Boolean> visited = new HashMap<>();
		LinkedList<Tree> queue = new LinkedList<Tree>();

		depthMap.put(root, 0);
		visited.put(root, true);
		queue.add(root);
		Tree node;
		while (queue.size() != 0) {
			node = queue.poll();
			int depth = depthMap.get(node);
			for (Tree child : node.children()) {
				depthMap.put(child, depth + 1);
				visited.putIfAbsent(child, false);
				if (!visited.get(child)) {
					visited.put(child, true);
					queue.add(child);
				}
			}
		}
		return depthMap;
	}
}
