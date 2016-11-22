package de.l3s.similarity.text;

import java.util.HashSet;
import java.util.Set;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.similarity.SimilarityType;
import de.l3s.util.TextUtil;

public class DictionaryOverlapSimilarity extends TextSimilarity {

	public String getName() {
		return "DictionaryOverlapSimilarity";
	}

	public String getEasyName() {
		return "Dictionary Overlap";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.DictionaryOverlapSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence sentence1, Sentence sentence2) {

		Sentence sentenceInTargetLanguage = sentence1;
		Sentence sentenceInOriginalLanguage = sentence2;

		if (sentence1.getDictionaryTranslations() != null) {
			sentenceInTargetLanguage = sentence2;
			sentenceInOriginalLanguage = sentence1;
		}

		Set<String> uniqueWords1 = new HashSet<String>();
		Set<String> uniqueWords2 = new HashSet<String>();
		uniqueWords2.addAll(sentenceInTargetLanguage.getSnowballStemmedWords());

		for (Set<String> translatedWords : sentenceInOriginalLanguage.getDictionaryTranslations()) {
			boolean found = false;
			String randomTranslatedWord = null;
			for (String translatedWord : translatedWords) {
				randomTranslatedWord = translatedWord;
				if (sentenceInTargetLanguage.getSnowballStemmedWords().contains(translatedWord)) {
					uniqueWords1.add(translatedWord);
					found = true;
					break;
				}
			}
			// insert any of the words, if it was not found
			if (!found)
				uniqueWords1.add(randomTranslatedWord);

		}

		// System.out.println("un1: " + uniqueWords1);
		// System.out.println("un2: " + uniqueWords2);

		return TextUtil.getJaccardSimilarity(uniqueWords1, uniqueWords2);
	}

}
