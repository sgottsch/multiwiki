package dbpopulate.translation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import app.Configuration;
import db.DbObject;
import db.QueryParam;
import db.QueryParam.QueryOperator;
import db.tables.Sentence_DB;
import db.tables.Text_DB;
import dbloader.DataLoader;
import extractor.Extractor;
import model.Revision;
import nlp.LuceneUtils;
import translate.Language;
import translate.MicrosoftTranslator;
import translate.TranslationI;

public class TranslationExtractor extends Extractor {

	// If true, then HTML texts are not translated
	private static final boolean RAW_TEXT_ONLY = true;

	private boolean ONLY_NEWEST_REVISIONS = false;
	private boolean UPDATE_ANNOTATIONS = true;
	private Pattern footnotePattern;
	private LuceneUtils luceneUtils = new LuceneUtils();
	private TranslationI languageProcessor = new MicrosoftTranslator();

	public TranslationExtractor(String database) {
		super(database);
		String footnoteRegex = "\\[\\d{1,3}\\]";
		this.footnotePattern = Pattern.compile(footnoteRegex);
	}

	public void translateSentences() throws Exception {

		this.connect();

		// int count = dbget.countSelectedDbObjects(conn, translation_a,
		// qparam_or, qparam_and);

		// Set<DbObject> selected = dbget.retrieveSelectedForTranslation(conn,
		// translation_a, qparam_or, qparam_and, new Translation(), 86030453);

		DataLoader dl = new DataLoader(Configuration.DATABASE1);

		Set<Revision> newestRevisions = null;

		if (this.ONLY_NEWEST_REVISIONS)
			newestRevisions = dl.loadMostRecentRevisions();

		Set<Text_DB> textsToTranslate = this.loadTextsToTranslate(newestRevisions);

		int count = textsToTranslate.size();

		System.out.println("Sentences to translate: " + count);

		int textsLength = 0;

		Map<String, Text_DB> sentencesToBeTranslatedWithTranslations = new HashMap<String, Text_DB>();

		Language originalLanguage = null;

		for (Text_DB text : textsToTranslate) {

			Language currentLanguage = Language.getLanguage(text.getValue("original_language").toString());

			if (originalLanguage == null) {
				originalLanguage = currentLanguage;
				System.out.println("Language: " + originalLanguage);
			} else if (currentLanguage != originalLanguage)
				continue;

			String htmlText = text.getString("original_text_html");
			htmlText = text.getString("original_text");
			try {
				htmlText = htmlText.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
				htmlText = htmlText.replaceAll("\\+", "%2B");
				htmlText = URLDecoder.decode((String) htmlText, "UTF-8");
			} catch (UnsupportedEncodingException var12_14) {
				// empty catch block
			}
			String textLater = htmlText.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;");
			int htmlTextLength = textLater.length();

			// Translate API conditions: The total of all texts to be
			// translated must not exceed 10000 characters. The maximum
			// number of array elements is 2000.
			if (textsLength + htmlTextLength > 10000 || sentencesToBeTranslatedWithTranslations.size() > 2000)
				continue;

			sentencesToBeTranslatedWithTranslations.put(htmlText, text);
			textsLength += htmlTextLength;
		}

		System.out.println(
				"Sentences to annotate with one Translate Query: " + sentencesToBeTranslatedWithTranslations.size());

		List<String> sentencesToBeTranslated = new ArrayList<String>();
		List<Text_DB> translationsToUpdate = new ArrayList<Text_DB>();

		for (String s : sentencesToBeTranslatedWithTranslations.keySet()) {
			sentencesToBeTranslated.add(s);
			System.out.println("To translate: " + s);
			translationsToUpdate.add(sentencesToBeTranslatedWithTranslations.get(s));
		}

		System.out.println("");

		List<String> translations = languageProcessor.translateTexts(sentencesToBeTranslated, originalLanguage,
				Language.EN);

		System.out.println("");

		for (int i = 0; i < translations.size(); i++) {

			String translation = translations.get(i).replace("\'", "\\'");

			Text_DB translationToUpdate = translationsToUpdate.get(i);
			// translationToUpdate.addUpdateValue(Text.translated_text_html_attr,
			// translation);

			// String rawText = makeSentenceRaw(translation);
			String rawText = translation;

			String stemmed = luceneUtils.stemmedEnglishText(rawText);

			System.out.println("Translated: " + translation);

			translationToUpdate.addUpdateValue(Text_DB.translated_text_attr, rawText);
			translationToUpdate.addUpdateValue(Text_DB.translated_text_stemmed_attr, stemmed);

			dbget.updateDbObject(conn, translationToUpdate);
		}

		if (UPDATE_ANNOTATIONS) {
			System.out.println("\nupdate_annotation_translations");
			dbget.callProcedure(conn, "update_annotation_translations");
		}

		conn.close();
		this.updateTranslations();
	}

