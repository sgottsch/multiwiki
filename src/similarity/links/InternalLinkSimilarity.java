package similarity.links;

import org.apache.commons.math3.linear.RealVector;

import model.BinaryComparison;
import model.Sentence;
import similarity.FeatureSimilarity;
import similarity.SimilarityType;
import tfidf.CollectionType;
import tfidf.TfIdfCollection;

public class InternalLinkSimilarity extends FeatureSimilarity {

	public String getName() {
		return "InternalLinkSimilarity";
	}

	public String getEasyName() {
		return "Internal Links";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.InternalLinkSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		TfIdfCollection entityCollection = comparison.getTfIdfCollection(CollectionType.Internal);

		RealVector idfVector = entityCollection.getIdfVector();
		RealVector tf1Vector = annotation1.getTermVector(CollectionType.Internal);
		RealVector tf2Vector = annotation2.getTermVector(CollectionType.Internal);

		if (tf1Vector == null || tf2Vector == null) {
			this.isApplicable = false;
			return 0.0;
		}

		this.isApplicable = true;

		RealVector w1Vector = idfVector.ebeMultiply(tf1Vector);
		RealVector w2Vector = idfVector.ebeMultiply(tf2Vector);

		RealVector product = w1Vector.ebeMultiply(w2Vector);

		product = product.ebeMultiply(entityCollection.getDfNormalizer());

		double nom = product.getL1Norm();

		if (nom == 0.0)
			return 0.0;

		double denom = w1Vector.getNorm() * w2Vector.getNorm();

		// System.out.println(annotation1.getRawText());
		// System.out.println(annotation2.getRawText());
		// System.out.println("nom: " + nom);
		// System.out.println("denom: " + denom);

		double similarity = nom / denom;

		return Math.min(1, similarity);
	}
	// public double calculateSimilarityOld(Annotation annotation1, Annotation
	// annotation2) {
	//
	// Language language1 = annotation1.getLanguage();
	// Language language2 = annotation2.getLanguage();
	//
	// Set<String> linksInLanguage1InBothlanguagesOfAnnotation1 = new
	// HashSet<String>();
	// Set<String> linksInLanguage1InBothlanguagesOfAnnotation2 = new
	// HashSet<String>();
	//
	// for (DbPediaLink il : annotation1.getDbPediaLinks()) {
	// if (linkExistsInBothLanguages(il, language1, language2)) {
	// linksInLanguage1InBothlanguagesOfAnnotation1.add(il.getLink(language1));
	// }
	// }
	//
	// for (DbPediaLink il : annotation2.getDbPediaLinks()) {
	// if (linkExistsInBothLanguages(il, language1, language2)) {
	// linksInLanguage1InBothlanguagesOfAnnotation2.add(il.getLink(language1));
	// }
	// }
	//
	// double numberOfInternalLinks1 =
	// linksInLanguage1InBothlanguagesOfAnnotation1.size();
	// int numberOfInternalLinks2 =
	// linksInLanguage1InBothlanguagesOfAnnotation2.size();
	//
	// linksInLanguage1InBothlanguagesOfAnnotation1.retainAll(linksInLanguage1InBothlanguagesOfAnnotation2);
	//
	// double numberOfCommonInternalLinks =
	// linksInLanguage1InBothlanguagesOfAnnotation1.size();
	//
	// if (numberOfInternalLinks1 == 0 && numberOfInternalLinks2 == 0)
	// return 0;
	//
	// // TODO: Don't return 0
	// if (numberOfInternalLinks1 == 0 || numberOfInternalLinks2 == 0)
	// return 0;
	//
	// double similarity = numberOfCommonInternalLinks /
	// Math.min(numberOfInternalLinks1, numberOfInternalLinks2);
	//
	// if (similarity > 0) {
	// System.out.println("Annotations have the following common links
	// (InternalLinkSimilarity "
	// + similarity
	// + "):");
	// for (String link : linksInLanguage1InBothlanguagesOfAnnotation1)
	// System.out.println(link);
	// }
	//
	// return similarity;
	// }
	//
	// private static boolean linkExistsInBothLanguagesOld(DbPediaLink il,
	// Language language1, Language language2) {
	// return (!il.getLink(language1).equals("-") &&
	// !il.getLink(language2).equals("-"));
	// }

}
