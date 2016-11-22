package dbpedia_spotlight;

import translate.Language;

public class SpotlightLink {

	private String metaData;
	private String wikiLink;
	private String coveredText;
	private boolean hasType;
	private int startPosition;
	private int endPosition;

	private Language language;

	public SpotlightLink(String wikiLink, Language language, int startPosition, int endPosition, String metaData) {
		this.metaData = metaData;
		this.wikiLink = wikiLink;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.language = language;
	}

	public SpotlightLink(String metaData, String wikiLink, String coveredText, boolean hasType) {
		this.metaData = metaData;
		this.wikiLink = wikiLink;
		this.coveredText = coveredText;
		this.hasType = hasType;
	}

	public String getMetaData() {
		return this.metaData;
	}

	public String getWikiLink() {
		return this.wikiLink;
	}

	public String getCoveredText() {
		return this.coveredText;
	}

	public boolean hasType() {
		return this.hasType;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public void setCoveredText(String coveredText) {
		this.coveredText = coveredText;
	}

	public void setHasType(boolean hasType) {
		this.hasType = hasType;
	}

}
