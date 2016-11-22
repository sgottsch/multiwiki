package similarity.text;

import java.util.ArrayList;
import java.util.List;

import model.Sentence;
import model.BinaryComparison;
import similarity.SimilarityType;

public class NGramSimilarity extends TextSimilarity {

	public String getName() {
		return "NGramSimilarity";
	}
	
	public String getEasyName() {
		return "NGrams";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.NGramSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		List<String> nGrams1 = annotation1.getNGrams();
		List<String> nGrams2 = annotation2.getNGrams();

		if (nGrams1.size() == 0 || nGrams2.size() == 0)
			return 0;

		List<String> commonNGrams = new ArrayList<String>();

		commonNGrams.addAll(nGrams1);
		commonNGrams.retainAll(nGrams2);

		double similarity = (2d * commonNGrams.size()) / (nGrams1.size() + nGrams2.size());

		return similarity;
	}

}
