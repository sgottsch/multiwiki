package de.l3s.wiki;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import de.l3s.db.tables.Article_DB;
import de.l3s.translate.Language;

public class WikiLinkGetter {
	private Client client;
	public String languages = "cs|de|es|fr|it|pl|pt|ru";
	private boolean debug = false;

	public WikiLinkGetter() {
		// TODO Auto-generated constructor stub
		client = Client.create();

	}

	public TreeMap<Language, String> getLanguageLinks(String title) throws Exception {
		// lang - > title
		TreeMap<Language, String> result = new TreeMap<Language, String>();
		String request = "https://www.wikidata.org/w/api.php" + "?action=wbgetentities&sites=enwiki&titles=" +
				// URLEncoder.encode(
				title +
				// , "UTF-8")+
				// URLEncoder.encode(
		"&languages=" + URLEncoder.encode(languages, "UTF-8") +
				// , "UTF-8")+
				// URLEncoder.encode(
				// "&props=labels&format=json";
		"&props=sitelinks&format=json";
		// , "UTF-8");
		if (debug)
			System.out.println(request);

		System.out.println(request);

		// request= URLEncoder.encode(request, "UTF-8");

		client.setReadTimeout(30000);
		WebResource webResource = client.resource(request);
		// requestCount++;

		if (debug)
			System.out.println(request + " " + new Date());

		ClientResponse cr = webResource.accept("application/json").get(ClientResponse.class);
		int status = cr.getStatus();

		if (status != 200) {
			System.out.println("STATUS " + status);
			return null;
		}

		String s = "";

		s = cr.getEntity(String.class);

		JSONObject jsonobject = new JSONObject(s);

		if (jsonobject.has("entities")) {
			JSONObject entities = jsonobject.getJSONObject("entities");

			if (debug)
				System.out.println(entities);

			String id = (String) entities.keys().next();

			JSONObject entity = (JSONObject) entities.get(id);

			// if (entity.has("labels")) {
			// JSONObject labels = entity.getJSONObject("labels");
			//
			// JSONArray names = labels.names();
			//
			// for (int i = 0; i < labels.length(); i++) {
			//
			// String name = (String) names.get(i);
			//
			// JSONObject langs = (JSONObject) labels.get(name);
			//
			// String value = langs.getString("value");
			// String language = langs.getString("language");
			// Language lang = Language.getLanguage(language);
			//
			// // System.out.println(value + " " + language);
			//
			// if (lang != null) {
			// result.put(lang, value);
			// } else {
			// System.err.println("Language is not defined as enum: " +
			// language);
			// }
			//
			// }
			// }

			if (entity.has("sitelinks")) {
				JSONObject sitelinks = entity.getJSONObject("sitelinks");

				JSONArray names = sitelinks.names();

				for (int i = 0; i < sitelinks.length(); i++) {

					String name = (String) names.get(i);

					JSONObject langs = (JSONObject) sitelinks.get(name);

					String value = langs.getString("title");
					String language = langs.getString("site");
					// Transform e.g. "enwiki" to "en" by removing "wiki"
					language = language.substring(0, language.length() - 4);
					Language lang = Language.getLanguage(language);

					// System.out.println(value + " " + language);

					if (lang != null) {
						result.put(lang, value);
					} else {
						// System.err.println("Language is not defined as enum:
						// "
						// + language);
					}

				}
			}

		}

		return result;

	}

	public static HashMap<String, HashMap<Language, String>> getTitlesWithLinks(Article_DB article,
			Language originalLanguage, List<Language> targetLanguages) {

		HashMap<String, HashMap<Language, String>> allTitlesWithLinks = new HashMap<String, HashMap<Language, String>>();

		for (Language targetLanguage : targetLanguages) {
			HashMap<String, HashMap<Language, String>> titlesWithLinks = getTitlesWithLinks(article, originalLanguage,
					targetLanguage);
			for (String title : titlesWithLinks.keySet()) {
				if (allTitlesWithLinks.containsKey(title)) {
					HashMap<Language, String> daa = allTitlesWithLinks.get(title);
					for (Language lang : titlesWithLinks.get(title).keySet()) {
						daa.put(lang, titlesWithLinks.get(title).get(lang));
					}
				} else
					allTitlesWithLinks.put(title, titlesWithLinks.get(title));
			}
		}

		return allTitlesWithLinks;
	}

