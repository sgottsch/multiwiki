package db.tables;

import java.util.TreeMap;

import db.DbField;
import db.DbObject;
import translate.Language;

public class Revision_InternalLink_DB extends DbObject {
	public static final String revision_internal_link_id_attr = "revision_internal_link_id";
	public static final String revision_number_attr = "revision_id";
	public static final String language_attr = "language";
	public static final String wiki_link_attr = "wiki_link";
	public static final String number_of_occurrences_attr = "number_of_occurrences";

	public Revision_InternalLink_DB() {
	}

	public Revision_InternalLink_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "revision_internal_link";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put(revision_internal_link_id_attr, new DbField("wiki_link", DbField.bigintType));

		// define all attributes
		this.fields.put(revision_internal_link_id_attr,
				new DbField(revision_internal_link_id_attr, DbField.bigintType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("number_of_occurrences", new DbField("number_of_occurrences", DbField.intType));
		this.fields.put("wiki_link", new DbField("wiki_link", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
	}

	public Revision_InternalLink_DB(Revision_DB revision, String wikiLink, Language language) {
		this.init();
		this.values.put("revision_id", revision.getValue("revision_id"));
		this.values.put("wiki_link", wikiLink);
		this.values.put("language", language.getLanguage());
		this.values.put("number_of_occurrences", 1);
	}

	public void incrementNumberOfOccurences() {
		this.values.put("number_of_occurrences", this.getInt("number_of_occurrences") + 1);
	}
}
