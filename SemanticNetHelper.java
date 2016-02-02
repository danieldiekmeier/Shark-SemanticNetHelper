package semanticNetHelper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import net.sharkfw.knowledgeBase.SNSemanticTag;
import net.sharkfw.knowledgeBase.SemanticNet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * SemanticNetHelper is a helper class that provides methods to check and change
 * a SemanticNet object for several characteristics of SemanticNets.
 *
 * @author Daniel Diekmeier
 */

public class SemanticNetHelper {

	private static ArrayList<ArrayList<SNSemanticTag>> tComponents;

	private static int tIndex;
	private static Stack<SNSemanticTag> tStack;

	private static HashMap<SNSemanticTag, Integer> tIndices;
	private static HashMap<SNSemanticTag, Integer> tLowLinks;
	private static HashMap<SNSemanticTag, Boolean> tChecked;

	/**
	 * Checks if the SemanticNet instance is a correct taxonomy
	 * using Tarjan's Strongly Connected Components Algorithm
	 * https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
	 *
	 * @param semanticNet A SemanticNet, probably with some SemanticTags that have some kind of connection
	 * @param predicate The predicate that should be checked.
	 *
	 * @return true if the connections of Predicate in the SemanticNet
	 * form a taxonomy, false if there are circular dependencies between the tags
	 */
	public static boolean isTaxonomy(SemanticNet semanticNet, String predicate) throws SharkKBException {

		// Reset
		tComponents = new ArrayList<ArrayList<SNSemanticTag>>();
		tIndex = 0;
		tStack = new Stack<SNSemanticTag>();

		tIndices = new HashMap<SNSemanticTag, Integer>();
		tLowLinks = new HashMap<SNSemanticTag, Integer>();
		tChecked = new HashMap<SNSemanticTag, Boolean>();

		// Return immediately if SemanticNet is empty
		if (semanticNet.size() == 0) {
			return true;
		}

		Iterator<SemanticTag> iter = semanticNet.stTags();
		ArrayList<SNSemanticTag> tags = new ArrayList<SNSemanticTag>();
		while (iter.hasNext()) {
			tags.add((SNSemanticTag) iter.next());
		}

		for (SNSemanticTag tag : tags) {
			if (connectsToItself(tag, predicate)) {
				return false;
			}

			if (!tIndices.containsKey(tag)) {
				strongConnect(tag, predicate);
			}
		}

		// Check if there are strongly connected components
		// that are longer than one item
		int longestListSize = 0;
		for (ArrayList<SNSemanticTag> list : tComponents) {
			if (longestListSize < list.size()) {
				longestListSize = list.size();
			}
		}

		return longestListSize <= 1;
	}

