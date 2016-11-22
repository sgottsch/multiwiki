package de.l3s.similarity.neighbour;

import de.l3s.model.BinaryComparison;
import de.l3s.model.SentencePair;
import de.l3s.similarity.Similarity;

public abstract class NeighbourSimilarity implements Similarity {

	public double calculateSimilarity(BinaryComparison comparison, SentencePair ap) {
		return calculateSimilarity(comparison, ap.getSentence1(), ap.getSentence2());
	}

	// Every annotation has a text -> TextSimilarities are always applicable
	public boolean isApplicable() {
		return true;
	}

}