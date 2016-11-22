package algorithms.sentences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import algorithms.ConditionWeightConfig;
import algorithms.SimilarityFunctionLinear;
import algorithms.passages.ParagraphMethodConfig;
import algorithms.passages.SimilarParagraphFinder;
import app.Configuration;
import dbloader.DataLoader;
import model.BinaryComparison;
import model.Revision;
import model.Sentence;
import model.SentencePair;
import model.SentenceType;
import model.links.DbPediaLink;
import similarity.SimilarityType;
import translate.Language;

/**
 * Given a revision pair, this class is used to do the sentence alignment. This
 * means, all possible sentence pairs are constructed to identify those that are
 * similar enough w.r.t. to a predefined similarity function.
 */
public class SimilarSentenceFinder {

	private double thresholdPartly = 0.25; // 0.34

	private double thresholdSame = 0.7;

	private ConditionWeightConfig weightConfig;

	private Set<SimilarityType> similarityTypes;

	private Set<SentencePair> similarAnnotationsPairs;

	private Set<Sentence> allAnnotations1;

	private Set<Sentence> allAnnotations2;

	private Map<Revision, Double> coverageOfRevision;

	private double totalCoverage;

	private boolean PRINT = false;

	public SimilarSentenceFinder() {
		loadDefaultTranslationSimilarityFunction();
	}

	public static void main(String[] args) {
		SimilarSentenceFinder finder = new SimilarSentenceFinder();

		BinaryComparison comparison = finder.loadComparison("Lawrence_Eagleburger");
		// BinaryComparison comparison =
		// finder.loadComparison("Studentenverbindung");

		finder.findSimilarParagraphs(comparison);

		finder.loadDefaultTranslationSimilarityFunction();
		finder.getWeightConfig().getStringRepresentation();
	}

	private BinaryComparison loadComparison(String englishId) {
		DataLoader dl = new DataLoader(Configuration.DATABASE1);
		BinaryComparison comparison = dl.loadNewestComparison(englishId, Language.EN, Language.DE);
		return comparison;
	}

	public Set<SentencePair> findSimilarParagraphs(BinaryComparison comparison) {
		return findSimilarParagraphs(comparison, ParagraphMethodConfig.getDefaultParagraphMethodConfig());
	}

	public Set<SentencePair> findSimilarParagraphs(BinaryComparison comparison, ParagraphMethodConfig config) {
		return this.findSimilarParagraphs(comparison, config, true);
	}

