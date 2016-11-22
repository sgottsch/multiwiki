package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;

public class Paragraph_Image_DB extends DbObject {
	public static final String article_uri_attr = "article_uri";
	public static final String language_attr = "language";
	public static final String revision_number_attr = "revision_id";
	public static final String paragraph_id_attr = "paragraph_id";
	public static final String image_url_attr = "image_url";

	public Paragraph_Image_DB() {
	}

	public Paragraph_Image_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "paragraph_image";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.pk_fields.put("paragraph_id", new DbField("paragraph_id", DbField.bigintType));
		this.pk_fields.put("image_url", new DbField("image_url", DbField.stringType));

		// define all attributes
		this.fields.put("article_uri", new DbField("article_uri", DbField.stringType));
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("paragraph_id", new DbField("paragraph_id", DbField.bigintType));
		this.fields.put("image_url", new DbField("image_url", DbField.stringType));
		this.fields.put("language", new DbField("language", DbField.stringType));
	}

	public Paragraph_Image_DB(Paragraph_DB paragraph, String imageUrl) {
		this.init();
		this.values.put("article_uri", paragraph.getValue("article_uri"));
		this.values.put("language", paragraph.getValue("language"));
		this.values.put("revision_id", paragraph.getValue("revision_id"));
		this.values.put("paragraph_id", paragraph.getValue("paragraph_id"));
		this.values.put("image_url", imageUrl);
	}
}
