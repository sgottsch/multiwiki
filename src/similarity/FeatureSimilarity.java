package similarity;

import model.BinaryComparison;
import model.SentencePair;

public abstract class FeatureSimilarity implements Similarity {

	public double calculateSimilarity(BinaryComparison comparison, SentencePair ap) {
		return calculateSimilarity(comparison, ap.getSentence1(), ap.getSentence2());
	}

	protected boolean isApplicable = true;

	public boolean isApplicable() {
		return SimilarityConfiguration.getInstance().alwaysApplicable || this.isApplicable;
	}

}