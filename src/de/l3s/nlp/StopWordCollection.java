package de.l3s.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import de.l3s.translate.Language;

public class StopWordCollection {

	private Language language;

	private Set<String> stopWords;

	public StopWordCollection(Language language) {
		this.language = language;
	}

	public void init() {
		stopWords = new HashSet<String>();
		try {
			InputStream stopwordList = StopWordCollection.class
					.getResourceAsStream("/resource/stopwords/stopwords_" + language.getLanguage() + ".txt");
			BufferedReader r = new BufferedReader(new InputStreamReader(stopwordList));
			String l;
			while ((l = r.readLine()) != null) {
				stopWords.add(l);
			}
			stopwordList.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getStopWords() {
		return stopWords;
	}

}
