package de.l3s.trials;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.l3s.algorithms.passages.ParagraphMethodConfig;
import de.l3s.algorithms.sentences.SimilarSentenceFinder;
import de.l3s.app.Configuration;
import de.l3s.dbloader.DataLoader;
import de.l3s.model.BinaryComparison;
import de.l3s.model.SentencePair;
import de.l3s.model.passages.StructureFreedom;
import de.l3s.translate.Language;

public class PassageAlignmentTest {

	public static void main(String[] args) {

		DataLoader dl = new DataLoader(Configuration.DATABASE1);
		String articleName = "Battle of Messkirch";

		BinaryComparison comparison = dl.loadNewestComparison(articleName, Language.EN, Language.DE);

		System.out.println("---");

		List<ParagraphMethodConfig> methods = new ArrayList<ParagraphMethodConfig>();
		methods.add(new ParagraphMethodConfig(StructureFreedom.MIN, 99, 0.21, true));

		for (ParagraphMethodConfig config : methods) {
			System.out.println(config);
			SimilarSentenceFinder ssf = new SimilarSentenceFinder();

			Set<SentencePair> similarParagraphs = ssf.findSimilarParagraphs(comparison, config);
			System.out.println("---");
			System.out.println(ssf.getWeightConfig().getName());
			System.out.println(comparison.getRevision1().getWikiLink());
			System.out.println(comparison.getRevision2().getWikiLink());
			for (SentencePair ap : similarParagraphs) {
				ap.printTexts();

				// for(DbPediaLink e: ap.getSentence1().getDbPediaLinks())
				// System.out.println(e.getEntity().getName(Language.EN));

				System.out.println(ap.getSimilarityString());
				System.out.println(ap.getSimilarity(ssf.getWeightConfig()));
				System.out.println("");
			}
		}
	}

}
