package de.l3s.similarity.time;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.model.TimeInterval;
import de.l3s.similarity.FeatureSimilarity;
import de.l3s.similarity.SimilarityType;

public class ParagraphTimeSimilarity extends FeatureSimilarity {

	public ParagraphTimeSimilarity() {
		super();
	}

	public String getName() {
		return "ParagraphTimeSimilarity";
	}

	public String getEasyName() {
		return "ParagraphTime";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.ParagraphTimeSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		TimeInterval timeInterval1 = annotation1.getParagraph().getTimeInterval();
		TimeInterval timeInterval2 = annotation2.getParagraph().getTimeInterval();

		if (timeInterval1 == null && timeInterval2 == null)
			return 0.5;

		if (timeInterval1 == null || timeInterval2 == null)
			return 0;

		if (timeInterval1.overlaps(timeInterval2))
			return 1;
		else
			return 0;
	}

}
