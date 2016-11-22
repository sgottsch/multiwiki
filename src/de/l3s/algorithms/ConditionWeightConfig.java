package de.l3s.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.l3s.model.Sentence;
import de.l3s.model.SentencePair;
import de.l3s.model.SentencePairComparator;
import de.l3s.similarity.SimilarityType;
import edu.stanford.nlp.util.StringUtils;

/**
 * Similarity function to compute a similarity value for a sentence pair. It can
 * be configurated to apply specific similarity functions according to the
 * applicabilty of specific sentences. For example, entity similarity is only
 * used if the sentence pair has entities.
 */
public class ConditionWeightConfig {

	private String name;

	private Map<List<SimilarityType>, SimilarityFunctionLinear> weightConfigs;

	private Set<SimilarityType> similarityTypes;

	int maxNrOfSimilarityTypes = 0;

	private List<SimilarityFunctionLinear> usedWeightConfigs;

	private boolean usePaperMethod = false;

	public ConditionWeightConfig(String name) {
		this.name = name;
		this.weightConfigs = new HashMap<List<SimilarityType>, SimilarityFunctionLinear>();
		this.usedWeightConfigs = new ArrayList<SimilarityFunctionLinear>();
		this.similarityTypes = new HashSet<SimilarityType>();
	}

	public void addWeightConfig(SimilarityFunctionLinear weightConfig) {
		addWeightConfig(new ArrayList<SimilarityType>(), weightConfig);
	}

	public void addWeightConfig(List<SimilarityType> sts, SimilarityFunctionLinear weightConfig) {
		weightConfigs.put(sts, weightConfig);
		if (sts.size() > maxNrOfSimilarityTypes)
			maxNrOfSimilarityTypes = sts.size();
		usedWeightConfigs.add(weightConfig);
		similarityTypes.addAll(sts);
		similarityTypes.addAll(weightConfig.getSimilaritiesWithWeights().keySet());
	}

	public String getName() {
		return this.name;
	}

	public SimilarityFunctionLinear getWeightConfig(SentencePair ap) {

		int i = maxNrOfSimilarityTypes;
		// At first search for those weightConfigs with the most conditions

		while (i >= 0) {
			for (List<SimilarityType> stList : weightConfigs.keySet()) {

				if (stList.size() != i)
					continue;

				boolean found = true;
				for (SimilarityType st : stList) {
					if (!ap.isApplicable(st)) {
						found = false;
					}
				}
				if (found) {
					return weightConfigs.get(stList);
				}
			}
			i -= 1;
		}

		return null;
	}

	public List<SimilarityFunctionLinear> getWeightConfigs() {
		return usedWeightConfigs;
	}

	public Set<SimilarityType> getSimilarityTypes() {
		return similarityTypes;
	}

	public boolean usePaperMethod() {
		return usePaperMethod;
	}

	public void setUsePaperMethod(boolean usePaperMethod) {
		this.usePaperMethod = usePaperMethod;
	}

	public void normalize() {
		normalize(true);
	}

	public void normalize(boolean normalizeThresholds) {
		for (SimilarityFunctionLinear wc : usedWeightConfigs) {
			wc.normalize(normalizeThresholds);
			System.out.println(wc.getStringRepresentation());
		}
	}

	public PriorityQueue<SentencePair> sortSentencePairs(Set<SentencePair> unsortedSentencePairs) {
		Comparator<SentencePair> comp = new SentencePairComparator(this);
		PriorityQueue<SentencePair> sortedSentencePairs = new PriorityQueue<SentencePair>(unsortedSentencePairs.size(),
				comp);
		sortedSentencePairs.addAll(unsortedSentencePairs);

		// special case for "paper method": never return two sentence pairs that
		// have a sentence in common
		if (this.usePaperMethod) {
			sortedSentencePairs = paperMethod(sortedSentencePairs);
		}

		return sortedSentencePairs;
	}

	private PriorityQueue<SentencePair> paperMethod(PriorityQueue<SentencePair> sortedSentencePairs) {

		// Create a mapping from each sentence to the sentence pairs where it is
		// contained

		Set<SentencePair> allAnnotationPairsSet = new HashSet<SentencePair>();
		Map<Sentence, Set<SentencePair>> aps1 = new HashMap<Sentence, Set<SentencePair>>();
		Map<Sentence, Set<SentencePair>> aps2 = new HashMap<Sentence, Set<SentencePair>>();

		for (SentencePair ap : sortedSentencePairs) {
			Sentence a1 = ap.getSentence1();
			Sentence a2 = ap.getSentence2();
			allAnnotationPairsSet.add(ap);

			if (!aps1.containsKey(a1)) {
				aps1.put(a1, new HashSet<SentencePair>());
			}
			if (!aps2.containsKey(a2)) {
				aps2.put(a2, new HashSet<SentencePair>());
			}

			aps1.get(a1).add(ap);
			aps2.get(a2).add(ap);
		}

		if (allAnnotationPairsSet.size() == 0)
			return sortedSentencePairs;

		List<SentencePair> remainingSentencePairs = new ArrayList<SentencePair>();

		// Iteration: Take annotation pair with highest similarity, remove
		// it from queue and all other annotation pairs containing one of the
		// annotations. Do this until queue is empty.
		while (!sortedSentencePairs.isEmpty()) {
			SentencePair currentAP = sortedSentencePairs.poll();

			// System.out.println(currentAP.getSimilarity(this) + " (paper) " +
			// currentAP.getSimilarityString());
			remainingSentencePairs.add(currentAP);

			// Remove every annotation pair containing one of the
			// annotations
			// contained in the annotation pair with currently highest
			// similarity.

			sortedSentencePairs.removeAll(aps1.get(currentAP.getSentence1()));
			sortedSentencePairs.removeAll(aps2.get(currentAP.getSentence2()));
		}

		// TODO: Not very efficient (ordering takes place twice)
		Comparator<SentencePair> comp = new SentencePairComparator(this);
		PriorityQueue<SentencePair> sortedAndFilteredSentencePairs = new PriorityQueue<SentencePair>(
				remainingSentencePairs.size(), comp);
		sortedAndFilteredSentencePairs.addAll(remainingSentencePairs);

		return sortedAndFilteredSentencePairs;
	}

	public String getStringRepresentation() {

		List<String> res = new ArrayList<String>();
		for (SimilarityFunctionLinear a : getWeightConfigs())
			res.add(a.getStringRepresentation());

		return this.name + ": " + StringUtils.join(res, " | ");
	}

}
