package de.l3s.db.tables;

import java.util.TreeMap;

import de.l3s.db.DbField;
import de.l3s.db.DbObject;
import de.l3s.util.FormatUtil;

public class Revision_Author_DB extends DbObject {
	public static final String revision_number_attr = "revision_id";
	public static final String author_attr = "author";
	public static final String edits_attr = "edits";
	public static final String language_attr = "language";

	public Revision_Author_DB() {
	}

	public Revision_Author_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "revision_author";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.pk_fields.put("author", new DbField("author", DbField.stringType));
		this.pk_fields.put("language", new DbField("language", DbField.stringType));

		// define all attributes
		this.fields.put("revision_id", new DbField("revision_id", DbField.bigintType));
		this.fields.put("author", new DbField("author", DbField.stringType));
		this.fields.put("edits", new DbField("edits", DbField.intType));
		this.fields.put("language", new DbField("language", DbField.stringType));
	}

	public Revision_Author_DB(Revision_DB revision, Author_DB author, Integer edits) {
		this.init();
		this.values.put("revision_id", FormatUtil.doubleToString(revision.getValue("revision_id")));
		this.values.put("author", author.getValue("name"));
		this.values.put("edits", edits);
		this.values.put("language", revision.getValue("language"));
	}
}
