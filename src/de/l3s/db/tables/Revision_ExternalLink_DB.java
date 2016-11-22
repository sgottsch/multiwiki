package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.util.URLUtil;

public class Revision_ExternalLink_DB extends DbObject {
	public static final String language_attr = "language";
	public static final String revision_number_attr = "revision_id";
	public static final String external_link_uri_attr = "external_link_uri";
	public static final String external_link_host_attr = "external_link_host";
	public static final String number_of_occurrences_attr = "number_of_occurrences";

	public Revision_ExternalLink_DB() {
	}

	public Revision_ExternalLink_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "revision_external_link";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.pk_fields.put("external_link_uri", new DbField("external_link_uri", DbField.stringType));
		this.pk_fields.put("language", new DbField("language", DbField.stringType));

		// define all attributes
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("number_of_occurrences", new DbField("number_of_occurrences", DbField.intType));
		this.fields.put("external_link_uri", new DbField("external_link_uri", DbField.stringType));
		this.fields.put("external_link_host", new DbField("external_link_host", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
	}

	public Revision_ExternalLink_DB(Revision_DB revision, String external_link) {
		this.init();
		this.values.put("revision_id", revision.getValue("revision_id"));
		this.values.put("external_link_uri", external_link);
		this.values.put("external_link_host", URLUtil.getHost(external_link));
		this.values.put("number_of_occurrences", 1);
		this.values.put("language", revision.getValue("language"));
	}

	public void incrementNumberOfOccurences() {
		this.values.put("number_of_occurrences", this.getInt("number_of_occurrences") + 1);
	}
}
