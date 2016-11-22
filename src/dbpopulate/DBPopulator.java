package dbpopulate;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import db.tables.Article_DB;
import db.tables.Author_DB;
import db.tables.Comparison_DB;
import db.tables.RevisionHistory_DB;
import db.tables.Revision_Author_DB;
import db.tables.Revision_DB;
import extractor.ArticleExtractor;
import extractor.ExtractionConfigThreadSafe;
import extractor.ExtractionDataStore;
import extractor.Extractor;
import extractor.ParagraphExtractor;
import extractor.RevisionExtractor;
import translate.Language;
import util.DateUtil;
import util.TimeLogger;

/**
 * This class starts the preprocessing pipeline for those steps that are
 * independent of external tools and only use the Wikipedia revision HTML itself
 * and some Wikipedia API calls. It is the first step that has to executed if a
 * new article is to be stored in the data base.
 */
public class DBPopulator extends Extractor {

	private ArticleExtractor articleExtractor;
	private RevisionExtractor revisionExtractor;

	private Language language1;
	private Language language2;

	public DBPopulator(String database, Language language1, Language language2) {
		super(database, language1, language2);

		if (!languages.contains(Language.EN)) {
			throw new NullPointerException("Languages must contain English.");
		}

		this.language1 = language1;
		this.language2 = language2;
		this.articleExtractor = new ArticleExtractor(database, languages);
		this.revisionExtractor = new RevisionExtractor(database, languages);
	}

	/**
	 * Loads comparions of different revisions of the given wiki id and their
	 * revisions for the given languages.
	 * 
	 * @param englishWikiId
	 *            Name of the Wiki page in the English wikipedia
	 */
	public void storeArticle(String englishWikiId, Date comparisonDate) {
		ExtractionConfigThreadSafe.getInstance().init(this.languages);
		TimeLogger.getInstance().start();

		// TODO: Find out whether article is already stored
		// TODO: Find out whether article exists / wikiId is valid

		this.dataStore = new ExtractionDataStore();
		englishWikiId = englishWikiId.replaceAll(" ", "_");

		// load an article instance for each language
		Map<Language, Article_DB> articles = loadArticles(englishWikiId);

		Article_DB article1 = articles.get(language1);
		Article_DB article2 = articles.get(language2);

		this.dataStore.addArticles(articles.values());

		// load the revision histories for each article
		List<RevisionHistory_DB> revisionHistoriesOfArticle1 = loadRevisionHistoryOfArticle(article1);
		this.dataStore.addRevisionHistories(revisionHistoriesOfArticle1);
		Revision_DB revision1 = getRevisionFromDate(revisionHistoriesOfArticle1, article1, comparisonDate);
		List<RevisionHistory_DB> revisionHistoriesOfArticle2 = loadRevisionHistoryOfArticle(article2);
		this.dataStore.addRevisionHistories(revisionHistoriesOfArticle2);
		Revision_DB revision2 = getRevisionFromDate(revisionHistoriesOfArticle2, article2, comparisonDate);

		Comparison_DB comparison = new Comparison_DB(revision1, revision2, comparisonDate);
		this.dataStore.addComparison(comparison);

		loadRevision(article1, revision1, revisionHistoriesOfArticle1);
		loadRevision(article2, revision2, revisionHistoriesOfArticle2);

		System.out.println("Store results in database.");
		TimeLogger.getInstance().logTotalTime("Total time: ");

		System.out.println("Revisions");
		for (Revision_DB revision : this.dataStore.getRevisions()) {
			System.out.println(revision.getValue(Revision_DB.article_uri_attr) + " - "
					+ revision.getValue(Revision_DB.revision_id_attr) + "(" + revision.getValue("date") + ")");
		}

		System.out.println("Comparison from " + comparison.getValue("date"));
		System.out.println(comparison.getValue(Comparison_DB.article1_uri_attr) + " - "
				+ comparison.getLong(Comparison_DB.revision1_id_attr));
		System.out.println(comparison.getValue(Comparison_DB.article2_uri_attr) + " - "
				+ comparison.getLong(Comparison_DB.revision2_id_attr));

		this.connect();
		this.dataStore.store(this.conn, this.dbget);
		TimeLogger.getInstance().logTotalTime("End.");
	}

