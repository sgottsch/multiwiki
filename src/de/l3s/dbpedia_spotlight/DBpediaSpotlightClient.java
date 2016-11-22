package de.l3s.dbpedia_spotlight;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.app.Configuration;
import de.l3s.translate.Language;

public class DBpediaSpotlightClient extends AnnotationClient {

	private static final DBPediaSpotlightSource SOURCE = DBPediaSpotlightSource.DEMO;

	private static final String API_URL_EN = "http://spotlight.sztaki.hu:2222/";
	private static final String API_URL_FR = "http://spotlight.sztaki.hu:2225/";
	private static final String API_URL_DE = "http://spotlight.sztaki.hu:2226/";
	private static final String API_URL_RU = "http://spotlight.sztaki.hu:2227/";
	private static final String API_URL_PT = "http://spotlight.sztaki.hu:2228/";
	private static final String API_URL_HU = "http://spotlight.sztaki.hu:2229/";
	private static final String API_URL_IT = "http://spotlight.sztaki.hu:2230/";
	private static final String API_URL_ES = "http://spotlight.sztaki.hu:2231/";
	private static final String API_URL_NL = "http://spotlight.sztaki.hu:2232/";
	private static final String API_URL_TR = "http://spotlight.sztaki.hu:2235/";
	private static final String API_URL_LOCAL_EN = "http://localhost:2222/";
	private static final String API_URL_LOCAL_FR = "http://localhost:2225/";
	private static final String API_URL_LOCAL_DE = "http://localhost:2226/";
	private static final String API_URL_LOCAL_RU = "http://localhost:2227/";
	private static final String API_URL_LOCAL_PT = "http://localhost:2228/";
	private static final String API_URL_LOCAL_HU = "http://localhost:2229/";
	private static final String API_URL_LOCAL_IT = "http://localhost:2230/";
	private static final String API_URL_LOCAL_ES = "http://localhost:2231/";
	private static final String API_URL_LOCAL_NL = "http://localhost:2232/";
	private static final String API_URL_LOCAL_TR = "http://localhost:2235/";
	private static final String API_URL_ONLINE_EN = Configuration.SPOTLIGHT_CLIENT + ":2222/";
	private static final String API_URL_ONLINE_FR = Configuration.SPOTLIGHT_CLIENT + ":2225/";
	private static final String API_URL_ONLINE_DE = Configuration.SPOTLIGHT_CLIENT + ":2226/";
	private static final String API_URL_ONLINE_RU = Configuration.SPOTLIGHT_CLIENT + ":2227/";
	private static final String API_URL_ONLINE_PT = Configuration.SPOTLIGHT_CLIENT + ":2228/";
	private static final String API_URL_ONLINE_HU = Configuration.SPOTLIGHT_CLIENT + ":2229/";
	private static final String API_URL_ONLINE_IT = Configuration.SPOTLIGHT_CLIENT + ":2230/";
	private static final String API_URL_ONLINE_ES = Configuration.SPOTLIGHT_CLIENT + ":2231/";
	private static final String API_URL_ONLINE_NL = Configuration.SPOTLIGHT_CLIENT + ":2232/";
	private static final String API_URL_ONLINE_TR = Configuration.SPOTLIGHT_CLIENT + ":2235/";

	private static final int SUPPORT = 0;

	public List<SpotlightLink> extractSpotlightLinks(String text, Language language, double confidence)
			throws MyAnnotationException {

		Set<SpotlightLink> dbobjects = this.extractNewPost(text, language, confidence, true);
		List<SpotlightLink> links = new ArrayList<SpotlightLink>();

		for (SpotlightLink dbo : dbobjects) {
			links.add(dbo);
		}

		return links;
	}

	private Set<SpotlightLink> extractNewPost(String text, Language language, double confidence,
			boolean extractAllTypes) throws MyAnnotationException {
		return this.extractNew(text, language, confidence, extractAllTypes, true);
	}

