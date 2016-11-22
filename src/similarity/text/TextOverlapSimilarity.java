package similarity.text;

import model.BinaryComparison;
import model.Sentence;
import similarity.SimilarityType;
import util.TextUtil;

public class TextOverlapSimilarity extends TextSimilarity {

	public String getName() {
		return "TextOverlapSimilarity";
	}
	
	public String getEasyName() {
		return "Text Overlap";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.TextOverlapSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {
		return TextUtil.getJaccardSimilarity(annotation1.getEnglishStemmedText(), annotation2.getEnglishStemmedText());
	}

}
