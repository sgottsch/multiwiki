package de.l3s.algorithms;

import java.util.HashMap;
import java.util.Map;

import de.l3s.similarity.RelativeDocumentPositionSimilarity;
import de.l3s.similarity.Similarity;
import de.l3s.similarity.SimilarityType;
import de.l3s.similarity.links.EntityAllTypesJaccardSimilarity;
import de.l3s.similarity.links.EntityOverlapSimilarity;
import de.l3s.similarity.links.EntitySimilarity;
import de.l3s.similarity.links.ExternalLinkHostSimilarity;
import de.l3s.similarity.links.ExternalLinkSimilarity;
import de.l3s.similarity.links.InternalLinkSimilarity;
import de.l3s.similarity.neighbour.PreviousSentenceCosineSimilarity;
import de.l3s.similarity.neighbour.WikiParagraphEntitySimilarity;
import de.l3s.similarity.text.DictionaryCosineSimilarity;
import de.l3s.similarity.text.DictionaryOverlapSimilarity;
import de.l3s.similarity.text.NGramSimilarity;
import de.l3s.similarity.text.TextLengthSimilarity;
import de.l3s.similarity.text.TextOverlapSimilarity;
import de.l3s.similarity.text.WordCosineSimilarity;
import de.l3s.similarity.time.HeidelTimeCosineSimilarity;
import de.l3s.similarity.time.HeidelTimeSimilarity;
import de.l3s.similarity.time.HeidelTimeStringsOverlapSimilarity;
import de.l3s.similarity.time.ParagraphTimeSimilarity;

public abstract class SimilarityFunction implements SimilarityFunctionI {

	private Map<SimilarityType, Double> similaritiesWithWeights;

	private double thresholdPartly;

	private double thresholdSame;

	private String name;

	private double intercept = 0;

	private boolean thresholdIsFix = false;

	private Map<SimilarityType, Double> similarityTypeThresholds;

	private double minThreshold = 0.0;

	public SimilarityFunction() {
		this.similaritiesWithWeights = new HashMap<SimilarityType, Double>();
		this.similarityTypeThresholds = new HashMap<SimilarityType, Double>();
	}

