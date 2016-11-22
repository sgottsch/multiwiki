package de.l3s.model;

import java.util.Comparator;

import de.l3s.algorithms.ConditionWeightConfig;

/**
 * Comparator class that sorts annotation pairs descending according to their
 * paragraph similarity that was computed during the paragraph alignment
 * process.
 */
public class SentencePairParagraphComparator implements Comparator<SentencePair> {
	private ConditionWeightConfig weightConfig;

	public SentencePairParagraphComparator(ConditionWeightConfig weightConfig) {
		this.weightConfig = weightConfig;
	}

	@Override
	public int compare(SentencePair ap1, SentencePair ap2) {
		Double sim1 = ap1.getParagraphSimilarity();
		Double sim2 = ap2.getParagraphSimilarity();
		if (sim1 == null) {
			sim1 = ap1.getSimilarity(this.weightConfig);
		}
		if (sim2 == null) {
			sim2 = ap2.getSimilarity(this.weightConfig);
		}
		return sim2.compareTo(sim1);
	}
}
