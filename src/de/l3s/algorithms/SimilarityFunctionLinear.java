package de.l3s.algorithms;

import java.util.HashMap;
import java.util.Map;

import de.l3s.model.SentencePair;
import de.l3s.similarity.SimilarityType;
import de.l3s.util.FormatUtil;

/**
 * A simple similarity function that assigns a similarity value to a sentence
 * pair according to feature similarities that are weighted and summed up.
 */
public class SimilarityFunctionLinear extends SimilarityFunction {

	private Map<SimilarityType, Double> similaritiesWithWeights;

	private double thresholdPartly;

	private double thresholdSame;

	private String name;

	private double intercept = 0;

	private boolean thresholdIsFix = false;

	private Map<SimilarityType, Double> similarityTypeThresholds;

	private double minThreshold = 0.0;

	public SimilarityFunctionLinear() {
		this.similaritiesWithWeights = new HashMap<SimilarityType, Double>();
		this.similarityTypeThresholds = new HashMap<SimilarityType, Double>();
	}

	public SimilarityFunctionLinear(SimilarityType similarityType, double thresholdPartly, double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType, 1.0);
	}

	public SimilarityFunctionLinear(SimilarityType similarityType, double weight, double thresholdPartly,
			double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType, weight);
	}

	public SimilarityFunctionLinear(SimilarityType similarityType1, double weight1, SimilarityType similarityType2,
			double weight2, double thresholdPartly, double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType1, weight1);
		addMeasure(similarityType2, weight2);
	}

	public SimilarityFunctionLinear(SimilarityType similarityType1, double weight1, SimilarityType similarityType2,
			double weight2, SimilarityType similarityType3, double weight3, double thresholdPartly,
			double thresholdSame) {
		this();
		this.thresholdPartly = thresholdPartly;
		this.thresholdSame = thresholdSame;
		addMeasure(similarityType1, weight1);
		addMeasure(similarityType2, weight2);
		addMeasure(similarityType3, weight3);
	}

	public SimilarityFunctionLinear(SimilarityType similarityType1, double weight1, SimilarityType similarityType2,
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

	public SimilarityFunctionLinear(double thresholdPartly, double thresholdSame) {
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
				sb.append(similarity + ": " + FormatUtil.round(similaritiesWithWeights.get(similarity), 2));
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

	public SimilarityFunctionLinear copy() {
		SimilarityFunctionLinear copy = new SimilarityFunctionLinear();
		copy.setIntercept(intercept);
		copy.setMinThreshold(minThreshold);
		copy.setThresholdIsFix(thresholdIsFix);
		copy.setThresholdPartly(thresholdPartly);
		copy.setThresholdSame(thresholdSame);
		copy.setName(name);

		Map<SimilarityType, Double> similaritiesWithWeightsCopy = new HashMap<SimilarityType, Double>();
		for (SimilarityType st : similaritiesWithWeights.keySet())
			similaritiesWithWeightsCopy.put(st, similaritiesWithWeights.get(st));

		Map<SimilarityType, Double> similaritiesWithWeightsThresholds = new HashMap<SimilarityType, Double>();
		for (SimilarityType st : similarityTypeThresholds.keySet())
			similaritiesWithWeightsThresholds.put(st, similarityTypeThresholds.get(st));

		copy.setSimilaritiesWithWeights(similaritiesWithWeightsCopy);
		copy.setSimilarityTypeThresholds(similaritiesWithWeightsThresholds);
		return copy;
	}

	public void normalize(boolean normalizeThresholds) {
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

		if (normalizeThresholds) {
			this.thresholdPartly = this.thresholdPartly / weightSum;
			this.thresholdSame = this.thresholdSame / weightSum;
		}
	}

	public void normalize() {
		normalize(true);
	}

	public String getStringRepresentation() {
		String result = "";
		for (SimilarityType type : similaritiesWithWeights.keySet()) {
			result += type.name() + " (" + similaritiesWithWeights.get(type) + ")\n";
		}
		result += "Threshold: " + getThresholdPartly();
		return result;
	}

	public double computeSimilarity(SentencePair sp) {
		double weightConfigSimilarity = 0.0;

		for (SimilarityType similarityType : this.getSimilaritiesWithWeights().keySet()) {
			double weight = this.getSimilaritiesWithWeights().get(similarityType);
			double s = sp.getSimilarities().get(similarityType);
			weightConfigSimilarity += weight * s;
		}

		weightConfigSimilarity += this.getIntercept();

		for (SimilarityType similarityType : this.getSimilarityTypeThresholds().keySet()) {
			if (this.getSimilarityTypeThresholds().containsKey(similarityType)) {
				double s = sp.getSimilarities().get(similarityType);
				if (s < this.getSimilarityTypeThresholds().get(similarityType))
					weightConfigSimilarity = 0.0;
			}
		}

		return weightConfigSimilarity;
	}

	public String getCode() {
		String line1 = "SimilarityFunctionLinear subSimFunction = new SimilarityFunctionLinear(";

		for (SimilarityType type : similaritiesWithWeights.keySet()) {
			line1 += "SimilarityType." + type.toString() + "," + similaritiesWithWeights.get(type) + ", ";
		}

		line1 += "thresholdPartly, thresholdSame);";

		String line2 = "subSimFunction.setIntercept(" + this.getIntercept() + ");";

		return line1 + "\n" + line2;
	}

}
