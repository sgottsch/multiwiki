package extractor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import db.tables.Article_DB;
import db.tables.RevisionHistory_DB;
import db.tables.Revision_DB;
import translate.Language;
import util.FormatUtil;
import util.WikiUtil;
import wiki.WikiAPIQuery;
import wiki.WikiLinkGetter;
import wiki.WikiTextGetter;

public class RevisionExtractor extends Extractor {

	private WikiTextGetter wikiTextGetter;

	public RevisionExtractor(String database, List<Language> languages) {
		super(database, languages);
		this.wikiTextGetter = new WikiTextGetter();
	}

	public Revision_DB loadRevisionByTimestamp(Article_DB article, String timestamp) {

		String url = "https://" + article.getValue(Article_DB.language_attr)
				+ ".wikipedia.org/w/api.php?action=query&prop=revisions&rvlimit=1&rvstart=" + timestamp + "&titles="
				+ article.getValue(Article_DB.wiki_id_attr) + "&rvprop=timestamp|ids";

		Revision_DB revision = new Revision_DB();

		JSONObject json = WikiAPIQuery.queryByUrlOnePage(url);
		try {
			json = json.getJSONObject("query").getJSONObject("pages");
			JSONArray names = json.names();
			json = json.getJSONObject(names.getString(0));

			// TODO: Unfortunately, there is no (simply API using) possibility
			// to retreive the title for a specific revision. It's always the
			// one that's the current one in the article.
			String title = json.getString("title");

			JSONObject jsonRevisions = json.getJSONArray("revisions").getJSONObject(0);

			long revisionId = Long.parseLong(jsonRevisions.getString("revid"));
			String revisionTimestamp = jsonRevisions.getString("timestamp");

			String htmlText = wikiTextGetter.getHTMLTextOfRevision(article.getLanguage(), revisionId,
					article.getString(Article_DB.title_attr));

			revision = new Revision_DB(article, revisionId, title, revisionTimestamp, htmlText);

		} catch (JSONException e) {
			System.out.println("URL: " + url);
			e.printStackTrace();
		}

		return revision;
	}

	public HashMap<String, Integer> loadAuthors(Revision_DB revision) {
		Article_DB article = revision.getArticle();
		return RevisionExtractor.loadAuthors(revision.getLanguage("language"), article.getString("wiki_id"),
				FormatUtil.doubleToString(revision.getValue("revision_id")));
	}

