package db.tables;

import java.util.TreeMap;

import db.DbField;
import db.DbObject;

public class Author_DB extends DbObject {
	public static final String name_attr = "name";
	public static final String location_attr = "location";
	public static final String has_ip_address_attr = "has_ip_address";

	public Author_DB() {
	}

	public Author_DB(TreeMap<String, Object> values) {
		super(values);
	}

	@Override
	protected void init() {
		// create table definition
		this.tableName = "author";
		this.values = new TreeMap<String, Object>();
		this.updateValues = new TreeMap<String, Object>();

		this.fields = new TreeMap<String, DbField>();
		this.pk_fields = new TreeMap<String, DbField>();

		// define primary key
		this.pk_fields.put("name", new DbField("name", DbField.stringType));

		// define all attributes
		this.fields.put("name", new DbField("name", DbField.stringType));
		this.fields.put("location", new DbField("location", DbField.stringType));
		this.fields.put("has_ip_address", new DbField("has_ip_address", DbField.booleanType));
	}

	public Author_DB(String name, String location) {
		
		this.init();
		
		this.values.put("name", name);
		this.values.put("location", location);
	}
}
