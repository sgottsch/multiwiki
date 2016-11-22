package de.l3s.model;

import java.util.Comparator;

import de.l3s.algorithms.ConditionWeightConfig;
import de.l3s.algorithms.SimilarityFunctionLinear;

/**
 * Comparator class that sorts annotations descending according to their
 * similarity w.r.t. to a similarity function.
 */
public class SentencePairComparator implements Comparator<SentencePair> {

	ConditionWeightConfig conditionWeightConfig;

	public SentencePairComparator() {
	}

	public SentencePairComparator(ConditionWeightConfig weightConfig) {
		this.conditionWeightConfig = weightConfig;
	}

	@Override
	public int compare(SentencePair ap1, SentencePair ap2) {

		Double sim1;
		Double sim2;

		if (this.conditionWeightConfig != null) {
			SimilarityFunctionLinear weightConfig1 = this.conditionWeightConfig.getWeightConfig(ap1);
			SimilarityFunctionLinear weightConfig2 = this.conditionWeightConfig.getWeightConfig(ap2);

			sim1 = ap1.getSimilarity(weightConfig1);
			sim2 = ap2.getSimilarity(weightConfig2);
		} else {
			sim1 = ap1.getSimilarity();
			sim2 = ap2.getSimilarity();
		}
		return sim2.compareTo(sim1);
	}
}
