package similarity.links;

import model.BinaryComparison;
import model.Sentence;
import similarity.CollectionSimilarity;
import similarity.SimilarityType;
import tfidf.CollectionType;
import tfidf.TfIdfCollection;

public class EntitySimilarity extends CollectionSimilarity {

	public String getName() {
		return "EntitySimilarity";
	}

	public String getEasyName() {
		return "Entities";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.EntitySimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence sentence1, Sentence sentence2) {

		TfIdfCollection entityCollection = comparison.getTfIdfCollection(CollectionType.Entity);

		if (sentence1.getEntityAllString().isEmpty() && sentence2.getEntityAllString().isEmpty()) {
			this.isApplicable = false;
			return 0.0;
		}

		double similarity = this.calculateSimilarity(CollectionType.Entity, entityCollection, comparison, sentence1,
				sentence2);

		return similarity;
	}

}