	public SimilarityFunction(SimilarityType similarityType, double thresholdPartly, double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType, 1.0);
	}

	public SimilarityFunction(SimilarityType similarityType, double weight, double thresholdPartly,
			double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType, weight);
	}

	public SimilarityFunction(SimilarityType similarityType1, double weight1, SimilarityType similarityType2,
			double weight2, double thresholdPartly, double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType1, weight1);
		addMeasure(similarityType2, weight2);
	}

	public SimilarityFunction(SimilarityType similarityType1, double weight1, SimilarityType similarityType2,
			double weight2, SimilarityType similarityType3, double weight3, double thresholdPartly,
			double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType1, weight1);
		addMeasure(similarityType2, weight2);
		addMeasure(similarityType3, weight3);
	}

	public SimilarityFunction(SimilarityType similarityType1, double weight1, SimilarityType similarityType2,
			double weight2, SimilarityType similarityType3, double weight3, SimilarityType similarityType4,
			double weight4, double thresholdPartly, double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType1, weight1);
		addMeasure(similarityType2, weight2);
		addMeasure(similarityType3, weight3);
		addMeasure(similarityType4, weight4);
	}

	public SimilarityFunction(double thresholdPartly, double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
	}

	public void addMeasure(SimilarityType similarityType, double weight) {
		if (similaritiesWithWeights.containsKey(similarityType))
			System.err.println("Warning: Weight configuration already contains " + similarityType);
		similaritiesWithWeights.put(similarityType, weight);
	}

	public boolean checkWeightSumEqualsOne() {
		double weightSum = 0.0;
		for (Double weight : similaritiesWithWeights.values()) {
			weightSum += weight;
		}
		return weightSum == 1.0;
	}

	public String getName() {
		if (this.name == null) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (SimilarityType similarity : similaritiesWithWeights.keySet()) {
				i += 1;
				sb.append(similarity + ": " + similaritiesWithWeights.get(similarity));
				if (i != similaritiesWithWeights.keySet().size()) {
					sb.append(", ");
				}
			}
			name = sb.toString();
		}
		return name;
	}

	public Map<SimilarityType, Double> getSimilaritiesWithWeights() {
		return this.similaritiesWithWeights;
	}

	public double getThresholdPartly() {
		if (thresholdPartly < minThreshold)
			return minThreshold;
		return thresholdPartly;
	}

	public double getThresholdSame() {
		if (thresholdPartly < minThreshold)
			return minThreshold;
		return thresholdSame;
	}

	public static Similarity getSimilarity(SimilarityType similarityType) {
		switch (similarityType) {
		case ExternalLinkHostSimilarity:
			return new ExternalLinkHostSimilarity();
		case ExternalLinkSimilarity:
			return new ExternalLinkSimilarity();
		case NGramSimilarity:
			return new NGramSimilarity();
		case TextLengthSimilarity:
			return new TextLengthSimilarity();
		case TextOverlapSimilarity:
			return new TextOverlapSimilarity();
		case WordCosineSimilarity:
			return new WordCosineSimilarity();
		case EntitySimilarity:
			return new EntitySimilarity();
		case InternalLinkSimilarity:
			return new InternalLinkSimilarity();
		case EntityAllTypesJaccardSimilarity:
			return new EntityAllTypesJaccardSimilarity();
		case RelativeDocumentPositionSimilarity:
			return new RelativeDocumentPositionSimilarity();
		case PreviousSentenceCosineSimilarity:
			return new PreviousSentenceCosineSimilarity();
		case WikiParagraphEntitySimilarity:
			return new WikiParagraphEntitySimilarity();
		case HeidelTimeCosineSimilarity:
			return new HeidelTimeCosineSimilarity();
		case HeidelTimeSimilarity:
			return new HeidelTimeSimilarity();
		case HeidelTimeStringsOverlap:
			return new HeidelTimeStringsOverlapSimilarity();
		case DictionaryOverlapSimilarity:
			return new DictionaryOverlapSimilarity();
		case DictionaryCosineSimilarity:
			return new DictionaryCosineSimilarity();
		case ParagraphTimeSimilarity:
			return new ParagraphTimeSimilarity();
		case EntityOverlapSimilarity:
			return new EntityOverlapSimilarity();
		default:
			throw new NullPointerException("SimilarityType " + similarityType + " does not exist.");
		}
	}

	public void setIntercept(double intercept) {
		this.intercept = intercept;
	}

	public double getIntercept() {
		return this.intercept;
	}

	public void setThresholdPartly(double thresholdPartly) {
		if (!thresholdIsFix)
			this.thresholdPartly = thresholdPartly;
	}

	public void setThresholdSame(double thresholdSame) {
		if (!thresholdIsFix)
			this.thresholdSame = thresholdSame;
	}

	public void setThresholdIsFix(boolean thresholdIsFix) {
		this.thresholdIsFix = thresholdIsFix;
	}

	public void addSimilarityTypeThreshold(SimilarityType similarityType, double threshold) {
		this.similarityTypeThresholds.put(similarityType, threshold);
	}

	public Map<SimilarityType, Double> getSimilarityTypeThresholds() {
		return similarityTypeThresholds;
	}

	public void setMinThreshold(double minThreshold) {
		this.minThreshold = minThreshold;
	}

	public void setSimilaritiesWithWeights(Map<SimilarityType, Double> similaritiesWithWeights) {
		this.similaritiesWithWeights = similaritiesWithWeights;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSimilarityTypeThresholds(Map<SimilarityType, Double> similarityTypeThresholds) {
		this.similarityTypeThresholds = similarityTypeThresholds;
	}

	public void normalize() {

		double weightSum = intercept;

		for (SimilarityType st : similaritiesWithWeights.keySet()) {
			weightSum += similaritiesWithWeights.get(st);
		}

		this.intercept = this.intercept / weightSum;

		for (SimilarityType st : similaritiesWithWeights.keySet()) {
			similaritiesWithWeights.put(st, similaritiesWithWeights.get(st) / weightSum);
		}

		// remove intercept
		for (SimilarityType st : similaritiesWithWeights.keySet()) {
			similaritiesWithWeights.put(st,
					similaritiesWithWeights.get(st) + (this.intercept * similaritiesWithWeights.get(st)));
		}
		this.intercept = 0;

		this.thresholdPartly = this.thresholdPartly / weightSum;
		this.thresholdSame = this.thresholdSame / weightSum;
	}

	public String getStringRepresentation() {
		String result = "";
		for (SimilarityType type : similaritiesWithWeights.keySet()) {
			result += type.name() + " (" + similaritiesWithWeights.get(type) + ")\n";
		}
		return result;
	}

}
