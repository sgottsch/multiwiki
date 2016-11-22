package similarity;

import model.Sentence;
import model.SentencePair;
import model.BinaryComparison;

public interface Similarity {

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2);

	public double calculateSimilarity(BinaryComparison comparison, SentencePair ap);
	
	public String getName();
	
	public String getEasyName();

	public SimilarityType getSimilarityType();

	public boolean isApplicable();

}