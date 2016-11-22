package de.l3s.wiki;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.translate.Language;

public class WikiRevisionIdGetter {

	// if there is not found a revision newer than this, take this date
	private static String defaultTimeStamp = "2015-05-27T12:00:00Z";

	public static void main(String[] args) {

		// Шлагинтвейт,_Адольф

		System.out.println(WikiRevisionIdGetter.getRevisionValidParallelTo(Language.EN, 660551326, Language.DE));

	}

	public static Long getRevisionValidParallelTo(Language rev1Lang, long rev1Id, Language rev2Lang) {

		MiniRevision rev1 = findTitleAndTimeStamp(rev1Lang, rev1Id);

		// System.out.println(rev1.getTitle() + " -> " + rev1.getTimeStamp());

		String titleInLang2 = null;
		try {
			titleInLang2 = WikiLinkGetter.getLanguageLink(rev1.getTitle(), rev1Lang, rev2Lang).getKey();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (titleInLang2 == null)
			return null;

		// SimpleDateFormat dateFormat = new
		// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		//
		// Date beginDate = null;
		// try {
		// beginDate = dateFormat.parse(rev1.getTimeStamp());
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }

		String nextRevStartTimeStamp = findNextRevisionTimeStamp(rev1Lang, rev1Id, rev1.getTitle());

		if (nextRevStartTimeStamp == null)
			nextRevStartTimeStamp = rev1.getTimeStamp();

		return findValidRevisionInTime(nextRevStartTimeStamp, rev2Lang, titleInLang2);
	}

	// private static Date findCreationTimeOfRevision(Language rev1Lang, int
	// rev1Id) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// private static Long findSucceedingRevision(Language rev1Lang, int rev1Id)
	// {
	// // TODO Auto-generated method stub
	// return null;
	// }

	private static String findNextRevisionTimeStamp(Language rev1Lang, long rev1Id, String rev1Title) {

		String apiCall = "https://" + rev1Lang.getLanguage().toLowerCase()
				+ ".wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=ids|timestamp|user|comment&rvstartid="
				+ rev1Id + "&titles=" + rev1Title + "&rvlimit=2&rvdir=newer";

		JSONObject json = WikiAPIQuery.queryByUrlOnePage(apiCall);

		try {
			json = json.getJSONObject("query");
			json = json.getJSONObject("pages");
			json = json.getJSONObject(json.names().getString(0));
			JSONArray jsonArr = json.getJSONArray("revisions");

			if (jsonArr.isNull(1))
				return defaultTimeStamp;

			String timeStamp = jsonArr.getJSONObject(1).getString("timestamp");
			return timeStamp;

		} catch (JSONException e) {
			System.err.println(e.getMessage());
			return null;
		}

	}

	private static MiniRevision findTitleAndTimeStamp(Language rev1Lang, long rev1Id) {

		String apiCall = "https://" + rev1Lang.getLanguage().toLowerCase()
				+ ".wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=timestamp&revids=" + rev1Id;

		JSONObject json = WikiAPIQuery.queryByUrlOnePage(apiCall);

		try {
			json = json.getJSONObject("query");
			json = json.getJSONObject("pages");
			json = json.getJSONObject(json.names().getString(0));

			String title = json.getString("title").replaceAll(" ", "_");

			JSONArray jsonArr = json.getJSONArray("revisions");

			String timeStamp = jsonArr.getJSONObject(0).getString("timestamp");

			MiniRevision rev1 = new MiniRevision(title, timeStamp);

			return rev1;

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static Long findValidRevisionInTime(String timeStamp, Language revLang, String revTitle) {

		String apiCall;
		try {
			apiCall = "https://" + revLang.getLanguage().toLowerCase()
					+ ".wikipedia.org/w/api.php?action=query&prop=revisions&rvlimit=1&rvstart=" + timeStamp + "&titles="
					+ URLEncoder.encode(revTitle, "UTF8");

			JSONObject json = WikiAPIQuery.queryByUrlOnePage(apiCall);

			try {
				json = json.getJSONObject("query");
				json = json.getJSONObject("pages");
				json = json.getJSONObject(json.names().getString(0));

				JSONArray jsonArr = json.getJSONArray("revisions");

				long revisionId = jsonArr.getJSONObject(0).getLong("revid");

				return revisionId;

			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	private static class MiniRevision {

		private String title;

		private String timeStamp;

		public MiniRevision(String title, String timeStamp) {
			super();
			this.title = title;
			this.timeStamp = timeStamp;
		}

		public String getTitle() {
			return title;
		}

		public String getTimeStamp() {
			return timeStamp;
		}

	}

}
