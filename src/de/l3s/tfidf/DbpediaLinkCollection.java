package de.l3s.tfidf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Entity;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.links.DbPediaLink;
import de.l3s.translate.Language;

public class DbpediaLinkCollection extends TfIdfCollection implements Collection {
	
	public DbpediaLinkCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		super(comparison, revision1, revision2);
	}

	@Override
	String createTerms(Sentence annotation, Revision revision) {
		Language otherLanguage;
		if (revision == revision1)
			otherLanguage = revision2.getLanguage();
		else
			otherLanguage = revision1.getLanguage();

		String entityText = "";

		Map<String, Integer> dbpediaEntities = new HashMap<String, Integer>();

		if (PRINT)
			System.out.println("Search entities for " + annotation.getEnglishRawText());

		for (DbPediaLink dbl : annotation.getDbPediaLinks()) {
			Entity entity = dbl.getEntity();

			// Don't add links not available in the other revision's language
			// and not in the English language
			if (entity.getName(Language.EN) != null && !entity.getName(Language.EN).equals("-")
					&& entity.getName(otherLanguage) != null && !entity.getName(otherLanguage).equals("-")) {
				String entityName = entity.getName(Language.EN);

				if (titleName.equals(entityName.toLowerCase()) || entityName.toLowerCase().startsWith(titleName + "#"))
					continue;

				if (dbpediaEntities.containsKey(entityName))
					dbpediaEntities.put(entityName, dbpediaEntities.get(entityName) + 1);
				else
					dbpediaEntities.put(entityName, 1);

				if (PRINT)
					System.out.println("DBPEDIA LINK: " + entityName + " (" + dbpediaEntities.get(entityName) + ")");
			}
		}

		Set<String> uniqueEntityNames = new HashSet<String>();
		uniqueEntityNames.addAll(dbpediaEntities.keySet());
		ArrayList<String> allEntityNames = new ArrayList<String>();
		for (String entityName : uniqueEntityNames) {
			int numberDbpedia = 0;
			if (dbpediaEntities.containsKey(entityName)) {
				numberDbpedia = (Integer) dbpediaEntities.get(entityName);
			}
			int i = 1;
			while (i <= numberDbpedia) {
				allEntityNames.add(entityName);
				++i;
			}
		}
		entityText = StringUtils.join(allEntityNames, (String) " ");
		annotation.setDbpediaLinksNERString(entityText);
		return entityText;
	}

	@Override
	String getTermName() {
		return "dbpediaNER";
	}

	@Override
	CollectionType getCollectionType() {
		return CollectionType.DbpediaLinkNER;
	}
}