	public Set<SentencePair> findSimilarParagraphs(BinaryComparison comparison, ParagraphMethodConfig config,
			boolean markParagraphsInHTML) {

		if (PRINT)
			System.out.println("Find similar sentences.");

		if (PRINT)
			System.out.println(config.getThreshold());

		// if (config.getMethod() != PassageMethod.SA_BASELINE) {
		setThresholdPartly(config.getThreshold());
		// }

		if (PRINT)
			System.out.println("Threshold: " + thresholdPartly);

		findSimilarSentences(comparison);

		if (PRINT)
			System.out.println("Found " + similarAnnotationsPairs.size() + " sentence pairs.");

		// Make some deep copies...
		Set<Sentence> allAnnotations1Tmp = new HashSet<Sentence>();
		allAnnotations1Tmp.addAll(allAnnotations1);

		Set<Sentence> allAnnotations2Tmp = new HashSet<Sentence>();
		allAnnotations2Tmp.addAll(allAnnotations2);

		boolean printSentences = false;

		if (printSentences)
			System.out.println("Sentences:");

		for (SentencePair ap : similarAnnotationsPairs) {
			if (printSentences) {
				System.out.println(ap.getSentence1().getNumber() + ": " + ap.getSentence1().getRawText());
				System.out.println(ap.getSentence2().getNumber() + ": " + ap.getSentence2().getRawText());
				System.out.println(ap.getSimilarity(weightConfig));
				System.out.println(ap.getSimilarityString());
				// System.out.println(ap.getAnnotation1().getEnglishStemmedTextConcatenated());
				// System.out.println(ap.getAnnotation2().getEnglishStemmedTextConcatenated());
			}
			List<String> dbps1 = new ArrayList<String>();
			for (DbPediaLink d : ap.getSentence1().getDbPediaLinks())
				dbps1.add(d.getEntity().getName(Language.EN));
			// System.out.println(StringUtils.join(dbps1, ", "));

			List<String> dbps2 = new ArrayList<String>();
			for (DbPediaLink d : ap.getSentence2().getDbPediaLinks())
				dbps2.add(d.getEntity().getName(Language.EN));
			// System.out.println(StringUtils.join(dbps2, ", "));
			if (printSentences)
				System.out.println("\n");
		}

		if (PRINT)
			System.out.println("Find paragraph pairs.");
		Set<SentencePair> paragraphs;

		SimilarParagraphFinder spf = new SimilarParagraphFinder(comparison, weightConfig, config, thresholdPartly);
		paragraphs = spf.findSimilarParagraphs(allAnnotations1Tmp, allAnnotations2Tmp, similarAnnotationsPairs);

		boolean printParagraphs = false;

		if (printParagraphs)
			System.out.println("Paragraphs:");

		List<Sentence> similarAnnotationsInRevison1 = new ArrayList<Sentence>();
		List<Sentence> similarAnnotationsInRevison2 = new ArrayList<Sentence>();
		List<Sentence> gapSentencesInRevison1 = new ArrayList<Sentence>();
		List<Sentence> gapSentencesInRevison2 = new ArrayList<Sentence>();

		for (SentencePair ap : paragraphs) {
			similarAnnotationsInRevison1.add(ap.getSentence1());
			similarAnnotationsInRevison2.add(ap.getSentence2());

			// gapSentencesInRevison1.addAll(ap.getGapSentences(ap.getAnnotation1()));
			// gapSentencesInRevison2.addAll(ap.getGapSentences(ap.getAnnotation2()));

			if (ap.getSentence1().getGapSentences() != null)
				gapSentencesInRevison1.addAll(ap.getSentence1().getGapSentences());
			if (ap.getSentence2().getGapSentences() != null)
				gapSentencesInRevison2.addAll(ap.getSentence2().getGapSentences());

			// System.out.println(ap.getSimilarity(weightConfig));
			if (printParagraphs) {
				System.out.println(ap.getSentence1().getRawText());
				// System.out.println(ap.getAnnotation1().getHtmlText());

				System.out.println(ap.getSentence2().getRawText());
				// System.out.println(ap.getAnnotation2().getHtmlText());

				System.out.println("\n");
			}
			// System.out.println(ap.getSimilarityString());
		}

		// TODO: Must be done at some time
		if (comparison.getRevision1().getArticleAnnotation() != null
				&& comparison.getRevision2().getArticleAnnotation() != null)
			computeCoverage(comparison, paragraphs);

		if (PRINT)
			System.out.println("Found " + paragraphs.size() + " paragraphs.");

		comparison.setSimilarParagraphs(paragraphs);

		return paragraphs;
	}

	public Set<SentencePair> findSimilarSentences(BinaryComparison comparison) {

		int i = 0;

		if (weightConfig == null)
			loadDefaultTranslationSimilarityFunction();

		filterSentences(comparison);

		if (PRINT)
			System.out.println(allAnnotations1.size() + " * " + allAnnotations2.size() + " = "
					+ (allAnnotations1.size() * allAnnotations2.size()) + " possible sentence pairs");

		for (Sentence a1 : allAnnotations1) {

			for (Sentence a2 : allAnnotations2) {

				SentencePair ap = new SentencePair(comparison, a1, a2);

				i += 1;

				if (i % 100000 == 0)
					System.out.println("Calculated " + i + " sentence pair similarities.");

				ap.calculateSimilarities(similarityTypes);

				double similarity = ap.calculateSimilarity(weightConfig);

				// if (ap.isApplicable(SimilarityType.EntityAllTypesSimilarity)
				// &&
				// ap.getSimilarities().get(SimilarityType.EntityAllTypesSimilarity)
				// > 0) {
				// System.out.println("Sim: " +
				// ap.getSimilarities().get(SimilarityType.EntityAllTypesSimilarity));
				// System.out.println(ap.getAnnotation1().getEntityAllString());
				// System.out.println(ap.getAnnotation2().getEntityAllString());
				// }

				// if (similarity > 0.1) {
				// System.out.println("Sim: " + similarity);
				// System.out.println(ap.getSentence1().getRawText());
				// System.out.println(ap.getSentence2().getRawText());
				// System.out.println(ap.getSentence1().getEnglishStemmedTextConcatenated());
				// System.out.println(ap.getSentence2().getEnglishStemmedTextConcatenated());
				// System.out.println("Entities " +
				// ap.getSentence1().getLanguage() + ": "
				// + ap.getSentence1().getEntityAllString());
				// System.out.println("Entities " +
				// ap.getSentence2().getLanguage() + ": "
				// + ap.getSentence2().getEntityAllString());
				// System.out.println(ap.getSimilarityString());
				// System.out.println("");
				// }

				if (PRINT && similarity > 0.1) {
					System.out.println("Total similarity: " + similarity);
					System.out.println(ap.getSimilarityString());
					if (ap.getSentence1().getDictionaryTranslations() == null) {
						System.out.println(ap.getSentence1().getSnowballStemmedWords());
						System.out.println(ap.getSentence2().getDictionaryTranslations());
					} else {
						System.out.println(ap.getSentence1().getDictionaryTranslations());
						System.out.println(ap.getSentence2().getSnowballStemmedWords());
					}
					ap.printTexts();
					System.out.println("---");
				}

				if (similarity >= this.thresholdPartly) {
					similarAnnotationsPairs.add(ap);
				}

			}
		}

		return similarAnnotationsPairs;
	}

