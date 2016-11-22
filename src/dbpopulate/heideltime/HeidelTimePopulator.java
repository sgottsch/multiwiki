package dbpopulate.heideltime;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import app.Configuration;
import db.QueryParam;
import db.tables.Revision_DB;
import extractor.Extractor;
import extractor.HeidelTimeExtractorWithNewHeidelProject;
import heideltime.HeidelTimeExtractor;
import translate.Language;

public class HeidelTimePopulator extends Extractor {

	public static void main(String[] args) {
		HeidelTimePopulator populator = new HeidelTimePopulator(Configuration.DATABASE1);
		populator.populate(Language.EN);
	}

	public HeidelTimePopulator(String database) {
		super(database);
	}

	public void populate(Language language) {
		HeidelTimeExtractor heidelTimeExtractor = HeidelTimeExtractorWithNewHeidelProject.getHeidelTimeFinder(language);
		Set<Revision_DB> revisions = this.loadRevisionsWithoutHeidelTimes(language);
		System.out.println("Load HeidelTimes of " + revisions.size() + " articles in " + language + ".");
		int i = 0;
		for (Revision_DB revision : revisions) {
			System.out.println("Revision " + i + "/" + revisions.size());
			long revisionId = revision.getLong(Revision_DB.revision_id_attr);
			HeidelTimeRevisionPopulator revisionPopulator = new HeidelTimeRevisionPopulator(revisionId, language,
					heidelTimeExtractor);
			revisionPopulator.init();
			revisionPopulator.loadTimes();
			revisionPopulator.storeTimesInDB(revision);
			i += 1;
		}
	}

	private Set<Revision_DB> loadRevisionsWithoutHeidelTimes(Language language) {
		this.connect();
		HashSet<Revision_DB> ids = new HashSet<Revision_DB>();
		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam("comment", QueryParam.QueryOperator.IS_NULL));
		qparam_and.add(new QueryParam("loaded_heideltimes", QueryParam.QueryOperator.IS_FALSE));
		qparam_and.add(new QueryParam("language", language.getLanguage()));
		Set<Revision_DB> dbRevisions = this.dbget.retrieveSelected(this.conn, new Revision_DB(), null, qparam_and,
				null);
		for (Revision_DB revision : dbRevisions) {
			ids.add(revision);
		}
		return ids;
	}
}
