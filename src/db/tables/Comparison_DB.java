package db.tables;

import java.util.Date;
import java.util.TreeMap;

import db.DbField;
import db.DbObject;

public class Comparison_DB extends DbObject {
	public static final String comparison_id_attr = "comparison_id";
	public static final String article1_uri_attr = "article1_uri";
	public static final String article2_uri_attr = "article2_uri";
	public static final String article3_uri_attr = "article3_uri";
	public static final String revision1_id_attr = "revision1_id";
	public static final String revision2_id_attr = "revision2_id";
	public static final String revision3_id_attr = "revision3_id";
	public static final String language1_attr = "language1";
	public static final String language2_attr = "language2";
	public static final String language3_attr = "language3";
	public static final String date_attr = "date";
	public static final String unused_attr = "unused";

	public Comparison_DB() {
	}

	public Comparison_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "comparison";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("comparison_id", new DbField("comparison_id", DbField.bigintType));

		// define all attributes
		this.fields.put("comparison_id", new DbField("comparison_id", DbField.bigintType));
		this.fields.put("article1_uri", new DbField("article1_uri", DbField.stringType));
		this.fields.put("article2_uri", new DbField("article2_uri", DbField.stringType));
		this.fields.put("article3_uri", new DbField("article3_uri", DbField.stringType));
		this.fields.put("revision1_id", new DbField("revision1_id", DbField.bigintType));
		this.fields.put("revision2_id", new DbField("revision2_id", DbField.bigintType));
		this.fields.put("revision3_id", new DbField("revision3_id", DbField.bigintType));
		this.fields.put("date", new DbField("date", DbField.datetimeType));
		this.fields.put("language1", new DbField("language1", DbField.stringType));
		this.fields.put("language2", new DbField("language2", DbField.stringType));
		this.fields.put("language3", new DbField("language2", DbField.stringType));
		this.fields.put("unused", new DbField("unused", DbField.booleanType));
	}

	public Comparison_DB(Revision_DB revision1, Revision_DB revision2, Date date) {
		this.init();
		Article_DB article1 = revision1.getArticle();
		Article_DB article2 = revision2.getArticle();
		this.values.put("article1_uri", article1.getValue("article_uri"));
		this.values.put("article2_uri", article2.getValue("article_uri"));
		this.values.put("revision1_id", revision1.getValue("revision_id"));
		this.values.put("revision2_id", revision2.getValue("revision_id"));
		this.values.put("language1", article1.getValue("language"));
		this.values.put("language2", article2.getValue("language"));
		this.values.put("date", date);
	}

	public Comparison_DB(Revision_DB revision1, Revision_DB revision2, Revision_DB revision3, Date date) {
		this.init();
		Article_DB article1 = revision1.getArticle();
		Article_DB article2 = revision2.getArticle();
		Article_DB article3 = revision3.getArticle();
		this.values.put("article1_uri", article1.getValue("article_uri"));
		this.values.put("article2_uri", article2.getValue("article_uri"));
		this.values.put("article3_uri", article3.getValue("article_uri"));
		this.values.put("revision1_id", revision1.getValue("revision_id"));
		this.values.put("revision2_id", revision2.getValue("revision_id"));
		this.values.put("revision3_id", revision3.getValue("revision_id"));
		this.values.put("language1", article1.getValue("language"));
		this.values.put("language2", article2.getValue("language"));
		this.values.put("language3", article3.getValue("language"));
		this.values.put("date", date);
	}

	public Comparison_DB(Article_DB article1, Article_DB article2, RevisionHistory_DB revisionHistory1,
			RevisionHistory_DB revisionHistory2, Date date) {
		this.init();
		this.values.put("article1_uri", revisionHistory1.getValue("article_uri"));
		this.values.put("article2_uri", revisionHistory2.getValue("article_uri"));
		this.values.put("revision1_id", revisionHistory1.getValue("revision_id"));
		this.values.put("revision2_id", revisionHistory2.getValue("revision_id"));
		this.values.put("language1", article1.getValue("language"));
		this.values.put("language2", article2.getValue("language"));
		this.values.put("date", date);
	}
}
