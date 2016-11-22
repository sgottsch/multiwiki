package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.util.URLUtil;

public class Sentence_ExternalLink_DB extends DbObject {
	public static final String article_uri_attr = "article_uri";
	public static final String language_attr = "language";
	public static final String revision_number_attr = "revision_id";
	public static final String annotation_id_attr = "annotation_id";
	public static final String external_link_uri_attr = "external_link_uri";
	public static final String external_link_host_attr = "external_link_host";
	public static final String start_attr = "start_position";
	public static final String end_attr = "end_position";

	public Sentence_ExternalLink_DB() {
	}

	public Sentence_ExternalLink_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "annotation_external_link";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("language", new DbField("language", DbField.stringType));
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.pk_fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));
		this.pk_fields.put("external_link_uri", new DbField("external_link_uri", DbField.stringType));
		this.pk_fields.put("start_position", new DbField("start_position", DbField.intType));
		this.pk_fields.put("end_position", new DbField("end_position", DbField.intType));

		// define all attributes
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));
		this.fields.put("external_link_uri", new DbField("external_link_uri", DbField.stringType));
		this.fields.put("external_link_host", new DbField("external_link_host", DbField.stringType));
		this.fields.put("start_position", new DbField("start_position", DbField.intType));
		this.fields.put("end_position", new DbField("end_position", DbField.intType));
	}

	public Sentence_ExternalLink_DB(Sentence_DB annotation, String external_link, int start, int end) {
		this.init();
		this.values.put("language", annotation.getValue("language"));
		this.values.put("article_uri", annotation.getValue("article_uri"));
		this.values.put("revision_id", annotation.getValue("revision_id"));
		this.values.put("annotation_id", annotation.getValue("annotation_id"));
		this.values.put("external_link_uri", external_link);
		this.values.put("external_link_host", URLUtil.getHost(external_link));
		this.values.put("start_position", start);
		this.values.put("end_position", end);
	}
}