	public static HashMap<String, Integer> loadAuthors(Language language, String wikiId, String revisionId) {

		String url = "https://" + language.getLanguage()
				+ ".wikipedia.org/w/api.php?action=query&prop=revisions&titles=" + wikiId + "&rvprop=user&rvstartid="
				+ revisionId + "&rvdir=older&rvlimit=max";

		HashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(url);

		HashMap<String, Integer> authorNamesAndEdits = new HashMap<String, Integer>();

		for (JSONObject json : jsons) {

			try {
				json = json.getJSONObject("query").getJSONObject("pages");
				json = json.getJSONObject(json.names().getString(0));
				JSONArray jsonArray = json.getJSONArray("revisions");

				for (int i = 0; i < jsonArray.length(); i++) {
					String authorName = jsonArray.getJSONObject(i).getString("user");
					if (authorNamesAndEdits.containsKey(authorName))
						authorNamesAndEdits.put(authorName, authorNamesAndEdits.get(authorName) + 1);
					else
						authorNamesAndEdits.put(authorName, 1);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return authorNamesAndEdits;
	}

	/**
	 * Collect all authors that contributed to a wikipedia revision (and those
	 * revisions the given revision consists of) together with the number of
	 * edits per user. This method does not query the Wikipedia API, but
	 * iterates through the given list of all revisions.
	 * 
	 * @param revision
	 *            Revision
	 * @param allRevisionHistories
	 * @return
	 */
	public HashMap<String, Integer> loadAuthors(Revision_DB revision, List<RevisionHistory_DB> allRevisionHistories) {

		long revisionId = revision.getLong(Revision_DB.revision_id_attr);

		// System.out.println("\nLoad authors of revision " +
		// FormatUtil.doubleToString(revisionId));

		// Extract all those revision that are part of the given revision
		Set<RevisionHistory_DB> revisionHistories = new HashSet<RevisionHistory_DB>();

		long currentParentId = 0;
		boolean foundRevision = false;

		// Iterate through the revisionHistories from new revisions to old
		// revisions. When you find the searched revision id, identify its
		// parent revision id. Go on iterating and find the revision with this
		// parent id. And so on...
		for (RevisionHistory_DB revisionHistory : allRevisionHistories) {

			long currentRevisionId = revisionHistory.getLong(RevisionHistory_DB.revision_id_attr);

			if (!foundRevision && currentRevisionId == revisionId) {
				revisionHistories.add(revisionHistory);
				currentParentId = revisionHistory.getLong(RevisionHistory_DB.parent_revision_id_attr);
				foundRevision = true;
			}

			if (currentRevisionId == currentParentId) {
				revisionHistories.add(revisionHistory);
				currentParentId = revisionHistory.getLong(RevisionHistory_DB.parent_revision_id_attr);
			}

		}

		// for (RevisionHistory rh : revisionHistories) {
		// System.out.println(rh);
		// }

		// Extract the author names together with their number of edits
		HashMap<String, Integer> authorNamesAndEdits = new HashMap<String, Integer>();

		for (RevisionHistory_DB revisionHistory : revisionHistories) {
			String authorName = revisionHistory.getValue(RevisionHistory_DB.author_attr).toString();
			if (authorNamesAndEdits.containsKey(authorName))
				authorNamesAndEdits.put(authorName, authorNamesAndEdits.get(authorName) + 1);
			else
				authorNamesAndEdits.put(authorName, 1);
		}

		// System.out.println("\nAuthors with edits");
		//
		// for (String author : authorNamesAndEdits.keySet()) {
		// System.out.println(author + ": " + authorNamesAndEdits.get(author));
		// }

		return authorNamesAndEdits;
	}

	/**
	 * Extracts from the revision's html all the external links that are
	 * mentioned in the article in a footnote and returns a mapping of all
	 * footnote ids to their external link.
	 * 
	 * @param revision
	 *            Revision whose footnotes are looked for.
	 * @return HashMap that contains the footnote ids and the belonging external
	 *         link.
	 */
	public HashMap<String, String> loadExternalLinks(Revision_DB revision) {

		HashMap<String, String> footnotesWithLinks = new HashMap<String, String>();

		String text = revision.getValue(Revision_DB.original_html_text_attr).toString();

		Document doc = Jsoup.parse(text);

		Elements footnoteLis = doc.select("li[id^=cite_note-]");

		for (Element footnoteLi : footnoteLis) {
			String liId = footnoteLi.attr("id");
			String refId = liId.substring(liId.indexOf("-") + 1, liId.length());

			String url = footnoteLi.select("a.external").attr("href");

			if (!url.isEmpty())
				footnotesWithLinks.put(refId, url);
		}

		return footnotesWithLinks;
	}

	public Set<String> loadImages(Revision_DB revision) {

		Article_DB article = revision.getArticle();

		String url = "http://" + article.getValue(Article_DB.language_attr)
				+ ".wikipedia.org/w/api.php?action=parse&oldid="
				+ FormatUtil.doubleToString(revision.getValue(Revision_DB.revision_id_attr)) + "&prop=images";

		HashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(url);

		Set<String> imageUrls = new HashSet<String>();

		for (JSONObject json : jsons) {
			try {
				JSONArray jsonArray = json.getJSONObject("parse").getJSONArray("images");

				for (int i = 0; i < jsonArray.length(); i++) {
					String imageName = jsonArray.getString(i);
					String imageUrl = WikiUtil.getURLOfImage(imageName);
					imageUrls.add(imageUrl);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return imageUrls;

	}

	public HashMap<String, HashMap<Language, String>> loadInternalLinks(Revision_DB revision,
			HashMap<String, HashMap<Language, String>> allTitlesWithInternalLinks, List<Language> languages) {

		HashMap<String, HashMap<Language, String>> internalLinks = new HashMap<String, HashMap<Language, String>>();

		Article_DB article = revision.getArticle();

		String url = "http://" + article.getLanguage() + ".wikipedia.org/w/api.php?action=parse&oldid="
				+ FormatUtil.doubleToString(revision.getValue(Revision_DB.revision_id_attr)) + "&prop=links";

		List<String> newTitles = new ArrayList<String>();

		try {
			JSONObject json = WikiAPIQuery.queryByUrlOnePage(url);
			JSONArray jsonArray = json.getJSONObject("parse").getJSONArray("links");

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonLink = jsonArray.getJSONObject(i);
				String title = jsonLink.getString("*");
				if (allTitlesWithInternalLinks.containsKey(title))
					internalLinks.put(title, allTitlesWithInternalLinks.get(title));
				else
					newTitles.add(title);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// For all the titles where no lang link was found for: Make another
		// query
		List<String> titlesParams = new ArrayList<String>();

		String titlesParamPart = "";

		int titlesCount = 0;
		for (String title : newTitles) {
			titlesParamPart += title + "|";
			if (titlesCount > 0 && (titlesCount + 1) % 50 == 0 || titlesCount == newTitles.size() - 1) {
				titlesParamPart = titlesParamPart.substring(0, titlesParamPart.length() - 1);
				titlesParams.add(titlesParamPart);
				titlesParamPart = "";
			}
			titlesCount += 1;
		}

		for (String titlesParam : titlesParams) {
			internalLinks = WikiLinkGetter.loadLangLinksForTitles(revision.getArticle().getLanguage(), titlesParam,
					languages, internalLinks);
		}

		return internalLinks;
	}

	public static Revision_DB transformToRevision(RevisionHistory_DB rh, Article_DB article) {
		WikiTextGetter wikiTextGetter = new WikiTextGetter();
		String htmlText = wikiTextGetter.getHTMLTextOfRevision(article.getLanguage(),
				rh.getLong(RevisionHistory_DB.revision_id_attr).longValue(), article.getString(Article_DB.title_attr));

		Revision_DB r = new Revision_DB(article, rh.getLong(RevisionHistory_DB.revision_id_attr),
				article.getValue(Article_DB.title_attr).toString(),
				(Date) rh.getValue(RevisionHistory_DB.start_date_attr), htmlText);
		return r;
	}

}
