package de.l3s.translate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import de.l3s.app.Configuration;

/**
 * This class offers methods to determine language of the strings and perform
 * translation using ngramj http://ngramj.sourceforge.net/index.html and
 * microsoft translator api http://api.microsofttranslator.com
 */
public class MicrosoftTranslator implements TranslationI {
	
	private int requestCount = 0;
	private static String APP_ID = Configuration.MICROSOFT_TRANSLATION_API_KEY;

	@Override
	public int getRequestCount() {
		return this.requestCount;
	}

	/**
	 * Translate given name into targetLang
	 */
	public String translate(String text, Language sourceLang, Language targetLang) throws Exception {
		// re-initialize translation
		// UPDATE wiki_text SET english_text = 'NULL' WHERE
		// original_language!='en';

		boolean test = false;
		if (test == true) {
			return "TEST";
		}
		String translated = "";
		int maxLength = 10241;

		if (text.length() > maxLength) {
			// split text in blocks respecting sentence boundaries.
			System.out.println("TEXT LENGTH: " + text.length() + "\n");

			String sentenceSplit = "(?<=[.?!])\\s+(?=[a-zA-Z])";

			// System.out.println(text.matches(sentenceSplit));
			String[] splitString = text.split(sentenceSplit);

			String curText = "";

			for (String sentence : splitString) {
				// System.out.println("Sentence length:
				// "+sentence.length()+"\t"+sentence);

				if ((curText.length() + sentence.length()) > maxLength) {
					// TODO: translate curText

					String blockTranslate = translateBlock(curText, sourceLang, targetLang);
					translated += blockTranslate;

					// re-initialize curText with the current sentence
					curText = sentence;
				} else {

					// append sentence to curText
					curText += sentence;

				}

			}

		} else {
			// string is not long
			translated = translateBlock(text, sourceLang, targetLang);
		}

		return translated;

	}

	private String translateBlock(String text, Language sourceLang, Language targetLang) throws Exception {
		String translatedString;
		this.requestCount++;
		String translated = "";
		String tobetranslated = URLEncoder.encode(text, "UTF-8");

		// FIXME: the parameter 'text' must be less than '10241' characters

		// translation to targetLang using microsoft translator
		// from argument can be missing, but we do detect the language first to
		// avoid unnecessary translation calls

		String urltranslator = "http://api.microsofttranslator.com/v2/Ajax.svc/Translate?appId=" + APP_ID + "&text="
				+ tobetranslated;
		urltranslator = String.valueOf(urltranslator) + "&contentType=text/html";
		System.out.println(tobetranslated);
		if (sourceLang != null) {
			urltranslator = String.valueOf(urltranslator) + "&from=" + sourceLang;
		}
		urltranslator = String.valueOf(urltranslator) + "&to=" + targetLang;
		URL url = new URL(urltranslator);
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setDoOutput(true);
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
		while ((translatedString = in.readLine()) != null) {
			String UTF8Str = new String(translatedString.getBytes("UTF8"), "UTF-8");
			translated = String.valueOf(translated) + UTF8Str + " ";
		}
		in.close();
		Pattern pattern = Pattern.compile("\\\"?\\w+Exception:");
		Matcher matcher = pattern.matcher(translated);
		if (matcher.find() && matcher.start() == 1) {
			if (translated.contains("AppId is over the quota")) {
				System.err.println(translated);
			}
			throw new TranslateException(translated);
		}
		if (translated.trim().length() > 3) {
			return translated.trim().substring(2, translated.length() - 1);
		}
		return translated.trim();
	}

