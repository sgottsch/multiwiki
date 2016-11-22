package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.extractor.HTMLParagraph;
import net.htmlparser.jericho.Segment;

public class Paragraph_DB extends DbObject implements Comparable<Paragraph_DB> {
	public static final String article_uri_attr = "article_uri";
	public static final String language_attr = "language";
	public static final String revision_number_attr = "revision_id";
	public static final String paragraph_id_attr = "paragraph_id";
	public static final String paragraph_type_attr = "paragraph_type";
	public static final String to_translate_attr = "to_translate";
	public static final String start_attr = "start_position";
	public static final String end_attr = "end_position";
	public static final String content_start_attr = "content_start_position";
	public static final String content_end_attr = "content_end_position";
	public static final String above_paragraph_id_attr = "above_paragraph_id";
	private Revision_DB revision;

	public Paragraph_DB() {
	}

	public Paragraph_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "paragraph";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("language", new DbField("language", DbField.stringType));
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.pk_fields.put("paragraph_id", new DbField("paragraph_id", DbField.bigintType));

		// define all attributes
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("above_paragraph_id", new DbField("above_paragraph_id", DbField.bigintType));
		this.fields.put("paragraph_id", new DbField("paragraph_id", DbField.bigintType));
		this.fields.put("paragraph_type", new DbField("paragraph_type", DbField.stringType));
		this.fields.put("to_translate", new DbField("to_translate", DbField.booleanType));
		this.fields.put("start_position", new DbField("start_position", DbField.intType));
		this.fields.put("end_position", new DbField("end_position", DbField.intType));
		this.fields.put("content_start_position", new DbField("content_start_position", DbField.intType));
		this.fields.put("content_end_position", new DbField("content_end_position", DbField.intType));
	}

	public Paragraph_DB(Revision_DB revision, double paragraph_id, String paragraph_type) {
		this.init();
		this.values.put("article_uri", revision.getValue("article_uri"));
		this.values.put("revision_id", revision.getValue("revision_id"));
		this.values.put("paragraph_id", paragraph_id);
		this.values.put("paragraph_type", paragraph_type);
		this.revision = revision;
	}

	public Paragraph_DB(Revision_DB revision, double paragraph_id, HTMLParagraph.Type paragraph_type, boolean toTranslate,
			int start, int end, Segment contentSegment) {
		this.init();
		this.values.put("language", revision.getValue("language"));
		this.values.put("article_uri", revision.getValue("article_uri"));
		this.values.put("revision_id", revision.getValue("revision_id"));
		this.values.put("paragraph_id", paragraph_id);
		this.values.put("paragraph_type", paragraph_type.toString());
		this.values.put("to_translate", toTranslate);
		this.values.put("start_position", start);
		this.values.put("end_position", end);
		if (contentSegment != null) {
			this.values.put("content_start_position", contentSegment.getBegin());
			this.values.put("content_end_position", contentSegment.getEnd());
		} else {
			this.values.put("content_start_position", "NULL");
			this.values.put("content_end_position", "NULL");
		}
		this.values.put("above_paragraph_id", "NULL");
		this.revision = revision;
	}

	public void setToTranslate(boolean toTranslate) {
		this.values.put("to_translate", toTranslate);
	}

	public void setAboveParagraph(Paragraph_DB aboveParagraph) {
		this.values.put("above_paragraph_id", aboveParagraph.getValue("paragraph_id"));
	}

	@Override
	public int compareTo(Paragraph_DB p) {
		if (this.getInt("start_position") < p.getInt("start_position")) {
			return -1;
		}
		if (this.getInt("start_position") != p.getInt("start_position")) {
			return 1;
		}
		return 0;
	}

	public Revision_DB getRevision() {
		return this.revision;
	}
}