	public static HashMap<String, HashMap<Language, String>> getTitlesWithLinks(Article_DB article,
			Language originalLanguage, Language targetLanguage) {

		String url = "https://" + originalLanguage.getLanguage()
				+ ".wikipedia.org/w/api.php?action=query&generator=links&prop=langlinks&lllang=en&redirects&titles="
				+ article.getValue(Article_DB.wiki_id_attr) + "&gpllimit=max&lllimit=max";

		return getTitlesWithLinks(url, originalLanguage, targetLanguage);
	}

	private static HashMap<String, HashMap<Language, String>> getTitlesWithLinks(String url, Language originalLanguage,
			Language targetLanguage) {
		HashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(url);

		HashMap<String, String> redirects = new HashMap<String, String>();

		HashMap<String, HashMap<Language, String>> titlesWithLinks = new HashMap<String, HashMap<Language, String>>();

		for (JSONObject json : jsons) {
			try {
				json = json.getJSONObject("query");
				if (json.has("redirects")) {
					JSONArray jsonArrayRedirects = json.getJSONArray("redirects");
					for (int i = 0; i < jsonArrayRedirects.length(); i++) {
						JSONObject jsonRedirect = jsonArrayRedirects.getJSONObject(i);
						redirects.put(jsonRedirect.getString("to"), jsonRedirect.getString("from"));
					}
				}
				json = json.getJSONObject("pages");
				JSONArray pages = json.names();

				for (int i = 0; i < pages.length(); i++) {
					JSONObject page = json.getJSONObject(pages.getString(i));
					String title = page.getString("title");

					HashMap<Language, String> pageLanguageLinks = new HashMap<Language, String>();
					pageLanguageLinks.put(originalLanguage, title);

					if (!page.has("missing") && page.has("langlinks")) {
						JSONArray langlinks = page.getJSONArray("langlinks");
						JSONObject langLink = langlinks.getJSONObject(0);

						pageLanguageLinks.put(targetLanguage, langLink.getString("*"));
					}

					titlesWithLinks.put(title, pageLanguageLinks);
					if (redirects.containsKey(title)) {
						titlesWithLinks.put(redirects.get(title), pageLanguageLinks);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
				return titlesWithLinks;
			}

		}

		return titlesWithLinks;
	}

	public static HashMap<String, HashMap<Language, String>> loadLangLinksForTitles(Language originalLanguage,
			String titlesParam, List<Language> targetLanguages,
			HashMap<String, HashMap<Language, String>> allTitlesWithLinks) {

		try {
			titlesParam = URLEncoder.encode(titlesParam, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		for (Language targetLanguage : targetLanguages) {

			String url = "https://" + originalLanguage + ".wikipedia.org/w/api.php?lllang=" + targetLanguage
					+ "&action=query&prop=langlinks&titles=" + titlesParam;

			HashMap<String, HashMap<Language, String>> titlesWithLinks = getTitlesWithLinks(url, originalLanguage,
					targetLanguage);
			for (String title : titlesWithLinks.keySet()) {
				if (allTitlesWithLinks.containsKey(title)) {
					HashMap<Language, String> daa = allTitlesWithLinks.get(title);
					for (Language lang : titlesWithLinks.get(title).keySet()) {
						daa.put(lang, titlesWithLinks.get(title).get(lang));
					}
				} else
					allTitlesWithLinks.put(title, titlesWithLinks.get(title));
			}
		}

		return allTitlesWithLinks;
	}

	/**
	 * 
	 * Retrieves a single language link for a single page and - if there is one
	 * - the redirect.
	 * 
	 * @param title
	 *            Title of the article whose language link is queried
	 * @param originalLanguage
	 *            Language of the article
	 * @param targetLanguage
	 *            Language of the queried language link
	 * @return A pair of the queried language link and - if there is one - the
	 *         redirect of the given link.
	 * @throws Exception
	 */
	public static Entry<String, String> getLanguageLink(String title, Language originalLanguage, Language targetLanguage)
			throws Exception {

		title = URLEncoder.encode(title, "UTF-8");

		String url = "https://" + originalLanguage.getLanguage() + ".wikipedia.org/w/api.php?action=query&titles="
				+ title + "&prop=langlinks&lllang=" + targetLanguage.getLanguage() + "&redirects";
		JSONObject jsonResult = WikiAPIQuery.queryByUrlOnePage(url);

		if (jsonResult == null) {
			System.out.println("ERROR");
			System.out.println("title: " + title);
			System.out.println(url);
		}

		jsonResult = jsonResult.getJSONObject("query");

		String redirect = null;

		if (jsonResult.has("redirects")) {
			String redirectTitle = jsonResult.getJSONArray("redirects").getJSONObject(0).getString("to");
			redirect = redirectTitle;
		}

		String langLink = null;

		try {
			JSONObject jsonResultPages = jsonResult.getJSONObject("pages");

			JSONObject jsonResultPage = jsonResultPages.getJSONObject(jsonResultPages.names().getString(0));
			JSONArray jsonResultPageLangLinks = jsonResultPage.getJSONArray("langlinks");
			langLink = jsonResultPageLangLinks.getJSONObject(0).getString("*");
		} catch (JSONException e) {
			// langLink stays null
		}

		if (originalLanguage == targetLanguage && redirect == null)
			langLink = title;
		else if (originalLanguage == targetLanguage && redirect != null)
			langLink = redirect;

		Entry<String, String> result = new AbstractMap.SimpleEntry<String, String>(langLink, redirect);

		return result;
	}

	public static HashMap<String, HashMap<Language, String>> getTitlesWithLinks2(List<String> titles,
			Language originalLanguage, List<Language> targetLanguages) {

		// Divide the title list into smaller sub parts of 300 titles (because
		// of limits) -> CHANGED TO 50

		List<List<String>> dividedTitles = new ArrayList<List<String>>();

		int i = 0;
		while (i < titles.size()) {
			dividedTitles.add(titles.subList(i, Math.min(titles.size(), i + 50)));
			i = i + 50;
		}

		HashMap<String, HashMap<Language, String>> result = new HashMap<String, HashMap<Language, String>>();

		for (List<String> titlesSub : dividedTitles) {

			String titlesParam = "";

			try {
				titlesParam = URLEncoder.encode(StringUtils.join(titlesSub, "|"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			for (String title : titles) {
				result.put(title, new HashMap<Language, String>());
			}

			String url = "https://" + originalLanguage.getLanguage() + ".wikipedia.org/w/api.php?action=query&titles="
					+ titlesParam + "&prop=langlinks&lllimit=max&redirects";

			// System.out.println(url);

			boolean returnRedirects = false;
			if (targetLanguages.contains(originalLanguage))
				returnRedirects = true;

			for (Language targetLanguage : targetLanguages) {

				if (targetLanguage == originalLanguage)
					continue;

				String targetLanguageUrl = url + "&lllang=" + targetLanguage.getLanguage();

				HashMap<String, HashMap<Language, String>> targetLanguageResult = getTitlesWithLinks2(targetLanguageUrl,
						originalLanguage, targetLanguage, returnRedirects);

				for (String s : targetLanguageResult.keySet()) {

					for (Language l : targetLanguageResult.get(s).keySet()) {
						result.get(s.replace(" ", "_")).put(l, targetLanguageResult.get(s).get(l).replace(" ", "_"));
					}

				}

				returnRedirects = false;
			}
		}

		return result;

	}

	public static HashMap<String, HashMap<Language, String>> getTitlesWithLinks2(List<String> titles,
			Language originalLanguage, Language targetLanguage) {

		// Divide the title list into smaller sub parts of 50 titles (because
		// of limits)

		List<List<String>> dividedTitles = new ArrayList<List<String>>();

		int i = 0;
		while (i < titles.size()) {
			dividedTitles.add(titles.subList(i, Math.min(titles.size(), i + 300)));
			i = i + 50;
		}

		HashMap<String, HashMap<Language, String>> result = new HashMap<String, HashMap<Language, String>>();

		for (List<String> titlesSub : dividedTitles) {

			String titlesParam = "";

			titlesParam = StringUtils.join(titlesSub, "|");

			for (String title : titles) {
				result.put(title, new HashMap<Language, String>());
			}

			String url = "https://" + originalLanguage.getLanguage() + ".wikipedia.org/w/api.php?action=query&titles="
					+ titlesParam + "&prop=langlinks&lllimit=max&lllang=" + targetLanguage.getLanguage() + "&redirects";

			boolean returnRedirects = false;
			if (targetLanguage == originalLanguage)
				returnRedirects = true;

			String targetLanguageUrl = url + "&lllang=" + targetLanguage.getLanguage();

			HashMap<String, HashMap<Language, String>> targetLanguageResult = getTitlesWithLinks2(targetLanguageUrl,
					originalLanguage, targetLanguage, returnRedirects);

			for (String s : targetLanguageResult.keySet()) {

				for (Language l : targetLanguageResult.get(s).keySet()) {
					if (!targetLanguageResult.containsKey(s) || !targetLanguageResult.get(s).containsKey(l)
							|| !result.containsKey(s.replace(" ", "_"))) {
						System.out.println("Error - s: " + s + " (" + l + ")");
						System.out.println(url);
					}
					result.get(s.replace(" ", "_")).put(l, targetLanguageResult.get(s).get(l).replace(" ", "_"));
				}

			}

			returnRedirects = false;
		}

		return result;

	}

	private static HashMap<String, HashMap<Language, String>> getTitlesWithLinks2(String url, Language originalLanguage,
			Language targetLanguage, boolean returnRedirects) {

		HashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(url);

		HashMap<String, String> redirects = new HashMap<String, String>();

		HashMap<String, HashMap<Language, String>> titlesWithLinks = new HashMap<String, HashMap<Language, String>>();

		for (JSONObject json : jsons) {

			try {
				json = json.getJSONObject("query");
				if (json.has("redirects")) {
					JSONArray jsonArrayRedirects = json.getJSONArray("redirects");
					for (int i = 0; i < jsonArrayRedirects.length(); i++) {
						JSONObject jsonRedirect = jsonArrayRedirects.getJSONObject(i);
						// redirects.put(jsonRedirect.getString("to"),
						// jsonRedirect.getString("from"));
						if (returnRedirects) {
							HashMap<Language, String> tmp = new HashMap<Language, String>();
							tmp.put(originalLanguage, jsonRedirect.getString("to"));
							titlesWithLinks.put(jsonRedirect.getString("from"), tmp);
						}
						redirects.put(jsonRedirect.getString("to"), jsonRedirect.getString("from"));
					}
				}
				json = json.getJSONObject("pages");
				JSONArray pages = json.names();

				for (int i = 0; i < pages.length(); i++) {
					JSONObject page = json.getJSONObject(pages.getString(i));
					String title = page.getString("title");

					HashMap<Language, String> pageLanguageLinks = new HashMap<Language, String>();
					pageLanguageLinks.put(originalLanguage, title);

					if (!page.has("missing") && page.has("langlinks")) {
						JSONArray langlinks = page.getJSONArray("langlinks");
						JSONObject langLink = langlinks.getJSONObject(0);

						pageLanguageLinks.put(targetLanguage, langLink.getString("*"));
					}

					if (!redirects.containsKey(title)) {
						titlesWithLinks.put(title, pageLanguageLinks);
					} else {
						titlesWithLinks.put(redirects.get(title), pageLanguageLinks);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
				return titlesWithLinks;
			}

		}

		return titlesWithLinks;
	}

	// public List<String> findExistingLinks(List<String> titles, Language
	// language) {
	//
	// List<String> existingLinks = new ArrayList<String>();
	//
	// // Divide the title list into smaller sub parts of 300 titles (because
	// // of limits) -> CHANGED TO 50
	//
	// List<List<String>> dividedTitles = new ArrayList<List<String>>();
	//
	// int i = 0;
	// while (i < titles.size()) {
	// dividedTitles.add(titles.subList(i, Math.min(titles.size(), i + 50)));
	// i = i + 50;
	// }
	//
	// for (List<String> titleSet : dividedTitles) {
	// existingLinks.addAll(findExistingLinksSplitted(titleSet, language));
	// }
	//
	// existingLinks.retainAll(titles);
	//
	// return existingLinks;
	// }
	//
	// public List<String> findExistingLinksSplitted(List<String> titles,
	// Language language) {
	//
	// List<String> existingLinks = new ArrayList<String>();
	//
	// String url = "http://" + language.getLanguage()
	// +
	// ".wikipedia.org/w/api.php?action=query&generator=links&prop=langlinks&lllang=en&redirects&titles="
	// + StringUtils.join(titles, "|");
	//
	// HashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(url);
	//
	// for (JSONObject json : jsons) {
	// try {
	// json = json.getJSONObject("query").getJSONObject("pages");
	// JSONArray pages = json.names();
	// for (int i = 0; i < pages.length(); i++) {
	// JSONObject page = json.getJSONObject(pages.getString(i));
	// String title = page.getString("title");
	// existingLinks.add(title);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// return existingLinks;
	// }

	public static HashMap<String, HashMap<Language, String>> getTitlesWithLinks2New(List<String> titles,
			Language originalLanguage, List<Language> targetLanguages) {

		// Divide the title list into smaller sub parts of 300 titles (because
		// of limits) -> CHANGED TO 50

		List<List<String>> dividedTitles = new ArrayList<List<String>>();

		int i = 0;
		while (i < titles.size()) {
			dividedTitles.add(titles.subList(i, Math.min(titles.size(), i + 50)));
			i = i + 50;
		}

		HashMap<String, HashMap<Language, String>> result = new HashMap<String, HashMap<Language, String>>();

		for (List<String> titlesSub : dividedTitles) {

			String titlesParam = "";

			try {
				titlesParam = URLEncoder.encode(StringUtils.join(titlesSub, "|"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// for (String title : titles) {
			// result.put(title, new HashMap<Language, String>());
			// }

			String url = "https://" + originalLanguage.getLanguage() + ".wikipedia.org/w/api.php?action=query&titles="
					+ titlesParam + "&prop=langlinks&lllimit=max&redirects";

			// System.out.println(url);

			boolean returnRedirects = false;
			// if (targetLanguages.contains(originalLanguage))
			// returnRedirects = true;

			for (Language targetLanguage : targetLanguages) {

				if (targetLanguage == originalLanguage)
					continue;

				String targetLanguageUrl = url + "&lllang=" + targetLanguage.getLanguage();

				HashMap<String, HashMap<Language, String>> targetLanguageResult = getTitlesWithLinks2(targetLanguageUrl,
						originalLanguage, targetLanguage, returnRedirects);

				for (String s : targetLanguageResult.keySet()) {

					for (Language l : targetLanguageResult.get(s).keySet()) {
						// System.out.println(l + "->" + targetLanguage + ": " +
						// s + " -> "
						// + targetLanguageResult.get(s).get(l));
						if (!result.containsKey(s))
							result.put(s, new HashMap<Language, String>());
						result.get(s).put(l, targetLanguageResult.get(s).get(l));
					}

				}

				returnRedirects = false;
			}
		}

		return result;

	}

}
