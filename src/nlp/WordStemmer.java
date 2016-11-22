package nlp;

import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.DutchStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.GermanStemmer;
import org.tartarus.snowball.ext.HungarianStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;
import org.tartarus.snowball.ext.PortugueseStemmer;
import org.tartarus.snowball.ext.RussianStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;
import org.tartarus.snowball.ext.TurkishStemmer;

import translate.Language;

public class WordStemmer {

	private Language language;

	private SnowballProgram stemmer;

	public static void main(String[] args) {

		WordStemmer stemmer = new WordStemmer(Language.DE);
		stemmer.stem("aber");
		stemmer.stem("auf");
		stemmer.stem("laufen");
		System.out.println(stemmer.stem("militärische Aspekte"));

		// stemmer.stem("федеральной");

	}

	public WordStemmer(Language language) {
		this.language = language;

		switch (this.language) {
		case EN:
			this.stemmer = new EnglishStemmer();
			break;
		case DE:
			this.stemmer = new GermanStemmer();
			break;
		case FR:
			this.stemmer = new FrenchStemmer();
			break;
		case ES:
			this.stemmer = new SpanishStemmer();
			break;
		case PT:
			this.stemmer = new PortugueseStemmer();
			break;
		case RU:
			this.stemmer = new RussianStemmer();
			break;
		case IT:
			this.stemmer = new ItalianStemmer();
			break;
		case NL:
			this.stemmer = new DutchStemmer();
			break;
		case HU:
			this.stemmer = new HungarianStemmer();
			break;
		case TR:
			this.stemmer = new TurkishStemmer();
			break;
		default:
			throw new IllegalArgumentException("No stemmer for language " + language.getLanguage() + ".");
		}

	}

	public String stem(String word) {
		// System.out.println(word);
		this.stemmer.setCurrent(word);
		this.stemmer.stem();
		// System.out.println(this.stemmer.getCurrent());
		return this.stemmer.getCurrent();
	}

}
