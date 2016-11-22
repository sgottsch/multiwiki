package heideltime;

import java.util.Date;
import java.util.Set;

import app.Configuration;
import de.unihd.dbs.heideltime.standalone.CLISwitch;
import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import de.unihd.dbs.uima.types.heideltime.Timex3Interval;

public abstract class HeidelTimeExtractor {

	private HeidelTimeStandalone heidelTimeStandalone;

	private Language language;

	private DocumentType documentType;

	private boolean isInitialized = false;

	protected HeidelTimeExtractor(Language language, DocumentType documentType) {
		this.language = language;
		this.documentType = documentType;
	}

	public void init() {
		OutputType outputType = OutputType.TIMEML;
		POSTagger posTagger = (POSTagger) CLISwitch.POSTAGGER.getValue();
		Boolean doIntervalTagging = true;

		System.out.println("Initialize HeidelTime for language " + language + ".");

		String propsFile = Configuration.HEIDEL_TIME_PROPS_FILE_LOCATION;

		heidelTimeStandalone = new HeidelTimeStandalone(this.language, this.documentType, outputType, propsFile,
				posTagger, doIntervalTagging);
		System.out.println("HeidelTime initialized.");

		this.isInitialized = true;
	}

	public Set<Timex3Interval> processText(String text) {
		return processText(text, null);
	}

	protected Set<Timex3Interval> processText(String text, Date documentCreationTime) {

		System.out.println("Text: " + text + ", " + documentCreationTime);

		try {
			Timex3Formatter formatter = new Timex3Formatter();

			if (documentCreationTime == null)
				heidelTimeStandalone.process(text, formatter);
			else
				heidelTimeStandalone.process(text, documentCreationTime, formatter);

			// System.out.println("--->");
			// System.out.println(text);
			// for (Timex3Interval timeExpression :
			// formatter.getTimeIntervals()) {
			// // System.out.println(timeExpression.getBegin() + "-" +
			// // timeExpression.getEnd());
			// System.out.println(timeExpression.getCoveredText());
			// }
			// System.out.println("<---");

			return formatter.getTimeIntervals();

		} catch (DocumentCreationTimeMissingException e) {
			e.printStackTrace();
		}
		return null;

	}

	public boolean isInitialized() {
		return isInitialized;
	}

}
