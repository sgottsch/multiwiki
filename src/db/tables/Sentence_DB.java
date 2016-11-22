package db.tables;

import java.util.TreeMap;

import db.DbField;
import db.DbObject;
import translate.Language;

public class Sentence_DB extends DbObject implements Comparable<Sentence_DB> {
	public static final String article_uri_attr = "article_uri";
	public static final String revision_number_attr = "revision_id";
	public static final String annotation_id_attr = "annotation_id";
	public static final String annotation_type_attr = "annotation_type";
	public static final String language_attr = "language";
	public static final String start_position_attr = "start_position";
	public static final String end_position_attr = "end_position";
	public static final String original_text_html_attr = "original_text_html";
	public static final String original_text_attr = "original_text";
	public static final String english_text_html_attr = "english_text_html";
	public static final String english_text_attr = "english_text";
	public static final String stemmed_english_text_attr = "stemmed_english_text";
	public static final String to_translate_attr = "to_translate";
	public static final String in_infobox_attr = "in_infobox";
	public static final String text_id_attr = "text_id";
	public static final String containing_paragraph_id_attr = "containing_paragraph_id";

	public static final String SENTENCE_TYPE = "SENTENCE";
	public static final String IMAGE_DESCRIPTION_TYPE = "IMAGE_DESCRIPTION";
	public static final String SEE_ALSO_TYPE = "SEE_ALSO";
	public static final String ARTICLE_TITLE_TYPE = "ARTICLE_TITLE";
	public static final String TITLE_TYPE = "TITLE";
	public static final String LIST_ELEMENT_TYPE = "LIST_ELEMENT";
	public static final String TABLE_CELL_TYPE = "TABLE_CELL";
	public static final String MISC_TYPE = "MISC";
	public static final String TABLE_HEADER_CELL_TYPE = "TABLE_HEADER_CELL";
	public static final String IN_PARAGRAPH_TYPE = "IN_PARAGRAPH";

	public Sentence_DB() {
	}

	public Sentence_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "annotation";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("language", new DbField("language", DbField.stringType));
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.pk_fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));

		// define all attributes
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("containing_paragraph_id", new DbField("containing_paragraph_id", DbField.bigintType));
		this.fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));
		this.fields.put("annotation_type", new DbField("annotation_type", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("start_position", new DbField("start_position", DbField.intType));
		this.fields.put("end_position", new DbField("end_position", DbField.intType));
		this.fields.put("original_text_html", new DbField("original_text_html", DbField.textType));
		this.fields.put("original_text", new DbField("original_text", DbField.textType));
		this.fields.put("english_text_html", new DbField("english_text_html", DbField.textType));
		this.fields.put("english_text", new DbField("english_text", DbField.textType));
		this.fields.put("stemmed_english_text", new DbField("stemmed_english_text", DbField.textType));
		this.fields.put("text_id", new DbField("text_id", DbField.bigintType));
		this.fields.put("to_translate", new DbField("to_translate", DbField.booleanType));
		this.fields.put("in_infobox", new DbField("in_infobox", DbField.booleanType));
	}

	public Sentence_DB(Revision_DB revision, Integer annotation_id, String annotation_type, Integer start_position,
			Integer end_position, String original_text_html, String original_text, String english_text_html,
			String english_text, String stemmed_english_text) {
		this.init();
		this.values.put("article_uri", revision.getValue("article_uri"));
		this.values.put("revision_id", revision.getValue("revision_id"));
		this.values.put("annotation_id", annotation_id);
		this.values.put("annotation_type", annotation_type);
		this.values.put("language", revision.getArticle().getValue("language"));
		this.values.put("start_position", start_position);
		this.values.put("end_position", end_position);
		this.values.put("english_text", english_text);
		this.values.put("original_text", original_text);
		this.values.put("stemmed_english_text", stemmed_english_text);
		this.values.put("english_text_html", english_text_html);
		this.values.put("original_text_html", original_text_html);
		this.values.put("in_infobox", false);
	}

	public void setContainingParagraph(Paragraph_DB containingParagraph) {
		this.values.put("containing_paragraph_id", containingParagraph.getValue("paragraph_id"));
	}

	public void setToTranslate(boolean toTranslate) {
		this.values.put("to_translate", toTranslate);
	}

	public void setInInfobox(boolean inInfobox) {
		this.values.put("in_infobox", inInfobox);
	}

	public Language getLanguage() {
		return Language.getLanguage(this.getValue("language").toString());
	}

	@Override
	public int compareTo(Sentence_DB a) {
		if (this.getInt("start_position") < a.getInt("start_position")) {
			return -1;
		}
		if (this.getInt("start_position") != a.getInt("start_position")) {
			return 1;
		}
		return 0;
	}
}
