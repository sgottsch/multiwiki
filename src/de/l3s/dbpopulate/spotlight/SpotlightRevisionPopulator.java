package de.l3s.dbpopulate.spotlight;

import java.util.HashSet;
import java.util.Set;

import de.l3s.app.Configuration;
import de.l3s.db.tables.Sentence_SpotlightLink_DB;
import de.l3s.dbloader.DataLoader;
import de.l3s.dbpedia_spotlight.MyAnnotationException;
import de.l3s.dbpedia_spotlight.SpotlightLink;
import de.l3s.dbpedia_spotlight.SpotlightLinkExtractorRawText;
import de.l3s.extractor.Extractor;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.translate.Language;

public class SpotlightRevisionPopulator extends Extractor {
	private DataLoader dataLoader;
	private Set<Sentence_SpotlightLink_DB> dbAnnotations;
	private long revisionId;
	private Revision revision;
	private Language language;

	// public static void main(String[] args) throws MyAnnotationException {
	// HashSet<Language> languages = new HashSet<Language>();
	// languages.add(Language.IT);
	// ExtractionConfigThreadSafe.getInstance().init(languages);
	// SpotlightRevisionPopulator populator = new
	// SpotlightRevisionPopulator(136234983, Language.DE);
	// populator.init();
	// populator.loadLinks();
	// }

	public SpotlightRevisionPopulator(long revisionId, Language language) {
		super(Configuration.DATABASE1);
		this.revisionId = revisionId;
		this.language = language;
	}

	public Revision loadRevision() {
		this.dataLoader = new DataLoader(Configuration.DATABASE1);
		return this.dataLoader.loadRevisionWithoutFeatures(this.revisionId, this.language);
	}

	public void init() {
		this.dbAnnotations = new HashSet<Sentence_SpotlightLink_DB>();
		this.revision = this.loadRevision();
	}

	public void loadLinks() throws MyAnnotationException {
		SpotlightLinkExtractorRawText extr = new SpotlightLinkExtractorRawText(this.revision);
		extr.run();
		for (Sentence sentence : this.revision.getSentences()) {
			int numberOfSentence = 0;
			for (SpotlightLink link : sentence.getSpotlightLinks()) {
				if (link.getWikiLink().equals("Viertelgeviertstrich"))
					continue;
				Sentence_SpotlightLink_DB dbAnnotation = new Sentence_SpotlightLink_DB(sentence, link.getWikiLink(),
						sentence.getLanguage(), numberOfSentence, link.getMetaData(), link.getCoveredText(),
						link.hasType());
				this.dbAnnotations.add(dbAnnotation);
				numberOfSentence += 1;
			}
		}
	}

	void storeSpotlightLinksInDB(de.l3s.db.tables.Revision_DB dbRevision) {
		this.connect();
		System.out.println("Store " + this.dbAnnotations.size() + " annotations for revision " + this.revision.getId()
				+ " (" + this.revision.getLanguage() + ").");

		for (Sentence_SpotlightLink_DB a : this.dbAnnotations)
			System.out.println(a);

		this.dbget.storeDbObjectsPrepared(this.conn, this.dbAnnotations);
		dbRevision.addUpdateValueAndValue("loaded_spotlightlinks_new", 1);
		this.dbget.updateDbObjectPrepared(this.conn, dbRevision);
	}
}
