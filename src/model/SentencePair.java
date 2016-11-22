package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.RealVector;

import algorithms.ConditionWeightConfig;
import algorithms.SimilarityFunctionLinear;
import similarity.Similarity;
import similarity.SimilarityType;
import tfidf.CollectionType;
import translate.Language;
import util.FormatUtil;

/**
 * An instance of this model represents a single sentence pair with sentences in
 * two different languages.
 */
public class SentencePair implements Comparable<SentencePair> {

	private Sentence sentence1;
	private Sentence sentence2;

	private BinaryComparison comparison;

	private double similarity;

	private String similarityString;

	private Double paragraphSimilarity;

	private Set<SimilarityType> applicableSimilarityTypes;

	private Map<SimilarityType, Similarity> similarityTypesToSimilarityMeasures;

	private Map<SimilarityType, Double> similarities;

	private String dbIdentifier;

	private Map<CollectionType, RealVector> termVectors;

	private Map<SimilarityFunctionLinear, Double> similaritiesForWeightConfigs;

	private Map<Sentence, List<Sentence>> gapSentences;

	// TODO: DELETE FOLLOWING VARIABLES
	private Double mutualInformation;

	private Integer distance;

	public SentencePair(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {
		this.comparison = comparison;
		this.sentence1 = annotation1;
		this.sentence2 = annotation2;
		this.similaritiesForWeightConfigs = new HashMap<SimilarityFunctionLinear, Double>();
		similarities = new HashMap<SimilarityType, Double>();
		similarityTypesToSimilarityMeasures = new HashMap<SimilarityType, Similarity>();
		this.applicableSimilarityTypes = new HashSet<SimilarityType>();
		this.termVectors = new HashMap<CollectionType, RealVector>();
		this.gapSentences = new HashMap<Sentence, List<Sentence>>();
		this.gapSentences.put(annotation1, new ArrayList<Sentence>());
		this.gapSentences.put(annotation2, new ArrayList<Sentence>());
	}

	public Sentence getSentence1() {
		return sentence1;
	}

	public void setSentence1(Sentence sentence1) {
		this.sentence1 = sentence1;
	}

	public Sentence getSentence2() {
		return sentence2;
	}

	public void setSentence2(Sentence sentence2) {
		this.sentence2 = sentence2;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public Map<SimilarityType, Double> getSimilarities() {
		return similarities;
	}

	public void setSimilarities(Map<SimilarityType, Double> similarities) {
		this.similarities = similarities;
	}

	public double calculateSimilarity(List<Double> weights, List<Similarity> similarityMeasures) {

		this.similarityString = "";

		double similarity = 0;
		for (int i = 0; i < weights.size(); i++) {
			double weight = weights.get(i);
			if (weight > 0) {
				double s = similarityMeasures.get(i).calculateSimilarity(comparison, sentence1, sentence2);
				similarities.put(similarityMeasures.get(i).getSimilarityType(), s);
				similarity += weight * s;
				similarityString += " - " + similarityMeasures.get(i).getName() + ": " + FormatUtil.round(s, 3);
			}
		}

		this.similarityString = "Similarity: " + FormatUtil.round(similarity, 3) + similarityString;

		this.similarity = similarity;
		return similarity;
	}

	@Override
	public int compareTo(SentencePair ap2) {
		if (this.similarity > ap2.getSimilarity())
			return -1;
		else if (this.similarity != ap2.getSimilarity())
			return 1;
		else
			return 0;
	}

	public String getSimilarityString() {
		return this.similarityString;
	}

	public double getSimilarity(Similarity s) {
		return this.similarities.get(s.getName());
	}

	public Similarity getSimilarity(SimilarityType similarityType) {
		if (this.similarityTypesToSimilarityMeasures.containsKey(similarityType))
			return this.similarityTypesToSimilarityMeasures.get(similarityType);
		else
			return null;
	}

	public double getSimilarityValue(SimilarityType similarityType) {
		return this.similarities.get(similarityType);
	}

	public void calculateSimilarities(Set<SimilarityType> similarityTypes) {
		similarityString = "";
		for (SimilarityType similarityType : similarityTypes) {
			Similarity similarityMeasure = SimilarityFunctionLinear.getSimilarity(similarityType);
			double s = similarityMeasure.calculateSimilarity(comparison, this);
			similarities.put(similarityType, s);
			similarityString += similarityMeasure.getName() + ":";
			if (similarityMeasure.isApplicable()) {
				similarityString += FormatUtil.round(s, 8) + ";";
				applicableSimilarityTypes.add(similarityType);
			} else
				similarityString += "-;";
			similarityTypesToSimilarityMeasures.put(similarityType, similarityMeasure);
		}
	}

	public void calculateSimilarities(List<SimilarityType> similarityTypes) {
		similarityString = "";
		for (SimilarityType similarityType : similarityTypes) {
			Similarity similarityMeasure = SimilarityFunctionLinear.getSimilarity(similarityType);
			double s = similarityMeasure.calculateSimilarity(comparison, this);
			similarities.put(similarityType, s);
			similarityString += similarityMeasure.getName() + ":";
			if (similarityMeasure.isApplicable()) {
				similarityString += FormatUtil.round(s, 8) + ";";
				applicableSimilarityTypes.add(similarityType);
			} else
				similarityString += "-;";
			similarityTypesToSimilarityMeasures.put(similarityType, similarityMeasure);
		}
	}

	public double calculateSimilarity(ConditionWeightConfig weightConfig) {
		SimilarityFunctionLinear function = weightConfig.getWeightConfig(this);
		return calculateSimilarity(function);
	}

	public double calculateSimilarity(SimilarityFunctionLinear weightConfig) {

		double sim = weightConfig.computeSimilarity(this);

		similaritiesForWeightConfigs.put(weightConfig, sim);

		return sim;

		// double weightConfigSimilarity = 0.0;
		//
		// for (SimilarityType similarityType :
		// weightConfig.getSimilaritiesWithWeights().keySet()) {
		// double weight =
		// weightConfig.getSimilaritiesWithWeights().get(similarityType);
		// double s = similarities.get(similarityType);
		// weightConfigSimilarity += weight * s;
		// }
		//
		// weightConfigSimilarity += weightConfig.getIntercept();
		//
		// for (SimilarityType similarityType :
		// weightConfig.getSimilarityTypeThresholds().keySet()) {
		// if
		// (weightConfig.getSimilarityTypeThresholds().containsKey(similarityType))
		// {
		// double s = similarities.get(similarityType);
		// if (s <
		// weightConfig.getSimilarityTypeThresholds().get(similarityType))
		// weightConfigSimilarity = 0.0;
		// }
		// }
		//
		// similaritiesForWeightConfigs.put(weightConfig,
		// weightConfigSimilarity);
		// return weightConfigSimilarity;
	}

	public double getSimilarity(SimilarityFunctionLinear weightConfig) {
		return this.similaritiesForWeightConfigs.get(weightConfig);
	}

	public double getSimilarity(ConditionWeightConfig conditionWeightConfig) {
		SimilarityFunctionLinear weightConfig = conditionWeightConfig.getWeightConfig(this);
		return this.similaritiesForWeightConfigs.get(weightConfig);
	}

	public double getThresholdPartly(ConditionWeightConfig conditionWeightConfig) {
		SimilarityFunctionLinear weightConfig = conditionWeightConfig.getWeightConfig(this);
		return weightConfig.getThresholdPartly();
	}

	public double getThresholdSame(ConditionWeightConfig conditionWeightConfig) {
		SimilarityFunctionLinear weightConfig = conditionWeightConfig.getWeightConfig(this);
		return weightConfig.getThresholdSame();
	}

	public boolean isApplicable(SimilarityType similarityType) {
		return this.applicableSimilarityTypes.contains(similarityType);
	}

	public BinaryComparison getComparison() {
		return comparison;
	}

	public Double getParagraphSimilarity() {
		return paragraphSimilarity;
	}

	public void setParagraphSimilarity(double paragraphSimilarity) {
		this.paragraphSimilarity = paragraphSimilarity;
	}

	public String getDbIdentifier() {
		if (this.dbIdentifier == null) {
			dbIdentifier = buildDBIdentifier(sentence1.getLanguage(), sentence1.getRevisionId(), +sentence1.getNumber(),
					sentence2.getLanguage(), sentence2.getRevisionId(), +sentence2.getNumber());
		}
		return dbIdentifier;
	}

	public void setDbIdentifier(String dbIdentifier) {
		this.dbIdentifier = dbIdentifier;
	}

	public static String buildDBIdentifier(Language language1, long revisionId1, int annotationNumber1,
			Language language2, long revisionId2, int annotationNumber2) {
		return language1 + "-" + revisionId1 + "-" + annotationNumber1 + "-" + language2 + "-" + revisionId2 + "-"
				+ annotationNumber2;
	}

	public boolean hasTermVector(CollectionType collectionType) {
		return termVectors.containsKey(collectionType);
	}

	public RealVector getTermVector(CollectionType collectionType) {
		return termVectors.get(collectionType);
	}

	public void setTermVector(CollectionType collectionType, RealVector termVector) {
		this.termVectors.put(collectionType, termVector);
	}

	public void printTexts() {
		System.out.println(sentence1.getRawText());
		System.out.println(sentence2.getRawText());
	}

	public String getFormattedSimilarityString() {
		// String formattedSimilarityString = "";
		//
		// String[] rows = similarityString.split(";");
		//
		// for (String row : rows) {
		// String[] vals = row.split(":");
		// formattedSimilarityString += vals[0];
		// if (vals[1].equals("-")) {
		// formattedSimilarityString += ": -";
		// } else {
		// double sim = Double.parseDouble(vals[1]);
		// sim = FormatUtil.round(sim, 3);
		// formattedSimilarityString += ": " + sim;
		// }
		// if (row != rows[rows.length - 1])
		// formattedSimilarityString += "\n";
		// }
		//
		// return formattedSimilarityString;

		String formattedSimilarityString = "";
		boolean first = true;
		for (SimilarityType similarityType : similarities.keySet()) {
			if (!first)
				formattedSimilarityString += "\n";
			Similarity similarityMeasure = SimilarityFunctionLinear.getSimilarity(similarityType);
			Double similarity = similarities.get(similarityType);
			formattedSimilarityString += similarityMeasure.getEasyName() + ": ";
			if (similarityMeasure.isApplicable())
				formattedSimilarityString += FormatUtil.round(similarity, 3);
			else
				formattedSimilarityString += "-";
			first = false;
		}

		return formattedSimilarityString;
	}

	public Map<Sentence, List<Sentence>> getGapSentences() {
		return gapSentences;
	}

	public List<Sentence> getGapSentences(Sentence sentence) {
		return gapSentences.get(sentence);
	}

	public void setGapSentences(Map<Sentence, List<Sentence>> gapSentences) {
		this.gapSentences = gapSentences;
	}

	public void setGapSentences(Sentence sentence, List<Sentence> gapSentences) {
		this.gapSentences.put(sentence, gapSentences);
	}

	public void addGapSentences(Sentence sentence, List<Sentence> gapSentences) {
		if (gapSentences.size() == 0)
			return;
		// System.out.println("Gaps for " + sentence.getRawText());
		// for (Annotation a : gapSentences)
		// System.out.println(a.getRawText());
		for (Sentence gapSentence : gapSentences) {
			for (Sentence sub : gapSentence.getAllSubAnnotations()) {
				if (!this.gapSentences.get(sentence).contains(sub))
					this.gapSentences.get(sentence).add(sub);
			}
		}
	}

	public void copyGapSentences(SentencePair ap) {
		setGapSentences(this.sentence1, ap.getGapSentences().get(ap.getSentence1()));
		setGapSentences(this.sentence2, ap.getGapSentences().get(ap.getSentence2()));
	}

	public String getAllSubIdsString() {
		String allSubIdsString = "";
		boolean first = true;
		for (Sentence s : sentence1.getAllSubAnnotations()) {
			if (!first)
				allSubIdsString += "-";
			else
				first = false;
			allSubIdsString += s.getNumber();
		}

		allSubIdsString += " | ";

		first = true;
		for (Sentence s : sentence2.getAllSubAnnotations()) {
			if (!first)
				allSubIdsString += "-";
			else
				first = false;
			allSubIdsString += s.getNumber();
		}

		return allSubIdsString;
	}

	public boolean containsSameSentenceMultipleTimes() {
		return sentence1.containsSameSentenceMultipleTimes() || sentence2.containsSameSentenceMultipleTimes();
	}

	public boolean isDirectSentenceNeighbourOf(SentencePair pair) {

		Sentence firstSentence1 = getSentence1().getFirstSentence();
		Sentence firstSentence2 = getSentence2().getFirstSentence();
		Sentence lastSentence1 = getSentence1().getLastSentence();
		Sentence lastSentence2 = getSentence2().getLastSentence();

		Sentence firstSentence1Pre = firstSentence1.getPreSentence();
		Sentence firstSentence2Pre = firstSentence2.getPreSentence();

		Sentence lastSentence1Seq = lastSentence1.getSeqSentence();
		Sentence lastSentence2Seq = lastSentence2.getSeqSentence();

		if (lastSentence1Seq != null && lastSentence2Seq != null
				&& lastSentence1Seq == pair.getSentence1().getFirstSentence()
				&& lastSentence2Seq == pair.getSentence2().getFirstSentence())
			return true;

		if (firstSentence1Pre != null && firstSentence2Pre != null
				&& firstSentence1Pre == pair.getSentence1().getLastSentence()
				&& firstSentence2Pre == pair.getSentence2().getLastSentence())
			return true;

		/**
		 * An instance of this model represents a single sentence pair with
		 * sentences in two different languages.
		 */
		return false;
	}

	public void calculateSimilarity(SimilarityType type) {
		Set<SimilarityType> types = new HashSet<SimilarityType>();
		types.add(type);
		calculateSimilarities(types);
	}

	public Set<SimilarityType> getApplicableSimilarityTypes() {
		return applicableSimilarityTypes;
	}

	public void computeMutualInformation() {
		this.mutualInformation = 0d;

		for (Sentence s1 : getSentence1().getAllSubAnnotations()) {
			double sum = 0;
			for (Sentence s2 : getSentence2().getAllSubAnnotations()) {
				SentencePair spxy = s1.getSentencePair(s2);
				double pxy = spxy.getSimilarity();

				double px = s1.getProbability();
				double py = s2.getProbability();
				// System.out.println("PXY: "+pxy+", px: "+px+", py: " + py+",
				// px*py: "+(px*py));
				double p = pxy * Math.log10((double) (pxy / (px * py)));
				if (!Double.isNaN(p))
					sum += p;
			}
			this.mutualInformation += sum;
		}
	}

	public double getMutualInformation() {
		return mutualInformation;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

}
