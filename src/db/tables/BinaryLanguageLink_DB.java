package db.tables;

import java.util.TreeMap;

import db.DbField;
import db.DbObject;

public class BinaryLanguageLink_DB extends DbObject {
	public static final String id_attr = "page_id";
	public static final String language1_attr = "original_language";
	public static final String language2_attr = "target_language";
	public static final String name1_attr = "page_title";
	public static final String name2_attr = "target_page_title";
	public static final String redirected_page_id_attr = "redirected_page_id";
	public static final String redirected_page_title_attr = "redirected_page_title";

	public BinaryLanguageLink_DB() {
	}

	public BinaryLanguageLink_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "bll_with_redirects2";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("page_id", new DbField("page_id", DbField.bigintType));

		// define all attributes
		this.fields.put("page_id", new DbField("page_id", DbField.bigintType));
		this.fields.put("original_language", new DbField("original_language", DbField.stringType));
		this.fields.put("target_language", new DbField("target_language", DbField.stringType));
		this.fields.put("page_title", new DbField("page_title", DbField.stringType));
		this.fields.put("target_page_title", new DbField("target_page_title", DbField.stringType));
		this.fields.put("redirected_page_id", new DbField("redirected_page_id", DbField.bigintType));
		this.fields.put("redirected_page_title", new DbField("redirected_page_title", DbField.stringType));
	}

	public BinaryLanguageLink_DB(String language1, String language2, String name1, String name2) {
		this.init();
		this.values.put("original_language", language1);
		this.values.put("target_language", language2);
		this.values.put("page_title", name1);
		this.values.put("target_page_title", name2);
	}
}
