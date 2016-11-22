package extractor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import db.DBConnector;
import db.DBGetter;
import translate.Language;

public abstract class Extractor {

	protected DBGetter dbget;
	protected Connection conn;

	protected List<Language> languages;
	protected String database;

	protected ExtractionDataStore dataStore;
	protected ExtractionConfigThreadSafe config;

	protected Extractor(String database, List<Language> languages) {
		this.database = database;
		this.languages = languages;
		this.dataStore = new ExtractionDataStore();
		this.config = ExtractionConfigThreadSafe.getInstance();
	}

	public Extractor(String database) {
		this.database = database;
		this.config = ExtractionConfigThreadSafe.getInstance();
	}

	public Extractor(String database, Language language1, Language language2) {
		this(database);
		this.languages = new ArrayList<Language>();
		languages.add(language1);
		languages.add(language2);
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	protected void connect() {

		try {
			if (this.conn != null && this.conn.isClosed()) {
				this.conn = DBConnector.getDBConnection(this.database);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (this.dbget == null) {

			this.dbget = new DBGetter();
			this.conn = null;

			try {
				this.conn = DBConnector.getDBConnection(this.database);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