	private void filterSentences(BinaryComparison comparison) {
		Revision revision1 = comparison.getRevision1();
		Revision revision2 = comparison.getRevision2();

		allAnnotations1 = new HashSet<Sentence>();
		allAnnotations2 = new HashSet<Sentence>();
		similarAnnotationsPairs = new HashSet<SentencePair>();

		for (Sentence a1 : revision1.getSentences()) {

			if (!a1.isImportant() || a1.isInInfobox() || a1.getType() != SentenceType.SENTENCE) {
				continue;
			}

			// Every valid sentence has to be at least 10 characters long and
			// must contain at least 3 spaces.
			// in case of contradiction search, the english text is null at this
			// point
			if (a1.getEnglishRawText() != null && (a1.getEnglishRawText().length() < 10
					|| StringUtils.countMatches(a1.getEnglishRawText(), " ") <= 2))
				continue;
			allAnnotations1.add(a1);
		}

		for (Sentence a2 : revision2.getSentences()) {
			if (!a2.isImportant() || a2.isInInfobox() || a2.getType() != SentenceType.SENTENCE) {
				continue;
			}

			// Every valid sentence has to be at least 10 characters long and
			// must contain at least 3 spaces.
			// in case of contradiction search, the english text is null at this
			// point
			if (a2.getEnglishRawText() != null && (a2.getEnglishRawText().length() < 10
					|| StringUtils.countMatches(a2.getEnglishRawText(), " ") <= 2))
				continue;

			allAnnotations2.add(a2);
		}
	}

	private void computeCoverage(BinaryComparison comparison, Set<SentencePair> paragraphs) {

		Revision revision1 = comparison.getRevision1();
		Revision revision2 = comparison.getRevision2();

		// get total length of revisions
		int lengthOfRevision1 = revision1.getLengthofRawSentences();
		int lengthOfRevision2 = revision2.getLengthofRawSentences();

		int lengthOfCoveredParagraphs1 = 0;
		int lengthOfCoveredParagraphs2 = 0;

		for (SentencePair ap : paragraphs) {
			lengthOfCoveredParagraphs1 += ap.getSentence1().getRawText().length();
			lengthOfCoveredParagraphs2 += ap.getSentence2().getRawText().length();
		}

		coverageOfRevision = new HashMap<Revision, Double>();

		coverageOfRevision.put(revision1, ((double) lengthOfCoveredParagraphs1) / lengthOfRevision1);
		coverageOfRevision.put(revision2, ((double) lengthOfCoveredParagraphs2) / lengthOfRevision2);

		totalCoverage = ((double) (lengthOfCoveredParagraphs1 + lengthOfCoveredParagraphs2))
				/ (lengthOfRevision1 + lengthOfRevision2);
	}

	public double getTotalCoverage() {
		return totalCoverage;
	}

	public double getCoverageOfRevision(Revision revision) {
		return coverageOfRevision.get(revision);
	}

