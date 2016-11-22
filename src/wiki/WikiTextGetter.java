package wiki;

import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import model.Revision;
import translate.Language;
import util.FormatUtil;
import util.URLUtil;

public class WikiTextGetter {
	private boolean debug = false;

	public String[] getTextArray(String articleURL) throws Exception {
		URL url = new URL(articleURL);

		// NOTE: Use ArticleExtractor unless DefaultExtractor gives better
		// results for you
		String text = ArticleExtractor.INSTANCE.getText(url);

		// splitting works but does not differentiate between paragraphs and
		// subtitles.
		// could join short paragraphs to the follow-up to avoid
		String[] parts = text.split("\n");

		if (debug) {
			for (String part : parts) {
				System.out.println(part + "\n\n");
			}
		}

		return parts;
	}

	public String getText(String url_s) throws Exception {
		URL url = new URL(url_s);
		// NOTE: Use ArticleExtractor unless DefaultExtractor gives better
		// results for you
		String text = ArticleExtractor.INSTANCE.getText(url);
		return text;

	}

	public String getText(URL url) throws Exception {
		// URL url = new URL(articleURL);
		// NOTE: Use ArticleExtractor unless DefaultExtractor gives better
		// results for you
		String text = DefaultExtractor.INSTANCE.getText(url);

		return text;
	}

	public String getExtractedText(Language lang, String wikiId) throws Exception {
		String articleURL = "https://" + lang.getLanguage() + ".wikipedia.org/wiki/" + wikiId;
		URL url = new URL(articleURL);
		String text = DefaultExtractor.INSTANCE.getText(url);
		
		// Remove foot notes
		String regex = "\\[\\d{1,3}\\]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		text = m.replaceAll("");
		
		return text;
	}

	public String getHTMLText(Language lang, String wikiId) throws Exception {
		String articleURL = "https://" + lang.getLanguage() + ".wikipedia.org/w/api.php?action=parse&page=" + wikiId
				+ "&prop=text";
		JSONObject json = WikiAPIQuery.queryByUrlOnePage(articleURL);
		String text = json.getJSONObject("parse").getJSONObject("text").getString("*");
		return text;
	}

	public Revision getRevision(Language lang, String wikiId) throws Exception {
		String articleURL = "https://" + lang.getLanguage() + ".wikipedia.org/w/api.php?action=parse&page=" + wikiId
				+ "&prop=text|revid";
		JSONObject json = WikiAPIQuery.queryByUrlOnePage(articleURL);
		String text = json.getJSONObject("parse").getJSONObject("text").getString("*");
		long revisionNumber = json.getJSONObject("parse").getLong("revid");
		Revision revision = new Revision(revisionNumber, new Date(), wikiId, null, lang);
		
		// Add a heading for the article title
		text = "<h1 id=\"firstHeading\" class=\"firstHeading\"><span>" + wikiId.replaceAll("_", " ") + "</span></h1>"
				+ text;
		revision.setHtmlText(text);

		return revision;
	}

	public String getHTMLText2(Language lang, String title) {
		String articleURL = "https://" + lang.getLanguage() + ".wikipedia.org/w/api.php?action=parse&page=" + title
				+ "&prop=text";

		JSONObject json = WikiAPIQuery.queryByUrlOnePage(articleURL);
		
		if (json == null) {
			System.err.println("Could not get HTML text. Try again.");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return this.getHTMLText2(lang, title);
		}
		
		String text = "";
		try {
			text = json.getJSONObject("parse").getJSONObject("text").getString("*");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Add a heading for the article title
		text = "<h1 id=\"firstHeading\" class=\"firstHeading\"><span>" + title.replaceAll("_", " ") + "</span></h1>"
				+ text;
		
		return text;
	}

	public String getHTMLTextOfRevision(Language lang, long revisionId, String title) {
		String articleURL = "https://" + lang.getLanguage() + ".wikipedia.org/w/api.php?action=parse&prop=text&oldid="
				+ FormatUtil.doubleToString(revisionId) + "&utf8";
		
		JSONObject json = WikiAPIQuery.queryByUrlOnePage(articleURL);
		
		if (json == null) {
			System.err.println("Could not get HTML text. Try again.");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return this.getHTMLTextOfRevision(lang, revisionId, title);
		}
		
		String text = "";
		try {
			text = json.getJSONObject("parse").getJSONObject("text").getString("*");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Add a heading for the article title
		text = "<h1 id=\"firstHeading\" class=\"firstHeading\"><span>" + title.replaceAll("_", " ") + "</span></h1>"
				+ text;
		
		return text;
	}

	public String getWikiSyntaxText(Language lang, String wikiId) throws Exception {
		String articleURL = "https://" + lang.getLanguage()
				+ ".wikipedia.org/w/api.php?format=json&action=query&titles=" + wikiId
				+ "&prop=revisions&rvprop=content";
		JSONObject json = new JSONObject(URLUtil.readUrl(articleURL));
		json = json.getJSONObject("query").getJSONObject("pages");
		JSONArray jsonArray = json.getJSONObject((String) json.names().get(0)).getJSONArray("revisions");
		String text = jsonArray.getJSONObject(0).getString("*");
		return text;
	}

	public static void main(String[] args) throws Exception {
		WikiTextGetter getter = new WikiTextGetter();
		Language lang = Language.EN;
		String wikiId = "Museum_of_Old_and_New_Art";
		System.out.println("Extracted Text");
		System.out.println(getter.getExtractedText(lang, wikiId));
		System.out.println("---------");
		System.out.println("HTML Text");
		System.out.println(getter.getHTMLText(lang, wikiId));
		System.out.println("---------");
		System.out.println("Wiki Syntax Text");
		System.out.println(getter.getWikiSyntaxText(lang, wikiId));
	}
}
