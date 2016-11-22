package de.l3s.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class TextUtil {
	
	/**
	 * Computes Jaccard coefficient between the sets of the keyword-attribute
	 * combinations contained in the both queries keywords not occurring in the
	 * query are bound to a zero-attribute
	 * 
	 * @return 0 for dissimilar queries
	 * @return 1 for the highest possible similarity
	 */
	public static double getJaccardSimilarity(List<String> text_a, List<String> text_b) {

		// determine the query with the bigger number of interpretations
		List<String> combi_big = text_a.size() >= text_b.size() ? text_a : text_b;

		// determine the query with the smaller number of interpretations
		List<String> combi_small = text_a.size() >= text_b.size() ? text_b : text_a;

		// build intersection
		Set<String> intersection = new HashSet<String>();
		// go through the smaller set of interpretations
		for (String keyword : combi_small) {
			// interpretation of the keyword in smaller set
			if (combi_big.contains(keyword)) {
				intersection.add(keyword);
			}

		}
		// build disjunction
		Set<String> disjunction = new HashSet<String>();
		disjunction.addAll(combi_small);
		disjunction.addAll(combi_big);

		// Build an intersection A AND B, compute the size
		// Build a disjunction A ODER B, compute the size
		// JC= (A AND B) / (A ODER B)
		double intersection_size = intersection.size();
		double disjunction_size = disjunction.size();

		// String s1 = q1.toFullString();
		// String s2 = q2.toFullString();

		double JC = intersection_size / disjunction_size;

		/*
		 * System.out.println("query 1: " + q1.toFullString());
		 * System.out.println("query 2: " + q2.toFullString());
		 * System.out.println("AND Size: " + intersection.keySet().size());
		 * System.out.println("OR Size: " + disjunction.keySet().size());
		 * System.out.println("JC: " + JC + "\n");
		 */

		return JC;
	}
	
	public static double getJaccardSimilarity(Set<String> text_a, Set<String> text_b) {

		// determine the query with the bigger number of interpretations
		Set<String> combi_big = text_a.size() >= text_b.size() ? text_a : text_b;

		// determine the query with the smaller number of interpretations
		Set<String> combi_small = text_a.size() >= text_b.size() ? text_b : text_a;

		// build intersection
		Set<String> intersection = new HashSet<String>();
		// go through the smaller set of interpretations
		for (String keyword : combi_small) {
			// interpretation of the keyword in smaller set
			if (combi_big.contains(keyword)) {
				intersection.add(keyword);
			}

		}
		// build disjunction
		Set<String> disjunction = new HashSet<String>();
		disjunction.addAll(combi_small);
		disjunction.addAll(combi_big);

		// Build an intersection A AND B, compute the size
		// Build a disjunction A ODER B, compute the size
		// JC= (A AND B) / (A ODER B)
		double intersection_size = intersection.size();
		double disjunction_size = disjunction.size();

		// String s1 = q1.toFullString();
		// String s2 = q2.toFullString();

		double JC = intersection_size / disjunction_size;

		/*
		 * System.out.println("query 1: " + q1.toFullString());
		 * System.out.println("query 2: " + q2.toFullString());
		 * System.out.println("AND Size: " + intersection.keySet().size());
		 * System.out.println("OR Size: " + disjunction.keySet().size());
		 * System.out.println("JC: " + JC + "\n");
		 */

		return JC;
	}
	
	public static String removeStopWords(String text) throws IOException {
		CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
		TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_48, new StringReader(text.trim()));

		tokenStream = new StopFilter(Version.LUCENE_48, tokenStream, stopWords);
		StringBuilder sb = new StringBuilder();
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			String term = charTermAttribute.toString();
			sb.append(term + " ");
		}
		tokenStream.close();
		return sb.toString();
	}

}
