package de.l3s.dbloader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import de.l3s.db.DBConnector;
import de.l3s.db.DBGetter;
import de.l3s.db.DbObject;
import de.l3s.db.QueryParam;
import de.l3s.db.QueryParam.QueryOperator;
import de.l3s.db.tables.BinaryLanguageLink_DB;
import de.l3s.db.tables.ComparisonSimilarity_DB;
import de.l3s.db.tables.Comparison_DB;
import de.l3s.db.tables.Sentence_HeidelTime_DB;
import de.l3s.db.tables.Sentence_SpotlightLink_DB;
import de.l3s.model.Article;
import de.l3s.model.BinaryComparison;
import de.l3s.model.Entity;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.SentenceType;
import de.l3s.model.WikiParagraph;
import de.l3s.model.links.DbPediaLink;
import de.l3s.model.links.EntityLink;
import de.l3s.model.links.ExternalLink;
import de.l3s.model.links.InternalLink;
import de.l3s.model.times.HeidelIntervalString;
import de.l3s.tfidf.EntityCollection;
import de.l3s.tfidf.WordCollection;
import de.l3s.translate.Language;
import de.l3s.util.TextUtil;
import de.l3s.util.TimeLogger;
import edu.stanford.nlp.util.StringUtils;

/**
 * To load any data that belongs to articles, revisions and features like images
 * and external links, this class is used.
 */
public class DataLoader {

	private DBGetter dbget;
	private Connection conn;

	private String database;

	private Map<Language, Set<String>> blackList;

	private boolean LOG_TIMES = false;
	private boolean PRINT = false;

	private SimpleDateFormat heidelTimeFormat;

	Set<String> germanExpressions = new HashSet<String>();
	Set<String> englishExpressions = new HashSet<String>();

	public DataLoader(String database) {
		this(database, false);
	}