	private Set<SpotlightLink> extractNew(String text, Language lang, double confidence, boolean extractAllTypes,
			boolean usePostMethod) throws MyAnnotationException {

		LinkedHashSet<SpotlightLink> dbpediaLinks;
		String spotlightResponse;
		System.out.println("TEXT: " + text);
		String confidenceString = String.valueOf(confidence);
		dbpediaLinks = new LinkedHashSet<SpotlightLink>();
		String url = this.getQueryUrl(lang);
		try {
			if (usePostMethod) {
				PostMethod postMethod = new PostMethod(String.valueOf(url) + "rest/annotate");
				postMethod.addRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
				postMethod.setParameter("confidence", confidenceString);
				postMethod.setParameter("support", String.valueOf(SUPPORT));
				NameValuePair textParam = new NameValuePair("text", text);
				NameValuePair confidenceParam = new NameValuePair("confidence", "0.6");
				NameValuePair supportParam = new NameValuePair("support", String.valueOf(0));
				postMethod.setRequestBody(new NameValuePair[] { textParam, confidenceParam, supportParam });
				postMethod.addRequestHeader(new Header("Accept", "application/json"));
				spotlightResponse = this.request((HttpMethod) postMethod);
			} else {
				GetMethod getMethod = new GetMethod(String.valueOf(url) + "rest/annotate/?" + "confidence=" + confidence
						+ "&support=" + 0 + "&text=" + URLEncoder.encode(text, "utf-8"));
				System.out.println(getMethod.getQueryString());
				getMethod.addRequestHeader(new Header("Accept", "application/json"));
				spotlightResponse = this.request((HttpMethod) getMethod);
			}
		} catch (UnsupportedEncodingException e) {
			throw new MyAnnotationException("Could not encode text.", e);
		}
		assert (spotlightResponse != null);

		JSONObject resultJSON = null;
		JSONArray entities = null;

		try {
			resultJSON = new JSONObject(spotlightResponse);
			// System.out.println("--- resultJSON ---");
			// System.out.println(resultJSON);
			// Make sure that there is a "text" (otherwise throws Exception)
			resultJSON.getString("@text");

			// If nothing was found: Resources is not there -> return empty list
			if (resultJSON.has("Resources"))
				entities = resultJSON.getJSONArray("Resources");
			else
				return dbpediaLinks;

		} catch (JSONException e) {
			System.out.println(spotlightResponse);
			throw new MyAnnotationException("Received invalid response from DBpedia Spotlight API.");
		}

		LinkedList<MyDBpediaResource> resources = new LinkedList<MyDBpediaResource>();

		for (int i = 0; i < entities.length(); i++) {
			try {
				JSONObject entity = entities.getJSONObject(i);
				resources.add(new MyDBpediaResource(entity.getString("@URI"),
						Integer.parseInt(entity.getString("@support"))));
				String types = entity.getString("@types");
				if (extractAllTypes || !types.isEmpty()) {
					int start = entity.getInt("@offset");
					int end = start + entity.getString("@surfaceForm").length();
					URI uri = URI.create(entity.getString("@URI"));
					String path = uri.getPath();
					String resourceURI = path.substring(path.lastIndexOf(47) + 1);
					SpotlightLink dbpediaLink = null;
					if (extractAllTypes) {
						dbpediaLink = new SpotlightLink(resourceURI, lang, start, end, entity.toString());
						dbpediaLink.setCoveredText(entity.getString("@surfaceForm"));
					} else {
						dbpediaLink = new SpotlightLink(resourceURI, lang, start, end, entity.toString());
					}
					if (extractAllTypes) {
						dbpediaLink.setHasType(!types.isEmpty());
					}
					dbpediaLinks.add(dbpediaLink);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			++i;
		}
		return dbpediaLinks;
	}

	private String getQueryUrl(Language lang) {

		String url;

		if (SOURCE == DBPediaSpotlightSource.DEMO) {
			if (lang == Language.DE)
				url = API_URL_DE;
			else if (lang == Language.FR)
				url = API_URL_FR;
			else if (lang == Language.NL)
				url = API_URL_NL;
			else if (lang == Language.EN)
				url = API_URL_EN;
			else if (lang == Language.ES)
				url = API_URL_ES;
			else if (lang == Language.RU)
				url = API_URL_RU;
			else if (lang == Language.PT)
				url = API_URL_PT;
			else if (lang == Language.HU)
				url = API_URL_HU;
			else if (lang == Language.IT)
				url = API_URL_IT;
			else if (lang == Language.TR)
				url = API_URL_TR;
			else
				throw new NullPointerException("language " + lang + " not supported");
		} else if (SOURCE == DBPediaSpotlightSource.LOCALHOST) {
			if (lang == Language.DE)
				url = API_URL_LOCAL_DE;
			else if (lang == Language.FR)
				url = API_URL_LOCAL_FR;
			else if (lang == Language.NL)
				url = API_URL_LOCAL_NL;
			else if (lang == Language.EN)
				url = API_URL_LOCAL_EN;
			else if (lang == Language.ES)
				url = API_URL_LOCAL_ES;
			else if (lang == Language.RU)
				url = API_URL_LOCAL_RU;
			else if (lang == Language.PT)
				url = API_URL_LOCAL_PT;
			else if (lang == Language.HU)
				url = API_URL_LOCAL_HU;
			else if (lang == Language.IT)
				url = API_URL_LOCAL_IT;
			else if (lang == Language.TR)
				url = API_URL_LOCAL_TR;
			else
				throw new NullPointerException("language " + lang + " not supported");
		} else {
			if (lang == Language.DE)
				url = API_URL_ONLINE_DE;
			else if (lang == Language.FR)
				url = API_URL_ONLINE_FR;
			else if (lang == Language.NL)
				url = API_URL_ONLINE_NL;
			else if (lang == Language.EN)
				url = API_URL_ONLINE_EN;
			else if (lang == Language.ES)
				url = API_URL_ONLINE_ES;
			else if (lang == Language.RU)
				url = API_URL_ONLINE_RU;
			else if (lang == Language.PT)
				url = API_URL_ONLINE_PT;
			else if (lang == Language.HU)
				url = API_URL_ONLINE_HU;
			else if (lang == Language.IT)
				url = API_URL_ONLINE_IT;
			else if (lang == Language.TR)
				url = API_URL_ONLINE_TR;
			else
				throw new NullPointerException("language " + lang + " not supported");
		}

		return url;
	}

	public static enum DBPediaSpotlightSource {
		LOCALHOST, DEMO, HADOOP3;
	}

}