	private static boolean connectsToItself(SNSemanticTag tag, String predicate) {
		Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
		if (subTags != null) {
			while(subTags.hasMoreElements()) {
				SNSemanticTag subTag = subTags.nextElement();
				if (subTag.equals(tag)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void strongConnect(SNSemanticTag tag, String predicate) throws SharkKBException {
		tIndices.put(tag, tIndex);
		tLowLinks.put(tag, tIndex);
		tChecked.put(tag, true);
		tIndex = tIndex + 1;
		tStack.push(tag);

		Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
		if (subTags != null) {
			while(subTags.hasMoreElements()) {
				SNSemanticTag w = subTags.nextElement();
				if (!tIndices.containsKey(w)) {
					strongConnect(w, predicate);
					tLowLinks.put(tag, Math.min(tLowLinks.get(tag), tLowLinks.get(w)));
				} else if (tChecked.containsKey(w)) {
					tLowLinks.put(tag, Math.min(tLowLinks.get(tag), tIndices.get(w)));
				}
			}
		}

		if (tLowLinks.get(tag) == tIndices.get(tag)) {
			ArrayList<SNSemanticTag> component = new ArrayList<SNSemanticTag>();
			SNSemanticTag w = null;
			while (w != tag) {
				w = tStack.pop();
				tChecked.put(w, false);
				component.add(w);
			}

			tComponents.add(component);
		}
	}

	/**
	 * Checks if all the connections of predicate in the SemanticNet instance 
	 * form a transitive net.
	 *
	 * @param semanticNet A SemanticNet, probably with some SemanticTags that have some kind of connection
	 * @param predicate The predicate that should be checked.
	 *
	 * @return true if all connections of the Predicate are transitive, false if they are not
	 */
	public static boolean isTransitive(SemanticNet semanticNet, String predicate) throws SharkKBException {
		tChecked = new HashMap<SNSemanticTag, Boolean>();
		
		Iterator<SemanticTag> iter = semanticNet.stTags();
		
		while (iter.hasNext()) {
			SNSemanticTag tag = (SNSemanticTag) iter.next();
			Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
			
			if (!tChecked.containsKey(tag) && subTags != null) {
				ArrayList<SNSemanticTag> rootSubTags = new ArrayList<SNSemanticTag>();
				while (subTags.hasMoreElements()) {
					rootSubTags.add((SNSemanticTag) subTags.nextElement());
				}

				if (!transitive(tag, rootSubTags, predicate)) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean transitive(SNSemanticTag tag, ArrayList<SNSemanticTag> rootSubTags, String predicate) {
		tChecked.put(tag, true);

		Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
		if (subTags != null) {
			while(subTags.hasMoreElements()) {
				SNSemanticTag w = subTags.nextElement();

				// w is not in the root's sub tags
				if (!rootSubTags.contains(w)) {
					return false;
				}

				// w has no further children
				if (w.targetTags(predicate) == null) {
					return true;
				}

				return transitive(w, rootSubTags, predicate);
			}
		}
		return true;
	}
	
	/**
	 * @param semanticNet A SemanticNet, probably with some SemanticTags that have some kind of connection
	 * @param predicate The predicate that should be checked.
	 *
	 * @return true if all connections with the predicate are symmetric
	 */
	public static boolean isSymmetric(SemanticNet semanticNet, String predicate) throws SharkKBException {
		Iterator<SemanticTag> iter = semanticNet.stTags();
		
		while (iter.hasNext()) {
			SNSemanticTag tag = (SNSemanticTag) iter.next();
			
			Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
			
			if (subTags != null) {
				while (subTags.hasMoreElements()) {
					Enumeration<SNSemanticTag> deeperSubTags = subTags.nextElement().targetTags(predicate);
					
					if (deeperSubTags != null) {
						ArrayList<SNSemanticTag> deeperSubTagsList = new ArrayList<>();
						while (deeperSubTags.hasMoreElements()) {
							deeperSubTagsList.add(deeperSubTags.nextElement());
						}
						
						if (!deeperSubTagsList.contains(tag)) {
							return false;
						}
					}
				}

			}
		}

		return true;
	}
	
	/**
	 * Changes the SemanticNet to make all the connections of predicate symmetric,
	 * that means that every connection that previously only went from A -> B, now also
	 * goes from B -> A.
	 * (The SemanticNet is then guaranteed to pass .isSymmetric for the same predicate.)
	 *
	 * @param semanticNet A SemanticNet, probably with some SemanticTags that have some kind of connection
	 * @param predicate The predicate that should be checked.
	 */
	public static void makeSymmetric(SemanticNet semanticNet, String predicate) throws SharkKBException {
		Iterator<SemanticTag> iter = semanticNet.stTags();
		
		while (iter.hasNext()) {
			SNSemanticTag tag = (SNSemanticTag) iter.next();
			Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
			
			if (subTags != null) {
				while (subTags.hasMoreElements()) {
					subTags.nextElement().setPredicate(predicate, tag);
				}

			}
		}
	}
	
	/**
	 * Changes the SemanticNet to make all the connections of predicate transitive.
	 * (The SemanticNet is then guaranteed to pass .isTransitive for the same predicate.)
	 *
	 * @param semanticNet A SemanticNet, probably with some SemanticTags that have some kind of connection
	 * @param predicate The predicate that should be checked.
	 */
	public static void makeTransitive(SemanticNet semanticNet, String predicate) throws SharkKBException {
		tChecked = new HashMap<SNSemanticTag, Boolean>();
		
		Iterator<SemanticTag> iter = semanticNet.stTags();
		
		while (iter.hasNext()) {
			SNSemanticTag tag = (SNSemanticTag) iter.next();
			Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
			
			if (subTags != null) {
				ArrayList<SNSemanticTag> rootSubTags = new ArrayList<SNSemanticTag>();
				while (subTags.hasMoreElements()) {
					rootSubTags.add((SNSemanticTag) subTags.nextElement());
				}

				makeTransitiveHelper(tag, tag, rootSubTags, predicate);
			}
		}
	}

	private static void makeTransitiveHelper(SNSemanticTag tag, SNSemanticTag rootTag, ArrayList<SNSemanticTag> rootSubTags, String predicate) {
		Enumeration<SNSemanticTag> subTags = tag.targetTags(predicate);
		if (subTags != null) {
			while(subTags.hasMoreElements()) {
				SNSemanticTag w = subTags.nextElement();

				// w is not in the root's sub tags
				if (!rootSubTags.contains(w)) {
					rootTag.setPredicate(predicate, w);
				}

				// w has no further children
				if (w.targetTags(predicate) == null) {
					return;
				}

				makeTransitiveHelper(w, rootTag, rootSubTags, predicate);
			}
		}
	}

}
