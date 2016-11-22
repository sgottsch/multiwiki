package similarity.revision;

import model.Revision;

/**
 * Interface for similarities that are not applied on the sentence but on the
 * revision level.
 */
public interface RevisionSimilarity {

	public double calculateSimilarity(Revision revision1, Revision revision2);

	public String getName();
}