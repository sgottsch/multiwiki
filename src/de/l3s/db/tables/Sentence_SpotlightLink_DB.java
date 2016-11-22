package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.model.Sentence;
import de.l3s.translate.Language;

public class Sentence_SpotlightLink_DB extends DbObject {
	public static final String language_attr = "language";
	public static final String revision_number_attr = "revision_id";
	public static final String annotation_id_attr = "annotation_id";
	public static final String wiki_link_attr = "wiki_link";
	public static final String number_in_sentence_attr = "number_in_sentence";
	public static final String meta_data_attr = "meta_data";
	public static final String covered_text_attr = "covered_text";
	public static final String has_type_attr = "has_type";

	public Sentence_SpotlightLink_DB() {
	}

	public Sentence_SpotlightLink_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "annotation_spotlightlink";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("language", new DbField("language", DbField.stringType));
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.pk_fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));
		this.pk_fields.put("number_in_sentence", new DbField("number_in_sentence", DbField.intType));

		// define all attributes
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));
		this.fields.put("wiki_link", new DbField("wiki_link", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("meta_data", new DbField("meta_data", DbField.textType));
		this.fields.put("number_in_sentence", new DbField("number_in_sentence", DbField.intType));
		this.fields.put("covered_text", new DbField("covered_text", DbField.stringType));
		this.fields.put("has_type", new DbField("has_type", DbField.booleanType));
	}

	public Sentence_SpotlightLink_DB(Sentence sentence, String wikiLink, Language language, int numberInSentence,
			String metaData, String coveredText, boolean hasType) {
		this.init();
		this.values.put("revision_id", sentence.getRevisionId());
		this.values.put("annotation_id", sentence.getNumber());
		this.values.put("language", language);
		this.values.put("wiki_link", wikiLink);
		this.values.put("number_in_sentence", numberInSentence);
		this.values.put("meta_data", metaData);
		this.values.put("covered_text", coveredText);
		this.values.put("has_type", hasType);
	}
}
