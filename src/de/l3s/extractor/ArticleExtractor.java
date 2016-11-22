package de.l3s.extractor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.db.DbObject;
import de.l3s.db.QueryParam;
import de.l3s.db.tables.Article_DB;
import de.l3s.db.tables.RevisionHistory_DB;
import de.l3s.translate.Language;
import de.l3s.util.FormatUtil;
import de.l3s.wiki.WikiAPIQuery;
import de.l3s.wiki.WikiLinkGetter;

public class ArticleExtractor extends Extractor {

	private WikiLinkGetter wikiLinkGetter;
	private String wikiPrefix = "https://en.wikipedia.org/wiki/";

	public ArticleExtractor(String database, List<Language> languages) {
		super(database, languages);
		this.wikiLinkGetter = new WikiLinkGetter();
	}

	/**
	 * Returns a set of articles that the language links of the current article
	 * refers to.
	 * 
	 * @return set of articles that the language links of the current article
	 *         refers to
	 */
	public Set<Article_DB> loadOtherLanguageArticles(String englishWikiId) {

		// normalize language specific id, replace white spaces with
		// underscores
		englishWikiId = englishWikiId.replaceAll(" ", "_");

		String englishUrl = wikiPrefix + englishWikiId;

		Set<Article_DB> articles = new HashSet<Article_DB>();

		// Add current article name (title) -> not done, because difficult/not
		// possible (even in old revisions, the article name is always the
		// recent one)

		try {
			TreeMap<Language, String> langLinks = wikiLinkGetter.getLanguageLinks(englishWikiId);

			// for each language
			for (Language lang : langLinks.keySet()) {
				// normalize language specific id, replace white spaces with
				// underscores
				String langId = langLinks.get(lang).replaceAll(" ", "_");
				// build language specific URL
				String langUrl = "https://" + lang + ".wikipedia.org/wiki/" + langId;
				// print language specific URL
				// System.out.println("Found article link for " + englishWikiId
				// + " - language: " + lang + ", id: "
				// + langId + ", url: " + langUrl);

				// TODO: Add current article name
				Article_DB article = new Article_DB(langUrl, lang.getLanguage(), langId, englishUrl, "NULL");

				articles.add(article);
				// dbget.storeDbObject(conn, al);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return articles;
	}

	// Retrieve all images of the current revision together with their title
	// string
	// not used
	public HashMap<String, String> loadImages(Article_DB article) {

		String url = "https://" + article.getValue(Article_DB.language_attr)
				+ ".wikipedia.org/w/api.php?action=query&prop=imageinfo&iiprop=url&generator=images&gimlimit=max";

		HashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(url);

		HashMap<String, String> imageUrls = new HashMap<String, String>();

		for (JSONObject json : jsons) {
			try {
				json = json.getJSONObject("query").getJSONObject("pages");
				JSONArray names = json.names();

				for (int i = 0; i < names.length(); i++) {
					JSONObject json2 = json.getJSONObject(names.getString(i));
					String title = json2.getString("title");
					JSONArray jsonArray = json2.getJSONArray("imageinfo");
					JSONObject json3 = jsonArray.getJSONObject(0);
					imageUrls.put(title, json3.getString("url"));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return imageUrls;
	}

	public HashMap<String, HashMap<Language, String>> loadTitlesWithInternalLinks(Article_DB article,
			List<Language> languages) {

		System.out.println(article.getValue(Article_DB.article_uri_attr));
		System.out.println(article.getValue(Article_DB.wiki_id_attr));

		return WikiLinkGetter.getTitlesWithLinks(article, article.getLanguage(), languages);
	}

	public List<RevisionHistory_DB> loadRevisionHistory(Article_DB article) {

		List<RevisionHistory_DB> revisionHistories = new ArrayList<RevisionHistory_DB>();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(RevisionHistory_DB.article_uri_attr,
				article.getValue(Article_DB.article_uri_attr).toString(), java.sql.Types.VARCHAR));
		qparam_and.add(new QueryParam(RevisionHistory_DB.valid_attr, "true", java.sql.Types.BOOLEAN));

		connect();
		// Retrieve the annotations that were updated before
		Set<DbObject> storedRevisionHistoryObjects = dbget.retrieveSelected(conn, new RevisionHistory_DB(), null,
				qparam_and, "1", "date DESC");
		storedRevisionHistoryObjects = new HashSet<DbObject>();
		Long newestRevisionId = null;

		if (storedRevisionHistoryObjects.size() > 0) {
			// A revision history was already stored for this article

			System.out.println("A revision history has already been stored for article "
					+ article.getValue(Article_DB.article_uri_attr));

			for (DbObject rHObj : storedRevisionHistoryObjects) {
				revisionHistories.add((RevisionHistory_DB) rHObj);
			}

			// Find out the revision id of the newest revision that is stored
			// fors this article
			newestRevisionId = revisionHistories.get(0).getLong(RevisionHistory_DB.revision_id_attr);

			// Remove newest revision again (the only one when working with
			// limit 1)
			revisionHistories.remove(0);
		}

		HashMap<Long, RevisionHistory_DB> datesWithRevisionHistories = new HashMap<Long, RevisionHistory_DB>();

		String url = "https://" + article.getValue(Article_DB.language_attr)
				+ ".wikipedia.org/w/api.php?action=query&prop=revisions&titles="
				+ article.getValue(Article_DB.wiki_id_attr) + "&rvprop=user|ids|flags|timestamp|size|sha1"
				+ "&rvdir=older&rvlimit=max";

		// Only retreive the revisions from now until the date of the newest
		// stored revision (the newest already stored revision will be
		// retreived!)
		if (newestRevisionId != null)
			url += "&rvendid=" + FormatUtil.doubleToString(newestRevisionId);

		LinkedHashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(url);

		for (JSONObject json : jsons) {

			try {
				json = json.getJSONObject("query").getJSONObject("pages");
				json = json.getJSONObject(json.names().getString(0));

				// Add title to the article
				article.setValue(Article_DB.title_attr, json.getString("title"));

				JSONArray jsonArray = json.getJSONArray("revisions");

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject currentRevisionJSON = jsonArray.getJSONObject(i);

					if (!currentRevisionJSON.has("user"))
						continue;

					if (!currentRevisionJSON.has("sha1"))
						continue;

					String authorName = currentRevisionJSON.getString("user");

					Long parentId;
					if (currentRevisionJSON.has("parentid"))
						parentId = currentRevisionJSON.getLong("parentid");
					else
						parentId = null;

					boolean minor;
					if (currentRevisionJSON.has("minor"))
						minor = true;
					else
						minor = false;

					long size = currentRevisionJSON.getLong("size");

					Long revisionId = currentRevisionJSON.getLong("revid");

					String timestamp = currentRevisionJSON.getString("timestamp");

					String hash = currentRevisionJSON.getString("sha1");

					RevisionHistory_DB revisionHistory = new RevisionHistory_DB(article, revisionId, parentId,
							authorName, minor, size, timestamp, null, hash, true);

					if (newestRevisionId != null && revisionId == newestRevisionId)
						revisionHistory.setIsNew(false);

					revisionHistories.add(revisionHistory);
					datesWithRevisionHistories.put(revisionId, revisionHistory);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		// Find out the end date for each revision. That means, the date, when
		// another revision was created that is based on the revision whose end
		// date you are searching for.
		for (RevisionHistory_DB revisionHistory : revisionHistories) {
			Long parentId = revisionHistory.getLong(RevisionHistory_DB.parent_revision_id_attr);

			if (datesWithRevisionHistories.containsKey(parentId)) {
				RevisionHistory_DB parentRevision = datesWithRevisionHistories.get(parentId);
				parentRevision.setValue(RevisionHistory_DB.end_date_attr,
						(Timestamp) revisionHistory.getValue(RevisionHistory_DB.start_date_attr));
			}

		}

		// the newest revision (that is not parent of any other revision) gets
		// the current timestamp as end date.
		// This is also correct for newest already stored article whose date
		// must be updated if there is no newer one.
		RevisionHistory_DB newestRevision = revisionHistories.get(0);
		// newestRevision.setValue(RevisionHistory.end_date_attr,
		// new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date()));

		Timestamp now = new Timestamp(new Date().getTime());
		newestRevision.setValue(RevisionHistory_DB.end_date_attr, now);

		// If the newest revision is already stored: Just change the end date by
		// update.
		if (!newestRevision.isNew()) {
			newestRevision.addUpdateValue(RevisionHistory_DB.end_date_attr, now);
			dataStore.addRevisionsHistoriesToUpdate(newestRevision);
		}

		markInvalidRevisions(revisionHistories);

		// System.out.println("All revision histories:");
		// for (RevisionHistory revisionHistory : revisionHistories)
		// if (!revisionHistory.getBoolean(RevisionHistory.valid_attr))
		// System.out.println(revisionHistory.getValue(RevisionHistory.article_uri_attr)
		// + " - "
		// + revisionHistory.getValue(RevisionHistory.revision_number_attr) +
		// " ("
		// + revisionHistory.getValue(RevisionHistory.date_attr));

		return revisionHistories;
	}

	/**
	 * Use some heuristic rules to mark revisions as invalid for further
	 * progress.
	 */
	public void markInvalidRevisions(List<RevisionHistory_DB> revisionHistories) {

		// Heuristics:
		// 1. time to next revision must be bigger than 15 minutes (to avoid
		// problems with very active contributing)
		// 2. Only take revisions that have an end time (so, no revision that
		// was not continued)
		// 3. Avoid edit war versions by not taking a revision where another
		// revision with the same content hash value exists within 5 revisions
		// Assumption: Two articles only
		// 4. Size of the article must be bigger than 300

		Set<RevisionHistory_DB> invalidRevisions = new HashSet<RevisionHistory_DB>();

		int windowSize = 5;
		// While iterating over the revisions: Keep track of the hashes of the
		// five previous and the five next revisionss. Doing so, you can
		// identify, when a revision lies in between two revisions with the same
		// hash.
		CircularFifoQueue<String> olderHashes = new CircularFifoQueue<String>(windowSize);
		CircularFifoQueue<String> newerHashes = new CircularFifoQueue<String>(windowSize);

		for (int i = revisionHistories.size() - 1; i > Math.max(revisionHistories.size() - windowSize - 2, -1); i--) {
			RevisionHistory_DB revision = revisionHistories.get(i);
			newerHashes.add(revision.getValue(RevisionHistory_DB.hash_attr).toString());
		}

		// TODO: Don't take revisions with too big size changes?

		// Start with the oldest revision
		for (int i = revisionHistories.size() - 1; i >= 0; i--) {

			boolean testCase = false;

			RevisionHistory_DB revision = revisionHistories.get(i);

			if (revision.getLong(RevisionHistory_DB.revision_id_attr) == 29501156)
				testCase = true;

			long revisionTimeMS = ((Timestamp) revision.getValue(RevisionHistory_DB.start_date_attr)).getTime();

			if (i != 0) {
				RevisionHistory_DB newerRevision = revisionHistories.get(i - 1);
				long newerRevisionTimeMS = ((Timestamp) newerRevision.getValue(RevisionHistory_DB.start_date_attr)).getTime();
				// 15 minutes = 900 000 milliseconds

				if (newerRevisionTimeMS - revisionTimeMS < 900000) {
					if (testCase)
						System.out.println("is invalid");
					invalidRevisions.add(revision);
				}
			}

			if (i < windowSize)
				newerHashes.remove();
			else {
				RevisionHistory_DB newerWindowRevision = revisionHistories.get(i - windowSize);
				newerHashes.add(newerWindowRevision.getValue(RevisionHistory_DB.hash_attr).toString());
			}

			if (i != revisionHistories.size() - 1) {
				RevisionHistory_DB olderRevision = revisionHistories.get(i + 1);
				olderHashes.add(olderRevision.getValue(RevisionHistory_DB.hash_attr).toString());
			}

			if (!Collections.disjoint(olderHashes, newerHashes))
				invalidRevisions.add(revision);

			// revisions without end date are invalid
			if (revision.getValue(RevisionHistory_DB.end_date_attr) == null)
				invalidRevisions.add(revision);

			// revisions without too small texts are invalid
			if (revision.getLong(RevisionHistory_DB.size_attr) < 300) {
				invalidRevisions.add(revision);
			}
		}

		System.out.println("invalid revisions: " + invalidRevisions.size() + " of " + revisionHistories.size());

		for (RevisionHistory_DB invalidRevision : invalidRevisions) {
			invalidRevision.setValue(RevisionHistory_DB.valid_attr, false);
		}

	}

}
