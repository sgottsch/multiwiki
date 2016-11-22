package de.l3s.wiki;

import java.util.HashMap;
import java.util.LinkedHashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.util.URLUtil;

/**
 * This class offers static methods to get the results - as JSONObject(s) - of a
 * Wikipedia API call by a given URL.
 * 
 * With this class you can overcome a problem: The Wikipedia API limits the
 * number of results to a specified number (mostly 500). To get the next 500
 * results, you have to do another URL query with some "continue" parameters
 * returned by the prior query.
 */
public class WikiAPIQuery {
	private static boolean PRINT_URIS = false;

	/**
	 * For a given Wikipedia API URL, the result is returned as a single
	 * JSONObject that is the first result retrieved by the API (no use of
	 * "continue").
	 * 
	 * @param baseUrl
	 *            URL that is a Wikipedia API call
	 * @return Single JSONObject that is the first response of the API
	 */
	public static JSONObject queryByUrlOnePage(String baseUrl) {
		baseUrl = String.valueOf(baseUrl) + "&format=json";
		if (PRINT_URIS) {
			System.out.println(baseUrl);
		}
		JSONObject json = new JSONObject();
		String url = baseUrl;
		try {
			// System.out.println("URL: " + url);
			json = new JSONObject(URLUtil.readUrl(url));
		} catch (JSONException e) {
			System.out.println("URL: " + url);
			e.printStackTrace();
		} catch (Exception e) {
			return null;
		}
		return json;
	}

	/**
	 * For a given Wikipedia API URL, the results are returned as a set of
	 * JSONObjects. With this method, Wikipedia is sequentially called until no
	 * new result is retrieved by the API.
	 * 
	 * @param baseUrl
	 *            URL that is a Wikipedia API call
	 * @return Set of JSONObjects that represent one page of Wikipedia's result
	 *         each
	 */
	public static LinkedHashSet<JSONObject> queryByURL(String baseUrl) {
		return WikiAPIQuery.queryByURL(baseUrl, true);
	}

	public static LinkedHashSet<JSONObject> queryByURL(String baseUrl, boolean doContinue) {
		baseUrl = String.valueOf(baseUrl) + "&format=json";
		JSONObject json = new JSONObject();
		LinkedHashSet<JSONObject> jsons = new LinkedHashSet<JSONObject>();
		HashMap<String, String> continueParams = new HashMap<String, String>();
		continueParams.put("continue", "");

		while (true) {

			String url = baseUrl;

			// Build URL with "continue" parameters
			for (String getKey : continueParams.keySet()) {
				url += "&" + getKey;
				if (!continueParams.get(getKey).equals(""))
					url += "=" + continueParams.get(getKey);
			}

			if (PRINT_URIS)
				System.out.println(url);

			try {

				json = new JSONObject(URLUtil.readUrl(url));
				jsons.add(json);

				// System.out.println(json);

				if (doContinue && json.has("continue")) {
					JSONObject jsonContinue = json.getJSONObject("continue");

					JSONArray continueNames = jsonContinue.names();

					continueParams = new HashMap<String, String>();

					for (int i = 0; i < continueNames.length(); i++) {
						String continueName = continueNames.getString(i);
						continueParams.put(continueName, jsonContinue.getString(continueName));
					}

				} else {
					break;
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return jsons;
	}

	/**
	 * For a given Wikipedia API URL, the results are returned as a single
	 * JSONObject where a part is cropped to merge the different JSONObjects
	 * returned by the Wikipedia API. With this method, Wikipedia is
	 * sequentially called until no new result is retrieved by the API.
	 * 
	 * @param baseUrl
	 *            URL that is a Wikipedia API call
	 * @param mergeParamsString
	 *            A concatenated "list" of strings (seperated by "-") that
	 *            represent JSON keys that appear in a single result. The
	 *            returned JSONObject will only contain everything "below" these
	 *            strings. Example: When calling this method with "query-pages",
	 *            it will just return a JSONObject with all pages (and no other
	 *            information (like the "continue" values) in front of that).
	 * @return A single JSONObject with the result of the query
	 */
	public static JSONObject queryByURL(String baseUrl, String mergeParamsString) {
		LinkedHashSet<JSONObject> jsons = WikiAPIQuery.queryByURL(baseUrl);
		JSONObject mergedJson = new JSONObject();
		String[] mergeParams = mergeParamsString.split("-");

		try {
			for (JSONObject jsonToMerge : jsons) {
				JSONObject jsonSplit = jsonToMerge;
				for (String mergeParam : mergeParams) {

					if (mergeParam.equals("[first]"))
						mergeParam = jsonSplit.names().getString(0);

					jsonSplit = jsonSplit.getJSONObject(mergeParam);
				}
				JSONArray names = jsonSplit.names();
				for (int i = 0; i < names.length(); i++) {
					String key = names.getString(i);
					JSONObject value = jsonSplit.getJSONObject(key);

					// There may be dual keys (e.g. when querying the links: The
					// keys of the dead links are "-1", "-2" etc. on each
					// "continue" page
					// Fix this by adding "b"s to the keys
					while (mergedJson.has(key))
						key = key + "b";

					mergedJson.put(key, value);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return mergedJson;
	}
}
