package db;

import java.util.Date;
import java.util.TreeMap;

import translate.Language;

public abstract class DbObject {
	protected String tableName;

	// fieldName -> DbField (typed object)
	protected TreeMap<String, DbField> fields;

	// fieldName -> DbField (typed object)
	protected TreeMap<String, DbField> pk_fields;

	// fieldName -> value
	protected TreeMap<String, Object> values;

	// fieldName -> value
	protected TreeMap<String, Object> updateValues;

	// should be false for all objects that were retreived from the database
	protected boolean isNew = true;

	// field name-> datatype of the field
	public TreeMap<String, DbField> getPrimaryKeyFields() {
		return pk_fields;
	}

	// field name-> datatype of the field
	public TreeMap<String, DbField> getFields() {
		return fields;
	}

	// field name-> value of the field
	public TreeMap<String, Object> getUpdateValues() {
		return updateValues;
	}

	// field name-> value of the field
	public TreeMap<String, Object> getValues() {
		return values;
	}

	public String getTableName() {
		return this.tableName;
	}

	public Object getValue(String fieldName) {
		return this.values.get(fieldName);
	}

	public String getString(String fieldName) {
		if (this.fields.get(fieldName).getType() != DbField.stringType
				&& this.fields.get(fieldName).getType() != DbField.textType) {
			throw new ClassCastException();
		}
		return this.values.get(fieldName).toString();
	}

	public boolean getBoolean(String fieldName) {
		if (this.fields.get(fieldName).getType() != DbField.booleanType) {
			throw new ClassCastException();
		}
		return (Boolean) this.values.get(fieldName);
	}

	public int getInt(String fieldName) {
		if (this.fields.get(fieldName).getType() != DbField.intType
				&& this.fields.get(fieldName).getType() != DbField.bigintType) {
			throw new ClassCastException();
		}
		if (this.fields.get(fieldName).getType() == DbField.intType) {
			String val = this.values.get(fieldName).toString();
			if (val.contains(".")) {
				val = val.substring(0, val.indexOf("."));
			}
			return Integer.parseInt(val);
		}
		String val = this.values.get(fieldName).toString();
		if (val.contains(".")) {
			val = val.substring(0, val.indexOf("."));
		}
		return (int) Long.parseLong(val);
	}

	public Integer getInteger(String fieldName) {
		if (this.values.get(fieldName).toString().equals("NULL")) {
			return null;
		}
		if (this.fields.get(fieldName).getType() != DbField.intType
				&& this.fields.get(fieldName).getType() != DbField.bigintType) {
			throw new ClassCastException();
		}
		if (this.fields.get(fieldName).getType() == DbField.intType) {
			String val = this.values.get(fieldName).toString();
			if (val.contains(".")) {
				val = val.substring(0, val.indexOf("."));
			}
			return Integer.parseInt(val);
		}
		String val = this.values.get(fieldName).toString();
		if (val.contains(".")) {
			val = val.substring(0, val.indexOf("."));
		}
		return Integer.parseInt(val);
	}

	public void setInt(String fieldName, int value) {
		if (this.fields.get(fieldName).getType() != DbField.intType
				&& this.fields.get(fieldName).getType() != DbField.bigintType) {
			throw new ClassCastException();
		}
		this.values.put(fieldName, value);
	}

	public void setString(String fieldName, String value) {
		if (this.fields.get(fieldName).getType() != DbField.stringType
				&& this.fields.get(fieldName).getType() != DbField.textType) {
			throw new ClassCastException();
		}
		this.values.put(fieldName, value);
	}

	public void setLong(String fieldName, long value) {
		if (this.fields.get(fieldName).getType() != DbField.bigintType) {
			throw new ClassCastException();
		}
		this.values.put(fieldName, value);
	}

	public void updateInt(String fieldName, int value) {
		if (this.fields.get(fieldName).getType() != DbField.intType
				&& this.fields.get(fieldName).getType() != DbField.bigintType) {
			throw new ClassCastException();
		}
		this.updateValues.put(fieldName, value);
		this.values.put(fieldName, value);
	}

	public Long getLong(String fieldName) {
		if (this.fields.get(fieldName).getType() != DbField.intType
				&& this.fields.get(fieldName).getType() != DbField.bigintType) {
			throw new ClassCastException();
		}
		return (Long) this.values.get(fieldName);
	}

	public Language getLanguage(String fieldName) {
		if (this.fields.get(fieldName).getType() != DbField.stringType
				&& this.fields.get(fieldName).getType() != DbField.textType) {
			throw new ClassCastException();
		}
		Language language = Language.getLanguage(this.values.get(fieldName).toString());
		if (language == null) {
			throw new ClassCastException();
		}
		return language;
	}

	public double getDouble(String fieldName) {
		if (this.fields.get(fieldName).getType() != DbField.doubleType) {
			throw new ClassCastException();
		}
		return (Double) this.values.get(fieldName);
	}

	public Date getDate(String fieldName) {
		if (this.fields.get(fieldName).getType() != DbField.datetimeType
				&& this.fields.get(fieldName).getType() != DbField.dateType) {
			throw new ClassCastException();
		}
		// String dateString = values.get(fieldName).toString();
		// // TODO: Load java.sql Date, convert to java.util.Date
		// System.out.println("dateString: " + dateString);
		// DateFormat df = new SimpleDateFormat("yyyy-MM-dd ");
		// return new Date(dateString);
		return (Date) this.values.get(fieldName);
	}

	public void setValue(String fieldName, Object value) {
		this.values.put(fieldName, value);
	}

	// create table definition
	protected abstract void init();

	public DbObject() {
		this.init();
	}

	// add values to the DbObject
	public DbObject(TreeMap<String, Object> values) {
		this.init();
		this.values.putAll(values);
	}

	public void addUpdateValue(String attribute, Object value) {
		this.updateValues.put(attribute, value);
	}

	public void addUpdateValueAndValue(String attribute, Object value) {
		this.updateValues.put(attribute, value);
		this.values.put(attribute, value);
	}

	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isNew() {
		return this.isNew;
	}

	public String toString() {
		String result = "";
		for (String value : this.values.keySet()) {
			Object valueString = this.values.get(value);

			// avoid conversion of numbers like "49" to "49.0"
			if (this.fields.get(value).getType() == DbField.bigintType) {
				valueString = this.values.get(value);
			}
			result = String.valueOf(result) + value + ": " + valueString + ", ";
		}
		result = result.substring(0, result.length() - 2);
		return result;
	}

	public int getSize() {
		int size = 0;
		for (String value : this.values.keySet()) {
			Object valueString = this.values.get(value);
			size += value.length();
			if (valueString != null) {
				size += valueString.toString().length();
				continue;
			}
			size += 6;
		}
		return size;
	}

}
