package de.l3s.similarity;

import de.l3s.model.BinaryComparison;
import de.l3s.model.SentencePair;

public abstract class FeatureSimilarity implements Similarity {

	public double calculateSimilarity(BinaryComparison comparison, SentencePair ap) {
		return calculateSimilarity(comparison, ap.getSentence1(), ap.getSentence2());
	}

	protected boolean isApplicable = true;

	public boolean isApplicable() {
		return SimilarityConfiguration.getInstance().alwaysApplicable || this.isApplicable;
	}

}