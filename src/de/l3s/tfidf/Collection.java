package de.l3s.tfidf;

import java.util.Map;

import org.apache.commons.math3.linear.RealVector;

public interface Collection {
	public void createLuceneIndex();

	public RealVector getIdfVector();

	public RealVector getTfVector();

	public Map<String, Integer> getDocTermFrequencies();

	public void sortDocFreq();

	public RealVector getDfNormalizer();
}
