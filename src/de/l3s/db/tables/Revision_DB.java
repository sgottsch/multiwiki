package de.l3s.db.tables;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.model.Revision;
import de.l3s.util.FormatUtil;

public class Revision_DB extends DbObject {
	public static final String article_uri_attr = "article_uri";
	public static final String revision_id_attr = "revision_id";
	public static final String title_attr = "title";
	public static final String date_attr = "date";
	public static final String original_html_text_attr = "original_html_text";
	public static final String language_attr = "language";
	public static final String loaded_heideltimes_attr = "loaded_heideltimes";
	public static final String loaded_spotlightlinks_attr = "loaded_spotlightlinks_new";
	public static final String comment_attr = "comment";
	public Article_DB article;
	public Set<Sentence_DB> sentences;

	public Revision_DB() {
	}

	public Revision_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "revision";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));

		// define all attributes
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("title", new DbField("title", DbField.stringType));
		this.fields.put("date", new DbField("date", DbField.dateType));
		this.fields.put("original_html_text", new DbField("original_html_text", DbField.textType));
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("loaded_heideltimes", new DbField("loaded_heideltimes", DbField.booleanType));
		this.fields.put("loaded_spotlightlinks_new", new DbField("loaded_spotlightlinks_new", DbField.booleanType));
		this.fields.put("comment", new DbField("comment", DbField.stringType));
	}

	public void setComment(String comment) {
		this.values.put("comment", comment);
	}

	public Revision_DB(Article_DB article, Long revision_id, String title, String date, String original_html_text) {
		this.init();
		this.article = article;
		this.values.put("article_uri", article.getValue("article_uri"));
		this.values.put("language", article.getValue("language"));
		this.values.put("revision_id", revision_id);
		this.values.put("title", title);
		this.values.put("date", FormatUtil.convertWikiTimestampToMySQLDate(date));
		this.values.put("original_html_text", original_html_text);
	}

	public Revision_DB(Article_DB article, Long revision_id, String title, Date date, String original_html_text) {
		this.init();
		this.article = article;
		this.values.put("article_uri", article.getValue("article_uri"));
		this.values.put("language", article.getValue("language"));
		this.values.put("revision_id", revision_id);
		this.values.put("title", title);
		this.values.put("date", date);
		this.values.put("original_html_text", original_html_text);
	}

	public Revision_DB(Revision revision) {
		this.init();
		this.article = new Article_DB();
		this.article.setValue("article_uri", revision.getArticle().getUri());
		this.article.setValue("language", revision.getArticle().getLanguage());
		this.values.put("article_uri", this.article.getValue("article_uri"));
		this.values.put("language", this.article.getValue("language"));
		this.values.put("revision_id", revision.getId());
		this.values.put("title", revision.getTitle());
		this.values.put("date", revision.getDate());
		this.values.put("original_html_text", revision.getHtmlText());
	}

	public Article_DB getArticle() {
		return this.article;
	}

	public void setArticle(Article_DB article) {
		this.article = article;
	}

	public Set<Sentence_DB> getSentences() {
		return this.sentences;
	}

	public void setSentences(Set<Sentence_DB> sentences) {
		this.sentences = sentences;
	}

	public void addSentence(Sentence_DB sentence) {
		if (this.sentences == null) {
			this.sentences = new HashSet<Sentence_DB>();
		}
		this.sentences.add(sentence);
	}
}
