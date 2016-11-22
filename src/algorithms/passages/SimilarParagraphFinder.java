package algorithms.passages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.RealVector;

import algorithms.ConditionWeightConfig;
import model.BinaryComparison;
import model.Revision;
import model.Sentence;
import model.SentencePair;
import model.SentencePairParagraphLengthComparator;
import model.links.DbPediaLink;
import model.links.ExternalLink;
import model.links.InternalLink;
import model.passages.StructureFreedom;
import model.times.HeidelIntervalString;
import tfidf.CollectionType;
import translate.Language;

/**
 * Given a revision pair and the previously computed list of similar sentences
 * within these revisions, this class is used to make paragraphs of these
 * sentence pairs (if possible).
 */
public class SimilarParagraphFinder {

	private Map<Language, Map<Sentence, Integer>> positions;
	private Map<Language, Map<Sentence, Integer>> originalPositions;

	private Map<Language, Map<Integer, Sentence>> reversePositions;
	private Map<Language, Map<Integer, Sentence>> originalReversePositions;

	private Map<Sentence, Set<SentencePair>> annotationToPair;

	private Set<Sentence> allowedAnnotations = new HashSet<Sentence>();

	private int annotationIdIndex;

	// For each sentence between combined sentences, similarities becomes
	// smaller by this value
	// private double distancePenalty = 0.03;

	// If the combined annotations all lie in the same paragraph, this is better
	// private double sameParagraphBonus = 0.05;

	// Each paragraph must at least reach this similarity
	private double paragraphThreshold = 0.25;

	// A paragraph has to at least as similar as the annotation pair was before
	// -- except of this little bonus for being a pragraph (we prefer "big"
	// sets)
	// private double paragraphThresholdBonus = 0.;

	private BinaryComparison comparison;

	// The maximum number of sentences that may be within two aligned sentences
	// in both aligned paragraphs together
	private int maxSummedGapSize = 3;

	// The maximum number of sentences that may be within two aligned sentences
	// in a single paragraph
	private int maxGapSize = 2;

	private ConditionWeightConfig weightConfig;

	private StructureFreedom structureFreedom = StructureFreedom.MID;

	private Map<Language, Map<String, Sentence>> annotationsIdentifier;

	private static final boolean PRINT = false;

	private static final boolean PRINT_MOST_IMPORTANT = false;

	private static final double FIRST_PARAGRAPH_THRESHOLD = 0.08;

	private SentencePair firstParagraph = null;

	public SimilarParagraphFinder(BinaryComparison comparison, ConditionWeightConfig weightConfig, Double threshold) {
		this.comparison = comparison;
		this.weightConfig = weightConfig;
		this.positions = new HashMap<Language, Map<Sentence, Integer>>();
		this.reversePositions = new HashMap<Language, Map<Integer, Sentence>>();
		if (threshold != null)
			this.paragraphThreshold = threshold;
	}

	public SimilarParagraphFinder(BinaryComparison comparison, ConditionWeightConfig weightConfig,
			ParagraphMethodConfig config, double threshold) {

		this(comparison, weightConfig, config.getThreshold());

		structureFreedom = config.getStructureFreedom();

		// if (method == ParagraphMethod.ALGORITHM_MIN || method ==
		// ParagraphMethod.ALGORITHM_MIN_3)
		// paragraphLimit = ParagraphLimit.MIN;
		// else if (method == ParagraphMethod.ALGORITHM_MIDDLE || method ==
		// ParagraphMethod.ALGORITHM_MIDDLE_3
		// || method == ParagraphMethod.ALGORITHM_MIDDLE_3_LOW || method ==
		// ParagraphMethod.ALGORITHM_MIDDLE_5_LOW)
		// paragraphLimit = ParagraphLimit.MIDDLE;
		// else if (method == ParagraphMethod.ALGORITHM_MAX || method ==
		// ParagraphMethod.ALGORITHM_MAX_3)
		// paragraphLimit = ParagraphLimit.MAX;

		// if (method == ParagraphMethod.ALGORITHM_MIDDLE_7_LOW)
		// maxGapSize = 7;
		// else if (method == ParagraphMethod.ALGORITHM_MIDDLE_5_LOW)
		// maxGapSize = 5;
		// else if (method == ParagraphMethod.ALGORITHM_MIN_3 || method ==
		// ParagraphMethod.ALGORITHM_MIDDLE_3
		// || method == ParagraphMethod.ALGORITHM_MAX_3)
		// maxGapSize = 3;
		// else
		// maxGapSize = 2;
		// try {
		// String[] parts = method.toString().split("_");
		// maxGapSize = Integer.parseInt(parts[2]);
		// } catch (Exception e) {
		// maxGapSize = 2;
		// }

		maxSummedGapSize = config.getGapSize();

		// maxGapSize = (int) Math.ceil(maxSummedGapSize / 2d);
		maxGapSize = maxSummedGapSize;

		// if(maxSummedGapSize==2)
		// maxGapSize = 1;

		// maxSummedGapSize = 2 * maxGapSize;

		// System.out.println("MAXGAP: " + maxGapSize);
	}

	public SentencePair mergeWithNeighbouredSentence(SentencePair ap) {

		if (PRINT) {
			System.out.println("aggregateAnnotationHelper");
			ap.printTexts();
			// System.out.println(createAPIdentifier(ap));
		}

		Sentence a1 = ap.getSentence1();
		Sentence a2 = ap.getSentence2();

		List<SentencePair> newAPs = new ArrayList<SentencePair>();
		newAPs.addAll(buildAggregateMatchingsHelper(a1, 1, a2, ap));
		newAPs.addAll(buildAggregateMatchingsHelper(a2, 2, a1, ap));
		newAPs.remove(ap);

		// find the annotation pair with highest similarity
		SentencePair newAP = ap;

		cand: for (SentencePair newAPCandidate : newAPs) {

			if (PRINT) {
				System.out.println("aggregate candidate:");
				System.out.println(annotationToPair.get(newAPCandidate.getSentence1()));
				System.out.println(annotationToPair.get(newAPCandidate.getSentence2()));
			}

			if (annotationToPair.containsKey(newAPCandidate.getSentence1()))
				for (SentencePair apS : annotationToPair.get(newAPCandidate.getSentence1())) {
					if (apS.getSimilarity(weightConfig) > newAPCandidate.getSimilarity(weightConfig)) {
						continue cand;
					}
				}
			if (annotationToPair.containsKey(newAPCandidate.getSentence2()))
				for (SentencePair apS : annotationToPair.get(newAPCandidate.getSentence2())) {
					if (apS.getSimilarity(weightConfig) > newAPCandidate.getSimilarity(weightConfig)) {
						continue cand;
					}
				}

			if (PRINT)
				newAPCandidate.printTexts();

			if (newAPCandidate.getSimilarity(weightConfig) > newAP.getSimilarity(weightConfig))
				newAP = newAPCandidate;
			if (newAPCandidate.getSimilarity(weightConfig) == newAP.getSimilarity(weightConfig)) {
				// make the algorithm deterministic by a meaningless rule
				if (newAPCandidate.getSentence1().getNumber() < newAP.getSentence2().getNumber())
					newAP = newAPCandidate;
			}
		}

		if (PRINT) {
			if (newAP != ap) {
				System.out.println("newAP: ");
				newAP.printTexts();
			} else
				System.out.println("not found");
			System.out.println("");
		}

		newAP.copyGapSentences(ap);

		// System.out.println(newAP + " (newAP)");
		return newAP;
	}

