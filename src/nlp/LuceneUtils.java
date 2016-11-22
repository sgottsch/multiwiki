package nlp;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class LuceneUtils {

	private Analyzer analyzer;

	public LuceneUtils() {
		// TODO Auto-generated constructor stub
	}

	// should be applied to English only
	public List<String> tokenizeEnglishText(String string) {
		List<String> result = new ArrayList<String>();
		analyzer = new StandardAnalyzer(Version.LUCENE_48);
		try {
			TokenStream stream = analyzer.tokenStream(null, new StringReader(string));
			stream.reset();
			while (stream.incrementToken()) {
				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}
		return result;
	}

	// should be applied to English only
	public List<String> stemEnglishText(String string) {
		List<String> result = new ArrayList<String>();

		analyzer = new EnglishAnalyzer(Version.LUCENE_48);// ,
															// StandardAnalyzer.STOP_WORDS_SET
															// );

		try {
			TokenStream stream = analyzer.tokenStream(null, new StringReader(string));
			stream.reset();
			while (stream.incrementToken()) {
				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}
		return result;
	}

	// should be applied to English only
	public String stemmedEnglishText(String string) {
		String result = "";

		analyzer = new EnglishAnalyzer(Version.LUCENE_48);// ,
															// StandardAnalyzer.STOP_WORDS_SET
															// );

		try {
			TokenStream stream = analyzer.tokenStream(null, new StringReader(string));
			stream.reset();
			while (stream.incrementToken()) {
				result += (stream.getAttribute(CharTermAttribute.class).toString()) + " ";
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LuceneUtils util = new LuceneUtils();
		String str = "Merkel has visited Israel four times. On 16 March 2008, Merkel arrived in Israel to mark the 60th anniversary of ";

		List<String> result = util.tokenizeEnglishText(str);

		List<String> result2 = util.stemEnglishText(str);

		System.out.println(result);
		System.out.println(result2);

	}

}
