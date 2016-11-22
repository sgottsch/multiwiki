package model;

import java.util.Comparator;

import algorithms.ConditionWeightConfig;

/**
 * Comparator class that sorts annotation pairs descending according to their
 * paragraph similarity that was computed during the paragraph alignment
 * process.
 */
public class SentencePairParagraphLengthComparator implements Comparator<SentencePair> {
	private ConditionWeightConfig weightConfig;

	public SentencePairParagraphLengthComparator(ConditionWeightConfig weightConfig) {
		this.weightConfig = weightConfig;
	}

	@Override
	public int compare(SentencePair ap1, SentencePair ap2) {
		int size1 = ap1.getSentence1().getAllSubAnnotations().size() + ap1.getSentence2().getAllSubAnnotations().size();
		int size2 = ap2.getSentence1().getAllSubAnnotations().size() + ap2.getSentence2().getAllSubAnnotations().size();
		Double sim1 = ap1.getParagraphSimilarity();
		Double sim2 = ap2.getParagraphSimilarity();
		if (sim1 == null) {
			sim1 = ap1.getSimilarity(this.weightConfig);
		}
		if (sim2 == null) {
			sim2 = ap2.getSimilarity(this.weightConfig);
		}
		if (size1 > size2) {
			return -1;
		}
		if (size1 < size2) {
			return 1;
		}
		return sim2.compareTo(sim1);
	}
}
