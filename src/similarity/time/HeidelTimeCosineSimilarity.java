package similarity.time;

import model.BinaryComparison;
import model.Sentence;
import similarity.CollectionSimilarity;
import similarity.SimilarityType;
import tfidf.CollectionType;
import tfidf.TfIdfCollection;

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
