package de.l3s.tfidf;

import java.util.ArrayList;
import java.util.List;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.times.HeidelIntervalString;
import edu.stanford.nlp.util.StringUtils;

public class HeidelTimeCollection extends TfIdfCollection implements Collection {

	public HeidelTimeCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		super(comparison, revision1, revision2);
	}

	String createTerms(Sentence sentence, Revision revision) {
		List<String> strings = new ArrayList<String>();
		for (HeidelIntervalString heidelInterval : sentence.getHeidelIntervals()) {
			strings.add(heidelInterval.getCompleteIntervalStringRepresentation());
		}
		return StringUtils.join(strings, (String) " ");
	}

	String getTermName() {
		return "times";
	}

	CollectionType getCollectionType() {
		return CollectionType.HeidelTime;
	}
}
