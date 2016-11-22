package de.l3s.similarity.text;

import org.apache.commons.math3.linear.RealVector;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.similarity.SimilarityType;
import de.l3s.tfidf.CollectionType;
import de.l3s.tfidf.TfIdfCollection;

public class DictionaryCosineSimilarity extends TextSimilarity {

	public String getName() {
		return "DictionaryCosineSimilarity";
	}

	public String getEasyName() {
		return "Dictionary (Cosine)";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.DictionaryCosineSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		TfIdfCollection wordCollection = comparison.getTfIdfCollection(CollectionType.Dictionary);

		RealVector idfVector = wordCollection.getIdfVector();
		RealVector tf1Vector = annotation1.getTermVector(CollectionType.Dictionary);
		RealVector tf2Vector = annotation2.getTermVector(CollectionType.Dictionary);

		if (tf1Vector == null || tf2Vector == null)
			return 0.0;

		RealVector w1Vector = idfVector.ebeMultiply(tf1Vector);
		RealVector w2Vector = idfVector.ebeMultiply(tf2Vector);

		double nom = (w1Vector.ebeMultiply(w2Vector)).getL1Norm();

		if (nom == 0.0)
			return 0.0;

		double denom = w1Vector.getNorm() * w2Vector.getNorm();
		double similarity = nom / denom;

		// System.out.println("Word Cosine similarity: " + similarity);

		return similarity;
	}

}
