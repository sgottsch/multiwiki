package db.tables;

import java.util.TreeMap;

import db.DbField;
import db.DbObject;
import model.Sentence;

public class Sentence_HeidelTime_DB extends DbObject {
	public static final String annotation_time_id_attr = "annotation_heidel_time_id";
	public static final String revision_number_attr = "revision_id";
	public static final String annotation_id_attr = "annotation_id";
	public static final String language_attr = "language";
	public static final String covered_text_attr = "covered_text";
	public static final String number_in_sentence_attr = "number_in_sentence";
	public static final String begin_time_attr = "begin_time";
	public static final String end_time_attr = "end_time";

	public Sentence_HeidelTime_DB() {
	}

	public Sentence_HeidelTime_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "annotation_heideltime";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("annotation_heidel_time_id", new DbField("annotation_heidel_time_id", DbField.bigintType));

		// define all attributes
		this.fields.put("annotation_heidel_time_id", new DbField("annotation_heidel_time_id", DbField.bigintType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("annotation_id", new DbField("annotation_id", DbField.bigintType));
		this.fields.put("begin_time", new DbField("begin_time", DbField.stringType));
		this.fields.put("end_time", new DbField("end_time", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
		this.fields.put("covered_text", new DbField("covered_text", DbField.stringType));
		this.fields.put("number_in_sentence", new DbField("number_in_sentence", DbField.intType));
	}

	public Sentence_HeidelTime_DB(Sentence sentence, String startTime, String endTime) {
		this.init();
		this.values.put("revision_id", sentence.getRevisionId());
		this.values.put("annotation_id", sentence.getNumber());
		this.values.put("language", sentence.getLanguage().getLanguage());
		this.values.put("begin_time", startTime);
		this.values.put("end_time", endTime);
	}

	public Sentence_HeidelTime_DB(Sentence_DB annotation, String startTime, String endTime) {
		this.init();
		this.values.put("revision_id", annotation.getValue("revision_id"));
		this.values.put("annotation_id", annotation.getValue("annotation_id"));
		this.values.put("language", annotation.getLanguage("language").getLanguage());
		this.values.put("begin_time", startTime);
		this.values.put("end_time", endTime);
	}
}