	private void loadRevision(Article_DB article, Revision_DB revision,
			List<RevisionHistory_DB> revisionHistoriesOfArticle) {

		System.out.println("Article: " + article.getString(Article_DB.article_uri_attr));
		TimeLogger.getInstance().logTime("article");

		this.dataStore.addRevision(revision);

		HashMap<String, Integer> authorNamesAndEdits = this.revisionExtractor.loadAuthors(revision,
				revisionHistoriesOfArticle);

		System.out.println("Found Revision: " + revision.getLong(Revision_DB.revision_id_attr) + " - "
				+ revision.getString(Article_DB.article_uri_attr));

		// Add images of revision
		// Set<String> imageUrls =
		// revisionExtractor.loadImages(revision);
		// for (String imageUrl : imageUrls) {
		// Revision_Image revision_image = new
		// Revision_Image(revision, imageUrl);
		// dataStore.addRevisionImage(revision_image);
		// }

		// Add authors of revision
		// HashMap<String, Integer> authorNamesAndEdits =
		// revisionExtractor.loadAuthors(revision);

		for (String authorName : authorNamesAndEdits.keySet()) {
			Author_DB author = new Author_DB(authorName, "NULL");
			this.dataStore.addAuthor(author);
			this.dataStore
					.addRevisionAuthor(new Revision_Author_DB(revision, author, authorNamesAndEdits.get(authorName)));
		}
		ParagraphExtractor paragraphExtractor = new ParagraphExtractor(this.database, revision, this.languages,
				this.dataStore);
		paragraphExtractor.extractAll();

		// Add images of revision
		paragraphExtractor.loadImagesOfRevision();

		// Add internal and external links of revision
		paragraphExtractor.loadLinksOfRevision();

		TimeLogger.getInstance().logTime("revision " + revision.getLong(Revision_DB.revision_id_attr) + " end");
		System.out.println("");
	}

	private Revision_DB getRevisionFromDate(List<RevisionHistory_DB> revisionHistoriesOfArticle, Article_DB article,
			Date comparisonDate) {

		RevisionHistory_DB lastRevisionHistoryEntry = null;
		for (RevisionHistory_DB revisionHistoryEntry : revisionHistoriesOfArticle) {
			if (DateUtil.dateWithin(comparisonDate, revisionHistoryEntry.getDate(RevisionHistory_DB.start_date_attr),
					revisionHistoryEntry.getDate(RevisionHistory_DB.end_date_attr)))
				return RevisionExtractor.transformToRevision(revisionHistoryEntry, article);
			lastRevisionHistoryEntry = revisionHistoryEntry;
		}

		if (comparisonDate.after(lastRevisionHistoryEntry.getDate(RevisionHistory_DB.start_date_attr)))
			return RevisionExtractor.transformToRevision(lastRevisionHistoryEntry, article);

		return null;
	}

	private List<RevisionHistory_DB> loadRevisionHistoryOfArticle(Article_DB article) {
		List<RevisionHistory_DB> revisionHistoriesOfArticle = this.articleExtractor.loadRevisionHistory(article);
		return revisionHistoriesOfArticle;
	}

	private Map<Language, Article_DB> loadArticles(String englishWikiId) {

		Set<Article_DB> articles = this.articleExtractor.loadOtherLanguageArticles(englishWikiId);

		Map<Language, Article_DB> articlesByLanguage = new HashMap<Language, Article_DB>();

		for (Article_DB article : articles) {
			article.setString(Article_DB.article_uri_attr,
					article.getString(Article_DB.article_uri_attr).replace("https:", "http:"));
		}

		TimeLogger.getInstance().logTime("articles");

		// Load revisionHistoryPerArticle
		for (Iterator<Article_DB> iterator = articles.iterator(); iterator.hasNext();) {
			Article_DB article = iterator.next();

			if (!this.languages.contains(article.getLanguage())) {
				// Don't care about articles that are not in the queried
				// languages
				iterator.remove();
				continue;
			}

			articlesByLanguage.put(article.getLanguage(), article);
		}

		return articlesByLanguage;
	}

}
