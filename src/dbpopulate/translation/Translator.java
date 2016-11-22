/**
 * 
 */
package dbpopulate.translation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import app.Configuration;

/**
 * This class controls the process of sentence translation by creating a task
 * that queries the translation API from time to time. Results are stored in the
 * database in each step.
 */
public class Translator {

	// if executed on the server (only working with the "text" table: set this
	// to "false")
	private static boolean UPDATE_ANNOTATIONS = true;

	private final static String database = Configuration.DATABASE1;

	public static void main(String args[]) {
		Translator translator = new Translator();
		translator.translate();
	}

	public void translate() {
		TranslationExtractor translationExtractor = new TranslationExtractor(database);

		// Before collecting translations, translate all annotations whose text
		// has already been translated once
		// translationExtractor.updateTranslations();

		if (UPDATE_ANNOTATIONS) {
			// Now collect all the texts that have to be translated
			translationExtractor.moveAnnotationsToTranslations();
		}

		// pool size == 5
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(5);

		SentenceTranslationTask2 st01 = new SentenceTranslationTask2(translationExtractor);
		// start right now and after every 8 minutes.
		stpe.scheduleAtFixedRate(st01, 0, 8 * 60, TimeUnit.SECONDS);

		// SimpleTask02 st02 = new SimpleTask02();
		// start in 1 sec and after every 2 sec.
		// stpe.scheduleAtFixedRate(st02, 1, 2, TimeUnit.SECONDS);

		// TODO: Stop process when ready
	}

}

class SentenceTranslationTask2 implements Runnable {

	int cnt;

	public TranslationExtractor translationExtractor;

	public SentenceTranslationTask2(TranslationExtractor translationExtractor) {
		this.translationExtractor = translationExtractor;
		this.cnt = 0;
	}

	public void run() {

		cnt += 1;
		if (cnt == 31)
			cnt = 1;

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("\nrunning (" + dateFormat.format(new Date()) + ") - " + cnt);

		// define languages for which translations should be stored
		// Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		// translate all but the English sentences
		// qparam_and.add(new QueryParam(Translation.translated_text_attr,
		// "NULL"));
		// qparam_and.add(new QueryParam(Translation.is_sentence_attr, "1"));

		if (cnt < 20) {
			try {
				System.out.println("Translate");
				translationExtractor.translateSentences();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		} else
			System.out.println("Break.");

		System.out.println("run completed (" + dateFormat.format(new Date()) + ")");
	}

}