	@Override
	public List<String> translateTexts(List<String> texts, Language sourceLang, Language targetLang)
			throws IOException, TranslateException {
		String result;
		BufferedReader reader;
		String urltranslator = "http://api.microsofttranslator.com/v2/Http.svc/TranslateArray";
		String translateArrayRequest = "<TranslateArrayRequest><AppId>" + APP_ID + "</AppId>" + "<From>"
				+ sourceLang.getLanguage() + "</From>" + "<Options>"
				+ "    <ContentType xmlns=\"http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2\">text/html</ContentType>"
				+ "</Options>\n" + "<Texts>";
		Iterator<String> iterator = texts.iterator();
		while (iterator.hasNext()) {
			String text = iterator.next();
			text = text.replaceAll("<", "&lt;");
			text = text.replaceAll(">", "&gt;");
			text = text.replaceAll("&", "&amp;");
			translateArrayRequest = String.valueOf(translateArrayRequest)
					+ "<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">" + text
					+ "</string>\n";
		}
		translateArrayRequest = String.valueOf(translateArrayRequest) + "</Texts><To>" + targetLang.getLanguage()
				+ "</To>" + "</TranslateArrayRequest>";
		System.out.println("translateArrayRequest: \n" + translateArrayRequest);
		URL url = new URL(urltranslator);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Host", "api.microsofttranslator.com");
		connection.setRequestProperty("User-Agent", "");
		connection.setRequestProperty("Content-Type", "text/xml");
		connection.setRequestProperty("Content-Length",
				Integer.toString(translateArrayRequest.getBytes("UTF-8").length));
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setDoOutput(true);
		connection.getOutputStream().write(translateArrayRequest.getBytes("UTF-8"));
		String line = null;
		System.out.println("Response code: " + connection.getResponseCode());
		if (connection.getResponseCode() != 200) {
			reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			result = "";
			while ((line = reader.readLine()) != null) {
				result = String.valueOf(result) + line + "\n";
			}
			System.out.println(result);
			System.out.println(connection.getContent());
		} else {
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		}
		result = "";
		while ((line = reader.readLine()) != null) {
			result = String.valueOf(result) + line + "\n";
		}
		if (connection.getResponseCode() != 200) {
			System.out.println(result);
		}
		reader.close();
		if (connection != null) {
			connection.disconnect();
		}
		Document doc = Jsoup.parse((String) result, (String) "", (Parser) Parser.xmlParser());
		ArrayList<String> translations = new ArrayList<String>();
		for (Element e : doc.select("TranslatedText")) {
			String translated = e.ownText();
			translated = translated.replaceAll("&lt;", "<");
			translated = translated.replaceAll("&gt;", ">");
			translated = translated.replaceAll("&amp;", "&");
			translations.add(translated);
		}
		return translations;
	}

	public List<TranslatedAndAlignedSentence> translateTextsWithWordAlignment(List<String> texts, Language sourceLang,
			Language targetLang) throws IOException, TranslateException {

		String urltranslator = "http://api.microsofttranslator.com/v2/Http.svc/TranslateArray2";

		String translateArrayRequest = "<TranslateArrayRequest>"

				+ "<AppId>" + APP_ID + "</AppId>"

				+ "<From>" + sourceLang.getLanguage() + "</From>"

				// + "<Options>"
				//
				// + " <ContentType
				// xmlns=\"http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2\">text/html</ContentType>"
				//
				// + "</Options>\n"

				+ "<Texts>";

		for (String text : texts) {
			// text = URLEncoder.encode(text, "UTF-8");
			text = text.replaceAll("<", "&lt;");
			text = text.replaceAll(">", "&gt;");
			text = text.replaceAll("&", "&amp;");

			translateArrayRequest += "<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">"
					+ text + "</string>\n";
		}

		translateArrayRequest += "</Texts>"

				+ "<To>" + targetLang.getLanguage() + "</To>"

				+ "</TranslateArrayRequest>";

		// System.out.println("translateArrayRequest: " +
		// translateArrayRequest);

		URL url = new URL(urltranslator);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Host", "api.microsofttranslator.com");
		connection.setRequestProperty("User-Agent", "");
		connection.setRequestProperty("Content-Type", "text/xml");

		connection.setRequestProperty("Content-Length",
				Integer.toString(translateArrayRequest.getBytes("UTF-8").length));
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setDoOutput(true);

		connection.getOutputStream().write(translateArrayRequest.getBytes("UTF-8"));

		// read the output from the server

		BufferedReader reader;

		String line = null;

		System.out.println("Response code: " + connection.getResponseCode());

		if (connection.getResponseCode() != 200) {
			reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
		} else {
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		}

		String result = "";

		while ((line = reader.readLine()) != null) {
			result += line + "\n";
		}

		System.out.println(result);

		if (connection.getResponseCode() != 200)
			System.out.println(result);

		reader.close();

		if (connection != null)
			connection.disconnect();

		// System.out.println("TranslateArrayResult: " + result);

		Document doc = Jsoup.parse(result, "", Parser.xmlParser());

		List<TranslatedAndAlignedSentence> translations = new ArrayList<TranslatedAndAlignedSentence>();

		int i = 0;
		for (Element e : doc.select("TranslateArray2Response")) {

			String alignmentString = null;
			for (Element e2 : e.select("Alignment")) {
				alignmentString = e2.ownText();
			}

			String translated = null;

			for (Element e2 : e.select("TranslatedText")) {
				translated = e2.ownText();
				translated = translated.replaceAll("&lt;", "<");
				translated = translated.replaceAll("&gt;", ">");
				translated = translated.replaceAll("&amp;", "&");
			}

			TranslatedAndAlignedSentence tr = new TranslatedAndAlignedSentence(texts.get(i), translated,
					alignmentString);

			translations.add(tr);
			i += 1;
		}

		return translations;
	}

