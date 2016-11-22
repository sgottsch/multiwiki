package de.l3s.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.l3s.translate.Language;

public class Entity {

	private Map<Language, String> names;
	private Map<Language, String> types;
	private Map<Language, Set<String>> derivedTypes;
	private Set<String> allTypes;

	public Entity() {
		this.names = new HashMap<Language, String>();
		this.types = new HashMap<Language, String>();
		this.derivedTypes = new HashMap<Language, Set<String>>();
	}

	public String getName(Language language) {
		if (names.containsKey(language))
			return names.get(language);
		else
			return null;
	}

	public String getType(Language language) {
		if (types.containsKey(language))
			return types.get(language);
		else
			return null;
	}

	public Set<String> getDerivedTypes(Language language) {
		if (derivedTypes.containsKey(language))
			return derivedTypes.get(language);
		else
			return null;
	}

	public void addName(Language language, String entityName) {
		String decodedEntityName = entityName;

		if (entityName == null || entityName.isEmpty())
			return;

		// entityName = entityName.replace(" ", "_");
		//
		// try {
		// decodedEntityName = URLDecoder.decode(entityName, "UTF-8");
		// } catch (UnsupportedEncodingException e) {
		// System.err.println("Warning: Decoding format not supported.");
		// }

		if (decodedEntityName == null || decodedEntityName.isEmpty())
			return;

		this.names.put(language, decodedEntityName);
	}

	public void addType(Language language, String type) {
		this.types.put(language, type);
	}

	public void addDerivedType(Language language, String type) {
		if (!this.derivedTypes.containsKey(language))
			this.derivedTypes.put(language, new HashSet<String>());
		this.derivedTypes.get(language).add(type);
	}

	public void addDerivedTypes(Language language, Set<String> types) {
		this.derivedTypes.put(language, types);
	}

	public boolean linkExistsInBothLanguages(Language language1, Language language2) {
		return names.containsKey(language1) && names.containsKey(language2) && !names.get(language1).equals("-")
				&& !names.get(language2).equals("-");
	}

	public String getSomeName() {
		if (this.names.containsKey(Language.EN))
			return this.names.get(Language.EN);
		else {
			for (Language lang : this.names.keySet())
				return this.names.get(lang);
		}
		return null;
	}

	public Map<Language, String> getTypes() {
		return types;
	}

	public Map<Language, Set<String>> getDerivedTypes() {
		return derivedTypes;
	}

	public Set<String> getAllTypes() {
		if (this.allTypes == null) {
			this.allTypes = new HashSet<String>();
			for (Language language : types.keySet()) {
				if (types.get(language) != null)
					allTypes.add(types.get(language));
			}
			for (Language language : derivedTypes.keySet()) {
				if (derivedTypes.get(language) != null)
					allTypes.addAll(derivedTypes.get(language));
			}
		}

		// Add default type if there is none
		if (this.allTypes.isEmpty())
			this.allTypes.add("NoType");

		return this.allTypes;
	}

	public Map<Language, String> getNames() {
		return names;
	}

}
