package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.translate.Language;

public class Text_DB extends DbObject {
	public static final String text_id_attr = "text_id";
	public static final String original_text_html_attr = "original_text_html";
	public static final String original_text_attr = "original_text";
	public static final String translated_text_attr = "translated_text";
	public static final String translated_text_html_attr = "translated_text_html";
	public static final String translated_text_stemmed_attr = "translated_text_stemmed";
	public static final String original_language_attr = "original_language";

	public Text_DB() {
	}

	public Text_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "text";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("text_id", new DbField("text_id", DbField.bigintType));

		// define all attributes
		this.fields.put("text_id", new DbField("text_id", DbField.bigintType));
		this.fields.put("original_text", new DbField("original_text", DbField.textType));
		this.fields.put("original_text_html", new DbField("original_text_html", DbField.textType));
		this.fields.put("translated_text", new DbField("translated_text", DbField.textType));
		this.fields.put("translated_text_html", new DbField("translated_text_html", DbField.textType));
		this.fields.put("translated_text_stemmed", new DbField("translated_text_stemmed", DbField.textType));
		this.fields.put("original_language", new DbField("original_language", DbField.stringType));
	}

	public Text_DB(String originalText, Language originalLanguage) {
		this.init();
		this.values.put("original_text", originalText);
		this.values.put("original_language", originalLanguage.getLanguage());
	}
}
