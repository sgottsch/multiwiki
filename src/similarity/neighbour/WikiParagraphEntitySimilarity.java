package similarity.neighbour;

import java.util.HashSet;
import java.util.Set;

import model.BinaryComparison;
import model.Sentence;
import model.WikiParagraph;
import similarity.CollectionSimilarity;
import similarity.SimilarityType;
import tfidf.CollectionType;
import tfidf.TfIdfCollection;

public class WikiParagraphEntitySimilarity extends CollectionSimilarity {

	public String getName() {
		return "WikiParagraphEntitySimilarity";
	}

	public String getEasyName() {
		return "Paragraph Entities";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.WikiParagraphEntitySimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {
		WikiParagraph paragraph1 = annotation1.getParagraph();
		WikiParagraph paragraph2 = annotation2.getParagraph();

		if (paragraph1 == null || paragraph2 == null)
			return 0.5;

		return calculateSimilarity2(comparison, paragraph1, paragraph2);
	}

	public double calculateSimilarity2(BinaryComparison comparison, WikiParagraph paragraph1,
			WikiParagraph paragraph2) {
		TfIdfCollection entityCollection = comparison.getTfIdfCollection(CollectionType.Entity);

		Set<Sentence> sentences1 = new HashSet<Sentence>();
		sentences1.addAll(paragraph1.getAnnotations());

		Set<Sentence> sentences2 = new HashSet<Sentence>();
		sentences2.addAll(paragraph2.getAnnotations());

		return calculateSimilarity(CollectionType.Entity, entityCollection, comparison, sentences1, sentences2, true);
	}

}
