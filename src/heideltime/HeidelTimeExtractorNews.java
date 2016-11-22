package heideltime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import de.unihd.dbs.uima.types.heideltime.Timex3Interval;

public class HeidelTimeExtractorNews extends HeidelTimeExtractor {

	public HeidelTimeExtractorNews(Language language) {
		super(language, DocumentType.NEWS);
	}

	public Set<Timex3Interval> getTimeExpressions(String text, Date documentCreationTime) {
		return super.processText(text, documentCreationTime);
	}

	public static void main(String[] args) throws DocumentCreationTimeMissingException {

		double currentTime = System.nanoTime();
		double logTime = currentTime;

		Date date = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = dateFormat.parse("1700-01-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		HeidelTimeExtractorNews trial = new HeidelTimeExtractorNews(Language.ENGLISH);
		trial.init();

		currentTime = System.nanoTime();
		System.out.println("log: " + ((currentTime - logTime) / 1e6));
		logTime = currentTime;

		// String text = "The General Post Office (GPO) was officially
		// established in England in 1660 by Charles II. On Sunday, Senator Ted
		// Cruz of Texas implored his supporters to caucus as he closed out his
		// Iowa campaign with events in Iowa City, Davenport and Des Moines.
		// This happened in 1756.";

		String text = "On Sunday, Senator Ted Cruz of Texas implored his supporters to caucus as he closed out his Iowa campaign with events in Iowa City, Davenport and Des Moines.";

		Set<Timex3Interval> timeExpressions = trial.getTimeExpressions(text, date);

		currentTime = System.nanoTime();
		System.out.println("log: " + ((currentTime - logTime) / 1e6));
		logTime = currentTime;

		for (Timex3Interval timeExpression : timeExpressions) {
			System.out.println(timeExpression.getCoveredText());
			 System.out.println(timeExpression.getBegin() + "-" +			 timeExpression.getEnd());
			System.out.println(timeExpression.getTimexValueEB() + "." + timeExpression.getTimexValueLE());
		}

		String text2 = "On Sunday, Senator Ted Cruz of Texas implored his supporters to caucus as he closed out his Iowa campaign with events in Iowa City, Davenport and Des Moines.";

		Date date2 = null;
		try {
			date2 = dateFormat.parse("1800-01-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Set<Timex3Interval> timeExpressions2 = trial.getTimeExpressions(text2, date2);

		currentTime = System.nanoTime();
		System.out.println("log: " + ((currentTime - logTime) / 1e6));
		logTime = currentTime;

		for (Timex3Interval timeExpression : timeExpressions2) {
			System.out.println(timeExpression.getCoveredText());
			// System.out.println(timeExpression.getBegin() + "-" +
			// timeExpression.getEnd());
			System.out.println(timeExpression.getTimexValueEB() + "." + timeExpression.getTimexValueLE());
		}

	}

}
