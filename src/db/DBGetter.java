package db;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import db.DBGetter.SortingAttribute.SortMode;
import db.QueryParam.QueryOperator;

public class DBGetter {

	private static final boolean PRINT_SQL = false;

	public static String AND_OPERATOR = "AND";
	public static String OR_OPERATOR = "OR";
	public static String LIKE_OPERATOR = "LIKE";
	public static String NOT_LIKE_OPERATOR = "NOT LIKE";

	public int MAX_PACKET_SIZE = 1048576;

	/**
	 * 
	 */
	public DBGetter() {

	}

	public Vector<String> retrieveTextFromAnnotations() {
		return null;
	}

	public int countDbObjects(Connection conn, DbObject obj) {
		int count = 0;
		String count_var = "count";

		try {

			Statement st = conn.createStatement();

			String count_query = "SELECT COUNT(*) AS " + count_var + " FROM " + obj.getTableName() + " ; ";

			if (PRINT_SQL)
				System.out.println(count_query);

			ResultSet rs1 = st.executeQuery(count_query);

			if (rs1.next()) {

				count = rs1.getInt(count_var);

			}

			rs1.close();

			st.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;

	}

	public void updateDbObject(Connection conn, DbObject obj) {
		this.updateDbObject(conn, obj, false);
	}

	// updates DbObject in the database using primary key to identify the object
	public void updateDbObject(Connection conn, DbObject obj, boolean deleteWhenConstraintError) {
		try {

			// UPDATE [LOW_PRIORITY] [IGNORE] tbl_name
			// SET col_name1=expr1 [, col_name2=expr2 ...]
			// [WHERE where_condition]
			// [ORDER BY ...]
			// [LIMIT row_count]

			Statement st = conn.createStatement();

			String update = " UPDATE " + obj.getTableName() + " SET ";

			for (String fieldName : obj.getUpdateValues().keySet()) {
				// do not add primary key fields
				if (!obj.getPrimaryKeyFields().containsKey(fieldName)) {
					update += " " + fieldName + " = '" + obj.getUpdateValues().get(fieldName) + "'" + " ,";
				}
			}
			// remove last AND
			if (update.endsWith(",")) {
				update = update.substring(0, update.length() - 1);
			}

			update += " WHERE (";

			for (String keyName : obj.getPrimaryKeyFields().keySet()) {
				if (obj.getValues().get(keyName) != null) {
					update += " " + keyName + " = '" + obj.getValues().get(keyName) + "'" + " AND";
				} else {
					System.err.println("pk field is null " + keyName);
				}
			}
			// remove last comma
			if (update.endsWith("AND")) {
				update = update.substring(0, update.length() - 3);
			}

			update += ");";
			if (PRINT_SQL)
				System.out.println(update);
			st.execute(update);

			if (!deleteWhenConstraintError) {
				st.executeUpdate(update);
			} else {
				try {
					st.executeUpdate(update);
				} catch (SQLException e) {
					String delete = " DELETE FROM " + obj.getTableName() + " WHERE ";
					for (String keyName : obj.getPrimaryKeyFields().keySet()) {
						if (obj.getValues().get(keyName) != null) {
							delete = String.valueOf(delete) + " " + keyName + " = '" + obj.getValues().get(keyName)
									+ "'" + " AND";
							continue;
						}
					}
					if (delete.endsWith("AND")) {
						delete = delete.substring(0, delete.length() - 3);
					}
					st.executeUpdate(delete);
				}
			}

			st.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void storeDbObject(Connection conn, DbObject obj) {

		try {

			Statement st = conn.createStatement();

			String insert = "INSERT INTO " + obj.getTableName() + " ( ";

			for (String fieldName : obj.getValues().keySet()) {
				insert += fieldName + ", ";
			}
			// cut last comma
			insert = insert.substring(0, insert.length() - 2);

			insert += " ) " + " VALUES " + " ( ";

			for (String fieldName : obj.getValues().keySet()) {
				insert += "'" + obj.getValues().get(fieldName) + "'" + ", ";
			}

			insert = insert.substring(0, insert.length() - 2);

			insert += " ) " + ";";

			if (PRINT_SQL)
				System.out.println(insert);
			st.execute(insert);
			st.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Long storeDbObjectPrepared(Connection conn, DbObject obj) {
		return storeDbObjectPrepared(conn, obj, false);
	}

	/**
	 * Store the object using prepared statements (e.g. important when importing
	 * HTML text)
	 * 
	 * @param conn
	 *            Database connection
	 * @param obj
	 *            Object to store
	 * @param insertIgnore
	 *            If true (default is false), only rows are inserted that are
	 *            not in the table already
	 */
	public Long storeDbObjectPrepared(Connection conn, DbObject obj, boolean insertIgnore) {

		Long generatedKey = null;

		try {

			String insert = "INSERT ";

			if (insertIgnore)
				insert += "IGNORE ";

			insert += "INTO " + obj.getTableName() + " ( ";

			for (String fieldName : obj.getValues().keySet()) {
				insert += fieldName + ", ";
			}
			// cut last comma
			insert = insert.substring(0, insert.length() - 2);

			insert += " ) " + " VALUES " + " ( ";

			for (int i = 0; i < obj.getValues().keySet().size(); i++) {
				insert += "?, ";
			}

			insert = insert.substring(0, insert.length() - 2);

			insert += " ) " + ";";

			if (PRINT_SQL)
				System.out.println(insert);

			PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

			int preparedParamIndex = 1;
			for (String fieldName : obj.getValues().keySet()) {
				Object value = obj.getValues().get(fieldName);
				int sqlType = obj.getFields().get(fieldName).getSQLType();

				if (value == null || value.toString().equals("NULL"))
					pstmt.setNull(preparedParamIndex, sqlType);
				else
					pstmt.setObject(preparedParamIndex, value, sqlType);

				preparedParamIndex += 1;
			}

			pstmt.executeUpdate();

			// update auto-generated keys in the input Java object
			ResultSet keys = pstmt.getGeneratedKeys();
			while (keys.next()) {
				generatedKey = keys.getLong(1);
			}

			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return generatedKey;
	}

	public void storeDbObjectsPrepared(Connection conn, List<? extends DbObject> objs, boolean insertIgnore) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		// TODO: Not good
		HashSet<? extends DbObject> objSet = new HashSet(objs);

		storeDbObjectsPrepared(conn, objSet, insertIgnore);
	}

	public void storeDbObjectsPrepared(Connection conn, Set<? extends DbObject> objs) {
		storeDbObjectsPrepared(conn, objs, false);
	}

	/**
	 * Store all the objects using prepared statements (e.g. important when
	 * importing HTML text) in one transaction.
	 * 
	 * @param conn
	 *            Database connection
	 * @param obj
	 *            Object to store
	 * @param insertIgnore
	 *            If true: Uses the MySQL "insert ignore" command to store the
	 *            objects. That means, an object is inserted only if it's
	 *            primary key is not existing already.
	 */
	public void storeDbObjectsPrepared(Connection conn, Set<? extends DbObject> objs, boolean insertIgnore) {

		if (objs.size() == 0)
			return;

		DbObject exampleObject = null;

		// if (PRINT_SQL) {
		for (DbObject dbo : objs) {
			exampleObject = dbo;
			break;
		}

		System.out.println("Try to store " + objs.size() + " objects in table " + exampleObject.getTableName());
		// }

		// split up because of error message
		// "Packet for query is too large (1362398 > 1048576). You can change
		// this value on the server by setting the max_allowed_packet'
		// variable."

		Set<Set<DbObject>> packets = new HashSet<Set<DbObject>>();
		Set<DbObject> packet = new HashSet<DbObject>();
		int size = 1000;

		for (DbObject dbo : objs) {
			int objSize = (int) 1.2 * dbo.getSize() + 200;
			if (size == 0 && objSize > MAX_PACKET_SIZE)
				System.err.println("Problem: single query probably too big.");
			if (size + objSize > MAX_PACKET_SIZE || packet.size() >= 250) {
				Set<DbObject> finalPacket = new HashSet<DbObject>();
				finalPacket.addAll(packet);
				packets.add(finalPacket);
				size = 1000;
				packet = new HashSet<DbObject>();
			}
			size += objSize;
			packet.add(dbo);
		}

		packets.add(packet);

		for (Set<DbObject> objsSubSet : packets)
			storeDbObjectsPreparedPacket(conn, objsSubSet, insertIgnore);

	}

	/**
	 * Store all the objects using prepared statements (e.g. important when
	 * importing HTML text) in one transaction.
	 * 
	 * @param conn
	 *            Database connection
	 * @param obj
	 *            Object to store
	 * @param insertIgnore
	 *            If true: Uses the MySQL "insert ignore" command to store the
	 *            objects. That means, an object is inserted only if it's
	 *            primary key is not existing already.
	 */
	private void storeDbObjectsPreparedPacket(Connection conn, Set<? extends DbObject> objs, boolean insertIgnore) {

		if (objs.size() == 0)
			return;

		DbObject exampleObject = null;

		// if (PRINT_SQL) {
		for (DbObject dbo : objs) {
			exampleObject = dbo;
			break;
		}

		// System.out.println("Try to store " + objs.size() + " objects in table
		// " + exampleObject.getTableName() + " (splitted)");

		// }

		try {

			Iterator<? extends DbObject> iter = objs.iterator();
			DbObject firstObj = iter.next();

			String insert = "INSERT ";

			if (insertIgnore)
				insert += "IGNORE ";

			insert += "INTO " + firstObj.getTableName() + " ( ";

			for (String fieldName : firstObj.getValues().keySet()) {
				insert += fieldName + ", ";
			}
			// cut last comma
			insert = insert.substring(0, insert.length() - 2);

			insert += " ) " + " VALUES ";

			for (int objIndex = 0; objIndex < objs.size(); objIndex++) {
				insert += " ( ";

				for (int i = 0; i < firstObj.getValues().keySet().size(); i++) {
					insert += "?, ";
				}

				insert = insert.substring(0, insert.length() - 2);

				insert += " ),";
			}

			// Replace last "," with ";"
			insert = insert.substring(0, insert.length() - 1) + ";";

			if (PRINT_SQL) {

				String insertWithParams = insert;

				for (DbObject obj : objs) {
					String out = "";
					for (String fieldName : obj.getValues().keySet()) {

						String param = "NULL";

						if (obj.getValues().get(fieldName) != null)
							param = obj.getValues().get(fieldName).toString();
						if (param.length() > 50)
							param = param.substring(0, 50) + "...";
						out += param + " (" + fieldName + ") - ";
						// insertWithParams.replaceFirst("\\?",
						// obj.getValues().get(fieldName).toString());
					}
					System.out.println("prepared \"?\" values: " + out);
				}

				System.out.println(insertWithParams);
			}

			PreparedStatement pstmt = conn.prepareStatement(insert);

			int preparedParamIndex = 1;
			for (DbObject obj : objs) {
				for (String fieldName : obj.getValues().keySet()) {
					Object value = obj.getValues().get(fieldName);
					int sqlType = obj.getFields().get(fieldName).getSQLType();

					if (value == null || value.toString().equals("NULL"))
						pstmt.setNull(preparedParamIndex, sqlType);
					else
						pstmt.setObject(preparedParamIndex, value, sqlType);

					preparedParamIndex += 1;
				}
			}

			int numberOfInsertedRows = pstmt.executeUpdate();

			if (PRINT_SQL)
				System.out.println("Inserted " + numberOfInsertedRows + " rows into " + exampleObject.getTableName());

			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void callProcedure(Connection conn, String procedureNameWithParameters) {

		try {

			Statement st = conn.createStatement();

			String call = "Call " + procedureNameWithParameters;

			if (PRINT_SQL)
				System.out.println(call);
			st.execute(call);
			st.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void executeQueryPrepared(Connection conn, String sql_query,
			List<Entry<Integer, String>> preparedParameters) {

		try {

			PreparedStatement pstmt = conn.prepareStatement(sql_query);

			int preparedParamIndex = 1;

			for (Entry<Integer, String> entry : preparedParameters) {
				pstmt.setObject(preparedParamIndex, entry.getValue(), entry.getKey());

				preparedParamIndex += 1;
			}

			if (PRINT_SQL)
				System.out.println(sql_query);

			pstmt.executeQuery();
			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int countSelectedDbObjectsDistinct(Connection conn, DbObject obj, Vector<QueryParam> parameter_or,
			Vector<QueryParam> parameter_and, String distinct) {
		int count = 0;
		String count_var = "count";

		try {

			Statement st = conn.createStatement();

			String count_query = "SELECT COUNT(";

			if (distinct == null)
				count_query += "*";
			else
				count_query += "DISTINCT(" + distinct + ")";

			count_query += ") AS " + count_var + " FROM " + obj.getTableName() + " WHERE ";

			if (parameter_or != null) {
				count_query += " ( ";
				for (QueryParam qparam_or : parameter_or) {
					count_query += " " + qparam_or.getFieldName() + " = '" + qparam_or.getValue() + "' ";
					count_query += DBGetter.OR_OPERATOR;
				}
				// remove last operator
				count_query = count_query.substring(0, count_query.length() - DBGetter.OR_OPERATOR.length());
				count_query += " ) ";
			}

			if (parameter_and != null) {
				if (parameter_or != null) {
					count_query += " AND ";
				}
				count_query += " ( ";

				for (QueryParam qparam_and : parameter_and) {

					String param_value = qparam_and.getValue();
					if (param_value.equals("NULL")) {
						count_query += " ( " + qparam_and.getFieldName() + " IS NULL " + " OR ";
					}

					count_query += " " + qparam_and.getFieldName() + " = '" + qparam_and.getValue() + "' ";

					if (param_value.equals("NULL")) {
						count_query += " ) ";
					}

					count_query += DBGetter.AND_OPERATOR;

					// count_query += " " + qparam_and.getFieldName() + " = '"
					// + qparam_and.getValue() + "' ";

				}

				// remove last operator
				count_query = count_query.substring(0, count_query.length() - DBGetter.AND_OPERATOR.length());
				count_query += " ) ";
			}

			count_query += ";";

			if (PRINT_SQL)
				System.out.println(count_query);

			ResultSet rs1 = st.executeQuery(count_query);

			if (rs1.next()) {

				count = rs1.getInt(count_var);

			}

			rs1.close();

			st.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;

	}

	public int countSelectedDbObjects(Connection conn, DbObject obj, Vector<QueryParam> parameter_or,
			Vector<QueryParam> parameter_and) {
		return countSelectedDbObjectsDistinct(conn, obj, parameter_or, parameter_and, null);
	}

	/**
	 * Checks whether an object is already stored in the database by checking
	 * whether its primary key already exists in the table. Uses prepared
	 * statements.
	 * 
	 * @param conn
	 *            Database connection
	 * @param obj
	 *            Object whose existance is to be checked
	 * @return
	 */
	public boolean objectExists(Connection conn, DbObject obj) {

		boolean exists = false;

		try {

			String exists_query = "SELECT 1 FROM " + obj.getTableName() + " WHERE ";

			for (String fieldName : obj.getValues().keySet()) {

				if (!obj.getPrimaryKeyFields().containsKey(fieldName))
					continue;

				exists_query += " " + fieldName + " = ? " + DBGetter.AND_OPERATOR;
			}

			exists_query = exists_query.substring(0, exists_query.length() - DBGetter.AND_OPERATOR.length());

			PreparedStatement pstmt = conn.prepareStatement(exists_query);

			int preparedParamIndex = 1;
			for (String fieldName : obj.getValues().keySet()) {

				if (!obj.getPrimaryKeyFields().containsKey(fieldName))
					continue;

				Object value = obj.getValues().get(fieldName);
				int sqlType = obj.getFields().get(fieldName).getSQLType();

				pstmt.setObject(preparedParamIndex, value, sqlType);

				preparedParamIndex += 1;
			}

			if (PRINT_SQL)
				System.out.println(exists_query);

			final ResultSet resultSet = pstmt.executeQuery();

			if (resultSet.absolute(1))
				exists = true;

			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return exists;
	}

	/**
	 * Retreive one single result for the query params.
	 */
	public DbObject retrieveOneSelected(Connection conn, DbObject obj, Vector<QueryParam> parameter_or,
			Vector<QueryParam> parameter_and, String order) {
		Set<DbObject> result = retrieveSelected(conn, obj, parameter_or, parameter_and, "1", order);
		DbObject resultObject = null;
		for (DbObject dbo : result) {
			resultObject = dbo;
			break;
		}
		return resultObject;
	}

	public <T extends DbObject> Set<T> retrieveSelected(Connection conn, DbObject obj, Vector<QueryParam> parameter_or,
			Vector<QueryParam> parameter_and, String limit) {
		return retrieveSelected(conn, obj, parameter_or, parameter_and, limit, "");
	}

	// SELECT * FROM freebase_movie.wiki_text WHERE original_language='pl' OR
	// original_language='en';

	public <T extends DbObject> Set<T> retrieveSelected(Connection conn, DbObject obj, Vector<QueryParam> parameter_or,
			Vector<QueryParam> parameter_and, String limit, String order) {

		Set<T> result;
		if (order == null || order.equals(""))
			result = new HashSet<T>();
		else
			result = new LinkedHashSet<T>();

		try {

			// Statement st = conn.createStatement();

			String query1 = "SELECT * FROM " + obj.getTableName() + " ";

			if (parameter_or != null || parameter_and != null)
				query1 += "WHERE ";

			// for (QueryParam qparam : parameter) {
			// query1 += " " + qparam.getFieldName() + " = '"
			// + qparam.getValue() + "' ";
			// query1 += operator;
			// }
			// // remove last operator
			// query1 = query1.substring(0, query1.length() -
			// operator.length());

			if (parameter_or != null) {
				query1 += " ( ";
				for (QueryParam qparam_or : parameter_or) {

					String operator = "=";
					if (qparam_or.getOperator() == QueryOperator.LIKE)
						operator = "LIKE";
					else if (qparam_or.getOperator() == QueryOperator.NOT_LIKE)
						operator = "NOT LIKE";

					String param_value = qparam_or.getValue();
					if (param_value.equals("NULL")) {
						query1 += " ( " + qparam_or.getFieldName() + " IS NULL " + " OR ";
					} else if (param_value.equals("NOT NULL")) {
						query1 += " ( " + qparam_or.getFieldName() + " IS NOT NULL " + " OR ";
					}

					query1 += " " + qparam_or.getFieldName() + " " + operator + " ";
					if (qparam_or.hasType())
						query1 += "? ";
					else
						query1 += "'" + qparam_or.getValue() + "' ";

					if (param_value.equals("NULL") || param_value.equals("NOT NULL")) {
						query1 += " ) ";
					}
					query1 += DBGetter.OR_OPERATOR;
				}
				// remove last operator
				query1 = query1.substring(0, query1.length() - DBGetter.OR_OPERATOR.length());
				query1 += " ) ";
			}

			if (parameter_and != null) {
				if (parameter_or != null) {
					query1 += " AND ";
				}
				query1 += " ( ";
				for (QueryParam qparam_and : parameter_and) {

					String operator = "=";
					if (qparam_and.getOperator() == QueryOperator.LIKE)
						operator = "LIKE";
					else if (qparam_and.getOperator() == QueryOperator.NOT_LIKE)
						operator = "NOT LIKE";

					String param_value = qparam_and.getValue();
					if (param_value.equals("NULL")) {
						query1 += " ( " + qparam_and.getFieldName() + " IS NULL " + " OR ";
					} else if (param_value.equals("NOT NULL")) {
						query1 += " ( " + qparam_and.getFieldName() + " IS NOT NULL " + " OR ";
					}

					query1 += " " + qparam_and.getFieldName() + " " + operator + " ";
					if (qparam_and.hasType())
						query1 += "? ";
					else
						query1 += "'" + qparam_and.getValue() + "' ";

					if (param_value.equals("NULL") || param_value.equals("NOT NULL")) {
						query1 += " ) ";
					}

					query1 += DBGetter.AND_OPERATOR;
				}
				// remove last operator
				query1 = query1.substring(0, query1.length() - DBGetter.AND_OPERATOR.length());
				query1 += " ) ";
			}

			if (order != null && order != "") {
				query1 += "ORDER BY " + order + " ";
			}

			if (limit != null && limit != "") {
				query1 += "LIMIT " + limit;
			}
			query1 += ";";

			if (PRINT_SQL)
				System.out.println(query1);

			PreparedStatement pstmt = conn.prepareStatement(query1);

			int preparedParamIndex = 1;

			if (parameter_or != null) {
				for (QueryParam qparam_or : parameter_or) {
					if (qparam_or.hasType()) {
						pstmt.setObject(preparedParamIndex, qparam_or.getValue(), qparam_or.getType());
						preparedParamIndex += 1;
					}
				}
			}

			if (parameter_and != null) {
				for (QueryParam qparam_and : parameter_and) {
					if (qparam_and.hasType()) {
						pstmt.setObject(preparedParamIndex, qparam_and.getValue(), qparam_and.getType());
						preparedParamIndex += 1;
					}
				}
			}

			ResultSet rs1 = pstmt.executeQuery();

			while (rs1.next()) {
				Map<String, Object> objectValueMap = new TreeMap<String, Object>();

				for (String fieldName : obj.getFields().keySet()) {

					String fieldType = obj.getFields().get(fieldName).getType();
					addToObjectValueMap(objectValueMap, fieldType, fieldName, rs1);
				}

				// create a new instance of the class of "obj" parameter
				Class<?> clazz = Class.forName(obj.getClass().getName());
				Constructor<?> ctor = clazz.getConstructor(TreeMap.class);
				@SuppressWarnings("unchecked")
				T object = (T) ctor.newInstance(new Object[] { objectValueMap });

				object.setIsNew(false);
				result.add(object);

			}
			rs1.close();

			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	public <T extends DbObject> Set<T> retrieveAll(Connection conn, DbObject obj) {
		return retrieveAll(conn, obj, null);
	}

	public <T extends DbObject> Set<T> retrieveAll(Connection conn, DbObject obj,
			List<SortingAttribute> sortingAttributes) {

		Set<T> result;
		if (sortingAttributes != null && !sortingAttributes.isEmpty())
			result = new LinkedHashSet<T>();
		else
			result = new HashSet<T>();

		try {

			Statement st = conn.createStatement();

			String query1 = "SELECT * FROM " + obj.getTableName();

			// create "ORDER BY [...] ASC/DESC, [...] ASC/DESC, ..." string
			if (sortingAttributes != null && !sortingAttributes.isEmpty()) {
				// don't add comma before first sorting attribute
				boolean firstSortingAttribute = true;
				query1 += " ORDER BY ";
				for (SortingAttribute attr : sortingAttributes) {
					if (!firstSortingAttribute)
						query1 += ",";
					else
						firstSortingAttribute = false;
					query1 += attr.getAttribute() + " ";
					if (attr.getOrder() == SortMode.ASC)
						query1 += " ASC ";
					else
						query1 += " DESC ";
				}
			}

			query1 += ";";

			ResultSet rs1 = st.executeQuery(query1);

			while (rs1.next()) {
				Map<String, Object> objectValueMap = new TreeMap<String, Object>();

				for (String fieldName : obj.getFields().keySet()) {

					String fieldType = obj.getFields().get(fieldName).getType();

					addToObjectValueMap(objectValueMap, fieldType, fieldName, rs1);
				}

				// create a new instance of the class of "obj" parameter
				Class<?> clazz = Class.forName(obj.getClass().getName());
				Constructor<?> ctor = clazz.getConstructor(TreeMap.class);
				@SuppressWarnings("unchecked")
				T object = (T) ctor.newInstance(new Object[] { objectValueMap });
				object.setIsNew(false);

				result.add(object);

			}
			rs1.close();

			st.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	private void addToObjectValueMap(Map<String, Object> objectValueMap, String fieldType, String fieldName,
			ResultSet rs1) throws SQLException {

		if (fieldType.equals(DbField.stringType) || fieldType.equals(DbField.textType)) {
			String strValue = rs1.getString(fieldName);
			objectValueMap.put(fieldName, strValue);
		} else if (fieldType.equals(DbField.intType)) {
			int intValue = rs1.getInt(fieldName);
			objectValueMap.put(fieldName, new Integer(intValue));
		} else if (fieldType.equals(DbField.doubleType)) {
			double doubleValue = rs1.getDouble(fieldName);
			objectValueMap.put(fieldName, doubleValue);
		} else if (fieldType.equals(DbField.timestampType)) {
			Timestamp timestamp = rs1.getTimestamp(fieldName);
			objectValueMap.put(fieldName, timestamp);
		} else if (fieldType.equals(DbField.bigintType)) {
			long longValue = rs1.getLong(fieldName);
			objectValueMap.put(fieldName, longValue);
		} else if (fieldType.equals(DbField.booleanType)) {
			boolean booleanValue = rs1.getBoolean(fieldName);
			objectValueMap.put(fieldName, booleanValue);
		} else if (fieldType.equals(DbField.datetimeType)) {
			Date datetimeValue = rs1.getTimestamp(fieldName);
			objectValueMap.put(fieldName, datetimeValue);
		} else if (fieldType.equals(DbField.dateType)) {
			Date dateValue = rs1.getTimestamp(fieldName);
			objectValueMap.put(fieldName, dateValue);
		}

	}

	/**
	 * Inserts an object in the database only if it is not already existing
	 * there (by primary key).
	 * 
	 * @param conn
	 *            Database connection
	 * @param obj
	 *            Object to insert
	 * @return true, if insertion was made. False, if object was already
	 *         existing.
	 */
	public boolean storeDbObjectIfNew(Connection conn, DbObject obj) {

		if (objectExists(conn, obj))
			return false;

		storeDbObjectPrepared(conn, obj);
		return true;
	}

	// updates DbObject in the database using primary key to identify the
	// object. Uses prepared statements.
	public void updateDbObjectPrepared(Connection conn, DbObject obj) {
		try {

			String update = " UPDATE " + obj.getTableName() + " SET ";

			for (String fieldName : obj.getUpdateValues().keySet()) {
				// do not add primary key fields
				// if (!obj.getPrimaryKeyFields().containsKey(fieldName)) {
				// update += " " + fieldName + " = '" +
				// obj.getUpdateValues().get(fieldName) + "'" + " ,";
				update += " " + fieldName + " = ?,";
				// }
			}

			// remove last AND
			if (update.endsWith(",")) {
				update = update.substring(0, update.length() - 1);
			}

			update += " WHERE (";

			for (String keyName : obj.getPrimaryKeyFields().keySet()) {
				if (obj.getValues().get(keyName) != null) {
					update += " " + keyName + " = ?" + " AND";
				} else {
					System.err.println("pk field is null " + keyName);
				}
			}
			// remove last comma
			if (update.endsWith("AND")) {
				update = update.substring(0, update.length() - 3);
			}

			update += ");";
			if (PRINT_SQL)
				System.out.println(update);

			PreparedStatement pstmt = conn.prepareStatement(update);

			int preparedParamIndex = 1;
			for (String fieldName : obj.getUpdateValues().keySet()) {
				// if (!obj.getPrimaryKeyFields().containsKey(fieldName)) {
				Object value = obj.getUpdateValues().get(fieldName);
				int sqlType = obj.getFields().get(fieldName).getSQLType();

				if (value.toString().equals("NULL"))
					pstmt.setNull(preparedParamIndex, sqlType);
				else
					pstmt.setObject(preparedParamIndex, value, sqlType);

				preparedParamIndex += 1;
				// }
			}
			for (String keyName : obj.getPrimaryKeyFields().keySet()) {
				Object value = obj.getValues().get(keyName);
				int sqlType = obj.getFields().get(keyName).getSQLType();
				pstmt.setObject(preparedParamIndex, value, sqlType);
				preparedParamIndex += 1;
			}

			pstmt.executeUpdate();
			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static class SortingAttribute {

		private String attribute;

		private SortMode order;

		public SortingAttribute(String attribute, SortMode order) {
			super();
			this.attribute = attribute;
			this.order = order;
		}

		public String getAttribute() {
			return attribute;
		}

		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}

		public SortMode getOrder() {
			return order;
		}

		public void setOrder(SortMode order) {
			this.order = order;
		}

		public enum SortMode {
			ASC, DESC;
		}

	}

}
