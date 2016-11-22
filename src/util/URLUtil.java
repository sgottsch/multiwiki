package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import sun.net.www.protocol.http.HttpURLConnection;

public class URLUtil {

	// source: http://stackoverflow.com/questions/7467568/parsing-json-from-url
	public static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			int read;
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1) {
				buffer.append(chars, 0, read);
			}
			String string = buffer.toString();
			return string;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static String getHost(String urlString) {
		String host = "";

		if (urlString.startsWith("//"))
			urlString = "http:" + urlString;

		try {
			URL url = new URL(urlString);
			host = url.getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return host;
	}

	// Source:
	// http://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
	public static String getText(String url) throws Exception {
		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		StringBuilder response = new StringBuilder();

		String inputLine;
		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);

		in.close();

		return response.toString();
	}

	private static String readAll(Reader rd) throws IOException {
		int cp;
		StringBuilder sb = new StringBuilder();
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = URLUtil.readAll(rd);
			JSONObject jSONObject = new JSONObject(jsonText);
			return jSONObject;
		} finally {
			is.close();
		}
	}

	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = URLUtil.readAll(rd);
			JSONArray jSONArray = new JSONArray(jsonText);
			return jSONArray;
		} finally {
			is.close();
		}
	}
}
