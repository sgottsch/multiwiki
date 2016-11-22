package de.l3s.similarity;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.model.SentencePair;

public interface Similarity {

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2);

	public double calculateSimilarity(BinaryComparison comparison, SentencePair ap);
	
	public String getName();
	
	public String getEasyName();

	public SimilarityType getSimilarityType();

	public boolean isApplicable();

}