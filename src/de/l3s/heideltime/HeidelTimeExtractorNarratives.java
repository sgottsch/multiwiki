package de.l3s.heideltime;

import java.util.Set;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import de.unihd.dbs.uima.types.heideltime.Timex3Interval;

public class HeidelTimeExtractorNarratives extends HeidelTimeExtractor {

	public HeidelTimeExtractorNarratives(Language language) {
		super(language, DocumentType.NARRATIVES);
	}
	
	public Set<Timex3Interval> getTimeExpressions(String text) {
		return super.processText(text);
	}
	
}
