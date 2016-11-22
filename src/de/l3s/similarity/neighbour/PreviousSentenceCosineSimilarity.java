package de.l3s.similarity.neighbour;

import org.apache.commons.math3.linear.RealVector;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.similarity.SimilarityType;
import de.l3s.tfidf.CollectionType;
import de.l3s.tfidf.TfIdfCollection;

public class PreviousSentenceCosineSimilarity extends NeighbourSimilarity {

	public String getName() {
		return "PreviousSentenceCosineSimilarity";
	}

	public String getEasyName() {
		return "Previous (Cosine)";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.PreviousSentenceCosineSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {
		Sentence sentenceBefore1 = annotation1.getPreSentence();
		Sentence sentenceBefore2 = annotation2.getPreSentence();

		if (sentenceBefore1 == null || sentenceBefore2 == null)
			return 0.5;

		return calculateSimilarity2(comparison, sentenceBefore1, sentenceBefore2);
	}

	public double calculateSimilarity2(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		TfIdfCollection wordCollection = comparison.getTfIdfCollection(CollectionType.Words);

		RealVector idfVector = wordCollection.getIdfVector();
		RealVector tf1Vector = annotation1.getTermVector(CollectionType.Words);
		RealVector tf2Vector = annotation2.getTermVector(CollectionType.Words);

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
