package com.group.proseminar.knowledge_graph.nlp;

import java.util.AbstractMap;
import java.util.ArrayList;
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
import java.util.stream.Stream;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

/**
 * 
 * @author Stefan Werner
 *
 */
public class TripletExtractor {

	/**
	 * Get dependent phrase of current predicate phrase.
	 * 
	 * @param sentence - sentence of the predicate
	 * @param word     - predicate
	 * @return (string representation of the mentions dependency root, string
	 *         representation of the new predicate)
	 */
	public static Entry<String, String> getDependentPredicate(CoreSentence sentence, String word) {
		SemanticGraph dependencies = sentence.dependencyParse();
		for (IndexedWord index : dependencies.getAllNodesByWordPattern(word)) {
			List<String> result = new ArrayList<>();
			for (SemanticGraphEdge e : dependencies.incomingEdgeIterable(dependencies.getNodeByIndex(index.index()))) {
				if (e.getRelation().toString().equals("cop")) {
					for (SemanticGraphEdge out : dependencies.outgoingEdgeIterable(e.getSource())) {
						if (out.getRelation().toString().equals("amod")) {
							result.add(out.getDependent().value());
						}
					}
					result.add(e.getSource().value());
					return new AbstractMap.SimpleEntry<String, String>(e.getSource().value(), String.join(" ", result));
				}
			}
		}
		return null;
	}

	/**
	 * Get dependent phrase of current object phrase.
	 * 
	 * @param sentence - sentence of the object
	 * @param word     - object
	 * @return (string representation of the mentions dependency root, string
	 *         representation of the new object)
	 */
	public static Entry<String, String> getDependentObject(CoreSentence sentence, String word) {
		SemanticGraph dependencies = sentence.dependencyParse();
		for (IndexedWord index : dependencies.getAllNodesByWordPattern(word)) {
			List<Integer> result = new ArrayList<>();
			for (SemanticGraphEdge e : dependencies.outgoingEdgeIterable(dependencies.getNodeByIndex(index.index()))) {
				if (e.getRelation().toString().startsWith("nmod")) {
					result.add(e.getDependent().index());
					for (SemanticGraphEdge out : dependencies.outgoingEdgeIterable(e.getTarget())) {
						if (out.getRelation().toString().startsWith("compound")) {
							result.add(out.getDependent().index());
						}
					}
					result.sort((e1, e2) -> e1.compareTo(e2));
					return new AbstractMap.SimpleEntry<String, String>(e.getSource().value(), String.join(" ", result
							.stream().map(x -> sentence.tokens().get(x - 1).value()).collect(Collectors.toList())));
				}
			}
		}
		return null;
	}

	/**
	 * Extracts a set of triplets (subject, predicate, object) given an annotated
	 * text.
	 * 
	 * @param document
	 * @return
	 */
	public static Map<CoreSentence, Set<Triplet<String, String, String>>> extractTriplets(CoreDocument document) {
		Map<CoreSentence, Set<Triplet<String, String, String>>> tripletMap = new HashMap<>();

		for (CoreSentence sentence : document.sentences()) {
			Set<Triplet<String, String, String>> triplets = new HashSet<>();
			List<CoreEntityMention> entities = sentence.entityMentions();
			Tree cparse = sentence.constituencyParse().firstChild();

			List<Tree> sroots = cparse.stream().filter(t -> t.label().value().equals("S")).collect(Collectors.toList());
			for (Tree sroot : sroots) {
				Triplet<String, String, String> triplet = extractFromRoot(entities, sroot);
				if (triplet != null) {
					triplets.add(triplet);
				}
			}
			tripletMap.put(sentence, triplets);
		}
		return tripletMap;
	}

