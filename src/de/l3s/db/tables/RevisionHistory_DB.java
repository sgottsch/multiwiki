package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.util.FormatUtil;

public class RevisionHistory_DB extends DbObject {
	public static final String article_uri_attr = "article_uri";
	public static final String language_attr = "language";
	public static final String revision_id_attr = "revision_id";
	public static final String parent_revision_id_attr = "parent_revision_id";
	public static final String author_attr = "author";
	public static final String minor_attr = "minor";
	public static final String size_attr = "size";
	public static final String start_date_attr = "start_date";
	public static final String end_date_attr = "end_date";
	public static final String hash_attr = "hash";
	public static final String valid_attr = "valid";
	public Article_DB article;

	public RevisionHistory_DB() {
	}

	public RevisionHistory_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "revision_history";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.pk_fields.put("language", new DbField("language", DbField.stringType));

		// define all attributes
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("parent_revision_id", new DbField("parent_revision_id", DbField.bigintType));
		this.fields.put("author", new DbField("author", DbField.stringType));
		this.fields.put("minor", new DbField("minor", DbField.booleanType));
		this.fields.put("size", new DbField("size", DbField.bigintType));
		this.fields.put(start_date_attr, new DbField(start_date_attr, DbField.datetimeType));
		this.fields.put("end_date", new DbField("end_date", DbField.datetimeType));
		this.fields.put("hash", new DbField("hash", DbField.stringType));
		this.fields.put("valid", new DbField("valid", DbField.booleanType));
	}

	public RevisionHistory_DB(Article_DB article, Long revision_id, Long parent_id, String author, boolean minor,
			Long size, String date, String end_date, String hash, boolean valid) {
		this.init();
		this.article = article;
		this.values.put(article_uri_attr, article.getValue(Article_DB.article_uri_attr));
		this.values.put("language", article.getValue("language"));
		this.values.put("revision_id", revision_id);
		this.values.put("parent_revision_id", parent_id);
		this.values.put("author", author);
		this.values.put("minor", minor);
		this.values.put("size", size);
		this.values.put(start_date_attr, FormatUtil.convertWikiTimestampToMySQLDateTime(date));
		this.values.put("end_date", FormatUtil.convertWikiTimestampToMySQLDateTime(end_date));
		this.values.put("hash", hash);
		this.values.put("valid", valid);
	}
}