	private Set<SentencePair> buildAggregateMatchingsHelper(Sentence annotation, int position, Sentence matchingPartner,
			SentencePair oldMatch) {

		if (PRINT)
			System.out.println("buildAggregateMatchingsHelper");

		Set<SentencePair> newAPs = new HashSet<SentencePair>();

		Set<Sentence> neighbours = findDirectNeighbours(annotation);

		// TODO: Force determinism. Is not needed in application,
		List<Sentence> neighboursSorted = new ArrayList<Sentence>();
		neighboursSorted.addAll(neighbours);

		Collections.sort(neighboursSorted);

		for (Sentence neighbour : neighboursSorted) {

			// Important: Never take neighbours that are part of other text
			// passage pair (because the other part gets problems then).
			if (neighbour.getAllSubAnnotations().size() > 1)
				continue;

			if (structureFreedom == StructureFreedom.MIN && annotation.getParagraph() != neighbour.getParagraph())
				continue;
			if (structureFreedom == StructureFreedom.MID
					&& annotation.getParagraph().getAboveParagraph() != neighbour.getParagraph().getAboveParagraph())
				continue;

			Sentence aggregatedAnnotation = combineSentences(annotation, neighbour);

			// TODO: Why is that happening sometimes?
			if (aggregatedAnnotation.containsSameSentenceMultipleTimes()) {
				continue;
			}

			SentencePair newMatch;

			if (position == 1)
				newMatch = new SentencePair(comparison, aggregatedAnnotation, matchingPartner);
			else
				newMatch = new SentencePair(comparison, matchingPartner, aggregatedAnnotation);

			newMatch.calculateSimilarities(weightConfig.getSimilarityTypes());
			newMatch.calculateSimilarity(weightConfig);
			double newSimilarity = newMatch.getSimilarity(weightConfig);

			// System.out.println("new: " + newSimilarity);
			// System.out.println("old: " +
			// oldMatch.getSimilarity(weightConfig));

			if (newSimilarity > oldMatch.getSimilarity(weightConfig))
				newAPs.add(newMatch);
		}

		return newAPs;
	}

	private Set<Sentence> findDirectNeighbours(Sentence annotation) {
		Language lang = annotation.getLanguage();

		Set<Sentence> directNeighbours = new HashSet<Sentence>();

		// TODO: Why is this happening?
		if (!positions.get(lang).containsKey(annotation))
			return directNeighbours;

		int position = positions.get(lang).get(annotation);

		int positionBefore = position - 1;
		int positionAfter = position + 1;

		// System.out.println("Find neighbours to " + positionBefore + " and " +
		// positionAfter);
		// for (Integer a : reversePositions.get(lang).keySet())
		// System.out.println(a + " -> " +
		// reversePositions.get(lang).get(a).getRawText());

		if (reversePositions.get(lang).containsKey(positionBefore))
			directNeighbours.add(reversePositions.get(lang).get(positionBefore));

		if (reversePositions.get(lang).containsKey(positionAfter))
			directNeighbours.add(reversePositions.get(lang).get(positionAfter));

		return directNeighbours;
	}

	public void buildNeighbours(Set<Sentence> annotations) {

		Map<Sentence, Integer> positionsMap = new HashMap<Sentence, Integer>();
		Map<Integer, Sentence> reversePositionsMap = new HashMap<Integer, Sentence>();

		// sort annotations according according to positions
		List<Sentence> sortedAnnotations = new ArrayList<Sentence>();
		sortedAnnotations.addAll(annotations);

		Collections.sort(sortedAnnotations);

		Language lang = sortedAnnotations.get(0).getLanguage();

		for (int i = 0; i < sortedAnnotations.size(); i++) {
			// System.out.println(i + ": " +
			// sortedAnnotations.get(i).getRawText());
			positionsMap.put(sortedAnnotations.get(i), i);
			reversePositionsMap.put(i, sortedAnnotations.get(i));
		}

		if (positions == null)
			positions = new HashMap<Language, Map<Sentence, Integer>>();
		if (reversePositions == null)
			reversePositions = new HashMap<Language, Map<Integer, Sentence>>();

		positions.put(lang, positionsMap);
		reversePositions.put(lang, reversePositionsMap);

		if (originalPositions == null) {
			originalPositions = new HashMap<Language, Map<Sentence, Integer>>();
		}
		if (originalReversePositions == null) {
			originalReversePositions = new HashMap<Language, Map<Integer, Sentence>>();
		}

		if (!originalPositions.containsKey(lang))
			originalPositions.put(lang, null);
		if (!originalReversePositions.containsKey(lang))
			originalReversePositions.put(lang, null);

		if (originalPositions.get(lang) == null) {
			originalPositions.put(lang, new HashMap<Sentence, Integer>());
			for (Sentence s : positions.get(lang).keySet()) {
				originalPositions.get(lang).put(s, positions.get(lang).get(s));
			}
		}

		if (originalReversePositions.get(lang) == null) {
			originalReversePositions.put(lang, new HashMap<Integer, Sentence>());
			for (Sentence s : positions.get(lang).keySet()) {
				originalReversePositions.get(lang).put(positions.get(lang).get(s), s);
			}
		}

		// System.out.println("After neighbour construction:");
		// for (Integer a : reversePositions.get(lang).keySet())
		// System.out.println(a + " -> " +
		// reversePositions.get(lang).get(a).getRawText());

	}

	public int getDistance(Sentence a1, Sentence a2) {

		int distance = 0;

		int position1 = positions.get(a1.getLanguage()).get(a1);
		int position2 = positions.get(a2.getLanguage()).get(a2);

		if (position1 < position2) {
			Sentence firstSentenceOfA2 = a2.getFirstSentence();
			Sentence lastSentenceOfA1 = a1.getLastSentence();
			distance = originalPositions.get(lastSentenceOfA1.getLanguage()).get(firstSentenceOfA2)
					- originalPositions.get(lastSentenceOfA1.getLanguage()).get(lastSentenceOfA1);
		} else {
			Sentence lastSentenceOfA2 = a2.getLastSentence();
			Sentence firstSentenceOfA1 = a1.getFirstSentence();
			distance = originalPositions.get(lastSentenceOfA2.getLanguage()).get(firstSentenceOfA1)
					- originalPositions.get(lastSentenceOfA2.getLanguage()).get(lastSentenceOfA2);
		}

		// -1 => directly neighboured annotations need to have
		// distance 0
		return Math.max(0, distance - 1);

		//
		// // -1 => directly neighboured annotations need to have
		// // distance 0
		// return Math.abs(position1 - position2) - 1;
	}

