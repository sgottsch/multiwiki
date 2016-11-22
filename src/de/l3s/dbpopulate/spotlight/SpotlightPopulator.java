package de.l3s.dbpopulate.spotlight;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import de.l3s.app.Configuration;
import de.l3s.db.QueryParam;
import de.l3s.db.tables.Revision_DB;
import de.l3s.dbpedia_spotlight.MyAnnotationException;
import de.l3s.extractor.ExtractionConfigThreadSafe;
import de.l3s.extractor.Extractor;
import de.l3s.translate.Language;

public class SpotlightPopulator extends Extractor {

	public static void main(String[] args) {
		SpotlightPopulator populator = new SpotlightPopulator(Configuration.DATABASE1);
		populator.populate(Language.RU);
	}

	public SpotlightPopulator(String database) {
		super(database);
	}

	public void populate(Language language) {

		HashSet<Language> languages = new HashSet<Language>();
		languages.add(language);

		ExtractionConfigThreadSafe.getInstance().init(languages);
		Set<Revision_DB> revisions = this.loadRevisionsWithoutSpotlightLinks(language);
		System.out.println("Load Spotlight links of " + revisions.size() + " articles in " + language + ".");

		int i = 1;

		for (Revision_DB revision : revisions) {

			System.out.println("Revision " + i + "/" + revisions.size());
			long revisionId = revision.getInt(Revision_DB.revision_id_attr);
			SpotlightRevisionPopulator revisionPopulator = new SpotlightRevisionPopulator(revisionId, language);
			revisionPopulator.init();

			try {
				revisionPopulator.loadLinks();
				revisionPopulator.storeSpotlightLinksInDB(revision);
			} catch (MyAnnotationException e) {
				System.out.println("Error. Ignore revision.");
				e.printStackTrace();
			}

			i += 1;
		}

	}

	private Set<Revision_DB> loadRevisionsWithoutSpotlightLinks(Language language) {

		this.connect();

		HashSet<Revision_DB> ids = new HashSet<Revision_DB>();
		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam("comment", QueryParam.QueryOperator.IS_NULL));
		qparam_and.add(new QueryParam("loaded_spotlightlinks_new", QueryParam.QueryOperator.IS_FALSE));
		qparam_and.add(new QueryParam("language", language.getLanguage()));
		Set<Revision_DB> dbRevisions = this.dbget.retrieveSelected(this.conn, new Revision_DB(), null, qparam_and, null);

		for (Revision_DB revision : dbRevisions) {
			ids.add(revision);
		}

		return ids;
	}
}
