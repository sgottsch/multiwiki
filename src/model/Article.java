package model;

import translate.Language;

public class Article {
	private String uri;
	private Language language;

	public Article(String uri, Language language) {
		this.uri = uri;
		this.language = language;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}
}
