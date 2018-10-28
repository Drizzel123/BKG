package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

public class TripletExtractor {


	public Set<Triplet<Tree, Tree, Tree>> extractFromText(Annotation annotation) {
		Tree parseTree = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0)
				.get(TreeCoreAnnotations.TreeAnnotation.class);
		Set<Triplet<Tree, Tree, Tree>> tripletSet = new HashSet<>();
		// Start extraction on every "S" node
		for (Tree node : parseTree) {
			if (node.nodeString().split(" ")[0].equals("S")) {
				tripletSet.addAll(extractTripletFromS(node));
			}
		}
		return tripletSet;
	}

	public Set<Triplet<Tree, Tree, Tree>> extractTripletFromS(Tree sroot) {
		Set<Triplet<Tree, Tree, Tree>> tripletSet = new HashSet<>();
		Tree subj = extractSubjectFromS(sroot);
		Tree pred = extractPredicateFromS(sroot);
		if (pred == null || subj == null) {
			return tripletSet;
		}
		Tree obj = extractObject(sroot, pred);
		if (obj == null) {
			return tripletSet;
		}
		for (Tree candidate : objectPostProcessing(obj)) {
			tripletSet.add(new Triplet<Tree, Tree, Tree>(subj, pred, candidate));
		}
		return tripletSet;
	}

	public Tree extractSubjectFromS(Tree sroot) {
		// Return null if S has no child with label NP
		// Take subject from previous triplet???
		if (!sroot.getChildrenAsList().stream().anyMatch(x -> x.nodeString().split(" ")[0].equals("NP"))) {
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
			String type = node.nodeString().split(" ")[0];
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
		List<Tree> listVP = sroot.getChildrenAsList().stream().filter(x -> x.nodeString().startsWith("VP"))
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

	public Tree extractObject(Tree sroot, Tree vt) {
		List<Tree> siblings = vt.siblings(sroot);
		String types = "NP,PP,ADJP";
		Set<String> typeSet = new HashSet<>(Arrays.asList(types.split(",")));
		// Candidates are nodes of a type contained in typeSet
		Set<Tree> candidates = siblings.stream().filter(tree -> typeSet.contains(tree.nodeString().split(" ")[0]))
				.collect(Collectors.toSet());

		// Find object in candidates
		Tree result = null;
		for (Tree candidate : candidates) {
			String type = candidate.nodeString().split(" ")[0];
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

	public Set<Tree> objectPostProcessing(Tree object) {
		Set<Tree> candidates = null;
		List<Tree> children = object.getChildrenAsList();
		List<String> types = children.stream().map(x -> x.nodeString().split(" ")[0]).collect(Collectors.toList());
		if (types.size() == 2) {
			if (types.get(0).equals("NP")) {
				if (types.get(1).equals("PP") || types.get(1).equals("SBAR")) {
					object = children.get(0);
					candidates = new HashSet<>();
					candidates.add(object);
				}
			}
		} else if (types.size() == 3) {
			if (types.get(0).equals("NP") && types.get(1).equals("CC") && types.get(2).equals("NP")) {
				candidates = new HashSet<>();
				candidates.add(children.get(0));
				candidates.add(children.get(2));
			}
		}
		if (candidates == null) {
			candidates = new HashSet<>();
			candidates.add(object);
		}
		return candidates;
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
			String type = node.nodeString().split(" ")[0];
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
