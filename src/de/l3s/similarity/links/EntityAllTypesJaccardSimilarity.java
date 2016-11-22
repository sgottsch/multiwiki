package de.l3s.similarity.links;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.similarity.CollectionSimilarity;
import de.l3s.similarity.SimilarityType;

public class EntityAllTypesJaccardSimilarity extends CollectionSimilarity {

	public String getName() {
		return "EntityAllTypesJaccardSimilarity";
	}

	public String getEasyName() {
		return "Entities";
	}
	
	public SimilarityType getSimilarityType() {
		return SimilarityType.EntityAllTypesJaccardSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		List<String> entities1 = annotation1.getDictionaryNGrams();
		List<String> entities2 = annotation2.getDictionaryNGrams();

		double similarity;

		if (entities1.size() == 0 || entities2.size() == 0)
			similarity = 0.0;
		else {
			similarity = getJaccardSimilarity2(entities1, entities2);
			if (similarity == Double.NaN)
				similarity = 0;
		}

		// if (similarity > 0) {
		// System.out.println(StringUtils.join(entities1, ","));
		// System.out.println(StringUtils.join(entities2, ","));
		// System.out.println("=> " + similarity);
		// System.out.println("");
		// }

		return similarity;
	}

	// /**
	// * Computes Jaccard coefficient between the sets of the keyword-attribute
	// * combinations contained in the both queries keywords not occurring in
	// the
	// * query are bound to a zero-attribute
	// *
	// * @return 0 for dissimilar queries
	// * @return 1 for the highest possible similarity
	// */
	// private static double getJaccardSimilarity(List<String> text_a,
	// List<String> text_b) {
	//
	// // determine the query with the bigger number of interpretations
	// List<String> combi_big = text_a.size() >= text_b.size() ? text_a :
	// text_b;
	//
	// // determine the query with the smaller number of interpretations
	// List<String> combi_small = text_a.size() >= text_b.size() ? text_b :
	// text_a;
	//
	// // build intersection
	// Set<String> intersection = new HashSet<String>();
	// // go through the smaller set of interpretations
	// for (String keyword : combi_small) {
	// // interpretation of the keyword in smaller set
	// if (combi_big.contains(keyword)) {
	// intersection.add(keyword);
	// }
	//
	// }
	// // build disjunction
	// Set<String> disjunction = new HashSet<String>();
	// disjunction.addAll(combi_small);
	// disjunction.addAll(combi_big);
	//
	// // Build an intersection A AND B, compute the size
	// // Build a disjunction A ODER B, compute the size
	// // JC= (A AND B) / (A ODER B)
	// double intersection_size = intersection.size();
	// double disjunction_size = disjunction.size();
	//
	// // String s1 = q1.toFullString();
	// // String s2 = q2.toFullString();
	//
	// double JC = intersection_size / disjunction_size;
	//
	// /*
	// * System.out.println("query 1: " + q1.toFullString());
	// * System.out.println("query 2: " + q2.toFullString());
	// * System.out.println("AND Size: " + intersection.keySet().size());
	// * System.out.println("OR Size: " + disjunction.keySet().size());
	// * System.out.println("JC: " + JC + "\n");
	// */
	//
	// return JC;
	// }

	/**
	 * Computes Jaccard coefficient between the sets of the keyword-attribute
	 * combinations contained in the both queries keywords not occurring in the
	 * query are bound to a zero-attribute
	 * 
	 * @return 0 for dissimilar queries
	 * @return 1 for the highest possible similarity
	 */
	private static double getJaccardSimilarity2(List<String> text_a, List<String> text_b) {

		// build intersection
		Set<String> intersection = new HashSet<String>();
		intersection.addAll(text_a);
		intersection.retainAll(text_b);

		double intersection_size = intersection.size();
		if (intersection_size == 0)
			return 0;

		// build disjunction
		Set<String> disjunction = new HashSet<String>();
		disjunction.addAll(text_a);
		disjunction.addAll(text_b);

		double JC = intersection_size / disjunction.size();

		return JC;
	}

}