	public DataLoader(String database, boolean loadTypes) {

		// for (int i = 1800; i <= 2020; i++) {
		// germanExpressions.add(String.valueOf(i));
		// englishExpressions.add(String.valueOf(i));
		// }

		Set<String> germanMonthNames = new HashSet<String>();
		Set<String> englishMonthNames = new HashSet<String>();
		germanMonthNames.addAll(Arrays.asList("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August",
				"September", "Oktober", "November", "Dezember"));
		englishMonthNames.addAll(Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December"));

		germanExpressions.addAll(germanMonthNames);
		englishExpressions.addAll(englishMonthNames);

		for (String month : germanMonthNames) {
			for (int i = 1; i <= 31; i++)
				germanExpressions.add(i + ". " + month);
		}

		for (String month : englishMonthNames) {
			for (int i = 1; i <= 31; i++)
				englishExpressions.add(month + " " + i);
		}

		for (int i = 1; i <= 22; i++) {
			englishExpressions.add(i + "th century");
			germanExpressions.add(i + ". Jahrhundert");
		}

		this.database = database;
		this.heidelTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

		if (database != null)
			connect();
	}

	private void connect() {
		if (this.dbget == null) {

			this.dbget = new DBGetter();
			this.conn = null;

			try {
				this.conn = DBConnector.getDBConnection(this.database);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public BinaryComparison loadComparison(long revisionId1, Language language1, long revisionId2, Language language2,
			Date date) {

		Revision revision1 = this.loadRevision(revisionId1, language1, null, language2);
		Revision revision2 = this.loadRevision(revisionId2, language2, revision1, language1);

		String englishName = revision1.getTitle();
		if (revision2.getLanguage() == Language.EN)
			englishName = revision2.getTitle();

		BinaryComparison comparison = new BinaryComparison(revision1, revision2);

		comparison.setDate(date);
		comparison.setTitle(englishName);

		Revision englishRevision = revision1;
		Revision otherRevision = revision2;
		if (revision2.getLanguage() == Language.EN) {
			englishRevision = revision2;
			otherRevision = revision1;
		}

		this.loadEntityTranslations(otherRevision, englishRevision);
		this.loadEntityTranslations(englishRevision, otherRevision);
		this.mergeEntities(otherRevision, englishRevision);

		EntityCollection ec = new EntityCollection(comparison, revision1, revision2);
		ec.createLuceneIndex();
		WordCollection wc = new WordCollection(comparison, revision1, revision2);
		wc.createLuceneIndex();

		return comparison;
	}

	public BinaryComparison loadNewestComparison(String englishId, Language language1, Language language2) {
		System.out.println("loadNewestComparison: " + englishId + ", " + language1 + ", " + language2);

		List<BinaryComparison> comparisons = loadNewestComparisons(Arrays.asList(englishId), language1, language2);
		if (comparisons.isEmpty())
			return null;
		BinaryComparison comparison = comparisons.get(0);

		System.out.println("Comparison, revision ids: " + comparison.getId1() + " / " + comparison.getId2());

		return loadComparison(comparison.getId1(), language1, comparison.getId2(), language2, comparison.getDate());
	}

	public List<BinaryComparison> loadNewestComparisonsCompletely(List<String> englishIds, Language language1,
			Language language2) {
		List<BinaryComparison> comparisons = loadNewestComparisons(englishIds, language1, language2);

		List<BinaryComparison> completeComparisons = new ArrayList<BinaryComparison>();
		for (BinaryComparison comparison : comparisons) {
			completeComparisons.add(loadComparison(comparison.getId1(), language1, comparison.getId2(), language2,
					comparison.getDate()));
		}

		return completeComparisons;
	}

	public List<BinaryComparison> loadAllComparisons(Collection<String> englishIds, Language language1,
			Language language2) {
		String query = "SELECT * FROM comparison c1 WHERE (language1 = ? OR language2 = ?  OR language3 = ?) AND (language1 = ? OR language2 = ?  OR language3 = ?)";
		return this.loadComparisons(englishIds, language1, language2, query);
	}

	public List<BinaryComparison> loadNewestComparisons(Collection<String> englishIds, Language language1,
			Language language2) {
		String query = "SELECT * FROM comparison c1 WHERE (language1 = ? OR language2 = ? "
				+ " OR language3 = ?) AND (language1 = ? OR language2 = ? "
				+ " OR language3 = ?)  AND NOT EXISTS( SELECT * FROM comparison c2 "
				+ " WHERE c1.article1_uri = c2.article1_uri AND c1.article2_uri = c2.article2_uri "
				+ "   AND c2.date > c1.date)";
		query += " AND (revision1_id != 81740730 AND revision2_id != 81740730 AND (revision3_id IS NULL OR revision3_id != 81740730))";
		return this.loadComparisons(englishIds, language1, language2, query);
	}

	public List<BinaryComparison> loadComparisons(Collection<String> englishIds, Language language1, Language language2,
			String query) {

		List<BinaryComparison> comparisons = new ArrayList<BinaryComparison>();

		try {

			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, language2.getLanguage());
			pstmt.setString(2, language2.getLanguage());
			pstmt.setString(3, language2.getLanguage());
			pstmt.setString(4, language1.getLanguage());
			pstmt.setString(5, language1.getLanguage());
			pstmt.setString(6, language1.getLanguage());

			ResultSet res = pstmt.executeQuery();

			while (res.next()) {

				Language lang1 = Language.getLanguage(res.getString("language1"));
				Language lang2 = Language.getLanguage(res.getString("language2"));
				Language lang3 = null;
				if (res.getString("language3") != null)
					lang3 = Language.getLanguage(res.getString("language3"));

				String englishId = "";
				if (lang1 == Language.EN)
					englishId = res.getString("article1_uri");
				else if (lang2 == Language.EN)
					englishId = res.getString("article2_uri");
				else if (lang3 == Language.EN)
					englishId = res.getString("article3_uri");
				else
					continue;

				int englishRevisionId = 0;
				if (lang1 == Language.EN)
					englishRevisionId = res.getInt("revision1_id");
				else if (lang2 == Language.EN)
					englishRevisionId = res.getInt("revision2_id");
				else if (lang3 == Language.EN)
					englishRevisionId = res.getInt("revision3_id");
				else
					continue;

				int germanRevisionId = 0;
				if (lang1 == language2)
					germanRevisionId = res.getInt("revision1_id");
				else if (lang2 == language2)
					germanRevisionId = res.getInt("revision2_id");
				else if (lang3 == language2)
					germanRevisionId = res.getInt("revision3_id");
				else
					continue;

				String englishIdTmp = englishId;
				englishId = englishIdTmp.substring("http://en.wikipedia.org/wiki/".length());
				String englishIdHttps = englishIdTmp.substring("https://en.wikipedia.org/wiki/".length());

				String englishId2 = englishId.replace("_", " ");
				String englishIdHttps2 = englishIdHttps.replace("_", " ");

				if (englishIds.contains(englishIdHttps) || englishIds.contains(englishIdHttps2)) {
					BinaryComparison comparison = new BinaryComparison(englishIdHttps2, englishRevisionId,
							germanRevisionId);
					comparison.setDate(res.getTimestamp(Comparison_DB.date_attr));
					comparisons.add(comparison);
				} else if (englishIds.contains(englishId) || englishIds.contains(englishId2)) {
					BinaryComparison comparison = new BinaryComparison(englishId2, englishRevisionId, germanRevisionId);
					comparison.setDate(res.getTimestamp(Comparison_DB.date_attr));
					comparisons.add(comparison);
				}
			}

			res.close();

			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return comparisons;
	}

	public Revision loadRevisionWithoutFeatures(long revisionId, Language language) {
		return this.loadRevisionWithoutFeatures(revisionId, language, null, null);
	}

	public Revision loadRevisionWithoutFeatures(long revisionId, Language language,
			Revision otherRevisionFromComparison, Language otherLanguage) {
		return this.loadRevisionWithoutFeatures(revisionId, language, otherRevisionFromComparison, false,
				otherLanguage);
	}

	private Revision loadRevisionWithoutFeatures(long revisionId, Language language,
			Revision otherRevisionFromComparison, boolean loadFeatures, Language otherLanguage) {

		TimeLogger.getInstance().start(LOG_TIMES);

		if (PRINT)
			System.out.println("Load revision " + revisionId + " (" + language.getLanguage() + ")");

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(de.l3s.db.tables.Revision_DB.revision_id_attr, revisionId));
		qparam_and.add(new QueryParam(de.l3s.db.tables.Revision_DB.language_attr, language.getLanguage()));

		de.l3s.db.tables.Revision_DB dbRevision = (de.l3s.db.tables.Revision_DB) dbget.retrieveOneSelected(conn,
				new de.l3s.db.tables.Revision_DB(), null, qparam_and, null);

		TimeLogger.getInstance().logTime("Revision");

		if (PRINT)
			System.out.println(dbRevision.getString(de.l3s.db.tables.Revision_DB.title_attr) + ", "
					+ dbRevision.getString(de.l3s.db.tables.Revision_DB.language_attr) + ", date: "
					+ dbRevision.getValue(de.l3s.db.tables.Revision_DB.date_attr));

		// TODO: Add date
		Revision revision = new Revision(revisionId, null, dbRevision.getString(de.l3s.db.tables.Revision_DB.title_attr),
				dbRevision.getString(de.l3s.db.tables.Revision_DB.original_html_text_attr), language,
				dbRevision.getString(de.l3s.db.tables.Revision_DB.article_uri_attr));

		// Make both revisions use the same entity instances
		if (otherRevisionFromComparison != null)
			revision.setEntities(otherRevisionFromComparison.getEntities());

		loadParagraphs(revision);
		TimeLogger.getInstance().logTime("Paragraphs");
		revision.setSentences(loadSentences(language, revision, loadFeatures, otherLanguage));
		TimeLogger.getInstance().logTime("Annotations");

		return revision;
	}

	public Revision loadRevision(long revisionId, Language language, Revision otherRevisionFromComparison,
			Language otherLanguage) {

		// first: load "without features" part
		Revision revision = this.loadRevisionWithoutFeatures(revisionId, language, otherRevisionFromComparison, true,
				otherLanguage);

		RevisionDataLoader revisionLoader = new RevisionDataLoader(conn, dbget);

		revisionLoader.loadAuthors(revision, otherRevisionFromComparison);
		TimeLogger.getInstance().logTime("Authors (Revision)");

		revisionLoader.loadImages(revision);
		TimeLogger.getInstance().logTime("Images (Revision)");

		revisionLoader.loadExternalLinksOfRevision(revision, otherRevisionFromComparison);
		TimeLogger.getInstance().logTime("External Links (Revision)");

		revisionLoader.loadInternalLinksOfRevision(revision);
		TimeLogger.getInstance().logTime("Internal Links (Revision)");

		revisionLoader.buildDBpediaLinksOfRevision(revision);
		TimeLogger.getInstance().logTime("dbPedia Links (Revision)");

		revisionLoader.loadRevisionHistory(revision);
		TimeLogger.getInstance().logTime("Edits per Date");

		if (PRINT)
			System.out
					.println("Loading Successfull (" + TimeLogger.getInstance().getTotalTimeInSeconds() + " seconds)");

		return revision;
	}

	private void loadParagraphs(Revision revision) {
		long revisionId = revision.getId();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(de.l3s.db.tables.Paragraph_DB.revision_number_attr, revisionId));

		Set<DbObject> dbParagraphs = dbget.retrieveSelected(conn, new de.l3s.db.tables.Paragraph_DB(), null, qparam_and, null);

		for (DbObject dboParagraph : dbParagraphs) {
			de.l3s.db.tables.Paragraph_DB dbParagraph = (de.l3s.db.tables.Paragraph_DB) dboParagraph;

			WikiParagraph paragraph = new WikiParagraph(dbParagraph.getInt(de.l3s.db.tables.Paragraph_DB.paragraph_id_attr),
					dbParagraph.getInt(de.l3s.db.tables.Paragraph_DB.above_paragraph_id_attr),
					dbParagraph.getString(de.l3s.db.tables.Paragraph_DB.paragraph_type_attr),
					dbParagraph.getInt(de.l3s.db.tables.Paragraph_DB.start_attr),
					dbParagraph.getInt(de.l3s.db.tables.Paragraph_DB.end_attr),
					dbParagraph.getInt(de.l3s.db.tables.Paragraph_DB.content_start_attr),
					dbParagraph.getInt(de.l3s.db.tables.Paragraph_DB.content_end_attr));

			revision.addParagraph(paragraph);
		}

		// Build hierarchical structure
		for (Integer paragraphId : revision.getParagraphs().keySet()) {
			WikiParagraph paragraph = revision.getParagraphs().get(paragraphId);

			Integer aboveParagraphId = paragraph.getTopParagraphId();

			if (aboveParagraphId != null && revision.getParagraphs().containsKey(aboveParagraphId)) {
				paragraph.setAboveParagraph(revision.getParagraphs().get(aboveParagraphId));
			} else
				paragraph.setAboveParagraph(null);
		}

	}

	private List<Sentence> loadSentences(Language language, Revision revision, boolean loadFeatures,
			Language otherLanguage) {

		long revisionId = revision.getId();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(de.l3s.db.tables.Sentence_DB.revision_number_attr, revisionId));
		qparam_and.add(new QueryParam(de.l3s.db.tables.Sentence_DB.language_attr, language.getLanguage()));

		qparam_and.add(new QueryParam(de.l3s.db.tables.Sentence_DB.in_infobox_attr, QueryOperator.IS_FALSE));
		qparam_and.add(new QueryParam(de.l3s.db.tables.Sentence_DB.to_translate_attr, QueryOperator.IS_TRUE));
		// qparam_and.add(new
		// QueryParam(db.tables.Annotation.annotation_type_attr,
		// "SENTENCE"));

		Set<DbObject> dbAnnotations = dbget.retrieveSelected(conn, new de.l3s.db.tables.Sentence_DB(), null, qparam_and, null,
				de.l3s.db.tables.Sentence_DB.annotation_id_attr + " ASC");

		TimeLogger.getInstance().logTime("Annotations (Annotations)");

		List<Sentence> annotations = new ArrayList<Sentence>();
		Map<Integer, Sentence> annotationIds = new HashMap<Integer, Sentence>();

		int untranslatedSentences = 0;
		int translatedSentences = 0;

		int numberOfSentencesWithSentenceType = 0;
		Map<Sentence, Integer> positionsOnlySentences = new HashMap<Sentence, Integer>();

		Set<WikiParagraph> noPrintParagraphs = new HashSet<WikiParagraph>();

		for (DbObject dbAnnoationObj : dbAnnotations) {

			de.l3s.db.tables.Sentence_DB dbAnnotation = (de.l3s.db.tables.Sentence_DB) dbAnnoationObj;

			if (!dbAnnotation.getBoolean(de.l3s.db.tables.Sentence_DB.to_translate_attr)
					|| dbAnnotation.getBoolean(de.l3s.db.tables.Sentence_DB.in_infobox_attr))
				continue;

			if (blackList != null && blackList.containsKey(language) && blackList.get(language)
					.contains(dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.original_text_attr))) {
				// System.out.println("BLACKLIST: " +
				// dbAnnotation.getString(db.tables.Annotation.original_text_attr)
				// +
				// ", "
				// + revision.getTitle());
				continue;
			}

			Sentence annotation;
			if (language == Language.EN) {
				annotation = new Sentence(dbAnnotation.getInt(de.l3s.db.tables.Sentence_DB.annotation_id_attr), language,
						dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.original_text_html_attr),
						dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.original_text_attr),
						dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.stemmed_english_text_attr),
						dbAnnotation.getInt(de.l3s.db.tables.Sentence_DB.start_position_attr),
						dbAnnotation.getInt(de.l3s.db.tables.Sentence_DB.end_position_attr),
						dbAnnotation.getBoolean(de.l3s.db.tables.Sentence_DB.to_translate_attr),
						dbAnnotation.getBoolean(de.l3s.db.tables.Sentence_DB.in_infobox_attr),
						dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.annotation_type_attr));
			} else {

				String english_text_html = null;
				String english_text = null;
				String stemmed_english_text = null;

				if (dbAnnotation.getValue(de.l3s.db.tables.Sentence_DB.english_text_html_attr) != null)
					english_text_html = dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.english_text_html_attr);
				if (dbAnnotation.getValue(de.l3s.db.tables.Sentence_DB.english_text_attr) != null)
					english_text = dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.english_text_attr);
				if (dbAnnotation.getValue(de.l3s.db.tables.Sentence_DB.stemmed_english_text_attr) != null)
					stemmed_english_text = dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.stemmed_english_text_attr);

				if (english_text == null) {
					if (dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.annotation_type_attr).equals("SENTENCE"))
						untranslatedSentences += 1;
				}

				if (english_text != null
						&& dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.annotation_type_attr).equals("SENTENCE"))
					translatedSentences += 1;

				annotation = new Sentence(dbAnnotation.getInt(de.l3s.db.tables.Sentence_DB.annotation_id_attr), language,
						dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.original_text_html_attr),
						dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.original_text_attr), english_text_html,
						english_text, stemmed_english_text,
						dbAnnotation.getInt(de.l3s.db.tables.Sentence_DB.start_position_attr),
						dbAnnotation.getInt(de.l3s.db.tables.Sentence_DB.end_position_attr),
						dbAnnotation.getBoolean(de.l3s.db.tables.Sentence_DB.to_translate_attr),
						dbAnnotation.getBoolean(de.l3s.db.tables.Sentence_DB.in_infobox_attr),
						dbAnnotation.getString(de.l3s.db.tables.Sentence_DB.annotation_type_attr));

				try {
					if (annotation.getEnglishStemmedTextConcatenated() != null) {
						annotation.setEnglishStemmedTextConcatenated(
								TextUtil.removeStopWords(annotation.getEnglishStemmedTextConcatenated()));
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}

			try {
				if (annotation.getEnglishStemmedTextConcatenated() != null)
					annotation.setEnglishStemmedTextConcatenated(
							TextUtil.removeStopWords(annotation.getEnglishStemmedTextConcatenated()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			annotation.setRevision(revision);
			annotations.add(annotation);
			annotationIds.put(annotation.getNumber(), annotation);

			// Add paragraph that contains the annotation
			int aboveParagraphId = dbAnnotation.getInt(de.l3s.db.tables.Sentence_DB.containing_paragraph_id_attr);
			if (revision.getParagraphs().containsKey(aboveParagraphId)) {
				annotation.setParagraph(revision.getParagraphs().get(aboveParagraphId));
				revision.getParagraphs().get(aboveParagraphId).addAnnotation(annotation);
				if (annotation.getType() == SentenceType.TITLE || annotation.getType() == SentenceType.ARTICLE_TITLE)
					revision.getParagraphs().get(aboveParagraphId).setTitleAnnotation(annotation);
			} else
				annotation.setParagraph(null);

			if (annotation.getType() == SentenceType.SENTENCE) {
				positionsOnlySentences.put(annotation, numberOfSentencesWithSentenceType);
				numberOfSentencesWithSentenceType += 1;
			}

			if (annotation.getType() == SentenceType.NO_PRINT)
				noPrintParagraphs.add(annotation.getParagraph());

		}

		// compute relative positions
		for (Sentence sentence : positionsOnlySentences.keySet()) {
			sentence.setRelativePosition(
					((double) positionsOnlySentences.get(sentence)) / ((double) numberOfSentencesWithSentenceType));
		}

		revision.setSentenceIds(annotationIds);

		if (PRINT && language != Language.EN && untranslatedSentences > 0) {
			System.out.println("Untranslated Sentences: " + untranslatedSentences + " of "
					+ (untranslatedSentences + translatedSentences) + "!");
		}

		if (loadFeatures) {
			Set<ExternalLink> externalLinks = loadExternalLinks(language, revisionId);
			for (ExternalLink externalLink : externalLinks) {
				// "contains" check because some annotations were not loaded
				// (isImportant, isInInfobox)
				if (annotationIds.containsKey(externalLink.getAnnotationNumber()))
					annotationIds.get(externalLink.getAnnotationNumber()).getExternalLinks().add(externalLink);
			}
			TimeLogger.getInstance().logTime("External Links (Annotations)");

			Set<InternalLink> internalLinks = loadInternalLinks(revision, language);
			for (InternalLink internalLink : internalLinks) {
				if (annotationIds.containsKey(internalLink.getAnnotationNumber())
						&& isNotADate(internalLink.getEntity(), language) && isNotANumber(internalLink, language))
					annotationIds.get(internalLink.getAnnotationNumber()).addInternalLink(internalLink);
				// System.out.println(internalLink.getSomeName() + " -> " +
				// internalLink.getEntity().getType(Language.EN));
			}
			TimeLogger.getInstance().logTime("Internal Links (Annotations)");

			Set<DbPediaLink> dbpediaLinks = this.loadSpotlightLinks(revision, language, otherLanguage);
			for (DbPediaLink dbpediaLink : dbpediaLinks) {

				if (annotationIds.containsKey(dbpediaLink.getAnnotationNumber())
						&& isNotADate(dbpediaLink.getEntity(), language) && isNotANumber(dbpediaLink, language)) {
					annotationIds.get(dbpediaLink.getAnnotationNumber()).addDbPediaLink(dbpediaLink);
				}

				// System.out.println(dbpediaLink.getSomeName() + " -> " +
				// dbpediaLink.getEntity().getType(Language.EN));
			}
			TimeLogger.getInstance().logTime("dbPedia Links (Annotations)");

			Set<HeidelIntervalString> times = this.loadHeidelTimes(language, revisionId);
			for (HeidelIntervalString time : times) {
				if (!annotationIds.containsKey(time.getSentenceNumber()))
					continue;
				annotationIds.get(time.getSentenceNumber()).addHeidelIntervalString(time);
			}
			TimeLogger.getInstance().logTime("HeidelTimes (Annotations)");

			// ngrams
			for (Sentence annotation : annotations) {
				try {
					annotation.setNGrams(loadNWordGrams(annotation, 2));
				} catch (IOException e) {
					annotation.setNGrams(new ArrayList<String>());
				}
			}
			TimeLogger.getInstance().logTime("nGrams (Annotations)");

			for (Sentence sentence : annotations) {
				if (noPrintParagraphs.contains(sentence.getParagraph())) {
					sentence.setType(SentenceType.NO_PRINT);
				}
			}
		}
		return annotations;
	}

	public HeidelIntervalString transformToHeidelTime(String b, String e) {

		String timeString = b + "." + e;

		if (b.charAt(2) == '-') {
			b = b.substring(0, 2) + "00" + b.substring(2, b.length());
		}
		if (b.charAt(3) == '-') {
			b = b.substring(0, 3) + "0" + b.substring(3, b.length());
		}

		if (e.charAt(2) == '-') {
			e = e.substring(0, 2) + "99" + e.substring(2, e.length());
		}
		if (e.charAt(3) == '-') {
			e = e.substring(0, 3) + "9" + e.substring(3, e.length());
		}

		try {
			Date beginTime = heidelTimeFormat.parse(b);

			Date endTime = heidelTimeFormat.parse(e);

			if (beginTime.after(endTime)) {
				Date tmpTime = beginTime;
				beginTime = endTime;
				endTime = tmpTime;
			}

			return new HeidelIntervalString(timeString, beginTime, endTime);

		} catch (ParseException ex) {
			System.out.println("Error: " + timeString);
			ex.printStackTrace();
			return null;
		}
	}

	public HeidelIntervalString transformToHeidelTime(String timeString) {
		String[] intervalParts = timeString.split("\\.");
		return this.transformToHeidelTime(intervalParts[0], intervalParts[1]);
	}

	public List<String> loadNGrams(Sentence annotation, int i) {
		List<String> nGrams = new ArrayList<String>();
		Reader reader = new StringReader(annotation.getEnglishRawText());
		NGramTokenizer tokenizer = new NGramTokenizer(Version.LUCENE_48, reader, i, i);
		try {
			tokenizer.reset();
			while (tokenizer.incrementToken()) {
				String token = tokenizer.getAttribute(CharTermAttribute.class).toString();
				nGrams.add(token);
			}
			tokenizer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nGrams;
	}

	public static List<String> loadNWordGrams(Sentence annotation, int i) throws IOException {
		List<String> ngrams = new ArrayList<String>();

		String sentence = annotation.getEnglishStemmedTextConcatenated();

		StringReader reader = new StringReader(sentence);
		StandardTokenizer source = new StandardTokenizer(Version.LUCENE_48, reader);
		TokenStream tokenStream = new StandardFilter(Version.LUCENE_48, source);
		ShingleFilter filter = new ShingleFilter(tokenStream);
		filter.setOutputUnigrams(false);

		filter.reset();
		while (filter.incrementToken()) {
			String token = filter.getAttribute(CharTermAttribute.class).toString();
			ngrams.add(token.toString());
		}
		filter.close();

		return ngrams;
	}

	private Set<InternalLink> loadInternalLinks(Revision revision, Language language) {
		long revisionId = revision.getId();
		HashSet<InternalLink> internalLinks = new HashSet<InternalLink>();
		try {
			String query1 = "SELECT annotation_id, start_position, end_position, wiki_link";

			query1 += " FROM annotation_internal_link al ";

			query1 += " WHERE language = ? AND revision_id = ?  AND wiki_link NOT LIKE 'Wikipedia:%' ";

			PreparedStatement pstmt = this.conn.prepareStatement(query1);
			pstmt.setString(1, language.getLanguage());
			pstmt.setLong(2, revisionId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {

				int startPosition = rs.getInt("start_position");
				int endPosition = rs.getInt("end_position");
				String wikiLink = rs.getString("wiki_link");
				int sentenceNumber = rs.getInt("annotation_id");

				InternalLink link = new InternalLink(startPosition, endPosition);

				this.buildEntityForLink(link, sentenceNumber, wikiLink, revision);

				if (link.getEntity() == null)
					continue;

				internalLinks.add(link);
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return internalLinks;
	}

	private void buildEntityForLink(EntityLink link, int sentenceNumber, String wikiLink, Revision revision) {
		link.setAnnotationNumber(sentenceNumber);
		link.setEntity(DataLoader.buildEntity(revision, wikiLink));
	}

	public static Entity buildEntity(Revision revision, String wikiLink) {

		Entity entity = revision.getEntities().get(revision.getLanguage()).get(wikiLink);

		if (entity == null) {
			HashMap<Language, String> langLinks = new HashMap<Language, String>();
			wikiLink = wikiLink.replaceAll(" ", "_");
			langLinks.put(revision.getLanguage(), wikiLink);
			entity = revision.buildEntity(langLinks);
		}

		return entity;
	}

	private boolean isNotADate(Entity entity, Language language) {

		// TODO: Generic languages
		if (language == Language.DE && germanExpressions.contains(entity.getName(language))) {
			return false;
		} else if (language == Language.EN && englishExpressions.contains(entity.getName(language))) {
			return false;
		}

		return true;
	}

	private boolean isNotANumber(InternalLink currentInternalLink, Language language) {
		return !StringUtils.isNumeric(currentInternalLink.getEntity().getName(language));
	}

	private boolean isNotANumber(DbPediaLink dbpediaLink, Language language) {
		return !StringUtils.isNumeric(dbpediaLink.getEntity().getName(language));
	}

	private Set<DbPediaLink> loadSpotlightLinks(Revision revision, Language language, Language otherLanguage) {

		long revisionId = revision.getId();
		HashSet<DbPediaLink> spotlightLinks = new HashSet<DbPediaLink>();

		try {

			Vector<QueryParam> qparam_and = new Vector<QueryParam>();
			qparam_and.add(new QueryParam(de.l3s.db.tables.Sentence_SpotlightLink_DB.language_attr, language.getLanguage()));
			qparam_and.add(new QueryParam(de.l3s.db.tables.Sentence_SpotlightLink_DB.revision_number_attr, revisionId));

			Set<Sentence_SpotlightLink_DB> dbSentenceSpotlightLinks = dbget.retrieveSelected(conn,
					new de.l3s.db.tables.Sentence_SpotlightLink_DB(), null, qparam_and, null);

			for (Sentence_SpotlightLink_DB dbSpotlightLinkObj : dbSentenceSpotlightLinks) {

				String wikiLink = dbSpotlightLinkObj.getString(Sentence_SpotlightLink_DB.wiki_link_attr);
				int sentenceNumber = dbSpotlightLinkObj.getInt(Sentence_SpotlightLink_DB.annotation_id_attr);

				DbPediaLink link = new DbPediaLink(
						dbSpotlightLinkObj.getBoolean(Sentence_SpotlightLink_DB.has_type_attr));

				this.buildEntityForLink(link, sentenceNumber, wikiLink, revision);

				if (link.getEntity() == null)
					continue;

				spotlightLinks.add(link);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return spotlightLinks;
	}

	private Set<ExternalLink> loadExternalLinks(Language language, long revisionId) {

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(de.l3s.db.tables.Revision_DB.revision_id_attr, revisionId));

		Set<DbObject> dbAnnotationsExternalLinks = dbget.retrieveSelected(conn,
				new de.l3s.db.tables.Sentence_ExternalLink_DB(), null, qparam_and, null);

		Set<ExternalLink> externalLinks = new HashSet<ExternalLink>();

		for (DbObject dbExternalLinkObj : dbAnnotationsExternalLinks) {

			de.l3s.db.tables.Sentence_ExternalLink_DB dbExternalLink = (de.l3s.db.tables.Sentence_ExternalLink_DB) dbExternalLinkObj;

			ExternalLink externalLink = new ExternalLink(
					dbExternalLink.getString(de.l3s.db.tables.Sentence_ExternalLink_DB.external_link_uri_attr),
					dbExternalLink.getString(de.l3s.db.tables.Sentence_ExternalLink_DB.external_link_host_attr));

			externalLink
					.setAnnotationNumber(dbExternalLink.getInt(de.l3s.db.tables.Sentence_ExternalLink_DB.annotation_id_attr));

			externalLinks.add(externalLink);
		}

		return externalLinks;
	}

	public Set<Revision> loadMostRecentRevisions() {
		Set<Revision> revisions = new HashSet<Revision>();

		connect();

		Set<DbObject> dbRevisions = dbget.retrieveSelected(conn, new de.l3s.db.tables.Revision_DB(), null, null, null,
				"article_uri ASC, date DESC");

		String currentArticleURI = "";

		for (DbObject revObj : dbRevisions) {
			de.l3s.db.tables.Revision_DB revDb = (de.l3s.db.tables.Revision_DB) revObj;

			if (!currentArticleURI.equals(revDb.getString(de.l3s.db.tables.Revision_DB.article_uri_attr))) {
				Language language = Language.getLanguage(revDb.getString(de.l3s.db.tables.Revision_DB.language_attr));

				revisions.add(new Revision(revDb.getLong(de.l3s.db.tables.Revision_DB.revision_id_attr), null,
						revDb.getString(de.l3s.db.tables.Revision_DB.article_uri_attr),
						new Article(revDb.getString(de.l3s.db.tables.Revision_DB.article_uri_attr), language), language));

				// System.out.println(revDb.getString(db2.Revision.article_uri_attr)
				// + ": "
				// + revDb.getLong(db2.Revision.revision_id_attr));

				currentArticleURI = revDb.getString(de.l3s.db.tables.Revision_DB.article_uri_attr);
			}

		}

		return revisions;
	}

	public List<String> loadMostRecentComparisons(Language language2) {
		List<String> recentComparisons = new ArrayList<String>();

		String query = "SELECT title FROM"
				+ " (SELECT CASE WHEN language1 = 'en' THEN article1_uri WHEN language2 = 'en' THEN article2_uri ELSE article3_uri END AS en_article_uri"
				+ " FROM comparison"
				+ " WHERE language1 = ? OR language2 = ? OR (language3 IS NOT NULL AND language3 = ?)"
				+ " GROUP BY (CONCAT_WS(article1_uri, article2_uri, article3_uri))) a"
				+ " JOIN article ON (a.en_article_uri = article.article_uri)" + " ORDER BY title ASC";

		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, language2.getLanguage());
			pstmt.setString(2, language2.getLanguage());
			pstmt.setString(3, language2.getLanguage());

			ResultSet res = pstmt.executeQuery();

			while (res.next()) {
				recentComparisons.add(res.getString("title"));
			}
		} catch (Exception e) {

		}
		return recentComparisons;
	}

	private Set<HeidelIntervalString> loadHeidelTimes(Language language, long revisionId) {

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam("revision_id", revisionId));
		qparam_and.add(new QueryParam("language", language.getLanguage()));
		Set<Sentence_HeidelTime_DB> dbAnnotationsTimes = this.dbget.retrieveSelected(this.conn,
				new Sentence_HeidelTime_DB(), null, qparam_and, null);

		HashSet<HeidelIntervalString> heidelTimes = new HashSet<HeidelIntervalString>();

		for (Sentence_HeidelTime_DB dbTime : dbAnnotationsTimes) {
			HeidelIntervalString heidelInterval = this.transformToHeidelTime(dbTime.getString("begin_time"),
					dbTime.getString("end_time"));
			heidelInterval.setSentenceNumber(dbTime.getInt("annotation_id"));
			heidelTimes.add(heidelInterval);
		}

		return heidelTimes;
	}

	// public static void main(String[] args) throws SQLException, URIException
	// {
	// DataLoader dl = new DataLoader(Configuration.DATABASE1);
	// dl.loadMostRecentRevisions();
	// }

	/**
	 * For a given comparison, this method returns a list of all comparisons
	 * stored in the database that compare the same articles. This list is
	 * sorted by the comparison's date.
	 * 
	 * @param comparison
	 *            Comparison with two revisions for whose articles other
	 *            comparisons are searched.
	 * @return List of comparisons with over allsimilarity values with the same
	 *         articles as the given comparison, sorted by date.
	 */
	public List<BinaryComparison> getComparisonSimilarities(BinaryComparison comparison) {
		List<BinaryComparison> comparisons = new ArrayList<BinaryComparison>();

		connect();

		String article1Uri = comparison.getRevision1().getArticleUri();
		String article2Uri = comparison.getRevision2().getArticleUri();

		Set<DbObject> revSimsTmp = dbget.retrieveAll(conn, new ComparisonSimilarity_DB());

		for (DbObject revSimTmp : revSimsTmp) {
			ComparisonSimilarity_DB revSim = (ComparisonSimilarity_DB) revSimTmp;

			if (!revSim.getString(ComparisonSimilarity_DB.article1_uri_attr).equals(article1Uri)
					&& !revSim.getString(ComparisonSimilarity_DB.article2_uri_attr).equals(article1Uri))
				continue;

			boolean firstArticleIsFirst = false;

			if (revSim.getString(ComparisonSimilarity_DB.article1_uri_attr).equals(article1Uri))
				firstArticleIsFirst = true;

			if ((firstArticleIsFirst && revSim.getString(ComparisonSimilarity_DB.article2_uri_attr).equals(article2Uri))
					|| (!firstArticleIsFirst
							&& revSim.getString(ComparisonSimilarity_DB.article1_uri_attr).equals(article2Uri))) {

				Article article1 = new Article(revSim.getString(ComparisonSimilarity_DB.article1_uri_attr),
						Language.getLanguage(revSim.getString(ComparisonSimilarity_DB.language1_attr)));
				Revision revision1 = new Revision(revSim.getLong(ComparisonSimilarity_DB.revision1_id_attr),
						revSim.getDate(ComparisonSimilarity_DB.date_attr), null, article1, article1.getLanguage());

				Article article2 = new Article(revSim.getString(ComparisonSimilarity_DB.article2_uri_attr),
						Language.getLanguage(revSim.getString(ComparisonSimilarity_DB.language2_attr)));
				Revision revision2 = new Revision(revSim.getLong(ComparisonSimilarity_DB.revision2_id_attr),
						revSim.getDate(ComparisonSimilarity_DB.date_attr), null, article2, article2.getLanguage());

				// Create binary comparison
				BinaryComparison comp = null;
				if (firstArticleIsFirst)
					comp = new BinaryComparison(revision1, revision2);
				else
					comp = new BinaryComparison(revision2, revision1);

				comp.setOverallSimilarity(revSim.getDouble(ComparisonSimilarity_DB.overall_similarity_attr));
				comp.setOverallSimilarityString(
						revSim.getString(ComparisonSimilarity_DB.overall_similarity_string_attr));
				comp.setDate(revSim.getDate(ComparisonSimilarity_DB.date_attr));

				comparisons.add(comp);
			}

		}

		// sort comparisons by date
		Collections.sort(comparisons, new Comparator<BinaryComparison>() {
			@Override
			public int compare(BinaryComparison comp1, BinaryComparison comp2) {

				return comp1.getDate().compareTo(comp2.getDate());
			}
		});

		return comparisons;
	}

	public void storeDBObjectPrepared(DbObject obj) {
		dbget.storeDbObject(conn, obj);
	}

	public Map<String, Translation> loadBinaryLanguageLinks(Language originalLanguage, Language targetLanguage,
			Set<String> entitiesInOriginalLanguage) {

		System.out.println("loadBinaryLanguageLinks");

		HashMap<String, Translation> translations = new HashMap<String, Translation>();

		HashSet<String> entitiesInOriginalLanguageEscaped = new HashSet<String>();
		for (String ent : entitiesInOriginalLanguage) {
			String newEnt = ent.replaceAll("\\'", "\\\\'");
			entitiesInOriginalLanguageEscaped.add(newEnt);
		}

		String entitiesString = "'" + StringUtils.join(entitiesInOriginalLanguageEscaped, (String) "','") + "'";
		String query = "SELECT CONVERT(page_title USING utf8) page_title, CONVERT(target_page_title USING utf8) target_page_title, CONVERT(redirected_page_title USING utf8) redirected_page_title FROM language_links WHERE original_language = '"
				+ originalLanguage.getLanguage() + "' AND " + "target_language" + "='" + targetLanguage + "' AND "
				+ "page_title" + " IN(" + entitiesString + ")";
		try {
			ResultSet rs = this.conn.prepareStatement(query).executeQuery();
			while (rs.next()) {
				String originalName = rs.getString("page_title");
				String targetName = rs.getString("target_page_title").replaceAll(" ", "_");
				String redirectedTitle = rs.getString("redirected_page_title");
				if (redirectedTitle != null) {
					redirectedTitle = redirectedTitle.replaceAll(" ", "_");
				}
				Translation translation = new Translation(targetName, redirectedTitle);
				translations.put(originalName, translation);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (String entityName : entitiesInOriginalLanguage) {
			if (translations.containsKey(entityName))
				continue;
			Translation translation = new Translation("-", null);
			translations.put(entityName, translation);
		}
		return translations;
	}

	public Map<Language, Map<Language, Map<String, String>>> loadAllBinaryLanguageLinks() {

		Vector<QueryParam> qparam_or = new Vector<QueryParam>();

		// one of the links has to be English because we use this as pivot
		qparam_or.add(new QueryParam(BinaryLanguageLink_DB.language1_attr, Language.EN.getLanguage()));
		qparam_or.add(new QueryParam(BinaryLanguageLink_DB.language2_attr, Language.EN.getLanguage()));
		Set<BinaryLanguageLink_DB> languageLinksDB = this.dbget.retrieveSelected(this.conn, new BinaryLanguageLink_DB(),
				qparam_or, null, null);

		HashMap<Language, Map<Language, Map<String, String>>> languageLinks = new HashMap<Language, Map<Language, Map<String, String>>>();
		System.out.println("loadAllLanguageLinks: " + languageLinksDB.size());

		for (BinaryLanguageLink_DB link : languageLinksDB) {

			Language lang1 = link.getLanguage(BinaryLanguageLink_DB.language1_attr);
			Language lang2 = link.getLanguage(BinaryLanguageLink_DB.language2_attr);
			String name1 = link.getString(BinaryLanguageLink_DB.name1_attr);
			String name2 = link.getString(BinaryLanguageLink_DB.name2_attr);

			if (!languageLinks.containsKey(lang1))
				languageLinks.put(lang1, new HashMap<Language, Map<String, String>>());

			if (!languageLinks.get(lang1).containsKey(lang2))
				languageLinks.get(lang1).put(lang2, new HashMap<String, String>());

			if (!languageLinks.containsKey(lang2))
				languageLinks.put(lang2, new HashMap<Language, Map<String, String>>());

			if (!languageLinks.get(lang2).containsKey(lang1))
				languageLinks.get(lang2).put(lang1, new HashMap<String, String>());

			languageLinks.get(lang1).get(lang2).put(name1, name2);
			languageLinks.get(lang2).get(lang1).put(name2, name1);
		}

		TimeLogger.getInstance().logTime("bll2");
		return languageLinks;
	}

	public void loadEntityTranslations(Revision revision, Revision targetRevision) {

		HashSet<String> entityNames = new HashSet<String>();
		Language originalLanguage = revision.getLanguage();
		Language targetLanguage = targetRevision.getLanguage();

		// collect entities without translations
		for (String entityName : revision.getEntities().get(originalLanguage).keySet()) {

			Entity entity = revision.getEntities().get(originalLanguage).get(entityName);

			if (entity.getName(targetLanguage) == null)
				entityNames.add(entityName);
		}

		Map<String, Translation> translations = this.loadBinaryLanguageLinks(originalLanguage, targetLanguage,
				entityNames);

		for (String entityName2 : entityNames) {

			Entity entity = revision.getEntities().get(originalLanguage).get(entityName2);
			Translation translation = translations.get(entityName2);

			entity.getNames().put(targetLanguage, translation.getTranslation());

			if (translation.getRedirectedTitle() != null)
				entity.getNames().put(originalLanguage, translation.getRedirectedTitle());
		}
	}

	private void mergeEntities(Revision revision1, Revision revision2) {

		HashMap<String, Entity> entitiesOfRevision1InLanguage2 = new HashMap<String, Entity>();

		for (Sentence s2 : revision1.getSentences()) {

			String language2Name;

			for (InternalLink link : s2.getInternalLinks()) {
				language2Name = link.getEntity().getName(revision2.getLanguage());
				if (language2Name == null)
					continue;
				entitiesOfRevision1InLanguage2.put(language2Name, link.getEntity());
			}

			for (DbPediaLink link : s2.getDbPediaLinks()) {
				language2Name = link.getEntity().getName(revision2.getLanguage());
				if (language2Name == null)
					continue;
				entitiesOfRevision1InLanguage2.put(language2Name, link.getEntity());
			}
		}

		for (Entity entity : revision1.getInternalAndDBpediaEntities().keySet()) {

			String language2Name = entity.getName(revision2.getLanguage());

			if (language2Name != null)
				entitiesOfRevision1InLanguage2.put(language2Name, entity);
		}

		for (Sentence s : revision2.getSentences()) {
			String linkName;

			for (InternalLink link : s.getInternalLinks()) {
				linkName = link.getEntity().getName(revision2.getLanguage());
				Entity entity2 = (Entity) entitiesOfRevision1InLanguage2.get(linkName);

				if (entity2 != null)
					link.setEntity(entity2);
			}

			for (DbPediaLink link : s.getDbPediaLinks()) {
				linkName = link.getEntity().getName(revision2.getLanguage());
				Entity entity2 = (Entity) entitiesOfRevision1InLanguage2.get(linkName);

				if (entity2 != null)
					link.setEntity(entity2);
			}

		}

		Map<Entity, Integer> mergedEntitiesRevision = new HashMap<Entity, Integer>();

		for (Entity entity : revision2.getInternalAndDBpediaEntities().keySet()) {

			String linkName = entity.getName(revision2.getLanguage());
			Entity mergedEntity = (Entity) entitiesOfRevision1InLanguage2.get(linkName);
			int cnt = revision2.getInternalAndDBpediaEntities().get(entity);

			if (mergedEntity != null) {
				mergedEntitiesRevision.put(mergedEntity, cnt);
				continue;
			}

			mergedEntitiesRevision.put(entity, cnt);
		}

		revision2.setInternalAndDBpediaEntities(mergedEntitiesRevision);
	}

	private class Translation {
		private String translation;
		private String redirectedTitle;

		public Translation(String translation, String redirectedTitle) {
			this.translation = translation;
			this.redirectedTitle = redirectedTitle;
		}

		public String getTranslation() {
			return this.translation;
		}

		public String getRedirectedTitle() {
			return this.redirectedTitle;
		}
	}

}