	/**
	 * Extracts a triplet from a root with label "S" with respect to the
	 * CoreEntityMentions of that sentence.
	 * 
	 * @param entities
	 * @param sroot
	 * @return
	 */
	private static Triplet<String, String, String> extractFromRoot(List<CoreEntityMention> entities, Tree sroot) {
		CoreEntityMention subject = extractSubject(entities, sroot);
		Tree predicate = extractPredicate(sroot);
		predicatePostProcessing(predicate, sroot);
		if (predicate != null) {
			CoreEntityMention object = extractObject(entities, sroot, predicate);
			if (subject != null && object != null) {
				return new Triplet<String, String, String>(subject.text(), toMention(predicate), object.text());
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
	private static Tree extractPredicate(Tree sroot) {
		// Find VP child from root S
		Tree nodeVP = sroot.stream().filter(x -> x.label().value().equals("VP")).findFirst().orElse(null);

		// DFS to find verb type nodes
		Set<String> matchSet = Stream.of("VB", "VBD", "VBG", "VBN", "VBP", "VBZ").collect(Collectors.toSet());
		Set<String> pruneSet = Stream.of("S", "SBAR").collect(Collectors.toSet());
		Set<Tree> resultSet = new HashSet<>();
		depthFirstSearch(nodeVP, matchSet, pruneSet, resultSet);

		// Determine verb type with largest depth
		Map<Tree, Integer> depthMap = determineDepth(nodeVP, resultSet);
		Tree deepestVT = depthMap.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.map(x -> x.getKey()).findFirst().orElse(null);
		return deepestVT;
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
	private static void depthFirstSearch(Tree root, Set<String> match, Set<String> prune, Set<Tree> result) {
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
	private static Map<Tree, Integer> determineDepth(Tree root, Set<Tree> nodes) {
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
		// Remove the nodes that are not in the set nodes
		return depthMap.entrySet().stream().filter(x -> nodes.contains(x.getKey()))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}

	/**
	 * If a token with part-of-speech tag "IN" follows the predicate, the original
	 * vtype is substituted by a new tree containing the original tree and the
	 * following preposition.
	 * 
	 * @param vtype - tree of the extracted predicate
	 * @param root  - root containing the predicate
	 * 
	 */
	private static void predicatePostProcessing(Tree vtype, Tree root) {
		List<Tree> leafs = root.getLeaves();
		List<Tree> parents = leafs.stream().map(x -> x.ancestor(1, root)).collect(Collectors.toList());
		boolean seen = false;
		for (Tree node : parents) {
			if (seen) {
				if (node.label().toString().equals("IN")) {
					// TODO: check if a VP node exists with (VT, IN) tag
					Tree artNode = node.deepCopy();
					// Substitute original vtype by new, modified tree
					Tree leaf = new LabeledScoredTreeNode(vtype.firstChild().label());
					leaf.setValue(vtype.firstChild().value());
					Tree artChild = new LabeledScoredTreeNode(vtype.label(),
							Stream.of(leaf).collect(Collectors.toList()));
					vtype.setChildren(Stream.of(artChild, artNode).collect(Collectors.toList()));
				}
				break;
			}
			if (node == vtype) {
				seen = true;
			}
		}
	}

	/**
	 * Extracts the object in a tree with respect to the verb.
	 * 
	 * @param entities - entities within the sentence.
	 * @param sroot    - the current root with label "S"
	 * @param vt       - the current verb
	 * @return
	 */
	private static CoreEntityMention extractObject(List<CoreEntityMention> entities, Tree sroot, Tree vt) {
		Tree origin = extractObjectOrigin(sroot, vt);
		if (origin != null) {
			return mentionFromTree(entities, origin);
		}
		return null;
	}

	/**
	 * Extracts the origin of the object with respect to the node of the predicate.
	 * 
	 * @param sroot root of the sentence with label "S"
	 * @param vt    node of the predicate (verb type)
	 * @return origin of the object
	 */
	private static Tree extractObjectOrigin(Tree sroot, Tree vt) {
		List<Tree> siblings = vt.siblings(sroot);
		Set<String> types = Stream.of("NP", "PP", "ADJP").collect(Collectors.toSet());

		// Candidates are nodes within a sibling of vt that have a label contained in
		// types
		List<Tree> candidates = new ArrayList<>();
		for (Tree sibling : siblings) {
			// For each sibling add all subtrees that correspond to a label contained in
			// types
			candidates.addAll(
					sibling.stream().filter(tree -> types.contains(tree.label().value())).collect(Collectors.toList()));
		}

		// Find object in candidates
		Set<String> npSet = Stream.of("NP").collect(Collectors.toSet());
		Set<String> adjSet = Stream.of("JJ", "JJR", "JJS").collect(Collectors.toSet());

		Tree result = null;
		for (Tree candidate : candidates) {
			String type = candidate.label().value();
			if (type.equals("NP") || type.equals("PP") || type.equals("S")) {
				// Search for first noun type
				result = findFirst(candidate, npSet);
			} else {
				// Find first adjective type
				result = findFirst(candidate, adjSet);
			}
			if (result != null) {
				return result;
			}
		}
		return result;
	}

	/**
	 * Derives the subject of a sentence given the root.
	 * 
	 * @param sroot root of a sentence with label "S"
	 * @return subject of the sentence
	 */
	private static CoreEntityMention extractSubject(List<CoreEntityMention> entities, Tree sroot) {
		// Return null if S has no child with label NP
		if (!sroot.stream().anyMatch(x -> x.label().toString().equals("NP"))) {
			return null;
		}
		Tree treeNP = findFirst(sroot, Stream.of("NP").collect(Collectors.toSet()));
		return mentionFromTree(entities, treeNP);
	}

	/**
	 * Derives the CoreEntityMention contained within a tree or null if there is
	 * none.
	 * 
	 * @param entities
	 * @param root
	 * @return CoreEntityMention
	 */
	private static CoreEntityMention mentionFromTree(List<CoreEntityMention> entities, Tree root) {
		CoreEntityMention mention = null;
		for (Tree leaf : root.getLeaves()) {
			CoreLabel label = (CoreLabel) leaf.label();
			if (label.get(CoreAnnotations.EntityMentionIndexAnnotation.class) != null) {
				mention = entities.get(label.get(CoreAnnotations.EntityMentionIndexAnnotation.class));
				break;
			}
		}
		return mention;
	}

	/**
	 * Finds the first occurrence of a node with a label matching a string in types
	 * by performing a BFS approach.
	 * 
	 * @param root  root of the node
	 * @param types set of labels for the possible result
	 * @return the first node whose label matches any string in types
	 */
	private static Tree findFirst(Tree root, Set<String> types) {
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
	 * Transforms the string representation of the given tree to a mention.
	 * 
	 * @param root - root node of the tree
	 * @return mention contained by the tree
	 */
	private static String toMention(Tree root) {
		String s = "";
		for (Tree leaf : root.getLeaves()) {
			s += leaf.value() + " ";
		}
		return s.substring(0, s.length() - 1);
	}
}
