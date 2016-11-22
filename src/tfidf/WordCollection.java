package tfidf;

import model.BinaryComparison;
import model.Revision;
import model.Sentence;

public class WordCollection extends TfIdfCollection implements Collection {

	public WordCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		super(comparison, revision1, revision2);
	}

	@Override
	String createTerms(Sentence sentence, Revision revision) {
		return sentence.getEnglishStemmedTextConcatenated();
	}

	@Override
	String getTermName() {
		return "words";
	}

	@Override
	CollectionType getCollectionType() {
		return CollectionType.Words;
	}
}
