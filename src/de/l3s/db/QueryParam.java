package de.l3s.db;

public class QueryParam {

	private String fieldName;
	private String value;
	private Integer type = null;
	private QueryOperator operator;

	public enum QueryOperator {
		LIKE, NOT_LIKE, IS_NOT_NULL, IS_NULL, IS_TRUE, IS_FALSE;
	}

	/**
	 * 
	 */
	public QueryParam(String fieldName, String value) {
		this.fieldName = fieldName;
		this.value = value;
	}

	public QueryParam(String fieldName, int value) {
		this.fieldName = fieldName;
		this.value = String.valueOf(value);
	}

	public QueryParam(String fieldName, long value) {
		this.fieldName = fieldName;
		this.value = String.valueOf(value);
	}

	public QueryParam(String fieldName, String value, QueryOperator operator) {
		this.fieldName = fieldName;
		this.value = value;
		this.operator = operator;
	}

	public QueryParam(String fieldName, String value, int type) {
		this.fieldName = fieldName;
		this.value = value;
		this.type = type;
	}

	public QueryParam(String fieldName, QueryOperator operator) {
		this.fieldName = fieldName;
		if (operator == QueryOperator.IS_NOT_NULL)
			this.value = "NOT NULL";
		else if (operator == QueryOperator.IS_NULL)
			this.value = "NULL";
		else if (operator == QueryOperator.IS_FALSE)
			this.value = "0";
		else if (operator == QueryOperator.IS_TRUE)
			this.value = "1";
		else
			throw new NullPointerException("QueryOperator " + operator + " not supported.");
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 *            the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * For prepared queries
	 */
	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public boolean hasType() {
		return this.type != null;
	}

	public QueryOperator getOperator() {
		return this.operator;
	}

}
