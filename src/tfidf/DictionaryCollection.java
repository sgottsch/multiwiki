package tfidf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.util.StringUtils;
import model.BinaryComparison;
import model.Revision;
import model.Sentence;

public class DictionaryCollection extends TfIdfCollection implements Collection {

	public DictionaryCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		super(comparison, revision1, revision2);
	}

	@Override
	String createTerms(Sentence sentence, Revision revision) {
		if (sentence.getDictionaryTranslations() != null) {
			List<String> words = new ArrayList<String>();
			for (Set<String> strings : sentence.getDictionaryTranslations()) {
				words.addAll(strings);
			}
			return StringUtils.join(words, (String) " ");
		}
		return StringUtils.join(sentence.getSnowballStemmedWords(), (String) " ");
	}

	@Override
	String getTermName() {
		return "dictionariwords";
	}

	@Override
	CollectionType getCollectionType() {
		return CollectionType.Dictionary;
	}
}