	public void setThresholdPartly(double thresholdPartly) {
		this.thresholdPartly = thresholdPartly;
	}

	public ConditionWeightConfig getWeightConfig() {
		return weightConfig;
	}

	public Set<SentencePair> getSimilarAnnotationsPairs() {
		return similarAnnotationsPairs;
	}

	public void setWeightConfig(ConditionWeightConfig weightConfig) {
		this.weightConfig = weightConfig;
	}

	public Set<SimilarityType> getSimilarityTypes() {
		return similarityTypes;
	}

	public void setSimilarityTypes(Set<SimilarityType> similarityTypes) {
		this.similarityTypes = similarityTypes;
	}

	public Set<Sentence> getAllAnnotations1() {
		return allAnnotations1;
	}

	public Set<Sentence> getAllAnnotations2() {
		return allAnnotations2;
	}

	public Set<SentencePair> findAllSentencePairs(BinaryComparison comparison) {

		filterSentences(comparison);

		Set<SentencePair> allSentencePairs = new HashSet<SentencePair>();

		for (Sentence s1 : allAnnotations1) {
			for (Sentence s2 : allAnnotations2) {
				allSentencePairs.add(new SentencePair(comparison, s1, s2));
			}
		}

		return allSentencePairs;
	}

	public ConditionWeightConfig loadDefaultTranslationSimilarityFunction() {

		SimilarityFunctionLinear subCo = new SimilarityFunctionLinear(SimilarityType.WordCosineSimilarity, 1.1917,
				thresholdPartly, thresholdSame);
		subCo.setIntercept(-0.0374);
		SimilarityFunctionLinear subCoE = new SimilarityFunctionLinear(SimilarityType.EntitySimilarity, 0.1396,
				SimilarityType.WordCosineSimilarity, 1.0891, thresholdPartly, thresholdSame);
		subCoE.setIntercept(-0.0411);
		SimilarityFunctionLinear subCoT = new SimilarityFunctionLinear(SimilarityType.HeidelTimeSimilarity, 0.2595,
				SimilarityType.WordCosineSimilarity, 1.0402, thresholdPartly, thresholdSame);
		subCoT.setIntercept(-0.1046);
		SimilarityFunctionLinear subCoET = new SimilarityFunctionLinear(SimilarityType.EntitySimilarity, 0.1543,
				SimilarityType.HeidelTimeSimilarity, 0.2692, SimilarityType.WordCosineSimilarity, 0.9151,
				thresholdPartly, thresholdSame);
		subCoET.setIntercept(-0.1024);
		ConditionWeightConfig co = new ConditionWeightConfig("Co");
		co.addWeightConfig(subCo);
		ConditionWeightConfig coT = new ConditionWeightConfig("CoT");
		coT.addWeightConfig(subCo);
		coT.addWeightConfig(Arrays.asList(new SimilarityType[] { SimilarityType.HeidelTimeSimilarity }), subCoT);
		ConditionWeightConfig coE = new ConditionWeightConfig("CoE");
		coE.addWeightConfig(subCo);
		coE.addWeightConfig(Arrays.asList(new SimilarityType[] { SimilarityType.EntitySimilarity }), subCoE);
		ConditionWeightConfig coET = new ConditionWeightConfig("CoET");

		coET.addWeightConfig(subCo);
		coET.addWeightConfig(Arrays.asList(new SimilarityType[] { SimilarityType.EntitySimilarity }), subCoE);
		coET.addWeightConfig(Arrays.asList(new SimilarityType[] { SimilarityType.HeidelTimeSimilarity }), subCoT);
		coET.addWeightConfig(
				Arrays.asList(
						new SimilarityType[] { SimilarityType.EntitySimilarity, SimilarityType.HeidelTimeSimilarity }),
				subCoET);

		coET.normalize();
		// System.out.println(subCoET.getStringRepresentation());

		this.similarityTypes = new HashSet<SimilarityType>();
		this.similarityTypes.add(SimilarityType.EntitySimilarity);
		this.similarityTypes.add(SimilarityType.HeidelTimeSimilarity);
		this.similarityTypes.add(SimilarityType.WordCosineSimilarity);

		this.weightConfig = coET;

		// System.out.println(coET.getStringRepresentation());

		return coET;
	}

}