	public List<SentencePair> findSentencePairInDistance(Collection<SentencePair> annotationPairs, SentencePair ap) {

		List<SentencePair> closeAPs = new ArrayList<SentencePair>();

		Sentence annotation1 = ap.getSentence1();
		Sentence annotation2 = ap.getSentence2();

		// A sentence may occur in different APs of which one may be deleted
		// already
		if (!positions.get(annotation1.getLanguage()).containsKey(annotation1))
			return closeAPs;
		if (!positions.get(annotation2.getLanguage()).containsKey(annotation2))
			return closeAPs;

		int position1 = positions.get(annotation1.getLanguage()).get(annotation1);
		int position2 = positions.get(annotation2.getLanguage()).get(annotation2);

		if (position2 < position1) {
			Sentence annotation1Tmp = annotation1;
			annotation1 = annotation2;
			annotation2 = annotation1Tmp;
			int position1Tmp = position1;
			position1 = position2;
			position2 = position1Tmp;
		}

		// search annotation pair next to first annotation (together with their
		// distance)
		Map<SentencePair, Integer> aps1 = new HashMap<SentencePair, Integer>();

		// System.out.println("Close annotations 1 for: " +
		// annotation1.getRawText());
		for (int i = Math.max(0, position1 - maxGapSize - 1); i <= position1 + maxGapSize + 1; i++) {

			Sentence closeAnnotation = reversePositions.get(annotation1.getLanguage()).get(i);

			if (closeAnnotation == null || closeAnnotation == annotation1)
				continue;

			if (PRINT)
				System.out.println("closeAnnotation: " + closeAnnotation.getRawText());

			// The original calculation of distance was "Math.abs(i -
			// position1)", but that does not work, because sentences can
			// consist of sub sentences, but are just counted as one sentence
			// when doing that. Therefore, we have to calculate the distance
			// given the positions of the sentences at the border of the
			// paragraphs.
			int distance = Math.abs(i - position1) - 1;

			annotation1.getAllSubAnnotations();
			closeAnnotation.getAllSubAnnotations();
			if (i < position1) {
				Sentence firstSentenceOfOriginal = annotation1.getFirstSentence();
				Sentence lastSentenceOfNew = closeAnnotation.getLastSentence();
				distance = originalPositions.get(lastSentenceOfNew.getLanguage()).get(firstSentenceOfOriginal)
						- originalPositions.get(lastSentenceOfNew.getLanguage()).get(lastSentenceOfNew);
			} else if (i > position1) {
				Sentence lastSentenceOfOriginal = annotation1.getLastSentence();
				Sentence firstSentenceOfNew = closeAnnotation.getFirstSentence();
				distance = originalPositions.get(lastSentenceOfOriginal.getLanguage()).get(firstSentenceOfNew)
						- originalPositions.get(lastSentenceOfOriginal.getLanguage()).get(lastSentenceOfOriginal);
			}
			// directly neighboured sentences have distance 0, not 1!
			distance -= 1;

			if (distance > maxGapSize)
				continue;

			if (annotationToPair.containsKey(closeAnnotation)) {
				for (SentencePair atp : annotationToPair.get(closeAnnotation)) {
					aps1.put(atp, distance);
				}
			}
		}

		// search annotation pair next to second annotation (together with
		// their distance)
		Map<SentencePair, Integer> aps2 = new HashMap<SentencePair, Integer>();
		// System.out.println("Close annotations 2 for: " +
		// annotation2.getRawText());

		for (int i = Math.max(0, position2 - maxGapSize - 1); i <= position2 + maxGapSize + 1; i++) {

			Sentence closeAnnotation = reversePositions.get(annotation2.getLanguage()).get(i);

			if (closeAnnotation == null || closeAnnotation == annotation2)
				continue;

			if (PRINT)
				System.out.println("closeAnnotation: " + closeAnnotation.getRawText());

			int distance = Math.abs(i - position2);

			annotation2.getAllSubAnnotations();
			closeAnnotation.getAllSubAnnotations();
			if (i < position2) {
				Sentence firstSentenceOfOriginal = annotation2.getFirstSentence();
				Sentence lastSentenceOfNew = closeAnnotation.getLastSentence();
				distance = originalPositions.get(lastSentenceOfNew.getLanguage()).get(firstSentenceOfOriginal)
						- originalPositions.get(lastSentenceOfNew.getLanguage()).get(lastSentenceOfNew);
			} else if (i > position2) {
				Sentence lastSentenceOfOriginal = annotation2.getLastSentence();
				Sentence firstSentenceOfNew = closeAnnotation.getFirstSentence();
				distance = originalPositions.get(lastSentenceOfOriginal.getLanguage()).get(firstSentenceOfNew)
						- originalPositions.get(lastSentenceOfOriginal.getLanguage()).get(lastSentenceOfOriginal);
			}
			// directly neighboured sentences have distance 0, not 1!
			distance -= 1;

			if (distance > maxGapSize)
				continue;

			if (annotationToPair.containsKey(closeAnnotation)) {
				for (SentencePair atp : annotationToPair.get(closeAnnotation)) {
					aps2.put(atp, distance);
				}
			}

		}

		// return those annotation pairs that are both close to first and to
		// second annotation
		Set<SentencePair> commonCloseAPs = new HashSet<SentencePair>();
		commonCloseAPs.addAll(aps1.keySet());
		commonCloseAPs.retainAll(aps2.keySet());

		for (SentencePair closeAP : commonCloseAPs) {
			if (PRINT) {
				System.out.println("common close:");
				closeAP.printTexts();
			}
			closeAPs.add(closeAP);
		}

		DistanceComparator comparator = new DistanceComparator(aps1, aps2);

		// Sort by gap size ascending
		Collections.sort(closeAPs, comparator);

		for (SentencePair sp : closeAPs)
			sp.setDistance(aps1.get(sp));

		return closeAPs;
	}

