package de.l3s.similarity.revision;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.l3s.model.Revision;
import de.l3s.translate.Language;

/**
 * SImilarity< on the revision level that computed some kind of an overlap of
 * the country codes of the anonymous authors. As a different number of authors
 * of the both revisions should not add a negative value, the number for the
 * revision with less authors is increased before relative to the other
 * revision.
 */
public class AuthorLocationSimilarity implements RevisionSimilarity {

	private double similarity;

	public double getSimilarity() {

		if (Double.isNaN(this.similarity)) {
			return 0.0;
		}

		return this.similarity;
	}

	public double calculateSimilarity(Revision revision1, Revision revision2) {

		this.similarity = 0;

		// Fetch all country codes
		Set<String> allCountryCodes = new HashSet<String>();
		allCountryCodes.addAll(revision1.getAuthorLocations().keySet());
		allCountryCodes.addAll(revision2.getAuthorLocations().keySet());

		int numberOfAuthorsWithCountryCodeInRevision1 = 0;

		for (int number : revision1.getAuthorLocations().values()) {
			numberOfAuthorsWithCountryCodeInRevision1 += number;
		}

		int numberOfAuthorsWithCountryCodeInRevision2 = 0;
		for (int number : revision2.getAuthorLocations().values()) {
			numberOfAuthorsWithCountryCodeInRevision2 += number;
		}

		// System.out.println("Authors with IP in revision 1 (" +
		// revision1.getLanguage() + "): "
		// + numberOfAuthorsWithCountryCodeInRevision1);
		// System.out.println("Authors with IP in revision 2 (" +
		// revision2.getLanguage() + "): "
		// + numberOfAuthorsWithCountryCodeInRevision2);

		int min = 0;

		for (String countryCode : allCountryCodes) {

			int authors1 = 0;
			int authors2 = 0;

			if (revision1.getAuthorLocations().containsKey(countryCode))
				authors1 = revision1.getAuthorLocations().get(countryCode);
			if (revision2.getAuthorLocations().containsKey(countryCode))
				authors2 = revision2.getAuthorLocations().get(countryCode);

			// System.out.println(countryCode + ": " + authors1 + " - " +
			// authors2);

			if (numberOfAuthorsWithCountryCodeInRevision2 < numberOfAuthorsWithCountryCodeInRevision1) {
				Double a = authors2
						* (((double) numberOfAuthorsWithCountryCodeInRevision1) / numberOfAuthorsWithCountryCodeInRevision2);
				authors2 = a.intValue();
			} else {
				Double a = authors1
						* (((double) numberOfAuthorsWithCountryCodeInRevision2) / numberOfAuthorsWithCountryCodeInRevision1);
				authors1 = a.intValue();
			}

			// System.out.println(countryCode + ": " + authors1 + " - " +
			// authors2);

			min = min + Math.min(authors1, authors2);
		}

		this.similarity = Math
				.min(1,
						((double) min)
								/ Math.max(numberOfAuthorsWithCountryCodeInRevision1,
										numberOfAuthorsWithCountryCodeInRevision2));

		if (Double.isNaN(this.similarity)) {
			return 0.0;
		}

		return this.similarity;
	}

	public String getName() {
		return "AuthorLocationSimilarity";
	}

	public static void main(String[] args) {
		Revision revision1 = new Revision(0, null, null, null, Language.EN);
		Revision revision2 = new Revision(0, null, null, null, Language.DE);

		Map<String, Integer> authors1 = new HashMap<String, Integer>();
		authors1.put("de", 5);
		authors1.put("fr", 4);
		authors1.put("me", 1);
		// authors1.put("de", 6);
		// authors1.put("fr", 4);
		// authors1.put("me", 2);
		revision1.setAuthorLocations(authors1);

		Map<String, Integer> authors2 = new HashMap<String, Integer>();
		authors2.put("de", 3);
		authors2.put("fr", 2);
		// authors2.put("de", 3);
		// authors2.put("fr", 2);
		// authors2.put("me", 1);
		revision2.setAuthorLocations(authors2);

		AuthorLocationSimilarity sim = new AuthorLocationSimilarity();
		System.out.println(sim.calculateSimilarity(revision1, revision2));
	}

}
