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
import model.links.InternalLink;
import translate.Language;

public class InternalLinkCollection extends TfIdfCollection implements Collection {

	public InternalLinkCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
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

		Map<String, Integer> internalEntities = new HashMap<String, Integer>();

		for (InternalLink il : annotation.getInternalLinks()) {
			Entity entity = il.getEntity();
			String entityName = entity.getName(Language.EN);

			// Don't add links not available in the other revision's language
			// and not in the English language
			if (entity.getName(Language.EN) != null && !entity.getName(Language.EN).equals("-")
					&& entity.getName(otherLanguage) != null && !entity.getName(otherLanguage).equals("-")) {

				if (titleName.equals(entityName.toLowerCase()) || entityName.toLowerCase().startsWith(titleName + "#"))
					continue;

				if (internalEntities.containsKey(entityName)) {
					internalEntities.put(entityName, (Integer) internalEntities.get(entityName) + 1);

				}

				internalEntities.put(entityName, 1);

				if (PRINT)
					System.out.println("INTERNAL LINK: " + entityName + " (" + internalEntities.get(entityName) + ")");
			}
		}

		Set<String> uniqueEntityNames = new HashSet<String>();
		uniqueEntityNames.addAll(internalEntities.keySet());
		List<String> allEntityNames = new ArrayList<String>();

		for (String entityName : uniqueEntityNames) {
			int numberInternal = 0;

			if (internalEntities.containsKey(entityName)) {
				numberInternal = (Integer) internalEntities.get(entityName);
			}

			for (int i = 1; i <= numberInternal; i++)
				allEntityNames.add(entityName);
		}
		entityText = StringUtils.join(allEntityNames, (String) " ");
		annotation.setInternalLinksString(entityText);
		return entityText;
	}

	@Override
	String getTermName() {
		return "internalLinks";
	}

	@Override
	CollectionType getCollectionType() {
		return CollectionType.Internal;
	}
}