	public Sentence combineSentences(Sentence annotation1, Sentence annotation2) {

		int position1 = positions.get(annotation1.getLanguage()).get(annotation1);
		int position2 = positions.get(annotation2.getLanguage()).get(annotation2);

		if (position2 < position1) {
			Sentence annotation1Tmp = annotation1;
			annotation1 = annotation2;
			annotation2 = annotation1Tmp;
		}

		List<Sentence> annotationsInBetween = findAnnotationsInBetween(annotation1, annotation2);

		List<Sentence> annotationsToCombine = new ArrayList<Sentence>();
		annotationsToCombine.add(annotation1);
		annotationsToCombine.addAll(annotationsInBetween);
		annotationsToCombine.add(annotation2);

		Sentence combinedAnnotations = combineSentences(annotationsToCombine);
		combinedAnnotations.setGapSentences(annotationsInBetween);

		return combinedAnnotations;
	}

	private List<Sentence> findAnnotationsInBetween(Sentence annotation1Tmp, Sentence annotation2Tmp) {

		// System.out.println("Find annotations between " +
		// createIdentifier(annotation1Tmp.getAllSubAnnotations())
		// + " and " + createIdentifier(annotation2Tmp.getAllSubAnnotations()));

		List<Sentence> annotationsInBetween = new ArrayList<Sentence>();

		Sentence annotation1 = annotation1Tmp;
		Sentence annotation2 = annotation2Tmp;

		// int position1 =
		// positions.get(annotation1.getLanguage()).get(annotation1);
		// int position2 =
		// positions.get(annotation2.getLanguage()).get(annotation2);

		int position1 = originalPositions.get(annotation1.getLanguage()).get(annotation1.getLastSentence());
		int position2 = originalPositions.get(annotation2.getLanguage()).get(annotation2.getFirstSentence());

		if (position2 < position1) {
			annotation1 = annotation2Tmp;
			annotation2 = annotation1Tmp;
			int position1Tmp = position1;
			position1 = position2;
			position2 = position1Tmp;
		}

		for (int i = position1 + 1; i <= position2 - 1; i += 1) {
			annotationsInBetween.add(originalReversePositions.get(annotation1.getLanguage()).get(i));
		}

		return annotationsInBetween;
	}

	public Sentence combineSentences(List<Sentence> annotations) {
		Sentence firstAnnotation = annotations.get(0);
		Language lang = firstAnnotation.getLanguage();

		String uniqueIdentifier = createIdentifier(annotations);

		// don't create annotations new if they were already aggregated that way
		if (annotationsIdentifier != null && annotationsIdentifier.get(lang).containsKey(uniqueIdentifier)) {
			return annotationsIdentifier.get(lang).get(uniqueIdentifier);
		}

		// int number = revision.getSentences().size() + 1;

		int number = annotationIdIndex;
		annotationIdIndex += 1;

		Sentence annotation = combineSentencesHelper(annotations, number);

		// annotationsIdentifier can be null when called e.g. to create the
		// aggregation of the whole article
		if (annotationsIdentifier != null)
			annotationsIdentifier.get(lang).put(uniqueIdentifier, annotation);

		return annotation;
	}

	public static Sentence combineSentencesHelper(List<Sentence> annotations, int number) {
		Sentence firstAnnotation = annotations.get(0);
		Sentence lastAnnotation = annotations.get(annotations.size() - 1);

		Language lang = firstAnnotation.getLanguage();

		Revision revision = firstAnnotation.getRevision();

		// collect annotation independent information
		List<String> htmlTexts = new ArrayList<String>();
		List<String> rawTexts = new ArrayList<String>();
		List<String> englishHtmlTexts = new ArrayList<String>();
		List<String> englishRawTexts = new ArrayList<String>();
		List<String> englishStemmedTextsConcatenated = new ArrayList<String>();
		List<String> entityAllTypes = new ArrayList<String>();

		List<String> snowballStemmedWords = new ArrayList<String>();
		List<Set<String>> dictionaryTranslations = null;

		List<String> nGrams = new ArrayList<String>();
		List<String> englishStemmedText = new ArrayList<String>();

		Set<InternalLink> internalLinks = new HashSet<InternalLink>();
		Set<ExternalLink> externalLinks = new HashSet<ExternalLink>();
		Set<DbPediaLink> dbpediaLinks = new HashSet<DbPediaLink>();
		List<HeidelIntervalString> heidelTimes = new ArrayList<HeidelIntervalString>();

		List<Sentence> gapSentences = new ArrayList<Sentence>();

		Map<CollectionType, List<RealVector>> termVectors = new HashMap<CollectionType, List<RealVector>>();

		for (CollectionType collectionType : CollectionType.values()) {
			if (firstAnnotation.hasTermVector(collectionType))
				termVectors.put(collectionType, new ArrayList<RealVector>());
		}

		// create new texts
		for (Sentence sentence : annotations) {
			htmlTexts.add(sentence.getHtmlText());
			rawTexts.add(sentence.getRawText());
			englishHtmlTexts.add(sentence.getEnglishText());
			englishRawTexts.add(sentence.getEnglishRawText());
			englishStemmedTextsConcatenated.add(sentence.getEnglishStemmedTextConcatenated());

			englishStemmedText.addAll(sentence.getEnglishStemmedText());

			// TODO: Condition not needed
			if (sentence.getSnowballStemmedWords() != null)
				snowballStemmedWords.addAll(sentence.getSnowballStemmedWords());

			if (sentence.getDictionaryTranslations() != null) {
				if (dictionaryTranslations == null)
					dictionaryTranslations = new ArrayList<Set<String>>();
				dictionaryTranslations.addAll(sentence.getDictionaryTranslations());
			}

			if (sentence.getNGrams() != null)
				nGrams.addAll(sentence.getNGrams());

			internalLinks.addAll(sentence.getInternalLinks());
			externalLinks.addAll(sentence.getExternalLinks());
			dbpediaLinks.addAll(sentence.getDbPediaLinks());
			heidelTimes.addAll(sentence.getHeidelIntervals());

			for (CollectionType collectionType : termVectors.keySet()) {
				termVectors.get(collectionType).add(sentence.getTermVector(collectionType));
			}

			if (sentence.getGapSentences() != null)
				gapSentences.addAll(sentence.getGapSentences());

			if (sentence.getEntityAllString() != null) {
				for (String ent : sentence.getEntityAllString().split(" "))
					entityAllTypes.add(ent);
			}

			// if (annotations.get(annotations.size() - 1).getEndPosition() >
			// revision.getHtmlText().length()) {
			// System.err.println("\nERROR");
			// System.out.println(revision.getTitle());
			// System.out.println(revision.getId() + " - " +
			// revision.getLanguage());
			// System.out.println(annotations.get(0).getStartPosition());
			// System.out.println(annotations.get(annotations.size() -
			// 1).getEndPosition());
			// System.out.println(revision.getHtmlText().length());
			// System.out.println("--- Sentences ---");
			// for (Sentence ann : annotations) {
			// System.out.println(ann.getNumber());
			// }
			// System.out.println("------");
			// System.out.println(revision.getHtmlText().substring(annotations.get(0).getStartPosition(),
			// revision.getHtmlText().length()));
			// System.out.println(revision.getHtmlText().substring(13180,
			// 13527));
			// }

		}

		// between two sentences, there can be additional HTML (e.g. "</p>" or a
		// section header. That's why concatenation does not suffice.
		String htmlText = null;
		try {
			htmlText = revision.getHtmlText().substring(annotations.get(0).getStartPosition(),
					annotations.get(annotations.size() - 1).getEndPosition());
		} catch (IndexOutOfBoundsException e) {
			// TODO: When reoncstructing text tiling paragraphs, sometimes gives
			// "String index out of range" error
			htmlText = "<error>";
		}

		String rawText = concenateTexts(rawTexts);
		String englishHtmlText = concenateTexts(englishHtmlTexts);
		String englishRawText = concenateTexts(englishRawTexts);
		String englishStemmedTextConcatenated = concenateTexts(englishStemmedTextsConcatenated);

		String entityAllTypesString = concenateTexts(entityAllTypes);

		int startPosition = firstAnnotation.getStartPosition();
		int endPosition = lastAnnotation.getEndPosition();

		boolean inInfobox = firstAnnotation.isInInfobox();
		boolean isImportant = firstAnnotation.isImportant();

		String type = firstAnnotation.getType().name();

		Sentence annotation = new Sentence(number, lang, htmlText, rawText, englishHtmlText, englishRawText,
				englishStemmedTextConcatenated, startPosition, endPosition, inInfobox, isImportant, type);

		annotation.setRevision(revision);
		annotation.setNGrams(nGrams);
		annotation.setHeidelIntervals(heidelTimes);
		annotation.setGapSentences(gapSentences);
		annotation.setDbPediaLinks(dbpediaLinks);

		annotation.setEntityAllString(entityAllTypesString);

		for (CollectionType collectionType : termVectors.keySet()) {
			RealVector newTermVector = null;
			for (RealVector rv : termVectors.get(collectionType)) {
				if (rv == null)
					continue;
				if (newTermVector == null)
					newTermVector = rv;
				else
					newTermVector = newTermVector.add(rv);
			}

			annotation.setTermVector(collectionType, newTermVector);
		}

		annotation.setParagraph(firstAnnotation.getParagraph());

		annotation.setSubAnnotations(annotations);

		annotation.setSnowballStemmedWords(snowballStemmedWords);
		if (dictionaryTranslations != null)
			annotation.setDictionaryTranslations(dictionaryTranslations);

		return annotation;
	}

