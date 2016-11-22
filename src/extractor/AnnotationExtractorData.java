package extractor;

import java.util.HashMap;
import java.util.List;

import db.tables.Revision_DB;
import translate.Language;

public class AnnotationExtractorData {

	private Revision_DB revision;

	private Language language;

	private List<Language> languages;

	private HashMap<String, String> footnoteIds;

	private String database;

	public AnnotationExtractorData(String database, Revision_DB revision, List<Language> languages,
			HashMap<String, String> footnoteIds) {

		// Important: At first, build the annotations, THEN correct their
		// sentence numbers in the paragraph extractor and then extract internal
		// links etc.
		this.languages = languages;
		this.database = database;
		this.language = revision.getArticle().getLanguage();
		this.revision = revision;
		this.footnoteIds = footnoteIds;
	}

	public Revision_DB getRevision() {
		return this.revision;
	}

	public HashMap<String, String> getFootnoteIds() {
		return this.footnoteIds;
	}

	public Language getLanguage() {
		return this.language;
	}

	public String getDatabase() {
		return this.database;
	}

	public List<Language> getLanguages() {
		return this.languages;
	}
}
