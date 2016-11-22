package dbpopulate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import translate.Language;
import wiki.WikiLinkGetter;
import wiki.WikiWords;

/**
 * Finds random articles that at least exist in the German and English Wikipedia
 * and - optional - for a third language as well.
 */
public class RandomArticleFinder {

	private static boolean CONTRADICTION_FINDER_OUTPUT_FORMAT = true;

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public static void findRandomArticles(int numberofArticles) throws Exception {
		findRandomArticles(numberofArticles, null);
	}

	// Finds articles in English and German
	public static void findRandomArticles(int numberofArticles, Language thirdLanguage) throws Exception {
		int numberOfResults = 0;

		if (thirdLanguage == null)
			System.out.println("de - en");
		else
			System.out.println("de - en - " + thirdLanguage.getLanguage().toLowerCase());

		while (numberOfResults < numberofArticles) {

			String title;
			boolean foundArticle = false;

			whileLoop: while (!foundArticle) {

				String uri = "https://de.wikipedia.org/w/api.php?action=query&list=random&rnlimit=1&format=json";

				JSONObject json = readJsonFromUrl(uri);

				title = json.getJSONObject("query").getJSONArray("random").getJSONObject(0).getString("title");

				for (String forbiddenPrefix : WikiWords.getInstance().getForbiddenInternalLinks(Language.DE)) {
					if (title.startsWith(forbiddenPrefix))
						continue whileLoop;
				}

				String thirdLangLink = null;

				if (thirdLanguage != null)
					thirdLangLink = WikiLinkGetter.getLanguageLink(title, Language.DE, thirdLanguage).getKey();

				if (thirdLangLink != null || thirdLanguage == null) {
					String englishLangLink = WikiLinkGetter.getLanguageLink(title, Language.DE, Language.EN).getKey();
					if (englishLangLink != null && (thirdLanguage == null || thirdLangLink != null)) {

						if (!CONTRADICTION_FINDER_OUTPUT_FORMAT) {
							if (thirdLangLink != null)
								System.out.println(title + " - " + englishLangLink + " - " + thirdLangLink);
							else
								System.out.println(title + " - " + englishLangLink);
						} else
							System.out.println("en\t" + englishLangLink.replaceAll(" ", "_") + "\tde\t"
									+ title.replaceAll(" ", "_"));

						foundArticle = true;
					}
				}
			}

			numberOfResults += 1;

		}
	}

	public static String findRandomArticles(Language language) {

		String title = null;
		boolean foundArticle = false;

		whileLoop: while (!foundArticle) {

			String uri = "https://" + language.getLanguage()
					+ ".wikipedia.org/w/api.php?action=query&list=random&rnlimit=1&format=json";

			JSONObject json;
			try {
				json = readJsonFromUrl(uri);
				title = json.getJSONObject("query").getJSONArray("random").getJSONObject(0).getString("title");
				for (String forbiddenPrefix : WikiWords.getInstance().getForbiddenInternalLinks(language)) {
					if (title.startsWith(forbiddenPrefix))
						continue whileLoop;
				}
				foundArticle = true;

			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return title;
	}

	public static void main(String[] args) throws Exception {
		RandomArticleFinder.findRandomArticles(900, null);
	}

}
