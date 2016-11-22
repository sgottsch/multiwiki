package dbpedia_spotlight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.StringUtils;
import model.Revision;
import model.Sentence;
import model.SentenceType;

public class SpotlightLinkExtractorRawText {

	private Revision revision;
	private static final double CONFIDENCE = 0.6;

	public SpotlightLinkExtractorRawText(Revision revision) {
		this.revision = revision;
	}

	public void run() throws MyAnnotationException {

		DBpediaSpotlightClient dbp = new DBpediaSpotlightClient();
		ArrayList<String> sentenceTexts = new ArrayList<String>();
		Map<Integer, Sentence> positions = new HashMap<Integer, Sentence>();

		int idx = 0;
		for (Sentence s : this.revision.getSentences()) {
			if (!s.isImportant() || s.isInInfobox() || s.getType() != SentenceType.SENTENCE
					|| s.getRawText().length() < 10)
				continue;
			sentenceTexts.add(s.getRawText());
			for (int i = idx; i <= idx + s.getRawText().length(); i++)
				positions.put(i, s);
			idx += s.getRawText().length() + 1;
		}

		String text = StringUtils.join(sentenceTexts, (String) " ");

		if (text.isEmpty())
			return;

		List<SpotlightLink> links = dbp.extractSpotlightLinks(text, this.revision.getLanguage(), CONFIDENCE);

		for (SpotlightLink sl : links) {

			Sentence sentence1 = (Sentence) positions.get(sl.getStartPosition());
			Sentence sentence2 = (Sentence) positions.get(sl.getEndPosition());

			sentence1.addSpotlightLink(sl);

			if (sentence1 == sentence2)
				continue;

			sentence2.addSpotlightLink(sl);
		}
	}

}
