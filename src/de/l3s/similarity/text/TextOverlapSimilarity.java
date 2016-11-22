package de.l3s.similarity.text;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.similarity.SimilarityType;
import de.l3s.util.TextUtil;

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
