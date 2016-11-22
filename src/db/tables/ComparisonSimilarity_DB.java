package db.tables;

import java.util.TreeMap;

import db.DbField;
import db.DbObject;
import model.BinaryComparison;
import model.Revision;

public class ComparisonSimilarity_DB extends DbObject {
	public static final String article1_uri_attr = "article1_uri";
	public static final String article2_uri_attr = "article2_uri";
	public static final String revision1_id_attr = "revision1_id";
	public static final String revision2_id_attr = "revision2_id";
	public static final String language1_attr = "language1";
	public static final String language2_attr = "language2";
	public static final String comparison_id_attr = "comparison_id";
	public static final String date_attr = "date";
	public static final String overall_similarity_string_attr = "overall_similarity_string";
	public static final String overall_similarity_attr = "overall_similarity";

	public ComparisonSimilarity_DB() {
	}

	public ComparisonSimilarity_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "comparison_similarity";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("revision1_id", new DbField("revision1_id", DbField.bigintType));
		this.pk_fields.put("revision2_id", new DbField("revision2_id", DbField.bigintType));
		this.pk_fields.put("language1", new DbField("language1", DbField.stringType));
		this.pk_fields.put("language2", new DbField("language2", DbField.stringType));

		// define all attributes
		this.fields.put("article1_uri", new DbField("article1_uri", DbField.stringType));
		this.fields.put("article2_uri", new DbField("article2_uri", DbField.stringType));
		this.fields.put("revision1_id", new DbField("revision1_id", DbField.bigintType));
		this.fields.put("revision2_id", new DbField("revision2_id", DbField.bigintType));
		this.fields.put("language1", new DbField("language1", DbField.stringType));
		this.fields.put("language2", new DbField("language2", DbField.stringType));
		this.fields.put("comparison_id", new DbField("comparison_id", DbField.bigintType));
		this.fields.put("date", new DbField("date", DbField.datetimeType));
		this.fields.put("overall_similarity_string", new DbField("overall_similarity_string", DbField.stringType));
		this.fields.put("overall_similarity", new DbField("overall_similarity", DbField.doubleType));
	}

	public ComparisonSimilarity_DB(BinaryComparison comparison, long comparisonId) {
		this.init();
		Revision revision1 = comparison.getRevision1();
		Revision revision2 = comparison.getRevision2();
		this.values.put(article1_uri_attr, revision1.getArticleUri());
		this.values.put(article2_uri_attr, revision2.getArticleUri());
		this.values.put(revision1_id_attr, revision1.getId());
		this.values.put(revision2_id_attr, revision2.getId());
		this.values.put(language1_attr, revision1.getLanguage());
		this.values.put(language2_attr, revision2.getLanguage());
		this.values.put("comparison_id", comparisonId);
		this.values.put("date", comparison.getDate());
		this.values.put("overall_similarity", comparison.getOverallSimilarity());
		this.values.put("overall_similarity_string", comparison.getOverallSimilarityString());
	}
}