	private Set<Text_DB> loadTextsToTranslate(Set<Revision> newestRevisions) throws SQLException {
		String query = "	SELECT t." + Text_DB.text_id_attr + ", t." + Text_DB.original_text_html_attr + ", t."
				+ Text_DB.original_text_attr + ", original_language " + "			FROM " + "			(	"
				+ "				SELECT text_id" + "				FROM annotation a "
				+ "				WHERE text_id IS NOT NULL";

		if (newestRevisions != null && newestRevisions.size() != 0) {

			query += " AND revision_id IN (";
			for (Revision r : newestRevisions) {
				query += r.getId() + ",";
			}
			query = query.substring(0, query.length() - 1) + ")";

		}

		query += "				GROUP BY text_id HAVING"
				// + " MAX(CASE WHEN annotation_type = 'SENTENCE' THEN 1 ELSE 0
				// END) = 1 AND "
				+ "						MAX(CASE WHEN to_translate = TRUE THEN 1 ELSE 0 END) = 1"
				+ "						AND MAX(CASE WHEN in_infobox = FALSE THEN 1 ELSE 0 END) = 1" + "			) a"
				+ "			JOIN text t ON(a.text_id = t.text_id) " + "			WHERE original_language != 'en'";

		if (RAW_TEXT_ONLY)
			query += " AND translated_text IS NULL";
		else
			query += " AND translated_text_html IS NULL";

		System.out.println(query);

		Statement st = conn.createStatement();

		ResultSet res = st.executeQuery(query);

		Set<Text_DB> texts = new HashSet<Text_DB>();

		while (res.next()) {
			db.tables.Text_DB textDb = new db.tables.Text_DB();
			textDb.setValue(Text_DB.text_id_attr, res.getLong(Text_DB.text_id_attr));
			textDb.setValue(Text_DB.original_text_html_attr, res.getString(Text_DB.original_text_html_attr));
			textDb.setValue(Text_DB.original_language_attr, res.getString(Text_DB.original_language_attr));
			textDb.setValue(Text_DB.original_text_attr, res.getString("original_text"));

			texts.add(textDb);
			// System.out.println(textDb);
		}

		return texts;
	}

	/**
	 * Inserts the translation for every annotation without translation where a
	 * translation can be found in the "translation" table
	 */
	public void updateTranslations() {

		connect();

		// Now: Remove HTML tags, stem (and load entities)

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(Sentence_DB.language_attr, Language.EN.getLanguage(), QueryOperator.NOT_LIKE));
		qparam_and.add(new QueryParam(Sentence_DB.english_text_html_attr, "NOT NULL"));
		qparam_and.add(new QueryParam(Sentence_DB.english_text_attr, "NULL"));

		// Retrieve the annotations that were updated before
		Set<DbObject> updatedAnnotations = dbget.retrieveSelected(conn, new Sentence_DB(), null, qparam_and, null);

		// Set<Annotation_Entity> annotation_entities = new
		// HashSet<Annotation_Entity>();

		for (DbObject aObj : updatedAnnotations) {
			Sentence_DB annotationToUpdate = (Sentence_DB) aObj;

			String rawText = makeSentenceRaw(
					annotationToUpdate.getValue(Sentence_DB.english_text_html_attr).toString());
			String stemmed = luceneUtils.stemmedEnglishText(rawText);

			annotationToUpdate.addUpdateValue(Sentence_DB.english_text_attr, rawText);

			annotationToUpdate.addUpdateValue(Sentence_DB.stemmed_english_text_attr, stemmed);

			dbget.updateDbObjectPrepared(conn, annotationToUpdate);

			// Having english text now, load entities TODO: Load times instead
			//
			// set the normal (non-update) value for loadEntities()
			// annotationToUpdate.setValue(Annotation.english_text_attr,
			// rawText);
			//
			// Set<Entry<String, String>> entities =
			// loadEntities(annotationToUpdate);
			// for (Entry<String, String> entity : entities) {
			// Annotation_Entity annotation_entity = new
			// Annotation_Entity(annotationToUpdate, entity.getKey(),
			// entity.getValue());
			// annotation_entities.add(annotation_entity);
			// }
		}

		// Write the translated texts into the annotations without translations
		dbget.callProcedure(conn, "update_annotation_translations");

		// dbget.storeDbObjectsPrepared(conn, annotation_entities, false);

	}

	public void moveAnnotationsToTranslations() {
		connect();

		System.out.println("moveAnnotationsToTranslations");

		dbget.callProcedure(conn, "move_annotations_to_texts");
	}

	/**
	 * Removes HTML tags and footnotes (e.g. "[3]") from string
	 * 
	 * @param sentence
	 * @return
	 */
	private String makeSentenceRaw(String sentence) {
		String rawSentence = Jsoup.parse(sentence).text();

		// Remove footnotes
		Matcher m = footnotePattern.matcher(rawSentence);
		rawSentence = m.replaceAll("");

		// No Wikipedia note removal because they are only removed in the
		// English texts

		return rawSentence.trim();
	}

	public static void main(String[] args) {
		TranslationExtractor te = new TranslationExtractor(Configuration.DATABASE1);

		te.updateTranslations();
		// te.moveAnnotationsToTranslations();
	}

}
