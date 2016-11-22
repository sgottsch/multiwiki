/**
 * 
 */
package dbpopulate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.json.JSONObject;

import app.Configuration;
import db.DBGetter;
import db.DbObject;
import db.QueryParam;
import db.QueryParam.QueryOperator;
import db.tables.Author_DB;
import extractor.Extractor;
import util.URLUtil;

/**
 * For each Wikipedia author that is found in the data base for which there has
 * not been searched for a country code, the country code is searched by this
 * class. This can only be done for the anonymous authors. For the other
 * authors, there is made a default entry like "-" as well.
 * 
 * The process is executed in a task, s.t. the IP Location API is not queried
 * too frequently.
 */
public class IPLocator extends Extractor {

	public IPLocator(String database) {
		super(database);
	}

	public static void main(String args[]) {
		IPLocator loc = new IPLocator(Configuration.DATABASE1);
		loc.markAuthorsWithoutIPAddress();
		Stack<Author_DB> authors = loc.loadAuthorsAndMarkAuthorsWithoutIPAddress();
		loc.startScheduler(authors);
	}

	/**
	 * Marks every author whose name does not begin with a number as not having
	 * an IP address (prefiltering for acceleration).
	 */
	private void markAuthorsWithoutIPAddress() {
		System.out.println(
				"Mark every author whose name does not begin with a number as not having an IP address (prefiltering for acceleration).");
		try {
			connect();
			// String query = "UPDATE author SET " + Author.has_ip_address_attr
			// + " = FALSE WHERE "
			// + Author.has_ip_address_attr + " IS NULL AND " + Author.name_attr
			// + " REGEXP '^[^0-9]''";
			String query = "UPDATE author SET " + Author_DB.has_ip_address_attr + " = FALSE WHERE "
					+ Author_DB.has_ip_address_attr + " IS NULL AND INET_ATON(" + Author_DB.name_attr + ") IS NOT NULL";
			System.out.println(query);
			Statement st = conn.createStatement();
			int numberOfAffectedAuthors = st.executeUpdate(query);
			System.out.println("Found " + numberOfAffectedAuthors + " whose names don't begin with a number.");
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void startScheduler(Stack<Author_DB> authors) {

		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(5);

		IPLocationTask st01 = new IPLocationTask(authors, dbget, conn);
		// start right now and after every 0.334 seconds.
		// From the website:
		// "Our system will automatically ban any IP addresses doing over 250
		// requests per minute. If your IP was banned, use this form to remove
		// the ban."
		stpe.scheduleAtFixedRate(st01, 0, 334, TimeUnit.MILLISECONDS);
	}

	public Stack<Author_DB> loadAuthorsAndMarkAuthorsWithoutIPAddress() {

		this.connect();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(Author_DB.location_attr, QueryOperator.IS_NULL));

		Vector<QueryParam> qparam_or = new Vector<QueryParam>();
		qparam_or.add(new QueryParam(Author_DB.has_ip_address_attr, QueryOperator.IS_NULL));
		qparam_or.add(new QueryParam(Author_DB.has_ip_address_attr, QueryOperator.IS_TRUE));

		Set<DbObject> authorsDbos = dbget.retrieveSelected(conn, new Author_DB(), qparam_or, qparam_and, null);

		Stack<Author_DB> authors = new Stack<Author_DB>();
		int i = 0;
		int numberOfAuthors = authorsDbos.size();

		for (DbObject dbo : authorsDbos) {
			Author_DB author = (Author_DB) dbo;
			if (InetAddressValidator.getInstance().isValid(author.getString(Author_DB.name_attr))) {
				author.addUpdateValueAndValue(Author_DB.has_ip_address_attr, 1);
				authors.add(author);
			} else if (author.getValue(Author_DB.has_ip_address_attr) == null) {
				System.out.println(author.getString(Author_DB.name_attr) + " -> no IP address (" + i + "/"
						+ numberOfAuthors + ")");
				author.addUpdateValueAndValue(Author_DB.has_ip_address_attr, 0);
				dbget.updateDbObjectPrepared(conn, author);
			}
			i += 1;
		}

		System.out.println("Found " + authors.size() + " authors without location");
		return authors;
	}
}

class IPLocationTask implements Runnable {

	private Stack<Author_DB> authors;

	private int originalSize;

	private int numberOfLoadedAuthors;

	private DBGetter dbget;

	private Connection conn;

	public IPLocationTask(Stack<Author_DB> authors, DBGetter dbget, Connection conn) {
		this.authors = authors;
		originalSize = authors.size();
		numberOfLoadedAuthors = 0;
		this.dbget = dbget;
		this.conn = conn;
	}

	public void run() {
		if (authors.size() == 0) {
			System.out.println("Ready.");
			System.exit(0);
		}
		Author_DB author = authors.pop();
		numberOfLoadedAuthors += 1;
		loadLocation(author);
		System.out.println(numberOfLoadedAuthors + "/" + originalSize + ": " + author.getString(Author_DB.name_attr)
				+ " -> " + author.getString(Author_DB.location_attr));
	}

	public void loadLocation(Author_DB author) {
		String countryCode = "-";

		JSONObject json = new JSONObject();

		String url = "http://ip-api.com/json/" + author.getString(Author_DB.name_attr) + "?fields=countryCode";

		try {
			json = new JSONObject(URLUtil.readUrl(url));

			if (json.has("countryCode") && !json.getString("countryCode").isEmpty())
				countryCode = json.getString("countryCode");
			else
				countryCode = "-";

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		author.addUpdateValueAndValue(Author_DB.location_attr, countryCode);
		dbget.updateDbObjectPrepared(conn, author);
	}

}
