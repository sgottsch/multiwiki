package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.translate.Language;

public class Article_DB extends DbObject {
	public static final String article_uri_attr = "article_uri";
	public static final String language_attr = "language";
	public static final String wiki_id_attr = "wiki_id";
	public static final String english_uri_attr = "english_uri";
	public static final String title_attr = "title";

	public Article_DB() {
	}

	public Article_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "article";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("article_uri", new DbField("article_uri", DbField.stringType));

		// define all attributes
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("wiki_id", new DbField("wiki_id", DbField.stringType));
		this.fields.put("english_uri", new DbField("english_uri", DbField.stringType));
		this.fields.put("title", new DbField("title", DbField.stringType));
	}

	public Article_DB(String article_uri, String language, String wiki_id, String english_uri, String title) {
		this.init();
		this.values.put(article_uri_attr, article_uri);
		this.values.put(language_attr, language);
		this.values.put(wiki_id_attr, wiki_id);
		this.values.put("english_uri", english_uri);
		this.values.put("title", title);
	}

	public Article_DB(String article_uri, String language, String wiki_id, String title) {
		this.init();
		this.values.put("article_uri", article_uri);
		this.values.put("language", language);
		this.values.put("wiki_id", wiki_id);
		this.values.put("title", title);
	}

	public Language getLanguage() {
		return Language.getLanguage(this.getValue("language").toString());
	}
}
