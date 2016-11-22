package similarity.links;

import org.apache.commons.math3.linear.RealVector;

import model.BinaryComparison;
import model.Sentence;
import similarity.FeatureSimilarity;
import similarity.SimilarityType;
import tfidf.CollectionType;
import tfidf.TfIdfCollection;

public class ExternalLinkSimilarity extends FeatureSimilarity {

	public String getName() {
		return "ExternalLinkSimilarity";
	}
	
	public String getEasyName() {
		return "External Links";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.ExternalLinkSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		TfIdfCollection externalLinkCollection = comparison.getTfIdfCollection(CollectionType.ExternalLinkCollection);

		RealVector idfVector = externalLinkCollection.getIdfVector();
		RealVector tf1Vector = annotation1.getTermVector(CollectionType.ExternalLinkCollection);
		RealVector tf2Vector = annotation2.getTermVector(CollectionType.ExternalLinkCollection);

		if (tf1Vector == null || tf2Vector == null) {
			this.isApplicable = false;
			return 0.0;
		}
		
		this.isApplicable = true;

		RealVector w1Vector = idfVector.ebeMultiply(tf1Vector);
		RealVector w2Vector = idfVector.ebeMultiply(tf2Vector);

		RealVector product = w1Vector.ebeMultiply(w2Vector);

		product = product.ebeMultiply(externalLinkCollection.getDfNormalizer());

		double nom = product.getL1Norm();

		if (nom == 0.0)
			return 0.0;

		double denom = w1Vector.getNorm() * w2Vector.getNorm();

		// System.out.println(annotation1.getRawText());
		// System.out.println(annotation2.getRawText());
		// System.out.println("nom: " + nom);
		// System.out.println("denom: " + denom);

		double similarity = nom / denom;

		ExternalLinkHostSimilarity hostSimilarityFinder = new ExternalLinkHostSimilarity();
		double hostSimilarity = hostSimilarityFinder.calculateSimilarity(comparison, annotation1, annotation2);

		similarity = 0.8 * similarity + 0.2 * hostSimilarity;

		return Math.min(1, similarity);
	}

}