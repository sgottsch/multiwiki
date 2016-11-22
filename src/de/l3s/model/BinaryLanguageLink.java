package de.l3s.model;

import de.l3s.translate.Language;

public class BinaryLanguageLink {
	private Language language1;
	private Language language2;
	private String title1;
	private String title2;

	public BinaryLanguageLink(Language language1, Language language2, String title1, String title2) {
		this.language1 = language1;
		this.language2 = language2;
		this.title1 = title1;
		this.title2 = title2;
	}

	public Language getLanguage1() {
		return this.language1;
	}

	public void setLanguage1(Language language1) {
		this.language1 = language1;
	}

	public Language getLanguage2() {
		return this.language2;
	}

	public void setLanguage2(Language language2) {
		this.language2 = language2;
	}

	public String getTitle1() {
		return this.title1;
	}

	public void setTitle1(String title1) {
		this.title1 = title1;
	}

	public String getTitle2() {
		return this.title2;
	}

	public void setTitle2(String title2) {
		this.title2 = title2;
	}
}
