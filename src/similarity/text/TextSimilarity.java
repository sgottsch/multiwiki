package similarity.text;

import model.SentencePair;
import model.BinaryComparison;
import similarity.Similarity;

public abstract class TextSimilarity implements Similarity {

	public double calculateSimilarity(BinaryComparison comparison, SentencePair ap) {
		return calculateSimilarity(comparison, ap.getSentence1(), ap.getSentence2());
	}

	// Every annotation has a text -> TextSimilarities are always applicable
	public boolean isApplicable() {
		return true;
	}

}