package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureFrequency<T> {

	private Map<Revision, List<T>> featuresByRevision;

	private Map<T, Set<Revision>> featureFrequences;

	private Set<Revision> revisions;

	public FeatureFrequency() {
		this.revisions = new HashSet<Revision>();
		this.featuresByRevision = new HashMap<Revision, List<T>>();
		this.featureFrequences = new HashMap<T, Set<Revision>>();
	}

	public void incrementFrequencies(Revision revision, Set<T> features) {
		this.revisions.add(revision);
		featuresByRevision.put(revision, new ArrayList<T>());
		for (T feature : features) {
			if (!this.featureFrequences.containsKey(feature))
				this.featureFrequences.put(feature, new HashSet<Revision>());
			this.featureFrequences.get(feature).add(revision);
			featuresByRevision.get(revision).add(feature);
		}
	}

	public void createOrderedFeatureLists() {
		// Order feature list, s.t. those features occuring in most of all
		// revisions are at the beginning of the feature list
		for (Revision revision : revisions) {
			Collections.sort(featuresByRevision.get(revision), new FeatureFrequencyComparator());
		}
	}

	public Map<Revision, List<T>> getFeaturesByRevision() {
		return featuresByRevision;
	}

	public Map<T, Set<Revision>> getFeatureFrequences() {
		return featureFrequences;
	}



	private class FeatureFrequencyComparator implements Comparator<T> {

		@Override
		public int compare(T f1, T f2) {

			Integer freq1 = featureFrequences.get(f1).size();
			Integer freq2 = featureFrequences.get(f2).size();
		
			return freq2.compareTo(freq1);
		}
	}

}