	public String getTranslation(String text, Language sorceLang, Language targetLang) throws Exception {
		String strTranslate = this.translate(text, sorceLang, targetLang);
		return strTranslate;
	}

	@Override
	public String translate(String string, String fromlang, String tolang) {
		return null;
	}

	public static void main(String[] args) {
		String xml = "<ArrayOfTranslateArray2Response xmlns=\"http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><TranslateArray2Response><Alignment>0:7-0:7 9:16-9:16 18:26-24:30 28:35-18:22 37:48-32:41 50:51-43:44 53:59-46:52 61:63-54:56 65:75-58:65 77:81-67:69 83:97-71:76 99:101-78:79 103:107-83:87 108:108-88:88</Alignment><From>de</From><OriginalTextSentenceLengths xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><a:int>109</a:int></OriginalTextSentenceLengths><TranslatedText>Michelle Monaghan first studied journalism in Chicago and financed her living as a model.</TranslatedText><TranslatedTextSentenceLengths xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><a:int>89</a:int></TranslatedTextSentenceLengths></TranslateArray2Response><TranslateArray2Response><Alignment>0:8-0:8 10:14-10:13 16:18-15:16 20:33-18:31 35:40-33:39 42:43-41:42 45:49-44:48 51:56-50:54 58:70-56:61 72:75-68:71 77:80-73:76 81:81-77:77 83:86-63:66 88:91-79:82 92:92-83:83 94:96-85:89 98:100-98:100 102:105-102:102 107:117-120:129 127:135-104:112 137:141-114:118 143:152-91:96 153:153-130:130 155:157-132:134 159:161-136:137 163:177-139:153 179:184-156:161 186:190-163:171 192:199-173:180 201:206-182:187 208:210-189:191 212:215-193:196 217:220-198:201 222:223-203:204 225:230-206:210 232:235-212:218 237:245-233:239 247:249-244:246 251:254-220:223 256:261-225:231 263:276-248:256 277:277-257:257</Alignment><From>de</From><OriginalTextSentenceLengths xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><a:int>278</a:int></OriginalTextSentenceLengths><TranslatedText>Monaghans role as schnellz\u00fcngige heroine in Shane black comedy bang Kiss Kiss, bang, which earned her a satellite Award nomination, and as Minenarbeiterin, Sherry alongside Charlize Theron and Sean Bean in North country 2005 however secured to her attention.</TranslatedText><TranslatedTextSentenceLengths xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><a:int>258</a:int></TranslatedTextSentenceLengths></TranslateArray2Response><TranslateArray2Response><Alignment>0:7-0:7 9:11-9:11 13:16-26:30 18:23-32:37 25:28-39:42 30:32-44:47 34:38-49:51 40:51-53:60 53:58-62:67 60:64-69:73 66:70-75:79 71:71-80:80 73:77-82:83 79:91-85:94 93:107-96:111 109:111-113:116 113:115-118:120 117:120-122:125 122:125-127:130 128:138-18:24 139:139-131:131</Alignment><From>de</From><OriginalTextSentenceLengths xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><a:int>140</a:int></OriginalTextSentenceLengths><TranslatedText>Monaghan has been married since August 2005 with her longtime friend Peter White, an Australian graphic designer from New York City.</TranslatedText><TranslatedTextSentenceLengths xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><a:int>132</a:int></TranslatedTextSentenceLengths></TranslateArray2Response></ArrayOfTranslateArray2Response>";
		Document doc = Jsoup.parse((String) xml, (String) "", (Parser) Parser.xmlParser());
		for (Element e : doc.select("TranslateArray2Response")) {
			System.out.println("a");
			for (Element e2 : e.select("Alignment")) {
				System.out.println(e2.ownText());
			}
		}
	}

	public class TranslatedAndAlignedSentence {
		private String originalText;
		private String translatedText;
		private String alignmentString;

		public TranslatedAndAlignedSentence(String originalText, String translatedText, String alignmendString) {
			this.originalText = originalText;
			this.translatedText = translatedText;
			this.alignmentString = alignmendString;
		}

		public String getOriginalText() {
			return this.originalText;
		}

		public void setOriginalText(String originalText) {
			this.originalText = originalText;
		}

		public String getTranslatedText() {
			return this.translatedText;
		}

		public void setTranslatedText(String translatedText) {
			this.translatedText = translatedText;
		}

		public String getAlignmentString() {
			return this.alignmentString;
		}

		public void setAlignmendString(String alignmendString) {
			this.alignmentString = alignmendString;
		}
	}

}
