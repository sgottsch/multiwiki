package de.l3s.similarity.revision;

import de.l3s.model.Revision;

/**
 * Interface for similarities that are not applied on the sentence but on the
 * revision level.
 */
public interface RevisionSimilarity {

	public double calculateSimilarity(Revision revision1, Revision revision2);

	public String getName();
}