package de.l3s.similarity.text;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.similarity.SimilarityType;
import de.l3s.translate.Language;

public class TextLengthSimilarity extends TextSimilarity {

	// private static final double LENGTH_FACTOR_EN = 1.0;

	private static final double LENGTH_FACTOR_DE = 285329860d / 320986580d;
	// German length: 320986580, Words: 44415672, Bytes: 328463491
	// English length: 285329860, Words: 47882402, Bytes: 287250069

	private static final double LENGTH_FACTOR_NL = 297161728d / 327409138d;
	// Dutch length: , Words: , Bytes: 327409138
	// English length: , Words: , Bytes: 297161728

	private static final double LENGTH_FACTOR_PT = 295674971d / 328095003d;

	// Portuguese length: , Words: , Bytes: 328095003
	// English length: , Words: , Bytes: 295674971

	// TODO: Real lengths

	public String getName() {
		return "TextLengthSimilarity";
	}
	
	public String getEasyName() {
		return "Length";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.TextLengthSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		double length1 = annotation1.getRawText().length();

		int length2 = annotation2.getRawText().length();

		if (annotation1.getLanguage() == Language.DE)
			length1 *= LENGTH_FACTOR_DE;
		if (annotation1.getLanguage() == Language.NL)
			length1 *= LENGTH_FACTOR_NL;
		if (annotation1.getLanguage() == Language.PT)
			length1 *= LENGTH_FACTOR_PT;

		if (length1 < length2)
			return length1 / length2;
		else
			return length2 / length1;

	}

}
