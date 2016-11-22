package tfidf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import model.BinaryComparison;
import model.Entity;
import model.Revision;
import model.Sentence;
import model.links.DbPediaLink;
import model.links.InternalLink;
import translate.Language;

public class EntityCollection extends TfIdfCollection implements Collection {

	// ESWC: 7.9
	// WWW: ca. 4.0?
	private static final Double DEFAULT_ALPHA = 12.2;

	private static final Integer DEFAULT_K = null;

	public EntityCollection(BinaryComparison comparison, Revision revision1, Revision revision2, Double alpha,
			Integer k) {
		super(comparison, revision1, revision2, alpha, k);
	}

	public EntityCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		super(comparison, revision1, revision2, DEFAULT_ALPHA, DEFAULT_K);
	}

	@Override
	String getTermName() {
		return "entities";
	}

	@Override
	CollectionType getCollectionType() {
		return CollectionType.Entity;
	}

	@Override
	String createTerms(Sentence sentence, Revision revision) {

		Language otherLanguage;
		Language language = revision.getLanguage();

		if (revision == revision1)
			otherLanguage = revision2.getLanguage();
		else
			otherLanguage = revision1.getLanguage();

		String entityText = "";

		// store dbpedia links and internal links in seperated lists to
		// avoid storing the same links for dbpedia AND internal. But
		// within a "entity type" (dbpedia/internal), an entity may
		// occur multiple times.
		// In the end, take the maximum number of occurences of the entity in
		// one of the sources (Example: "Greenland" occurs twice in Dbpedia and
		// once in internal links -> Add it twice to the entity string.
		// That's because dbpedia and internal links may be assigned to the same
		// words (overlap).
		Map<String, Integer> dbpediaEntities = new HashMap<String, Integer>();
		Map<String, Integer> internalEntities = new HashMap<String, Integer>();

		if (PRINT)
			System.out.println("Search entities (" + sentence.getDbPediaLinks().size() + ", "
					+ sentence.getInternalLinks().size() + ") for " + sentence.getEnglishRawText());

		for (DbPediaLink dbl : sentence.getDbPediaLinks()) {
			Entity entity = dbl.getEntity();

			if (entity.getName(Language.EN) == null)
				continue;

			String entityName = entity.getName(Language.EN).replace(" ", "_");

			// TODO: Consider the case where none of the articles is English

			// Don't add links not available in the other revision's language
			// and not in the English language

			if (entity.getName(language) != null && !entity.getName(language).equals("-")
					&& entity.getName(otherLanguage) != null && !entity.getName(otherLanguage).equals("-")) {

				if (this.titleName.equals(entity.getName(Language.EN).replace(" ", "_").toLowerCase())
						|| entityName.toLowerCase().startsWith(this.titleName + "#"))
					continue;

				if (dbpediaEntities.containsKey(entityName)) {
					dbpediaEntities.put(entityName, (Integer) dbpediaEntities.get(entityName) + 1);
				} else
					dbpediaEntities.put(entityName, 1);

				if (PRINT)
					System.out.println("DBPEDIA LINK: " + entityName + " (" + dbpediaEntities.get(entityName) + ")");
			}
		}
		for (InternalLink il : sentence.getInternalLinks()) {
			Entity entity = il.getEntity();

			if (entity.getName(Language.EN) == null)
				continue;

			String entityName = entity.getName(Language.EN).replace(" ", "_");

			if (entity.getName(language) != null && !entity.getName(language).equals("-")
					&& entity.getName(otherLanguage) != null && !entity.getName(otherLanguage).equals("-")) {

				if (this.titleName.equals(entity.getName(Language.EN).replace(" ", "_").toLowerCase())
						|| entityName.toLowerCase().startsWith(this.titleName + "#"))
					continue;

				if (internalEntities.containsKey(entityName)) {
					internalEntities.put(entityName, (Integer) internalEntities.get(entityName) + 1);
				} else
					internalEntities.put(entityName, 1);

				if (PRINT)
					System.out.println("INTERNAL LINK: " + entityName + " (" + internalEntities.get(entityName) + ")");
			}
		}

		Set<String> uniqueEntityNames = new HashSet<String>();
		uniqueEntityNames.addAll(dbpediaEntities.keySet());
		uniqueEntityNames.addAll(internalEntities.keySet());

		List<String> allEntityNames = new ArrayList<String>();

		for (String entityName : uniqueEntityNames) {
			int numberDbpedia = 0;
			int numberInternal = 0;
			int numberMax = 0;

			if (internalEntities.containsKey(entityName)) {
				numberInternal = (Integer) internalEntities.get(entityName);
			}

			if (dbpediaEntities.containsKey(entityName)) {
				numberDbpedia = (Integer) dbpediaEntities.get(entityName);
			}
			numberMax = Math.max(numberInternal, numberDbpedia);

			for (int i = 1; i <= numberMax; i++)
				allEntityNames.add(entityName);
		}

		entityText = StringUtils.join(allEntityNames, (String) " ");

		// System.out.println(sentence.getRawText());
		// System.out.println("=> " + entityText);

		sentence.setEntityAllString(entityText);

		return entityText;
	}
}
