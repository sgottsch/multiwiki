package model.links;

import java.util.HashMap;
import java.util.Map;

import translate.Language;

/**
 * An internal link is a Wikipedia link that is directly given by the Wikipedia
 * markup.
 */
public class InternalLink extends EntityLink {
	private long dbId;
	private boolean hasType = false;
	private Map<Language, String> links;

	public InternalLink(int startPosition, int endPosition) {
		super(startPosition, endPosition);
	}

	public InternalLink() {
	}

	public String getLink(Language language) {
		if (this.links.containsKey(language)) {
			return this.links.get(language);
		}
		return null;
	}

	public void addLink(Language language, String link) {
		if (this.links == null) {
			this.links = new HashMap<Language, String>();
		}
		this.links.put(language, link);
	}

	public long getDbId() {
		return this.dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	public boolean hasType() {
		return this.hasType;
	}

	public void setHasType(boolean hasType) {
		this.hasType = hasType;
	}
}