	public static void ensureOneOnOneAlignment(Set<SentencePair> paragraphs, ConditionWeightConfig weightConfig,
			boolean printStatistics) {
		HashSet<SentencePair> apsToDelete = new HashSet<SentencePair>();

		// Ensure 1:1 paragraph alignment by sorting all aligned paragraphs by
		// similarity and only take those with new sub sentences (similar to
		// paper method)

		List<SentencePair> sortedParagraphs = new ArrayList<SentencePair>();
		SentencePairParagraphLengthComparator comparator = new SentencePairParagraphLengthComparator(weightConfig);
		sortedParagraphs.addAll(paragraphs);
		Collections.sort(sortedParagraphs, comparator);

		Set<Sentence> alignedSentences = new HashSet<Sentence>();

		for (SentencePair sp : sortedParagraphs) {

			Set<Sentence> sentencesOfPair = new HashSet<Sentence>();
			sentencesOfPair.addAll(sp.getSentence1().getAllSubAnnotations());
			sentencesOfPair.addAll(sp.getSentence2().getAllSubAnnotations());
			boolean contains = false;
			for (Sentence sentenceInPair : sentencesOfPair) {
				if (alignedSentences.contains(sentenceInPair)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				alignedSentences.addAll(sentencesOfPair);
			} else {
				apsToDelete.add(sp);
			}
		}

		if (PRINT_MOST_IMPORTANT)
			System.out.println(
					"Original size: " + paragraphs.size() + " - delete " + apsToDelete.size() + " paragraphs.");
		paragraphs.removeAll(apsToDelete);
		if (PRINT_MOST_IMPORTANT)
			System.out.println("Corrected size: " + paragraphs.size());

		double avgSize = 0;
		for (SentencePair ap : paragraphs) {
			avgSize += ap.getSentence1().getAllSubAnnotations().size()
					+ ap.getSentence2().getAllSubAnnotations().size();
		}
		avgSize /= (double) paragraphs.size();

		if (PRINT_MOST_IMPORTANT || printStatistics) {
			System.out.println("Average size: " + avgSize);
			System.out.println("Aligned sentences: " + (avgSize * paragraphs.size()));
		}

	}

	public void ensureThreshold(Set<SentencePair> paragraphs, ConditionWeightConfig weightConfig, double threshold) {
		HashSet<SentencePair> apsToDelete = new HashSet<SentencePair>();

		for (SentencePair sp : paragraphs) {
			if (sp != firstParagraph && sp.getSimilarity(weightConfig) < threshold)
				apsToDelete.add(sp);
		}

		paragraphs.removeAll(apsToDelete);
	}

	private String createIdentifier(List<Sentence> annotations) {

		List<Sentence> allSubAnnotations = new ArrayList<Sentence>();
		for (Sentence a : annotations) {
			allSubAnnotations.addAll(a.getAllSubAnnotations());
		}

		Collections.sort(allSubAnnotations);

		String identifier = "";

		for (Sentence a : allSubAnnotations)
			identifier += "-" + a.getNumber();

		return identifier.substring(1, identifier.length());
	}

	private static String concenateTexts(List<String> htmlTexts) {
		return StringUtils.join(htmlTexts, " ");
	}

	private SentencePair findProximateSentencePair(SentencePair ap,
			Collection<SentencePair> similarAnnotationsAndParagraphs) {

		if (PRINT) {
			System.out.println("findProximateAnnotationPair for ");
			ap.printTexts();
		}

		double similarity = ap.getSimilarity(weightConfig);

		Sentence annotation1 = ap.getSentence1();
		Sentence annotation2 = ap.getSentence2();

		List<SentencePair> closeAPs = findSentencePairInDistance(similarAnnotationsAndParagraphs, ap);

		// Create new, combined, annotation pairs.
		double highestSimilarity = 0;
		SentencePair apWithHighestSimilarity = null;

		// closeAPs are sorted by their distance (starting with
		// directly neighboured paragraphs)
		for (SentencePair closeAP : closeAPs) {

			if (PRINT) {
				System.out.println("Close:");
				closeAP.printTexts();
			}

			Sentence closeAnnotation1 = closeAP.getSentence1();
			Sentence closeAnnotation2 = closeAP.getSentence2();

			int distance1 = getDistance(annotation1, closeAnnotation1);
			int distance2 = getDistance(annotation2, closeAnnotation2);
			int distance = distance1 + distance2;

			if (distance > maxSummedGapSize)
				continue;

			Sentence combinedAnnotation1 = combineSentences(ap.getSentence1(), closeAP.getSentence1());
			Sentence combinedAnnotation2 = combineSentences(ap.getSentence2(), closeAP.getSentence2());

			SentencePair combinedCloseAP = new SentencePair(comparison, combinedAnnotation1, combinedAnnotation2);

			// TODO: Why does this happen?
			if (combinedCloseAP.containsSameSentenceMultipleTimes()) {
				continue;
			}

			// the gaps of new sentence pair can come from three sources:<br>
			// a) Gap within first old sentence pair<br>
			// b) Gap within second old sentence pair<br>
			// c) Newly found gap between both old sentence pairs
			combinedCloseAP.addGapSentences(combinedAnnotation1, ap.getGapSentences().get(ap.getSentence1()));
			combinedCloseAP.addGapSentences(combinedAnnotation2, ap.getGapSentences().get(ap.getSentence2()));
			combinedCloseAP.addGapSentences(combinedAnnotation1, closeAP.getGapSentences().get(closeAP.getSentence1()));
			combinedCloseAP.addGapSentences(combinedAnnotation2, closeAP.getGapSentences().get(closeAP.getSentence2()));
			combinedCloseAP.addGapSentences(combinedAnnotation1, combinedAnnotation1.getGapSentences());
			combinedCloseAP.addGapSentences(combinedAnnotation2, combinedAnnotation2.getGapSentences());

			combinedCloseAP.calculateSimilarities(weightConfig.getSimilarityTypes());
			combinedCloseAP.calculateSimilarity(weightConfig);
			double newSimilarity = combinedCloseAP.getSimilarity(weightConfig);

			/*
			 * if (annotation1.getParagraph() ==
			 * closeAnnotation1.getParagraph()) newSimilarity +=
			 * sameParagraphBonus;
			 * 
			 * if (annotation2.getParagraph() ==
			 * closeAnnotation2.getParagraph()) newSimilarity +=
			 * sameParagraphBonus;
			 */

			if (structureFreedom == StructureFreedom.MIN
					&& annotation1.getParagraph() != closeAnnotation1.getParagraph())
				continue;
			if (structureFreedom == StructureFreedom.MIN
					&& annotation2.getParagraph() != closeAnnotation2.getParagraph())
				continue;

			if (structureFreedom == StructureFreedom.MID && annotation1.getParagraph()
					.getAboveParagraph() != closeAnnotation1.getParagraph().getAboveParagraph())
				continue;

			if (structureFreedom == StructureFreedom.MID && annotation2.getParagraph()
					.getAboveParagraph() != closeAnnotation2.getParagraph().getAboveParagraph())
				continue;

			// newSimilarity -= (distance1 + distance2) * distancePenalty;
			// newSimilarity += (distance1 + distance2) * 0.05;

			// System.out.println("sim: " + similarity + ", new: " +
			// newSimilarity + ", " + distance);

			boolean distanceIsOkay = distance == 0;
			// System.out.println("distanceIsOkay: " + distanceIsOkay);

			combinedCloseAP.setParagraphSimilarity(newSimilarity);

			// immediately merge directly neighboured paragraphs
			if (distanceIsOkay || (newSimilarity > similarity && newSimilarity > highestSimilarity
					&& newSimilarity >= paragraphThreshold)) {
				highestSimilarity = newSimilarity;
				// System.out.println("highest: " +
				// combinedCloseAP.getSimilarity(weightConfig));
				// combinedCloseAP.printTexts();
				// System.out.println(combinedCloseAP.getSimilarityString());

				// if(closeAP.getDistance()>1) {
				// System.out.println("INTER BIGGER");
				// ap.printTexts();
				// System.out.println("");
				// closeAP.printTexts();
				// System.out.println("");
				// combinedCloseAP.printTexts();
				// System.out.println("");
				// }

				apWithHighestSimilarity = combinedCloseAP;
				if (distance == 0)
					break;
			}

		}

		if (PRINT) {
			if (apWithHighestSimilarity == null)
				System.out.println("found nothing");
			else {
				System.out.println("Found: ");
				apWithHighestSimilarity.printTexts();
			}
			System.out.println("");
		}

		return apWithHighestSimilarity;
	}

	public Set<SentencePair> findSimilarParagraphs(Set<Sentence> allAnnotations1, Set<Sentence> allAnnotations2,
			Set<SentencePair> similarAnnotations) {

		this.annotationsIdentifier = new HashMap<Language, Map<String, Sentence>>();
		this.annotationsIdentifier.put(comparison.getRevision1().getLanguage(), new HashMap<String, Sentence>());
		this.annotationsIdentifier.put(comparison.getRevision2().getLanguage(), new HashMap<String, Sentence>());

		annotationIdIndex = 10000;

		Set<SentencePair> similarAnnotationsAndParagraphs = new HashSet<SentencePair>();

		if (similarAnnotations.size() == 0)
			return similarAnnotationsAndParagraphs;

		similarAnnotationsAndParagraphs.addAll(similarAnnotations);

		annotationToPair = new HashMap<Sentence, Set<SentencePair>>();
		for (SentencePair ap : similarAnnotationsAndParagraphs) {

			if (!annotationToPair.containsKey(ap.getSentence1()))
				annotationToPair.put(ap.getSentence1(), new HashSet<SentencePair>());
			if (!annotationToPair.containsKey(ap.getSentence2()))
				annotationToPair.put(ap.getSentence2(), new HashSet<SentencePair>());

			// if (PRINT) {
			// System.out.println("put to aToPair: ");
			// ap.printTexts();
			// System.out.println("");
			// }
			annotationToPair.get(ap.getSentence1()).add(ap);
			annotationToPair.get(ap.getSentence2()).add(ap);
			allowedAnnotations.add(ap.getSentence1());
			allowedAnnotations.add(ap.getSentence2());
		}

		buildNeighbours(allAnnotations1);
		buildNeighbours(allAnnotations2);

		boolean foundChanges = true;

		// Second condition for while loop that is needed in rare cases: If no
		// extension for any sentence was done, there are no changes. However,
		// try to do the proximate annotation pair finding.
		boolean startedSearchingForProximateAnnotationPairs = false;

		Set<SentencePair> extendedAPs = new HashSet<SentencePair>();
		Set<SentencePair> searchedInProximiaryAPs = new HashSet<SentencePair>();

		List<SentencePair> sortedAPs = new ArrayList<SentencePair>();
		sortedAPs.addAll(similarAnnotationsAndParagraphs);

		alignWikiFirstParagraphs(sortedAPs, allAnnotations1, allAnnotations2);

		NonExtendedFirstComparator comparator = new NonExtendedFirstComparator(extendedAPs);

		while (foundChanges == true || !startedSearchingForProximateAnnotationPairs) {
			foundChanges = false;
			SentencePair newAP = null;

			Collections.sort(sortedAPs, comparator);

			SentencePair oldAP = null;

			// System.out.println("Size: " + sortedAPs.size());

			for (SentencePair ap : sortedAPs) {

				if (PRINT_MOST_IMPORTANT)
					System.out.println("TOP: " + ap.getSimilarity(weightConfig));

				// The following situation was possible before introducing the
				// "allowed annotations":
				// 1. Take (a)-(x) and try to extend it. Result: (a,b)-(x,y)
				// 2. Take (a)-(x,y) and try to extend it.
				// The problem was that no sentence pair with just "a" is
				// allowed anymore at this point. Therefore, the set
				// "allowedAnnotations" keeps track of allowed
				// sentences/paragraphs (starts with the single sentences and
				// later replaces them with paragraphs).

				boolean allowed = true;

				if (!allowedAnnotations.contains(ap.getSentence1())) {
					allowed = false;
				}
				if (!allowedAnnotations.contains(ap.getSentence2())) {
					allowed = false;
				}

				if (!allowed) {
					continue;
				}
				if (!extendedAPs.contains(ap)) {
					newAP = mergeWithNeighbouredSentence(ap);
					extendedAPs.add(ap);
					if (newAP == ap)
						newAP = null;

					if (newAP != null) {
						if (ap == firstParagraph)
							firstParagraph = newAP;
						if (PRINT_MOST_IMPORTANT) {
							System.out.println("Merged neighboured sentences (" + newAP.getAllSubIdsString() + ") ("
									+ ap.getSimilarity(weightConfig) + " -> " + newAP.getSimilarity(weightConfig)
									+ ":");
							ap.printTexts();
							newAP.printTexts();
						}
					}
				} else {
					startedSearchingForProximateAnnotationPairs = true;
					if (!searchedInProximiaryAPs.contains(ap)) {
						newAP = findProximateSentencePair(ap, sortedAPs);
						foundChanges = true;
						searchedInProximiaryAPs.add(ap);
						if (newAP != null) {
							if (ap == firstParagraph)
								firstParagraph = newAP;
							if (PRINT_MOST_IMPORTANT) {
								System.out.println("Merged neighboured sentence pairs:");
								ap.printTexts();
								newAP.printTexts();
							}
						}
					}
				}
				if (newAP != null) {
					oldAP = ap;
					break;
				}
			}

			if (newAP != null) {
				foundChanges = true;
				registerNewAP(newAP, sortedAPs, allAnnotations1, allAnnotations2, oldAP);
			}
		}

		// foundChanges = true;
		//
		// while (foundChanges == true) {
		// foundChanges = false;
		//
		// AnnotationPair newAP = null;
		//
		// for (AnnotationPair ap : sortedAPs) {
		// newAP = findProximateAnnotationPair(ap, sortedAPs);
		// if (newAP != null)
		// break;
		// }
		//
		// if (newAP != null) {
		// registerNewAP(newAP, sortedAPs, allAnnotations1, allAnnotations2);
		// foundChanges = true;
		// }
		//
		// }

		similarAnnotationsAndParagraphs.clear();
		similarAnnotationsAndParagraphs.addAll(sortedAPs);

		// remove non-paragraph annotation pairs
		// similarAnnotationsAndParagraphs.removeAll(similarAnnotations);

		// Set<AnnotationPair> validAPs = new HashSet<AnnotationPair>();
		// Set<Annotation> commonAnnotations = new HashSet<Annotation>();

		// sort

		// System.out.println("Number of paragraphs: " +
		// similarAnnotationsAndParagraphs.size());

		// Comparator<AnnotationPair> comp = new
		// AnnotationPairParagraphComparator(weightConfig);
		// PriorityQueue<AnnotationPair> sortedAPs2 = new
		// PriorityQueue<AnnotationPair>(
		// similarAnnotationsAndParagraphs.size(), comp);
		// sortedAPs2.addAll(similarAnnotationsAndParagraphs);

		// for (AnnotationPair ap : sortedAPs2) {
		//
		// boolean valid = true;
		// for (Annotation sub : ap.getAnnotation1().getAllSubAnnotations()) {
		// if (commonAnnotations.contains(sub)) {
		// valid = false;
		// break;
		// }
		// }
		//
		// if (valid == false)
		// continue;
		//
		// for (Annotation sub : ap.getAnnotation2().getAllSubAnnotations()) {
		// if (commonAnnotations.contains(sub)) {
		// valid = false;
		// break;
		// }
		// }
		//
		// if (valid == false)
		// continue;
		//
		// validAPs.add(ap);
		// commonAnnotations.addAll(ap.getAnnotation1().getAllSubAnnotations());
		// commonAnnotations.addAll(ap.getAnnotation2().getAllSubAnnotations());
		// }

		// TODO: Correct the algorithm (don't return duplicates)
		// HashSet<Integer> commonNumbers1 = new HashSet<Integer>();
		// HashSet<Integer> commonNumbers2 = new HashSet<Integer>();
		//
		// HashSet<AnnotationPair> apsToDelete = new HashSet<AnnotationPair>();
		// for (AnnotationPair ap : validAPs) {
		// for (Annotation a : ap.getAnnotation1().getAllSubAnnotations()) {
		// if (commonNumbers1.contains(a.getNumber())) {
		// System.out.println("Error: " + a.getRawText());
		// apsToDelete.add(ap);
		// }
		// commonNumbers1.add(a.getNumber());
		// }
		// for (Annotation a : ap.getAnnotation2().getAllSubAnnotations()) {
		// if (commonNumbers2.contains(a.getNumber())) {
		// System.out.println("Error: " + a.getRawText());
		// apsToDelete.add(ap);
		// commonNumbers2.add(a.getNumber());
		// }
		// }
		// }
		// similarAnnotationsAndParagraphs.removeAll(apsToDelete);

		ensureThreshold(similarAnnotationsAndParagraphs, this.weightConfig, paragraphThreshold);

		ensureOneOnOneAlignment(similarAnnotationsAndParagraphs, this.weightConfig, PRINT_MOST_IMPORTANT);

		return similarAnnotationsAndParagraphs;
	}

	private void alignWikiFirstParagraphs(List<SentencePair> sortedAPs, Set<Sentence> allAnnotations1,
			Set<Sentence> allAnnotations2) {

		if (paragraphThreshold >= FIRST_PARAGRAPH_THRESHOLD)
			return;

		Sentence sentence1 = reversePositions.get(comparison.getRevision1().getLanguage()).get(0);
		Sentence sentence2 = reversePositions.get(comparison.getRevision2().getLanguage()).get(0);

		SentencePair sp = new SentencePair(comparison, sentence1, sentence2);
		sp.calculateSimilarities(weightConfig.getSimilarityTypes());
		sp.calculateSimilarity(weightConfig);
		double sim = sp.getSimilarity(weightConfig);

		if (sim >= paragraphThreshold)
			return;

		if (sim >= FIRST_PARAGRAPH_THRESHOLD) {
			firstParagraph = sp;
			registerNewAP(sp, sortedAPs, allAnnotations1, allAnnotations2, null);
		}

	}

	private void registerNewAP(SentencePair newAP, Collection<SentencePair> similarAnnotationsAndParagraphs,
			Set<Sentence> allAnnotations1, Set<Sentence> allAnnotations2, SentencePair oldAP) {

		// System.out.println("");
		// oldAP.printTexts();
		// System.out.println("ap, W: " +
		// oldAP.getSimilarityValue(SimilarityType.WordCosineSimilarity));
		// System.out.println("ap, E: " +
		// oldAP.getSimilarityValue(SimilarityType.EntitySimilarity));
		// System.out.println("ap, T: " +
		// oldAP.getSimilarityValue(SimilarityType.HeidelTimeSimilarity));
		// System.out.println("ap ->: " + oldAP.getSimilarity(weightConfig));
		// newAP.printTexts();
		// System.out.println("NEWAP, W: " +
		// newAP.getSimilarityValue(SimilarityType.WordCosineSimilarity));
		// System.out.println("NEWAP, E: " +
		// newAP.getSimilarityValue(SimilarityType.EntitySimilarity));
		// System.out.println("NEWAP, T: " +
		// newAP.getSimilarityValue(SimilarityType.HeidelTimeSimilarity));
		// System.out.println("NEWAP ->: " + newAP.getSimilarity(weightConfig));
		//
		// if (PRINT) {
		// System.out.println("Register new AP:");
		// newAP.printTexts();
		// System.out.println("");
		// }

		List<Sentence> subAnnotations1 = newAP.getSentence1().getSubAnnotations();
		List<Sentence> subAnnotations2 = newAP.getSentence2().getSubAnnotations();

		for (Sentence sub : newAP.getSentence1().getAllSubAnnotations()) {
			if (!subAnnotations1.contains(sub))
				subAnnotations1.add(sub);
		}

		for (Sentence sub : newAP.getSentence2().getAllSubAnnotations()) {
			if (!subAnnotations2.contains(sub))
				subAnnotations2.add(sub);
		}

		List<Sentence> allSubAnnotations = new ArrayList<Sentence>();
		allSubAnnotations.addAll(subAnnotations1);
		allSubAnnotations.addAll(subAnnotations2);

		for (Sentence subAnnotation : allSubAnnotations) {
			allowedAnnotations.remove(subAnnotation);
			if (annotationToPair.containsKey(subAnnotation))
				for (SentencePair ap : annotationToPair.get(subAnnotation)) {
					similarAnnotationsAndParagraphs.remove(ap);
				}
		}

		similarAnnotationsAndParagraphs.add(newAP);

		if (!annotationToPair.containsKey(newAP.getSentence1()))
			annotationToPair.put(newAP.getSentence1(), new HashSet<SentencePair>());
		if (!annotationToPair.containsKey(newAP.getSentence2()))
			annotationToPair.put(newAP.getSentence2(), new HashSet<SentencePair>());

		annotationToPair.get(newAP.getSentence1()).add(newAP);
		annotationToPair.get(newAP.getSentence2()).add(newAP);
		allowedAnnotations.add(newAP.getSentence1());
		allowedAnnotations.add(newAP.getSentence2());

		for (Sentence sub : subAnnotations1) {
			if (!annotationToPair.containsKey(sub))
				annotationToPair.put(sub, new HashSet<SentencePair>());
			annotationToPair.get(sub).add(newAP);
		}

		for (Sentence sub : subAnnotations2) {
			if (!annotationToPair.containsKey(sub))
				annotationToPair.put(sub, new HashSet<SentencePair>());
			annotationToPair.get(sub).add(newAP);
		}

		for (Sentence subAnnotation : subAnnotations1) {
			allAnnotations1.remove(subAnnotation);
		}
		allAnnotations1.add(newAP.getSentence1());

		for (Sentence subAnnotation : subAnnotations2) {
			allAnnotations2.remove(subAnnotation);
		}
		allAnnotations2.add(newAP.getSentence2());

		positions = new HashMap<Language, Map<Sentence, Integer>>();
		reversePositions = new HashMap<Language, Map<Integer, Sentence>>();

		buildNeighbours(allAnnotations1);
		buildNeighbours(allAnnotations2);
	}

	private class NonExtendedFirstComparator implements Comparator<SentencePair> {

		Set<SentencePair> extendedAPs;

		private NonExtendedFirstComparator(Set<SentencePair> extendedAPs) {
			this.extendedAPs = extendedAPs;
		}

		public int compare(SentencePair ap1, SentencePair ap2) {
			if (this.extendedAPs.contains(ap1) && !this.extendedAPs.contains(ap2))
				return 1;
			else if (this.extendedAPs.contains(ap2) && !this.extendedAPs.contains(ap1))
				return -1;
			else {
				Double similarity1 = ap1.getParagraphSimilarity();
				Double similarity2 = ap2.getParagraphSimilarity();

				if (similarity1 == null)
					similarity1 = ap1.getSimilarity(weightConfig);

				if (similarity2 == null)
					similarity2 = ap2.getSimilarity(weightConfig);

				if (similarity2.compareTo(similarity1) != 0)
					return similarity2.compareTo(similarity1);

				// return 1 to make it deterministic
				else {
					// TODO: MAKE THAT DETERMINISTIC AGAIN!
					return 0;
					// return
					// ap1.getAnnotation1().getRawText().compareTo(ap2.getAnnotation2().getRawText());
				}
			}
		}
	}

	private class DistanceComparator implements Comparator<SentencePair> {

		Map<SentencePair, Integer> distances1;
		Map<SentencePair, Integer> distances2;

		private DistanceComparator(Map<SentencePair, Integer> distances1, Map<SentencePair, Integer> distances2) {
			this.distances1 = distances1;
			this.distances2 = distances2;
		}

		public int compare(SentencePair a1, SentencePair a2) {
			Integer distanceAP1 = distances1.get(a1) + distances2.get(a1);
			Integer distanceAP2 = distances1.get(a2) + distances2.get(a2);

			if (distanceAP1 != distanceAP2)
				return distanceAP1.compareTo(distanceAP2);

			else {
				// TODO: MAKE THAT DETERMINISTIC AGAIN!
				// make the algorithm deterministic by a meaningless rule
				// if (a1.getAnnotation1().getNumber() <
				// a2.getAnnotation2().getNumber())
				// return 1;
				// else
				// return 0;
				return 0;
			}
		}
	}

}
