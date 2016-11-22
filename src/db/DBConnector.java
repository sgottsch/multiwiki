package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import util.FileGetter;

/**
 * Connection to the database
 **/
public class DBConnector {
		
	private static boolean driverload = false;
	private static long lastConnectionTime = 0;
	private static HashMap<String, Connection> conmap = new HashMap<String, Connection>();
	private static FileGetter fileGetter = new FileGetter(null);
	private static String name;

	public static void setFileGetter(FileGetter fg) {
		fileGetter = fg;
	}

	/**
	 * retrieves table names and their primary keys from the database
	 */
	// @returns tablename->pk
	public static HashMap<String, String> getDBMetadata(String name) throws Exception {
		Connection conn = DBConnector.getDBConnection(name);
		HashMap<String, String> metamap = new HashMap<String, String>();
		DatabaseMetaData meta = conn.getMetaData();

		ResultSet tables_rs = meta.getTables(null, null, null, null);

		while (tables_rs.next()) {
			String tableName = tables_rs.getString("TABLE_NAME");

			ResultSet rsmeta = meta.getPrimaryKeys(null, null, tableName);

			while (rsmeta.next()) {
				String columnName = rsmeta.getString("COLUMN_NAME");
				metamap.put(tableName, columnName);
				// DEBUG
				// System.out.println(tableName+" pk=" + columnName);
			}
			rsmeta.close();
		}

		tables_rs.close();
		// conn.close();
		return metamap;
	}

	public static Connection getDBConnection(String name) throws SQLException, ClassNotFoundException {
		return DBConnector.getDBConnection(name, null);
	}

	public static Connection getDBConnection(String name, FileGetter fg) throws SQLException, ClassNotFoundException {
		DBConnector.name = name;
		Connection con = null;
		long timenow = new Date().getTime();
		if (conmap.get(name.toLowerCase()) != null) {
			con = conmap.get(name.toLowerCase());

			// TODO: Changed "60000" to "600000" - okay?
			if (timenow - lastConnectionTime > 600000) {
				System.out.println("DBConnector: Restart connection, too long ago");
				con.close();
			}

			if (!con.isClosed())
				return con;

			System.out.println("Connection was closed!");
		}
		if (!driverload) {
			Class.forName("com.mysql.jdbc.Driver");
		}
		Properties properties = new Properties();
		try {
			InputStream is = DBConnector.class.getResourceAsStream("/resource/db.properties");
			properties.load(is);
			String url = "jdbc:mysql://"
					+ properties.getProperty(new StringBuilder(String.valueOf(name)).append("_host").toString()) + ":"
					+ properties.getProperty(new StringBuilder(String.valueOf(name)).append("_port").toString()) + "/"
					+ properties.getProperty(new StringBuilder(String.valueOf(name)).append("_dbname").toString())
					+ "?useUnicode=true&characterEncoding=utf-8&max_allowed_packet=100M";
			System.out.println(url);
			con = DriverManager.getConnection(url, properties.getProperty(String.valueOf(name) + "_username"),
					properties.getProperty(String.valueOf(name) + "_password"));
			System.out.println("-- Established DB connection");

			conmap.put(name.toLowerCase(), con);
			driverload = true;
			lastConnectionTime = timenow;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			driverload = false;
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			driverload = false;
			return null;
		}
		return new MyCon(String.valueOf(Math.random()), con);
	}

	public static void closeConnections() {
		for (String db : conmap.keySet()) {
			Connection con = conmap.get(db);
			try {
				con.close();
			} catch (SQLException e) {

				// e.printStackTrace();
			}
		}
	}

	// for test only
	public static void main(final String[] args) {
		try {
			Connection conn = DBConnector.getDBConnection("lyrics");
			System.out.println("Connected to the database ");

			conn.commit();
			conn.close();
			System.out.println("Closed connection");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Properties getProperties() throws IOException {
		Properties properties = new Properties();
		FileGetter hlpfileGetter = fileGetter;
		FileInputStream stream = new FileInputStream(new File(hlpfileGetter.path("db.properties")));
		properties.load(stream);
		stream.close();
		return properties;
	}

	public static String getName() {
		return name;
	}

	public static boolean isDriverload() {
		return driverload;
	}
}
