package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureComparison<T> {
	private Set<T> commonFeatures;
	private List<T> commonFeaturesSorted;
	private Set<T> allFeatures;
	private Map<T, Integer> featuresInRevision1;
	private Map<T, Integer> featuresInRevision2;
	private Set<T> featuresOnlyInRevision1;
	private List<T> featuresOnlyInRevision1Sorted;
	private Set<T> featuresOnlyInRevision2;
	private List<T> featuresOnlyInRevision2Sorted;
	Double similarity;

	public FeatureComparison(Map<T, Integer> featureSet1, Map<T, Integer> featureSet2) {
		this.featuresInRevision1 = featureSet1;
		this.featuresInRevision2 = featureSet2;
		this.commonFeatures = new LinkedHashSet<T>();
		this.commonFeatures.addAll(featureSet1.keySet());
		this.commonFeatures.retainAll(featureSet2.keySet());
		this.allFeatures = new HashSet<T>();
		this.allFeatures.addAll(featureSet1.keySet());
		this.allFeatures.addAll(featureSet2.keySet());
		this.featuresOnlyInRevision1 = new HashSet<T>();
		this.featuresOnlyInRevision1.addAll(this.allFeatures);
		this.featuresOnlyInRevision1.removeAll(this.featuresInRevision2.keySet());
		this.featuresOnlyInRevision2 = new HashSet<T>();
		this.featuresOnlyInRevision2.addAll(this.allFeatures);
		this.featuresOnlyInRevision2.removeAll(this.featuresInRevision1.keySet());
	}

	public Set<T> getCommonFeatures() {
		return this.commonFeatures;
	}

	public void setCommonFeatures(Set<T> commonFeatures) {
		this.commonFeatures = commonFeatures;
	}

	public List<T> getCommonFeaturesSorted() {
		if (this.commonFeaturesSorted == null) {
			this.commonFeaturesSorted = new ArrayList<T>();
			this.commonFeaturesSorted.addAll(this.commonFeatures);
			NumberOfOccurrencesComparator comparator = new NumberOfOccurrencesComparator();
			Collections.sort(this.commonFeaturesSorted, comparator);
		}
		return this.commonFeaturesSorted;
	}

	public List<T> getFeaturesOnlyInRevision1Sorted() {
		if (this.featuresOnlyInRevision1Sorted == null) {
			this.featuresOnlyInRevision1Sorted = new ArrayList<T>();
			this.featuresOnlyInRevision1Sorted.addAll(this.featuresOnlyInRevision1);
			NumberOfOccurrencesComparator comparator = new NumberOfOccurrencesComparator();
			Collections.sort(this.featuresOnlyInRevision1Sorted, comparator);
		}
		return this.featuresOnlyInRevision1Sorted;
	}

	public List<T> getFeaturesOnlyInRevision2Sorted() {
		if (this.featuresOnlyInRevision2Sorted == null) {
			this.featuresOnlyInRevision2Sorted = new ArrayList<T>();
			this.featuresOnlyInRevision2Sorted.addAll(this.featuresOnlyInRevision2);
			NumberOfOccurrencesComparator comparator = new NumberOfOccurrencesComparator();
			Collections.sort(this.featuresOnlyInRevision2Sorted, comparator);
		}
		return this.featuresOnlyInRevision2Sorted;
	}

	public Set<T> getAllFeatures() {
		return this.allFeatures;
	}

	public void setAllFeatures(Set<T> allFeatures) {
		this.allFeatures = allFeatures;
	}

	public Set<T> getFeaturesInRevision1() {
		return this.featuresInRevision1.keySet();
	}

	public void setFeaturesOnlyInRevision1(Set<T> featuresOnlyInRevision1) {
		this.featuresOnlyInRevision1 = featuresOnlyInRevision1;
	}

	public Set<T> getFeaturesInRevision2() {
		return this.featuresInRevision2.keySet();
	}

	public void setFeaturesOnlyInRevision2(Set<T> featuresOnlyInRevision2) {
		this.featuresOnlyInRevision2 = featuresOnlyInRevision2;
	}

	public Set<T> getFeaturesOnlyInRevision1() {
		return this.featuresOnlyInRevision1;
	}

	public Set<T> getFeaturesOnlyInRevision2() {
		return this.featuresOnlyInRevision2;
	}

	public double getSimilarity() {
		if (this.similarity == null) {
			this.similarity = (double) this.commonFeatures.size() / (double) this.allFeatures.size();
		}
		if (Double.isNaN(this.similarity)) {
			return 0.0;
		}
		return this.similarity;
	}

	public int getNumberOfOccurrencesInRevision1(T feature) {
		if (this.featuresOnlyInRevision2.contains(feature)) {
			return 0;
		}
		return this.featuresInRevision1.get(feature);
	}

	public int getNumberOfOccurrencesInRevision2(T feature) {
		if (this.featuresOnlyInRevision1.contains(feature)) {
			return 0;
		}
		return this.featuresInRevision2.get(feature);
	}

	public int getNumberOfOccurrences(T feature) {
		return this.getNumberOfOccurrencesInRevision1(feature) + this.getNumberOfOccurrencesInRevision2(feature);
	}

	class NumberOfOccurrencesComparator implements Comparator<T> {
		NumberOfOccurrencesComparator() {
		}

		@Override
		public int compare(T feature1, T feature2) {
			int numberOfOccurences2;
			int numberOfOccurences1 = FeatureComparison.this.getNumberOfOccurrences(feature1);
			if (numberOfOccurences1 < (numberOfOccurences2 = FeatureComparison.this.getNumberOfOccurrences(feature2))) {
				return 1;
			}
			if (numberOfOccurences1 > numberOfOccurences2) {
				return -1;
			}
			return 0;
		}
	}

}
