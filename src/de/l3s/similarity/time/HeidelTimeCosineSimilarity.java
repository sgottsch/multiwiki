package de.l3s.similarity.time;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.similarity.CollectionSimilarity;
import de.l3s.similarity.SimilarityType;
import de.l3s.tfidf.CollectionType;
import de.l3s.tfidf.TfIdfCollection;

public class HeidelTimeCosineSimilarity extends CollectionSimilarity {

	public String getName() {
		return "HeidelTimeCosineSimilarity";
	}

	public String getEasyName() {
		return "Heidel Times";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.HeidelTimeCosineSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {
		TfIdfCollection heidelTimeCollection = comparison.getTfIdfCollection(CollectionType.HeidelTime);

		if (annotation1.getHeidelIntervals() == null || annotation1.getHeidelIntervals().isEmpty()
				&& (annotation2.getHeidelIntervals() == null || annotation2.getHeidelIntervals().isEmpty())) {
			this.isApplicable = false;
			return 0.0;
		}

		return this.calculateSimilarity(CollectionType.HeidelTime, heidelTimeCollection, comparison, annotation1,
				annotation2, false);
	}
}
