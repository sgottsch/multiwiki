package db.tables;

import java.util.TreeMap;

import db.DbField;
import db.DbObject;
import model.Sentence;
import translate.Language;

public class Sentence_InternalLink_DB extends DbObject {
	public static final String annotation_internal_link_id_attr = "annotation_internal_link_id";
	public static final String article_uri_attr = "article_uri";
	public static final String revision_number_attr = "revision_id";
	public static final String annotation_id_attr = "annotation_id";
	public static final String wiki_link_attr = "wiki_link";
	public static final String language_attr = "language";
	public static final String start_attr = "start_position";
	public static final String end_attr = "end_position";

	public Sentence_InternalLink_DB() {
	}

	public Sentence_InternalLink_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "annotation_internal_link";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("annotation_internal_link_id",
				new DbField("annotation_internal_link_id", DbField.bigintType));

		// define all attributes
		this.fields.put("annotation_internal_link_id", new DbField("annotation_internal_link_id", DbField.bigintType));
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));
		this.fields.put("wiki_link", new DbField("wiki_link", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("start_position", new DbField("start_position", DbField.intType));
		this.fields.put("end_position", new DbField("end_position", DbField.intType));
	}

	public Sentence_InternalLink_DB(Sentence_DB annotation, String originalLink, Language language, int start, int end) {
		this.init();
		this.values.put("article_uri", annotation.getValue("article_uri"));
		this.values.put("revision_id", annotation.getValue("revision_id"));
		this.values.put("annotation_id", annotation.getValue("annotation_id"));
		this.values.put("wiki_link", originalLink);
		this.values.put("language", language.getLanguage());
		this.values.put("start_position", start);
		this.values.put("end_position", end);
	}

	public Sentence_InternalLink_DB(Sentence annotation, String originalLink, Language language, int start, int end) {
		this.init();
		this.values.put("article_uri", annotation.getArticleUri());
		this.values.put("revision_id", annotation.getRevisionId());
		this.values.put("annotation_id", annotation.getNumber());
		this.values.put("wiki_link", originalLink);
		this.values.put("language", language.getLanguage());
		this.values.put("start_position", start);
		this.values.put("end_position", end);
	}
}
