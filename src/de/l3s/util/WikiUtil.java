package de.l3s.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

public class WikiUtil {

	public static String getURLOfImage(String imageName) {

		// To find the URL of the image, do exactly what is stated here:
		// http://commons.wikimedia.org/wiki/Commons:FAQ#What_are_the_strangely_named_components_in_file_paths.3F

		// Source for a part of the code:
		// http://stackoverflow.com/questions/4183646/java-md5-the-php-way

		String hashValue = "";

		try {
			MessageDigest md = MessageDigest.getInstance("MD5"); // or "SHA-1"
			md.update(imageName.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			hashValue = hash.toString(16);
			while (hashValue.length() < 32) { // 40 for SHA-1
				hashValue = "0" + hashValue;
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String url = "http://upload.wikimedia.org/wikipedia/commons/";
		url += hashValue.substring(0, 1);
		url += "/";
		url += hashValue.substring(0, 2);
		url += "/";

		url += imageName;

		return url;
	}

	public static String getUrlOfThumb(String url) {

		// from
		// "//upload.wikimedia.org/wikipedia/commons/thumb/4/43/Angela-Merkel-2014.jpg/220px-Angela-Merkel-2014.jpg"
		// to
		// "http://upload.wikimedia.org/wikipedia/commons/4/43/Angela-Merkel-2014.jpg"

		String newUrl = "";
		String baseUrl = "http://upload.wikimedia.org/wikipedia/commons/";

		String thumbPart = "/thumb/";

		if (url.contains(thumbPart)) {

			// Remove the "thumb/" part

			String lastPart = url.substring(url.indexOf(thumbPart) + thumbPart.length(), url.length());

			// Remove the "/220px-Angela-Merkel-2014.jpg" part

			Character ch = '/';
			int occ = WikiUtil.nthOccurrence(lastPart, ch, 2);
			if (occ == -1)
				return null;
			lastPart = lastPart.substring(0, occ);
			newUrl = baseUrl + lastPart;
		} else
			return null;

		return newUrl;
	}

	// Source:
	// http://stackoverflow.com/questions/3976616/how-to-find-nth-occurrence-of-character-in-a-string
	public static int nthOccurrence(String str, char c, int n) {
		int pos = str.indexOf(c, 0);
		while (n-- > 0 && pos != -1)
			pos = str.indexOf(c, pos + 1);
		return pos;
	}

	public static String makeSentenceRaw(String sentence, Pattern footnotePattern, Pattern wikiNotePattern) {
		String rawSentence = Jsoup.parse(sentence).text();

		// Remove footnotes
		Matcher m = footnotePattern.matcher(rawSentence);
		rawSentence = m.replaceAll("");

		// Remove "[note:4]", "[verification needed]" etc.
		Matcher m2 = wikiNotePattern.matcher(rawSentence);
		rawSentence = m2.replaceAll("");

		return rawSentence.trim();
	}

	public static String normalizeLink(String link) {
		link = link.replace(" ", "_");
		try {
			link = link.replace("%", "%25");
			link = URLDecoder.decode(link, "UTF-8");
			link = link.replace("%25", "%");
		} catch (UnsupportedEncodingException e) {
			System.err.println("Warning: Decoding format not supported.");
		}
		link = link.trim();
		return link;
	}
}
