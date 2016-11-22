package de.l3s.db;

public class DbField implements Comparable<DbField> {

	public static String timestampType = "TIMESTAMP";
	public static String textType = "TEXT";
	public static String stringType = "STRING";
	public static String intType = "INT";
	public static String bigintType = "BIGINT";
	public static String dateType = "DATE";
	public static String booleanType = "BOOLEAN";
	public static String datetimeType = "DATETIME";
	public static String doubleType = "DOUBLE";

	private String name;
	private String type;

	public DbField(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	@Override
	public int compareTo(DbField o) {
		return this.name.compareTo(o.name);
	}

	public int getSQLType() {
		if (type.equals(DbField.stringType))
			return java.sql.Types.VARCHAR;
		else if (type.equals(DbField.textType))
			return java.sql.Types.VARCHAR;
		else if (type.equals(DbField.bigintType))
			return java.sql.Types.BIGINT;
		else if (type.equals(DbField.intType))
			return java.sql.Types.INTEGER;
		else if (type.equals(DbField.dateType))
			return java.sql.Types.DATE;
		else if (type.equals(DbField.booleanType))
			return java.sql.Types.BOOLEAN;
		else if (type.equals(DbField.datetimeType))
			return java.sql.Types.TIMESTAMP;
		else if (type.equals(DbField.doubleType))
			return java.sql.Types.DOUBLE;
		else
			return -1;
	}

}
