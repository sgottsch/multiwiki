package extractor;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import translate.Language;
import db.DBConnector;
import db.DBGetter;
import db.DbObject;
import nlp.OpenNLPutils;

public abstract class SingleLanguageExtractor {

	protected DBGetter dbget;
	protected Connection conn;

	protected ExtractionDataStore dataStore;
	protected ExtractionConfigThreadSafe config;

	protected List<Language> languages;
	protected Language language;
	protected String database;

	protected OpenNLPutils langTool;

	protected List<DbObject> dbObjectsToUpdate = new ArrayList<DbObject>();

	public SingleLanguageExtractor(String database, Language language, List<Language> languages, ExtractionDataStore dataStore) {
		this.database = database;
		this.language = language;
		this.languages = languages;
		this.dataStore = dataStore;
		this.config = ExtractionConfigThreadSafe.getInstance();
	}

	protected void connect() {
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

	protected void loadOpenNLPTools() {
		try {
			langTool = new OpenNLPutils(language);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public List<DbObject> getDbObjectsToUpdate() {
		return this.dbObjectsToUpdate;
	}

	public ExtractionDataStore getDataStore() {
		return dataStore;
	}
	
}
