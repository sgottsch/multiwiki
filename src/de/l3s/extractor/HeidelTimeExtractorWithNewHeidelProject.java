package de.l3s.extractor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.heideltime.HeidelTimeExtractor;
import de.l3s.heideltime.HeidelTimeExtractorNarratives;
import de.l3s.heideltime.HeidelTimeExtractorNews;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.SentenceType;
import de.l3s.model.times.HeidelIntervalString;
import de.l3s.translate.Language;
import de.unihd.dbs.uima.types.heideltime.Timex3Interval;
import edu.stanford.nlp.util.StringUtils;

public class HeidelTimeExtractorWithNewHeidelProject {

	private HeidelTimeExtractor heidelTimeFinder;

	private Revision revision;

	private Date documentCreationTime;

	// public static void main(String[] args) {
	//
	// DataLoader dl = new DataLoader("multiwiki2");
	// BinaryComparison comparison = dl.loadNewestComparison("Tomte",
	// Language.EN, Language.DE);
	//
	// HeidelTimeExtractorWithNewHeidelProject extractor = new
	// HeidelTimeExtractorWithNewHeidelProject(
	// comparison.getRevision1(),
	// new
	// HeidelTimeExtractorNarratives(de.unihd.dbs.uima.annotator.heideltime.resources.Language.ENGLISH));
	//
	// extractor.run();
	// }

	public HeidelTimeExtractorWithNewHeidelProject(Revision revision, HeidelTimeExtractor heidelTimeFinder,
			Date documentCreationTime) {
		super();
		this.revision = revision;
		this.heidelTimeFinder = heidelTimeFinder;
		this.documentCreationTime = documentCreationTime;
	}

	public HeidelTimeExtractorWithNewHeidelProject(Revision revision, HeidelTimeExtractor heidelTimeFinder) {
		super();
		this.revision = revision;
		this.heidelTimeFinder = heidelTimeFinder;
	}

	public HeidelTimeExtractorWithNewHeidelProject(Revision revision, Language language) {
		this.heidelTimeFinder = getHeidelTimeFinder(revision.getLanguage());
		this.revision = revision;
	}

	public HeidelTimeExtractorWithNewHeidelProject(HeidelTimeExtractor heidelTimeExtractor, Revision revision,
			Language language) {
		this.heidelTimeFinder = heidelTimeExtractor;
		this.revision = revision;
	}

	public static HeidelTimeExtractor getHeidelTimeFinder(Language language) {
		// Missing Turkish and Hungarian!
		de.unihd.dbs.uima.annotator.heideltime.resources.Language heidelLanguage = null;
		switch (language) {
		case DE:
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.GERMAN;
			break;
		case EN:
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.ENGLISH;
			break;
		case FR:
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.FRENCH;
			break;
		case PT:
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.PORTUGUESE;
			break;
		case NL:
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.DUTCH;
			break;
		case IT:
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.ITALIAN;
			break;
		case RU:
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.RUSSIAN;
			break;
		case ES: {
			heidelLanguage = de.unihd.dbs.uima.annotator.heideltime.resources.Language.SPANISH;
			break;
		}
		default:
			System.err.println("Currently missing HeidelTime language " + language);
		}

		return new HeidelTimeExtractorNarratives(heidelLanguage);
	}

	public void run() {

		if (!heidelTimeFinder.isInitialized())
			heidelTimeFinder.init();

		List<String> sentenceTexts = new ArrayList<String>();

		Map<Integer, Sentence> positions = new HashMap<Integer, Sentence>();

		int idx = 0;

		for (Sentence s : revision.getSentences()) {

			s.setHeidelIntervals(new ArrayList<HeidelIntervalString>());

			if (!s.isImportant() || s.isInInfobox() || s.getType() != SentenceType.SENTENCE
					|| s.getRawText().length() < 10)
				continue;

			sentenceTexts.add(s.getRawText());
			for (int i = idx; i <= idx + s.getRawText().length(); i++) {
				positions.put(i, s);
			}
			idx += s.getRawText().length() + 1;
		}

		String text = StringUtils.join(sentenceTexts, " ");
		List<HeidelIntervalString> intervals = extractHeidelTimes(text);

		for (HeidelIntervalString interval : intervals) {
			Sentence sentence1 = positions.get(interval.getBegin());
			Sentence sentence2 = positions.get(interval.getEnd());
			sentence1.addHeidelIntervalString(interval);
			if (sentence1 != sentence2) {
				// This is possible and it makes sense to add the time in both
				// sentences. This happens when the sentence splitting fails:
				// <first sentence> Die montenegrinische Armee konnte in der
				// Schlacht von Mojkovac vom 6./7.
				// <next sentence> Januar 1916 den Durchbruch zweier
				// österreichischer Divisionen, der 62. und 53., über die Tara
				// zurückschlagen.
				sentence2.addHeidelIntervalString(interval);
			}
		}

	}

	public List<HeidelIntervalString> extractHeidelTimes(String text) {
		return HeidelTimeExtractorWithNewHeidelProject.extractHeidelTimes(text, this.heidelTimeFinder,
				this.documentCreationTime);
	}

	public static List<HeidelIntervalString> extractHeidelTimes(String text, HeidelTimeExtractor heidelTimeFinder,
			Date documentCreationTime) {
		// public Set<HeidelIntervalString> extractHeidelTimes(String text) {
		// System.out.println("extractHeidelTimes");
		List<HeidelIntervalString> intervals = new ArrayList<HeidelIntervalString>();

		Set<Timex3Interval> timex3Intervals = new HashSet<Timex3Interval>();
		if (heidelTimeFinder instanceof HeidelTimeExtractorNarratives)
			timex3Intervals = ((HeidelTimeExtractorNarratives) heidelTimeFinder).getTimeExpressions(text);
		else
			timex3Intervals = ((HeidelTimeExtractorNews) heidelTimeFinder).getTimeExpressions(text,
					documentCreationTime);

		for (Timex3Interval interval : timex3Intervals) {
			// System.out.println("TIMEX: " + interval);
			String timeRepr;

			String completeIntervalStringRepresentation = interval.getTimexValueEB() + "." + interval.getTimexValueLE();

			// System.out.println(text.substring(interval.getBegin(),
			// interval.getEnd()));
			// System.out.println(interval.getTimexValue());
			// System.out.println(interval.getTimexValueEB());
			// System.out.println(interval);
			// System.out.println("");

			if (interval.getTimexValue() != null)
				timeRepr = interval.getTimexValue();
			else
				timeRepr = completeIntervalStringRepresentation;

			HeidelIntervalString string = new HeidelIntervalString(interval, timeRepr);
			// System.out.println(completeIntervalStringRepresentation);
			string.setCompleteIntervalStringRepresentation(completeIntervalStringRepresentation);

			intervals.add(string);
		}

		return intervals;
	}

}
