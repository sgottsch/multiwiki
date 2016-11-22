package similarity;

import model.Sentence;
import model.BinaryComparison;
import similarity.text.TextSimilarity;

public class RelativeDocumentPositionSimilarity extends TextSimilarity {

	public String getName() {
		return "RelativeDocumentPositionSimilarity";
	}

	public String getEasyName() {
		return "Text Position";
	}
	
	public SimilarityType getSimilarityType() {
		return SimilarityType.RelativeDocumentPositionSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		return 1 - Math.abs(annotation1.getRelativePosition() - annotation2.getRelativePosition());

	}

}
