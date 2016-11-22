package de.l3s.dbpopulate.heideltime;

import java.util.HashSet;
import java.util.Set;

import de.l3s.app.Configuration;
import de.l3s.db.tables.Sentence_HeidelTime_DB;
import de.l3s.dbloader.DataLoader;
import de.l3s.extractor.Extractor;
import de.l3s.extractor.HeidelTimeExtractorWithNewHeidelProject;
import de.l3s.heideltime.HeidelTimeExtractor;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.times.HeidelIntervalString;
import de.l3s.translate.Language;

public class HeidelTimeRevisionPopulator extends Extractor {

	private HeidelTimeExtractor heidelTimeExtractor;
	private DataLoader dataLoader;
	private Set<Sentence_HeidelTime_DB> dbAnnotations;
	private long revisionId;
	private Revision revision;
	private Language language;

	// public static void main(String[] args) {
	// HeidelTimeExtractor heidelTimeExtractor =
	// HeidelTimeExtractorWithNewHeidelProject
	// .getHeidelTimeFinder(Language.EN);
	// HeidelTimeRevisionPopulator populator = new
	// HeidelTimeRevisionPopulator(680960695, Language.EN,
	// heidelTimeExtractor);
	// populator.init();
	// populator.loadTimes();
	// }

	public HeidelTimeRevisionPopulator(long revisionId, Language language, HeidelTimeExtractor heidelTimeExtractor) {
		super(Configuration.DATABASE1);
		this.heidelTimeExtractor = heidelTimeExtractor;
		this.revisionId = revisionId;
		this.language = language;
	}

	public Revision loadRevision() {
		this.dataLoader = new DataLoader(Configuration.DATABASE1);
		return this.dataLoader.loadRevisionWithoutFeatures(this.revisionId, this.language);
	}

	public void init() {
		this.heidelTimeExtractor = HeidelTimeExtractorWithNewHeidelProject.getHeidelTimeFinder(this.language);
		this.dbAnnotations = new HashSet<Sentence_HeidelTime_DB>();
		this.revision = this.loadRevision();
	}

	public void loadTimes() {
		HeidelTimeExtractorWithNewHeidelProject extractor = new HeidelTimeExtractorWithNewHeidelProject(
				this.heidelTimeExtractor, this.revision, this.revision.getLanguage());
		extractor.run();
		for (Sentence sentence : this.revision.getSentences()) {
			if (sentence.getHeidelIntervals() == null || sentence.getHeidelIntervals().isEmpty())
				continue;
			int numberInSentence = 0;
			for (HeidelIntervalString heidelTimeString : sentence.getHeidelIntervals()) {
				String[] intervalParts = heidelTimeString.getCompleteIntervalStringRepresentation().split("\\.");
				Sentence_HeidelTime_DB annotation = new Sentence_HeidelTime_DB(sentence, intervalParts[0],
						intervalParts[1]);
				annotation.setValue("covered_text", heidelTimeString.getTimex3interval().getCoveredText());
				annotation.setValue("number_in_sentence", numberInSentence);
				this.dbAnnotations.add(annotation);
				numberInSentence += 1;
			}
		}
	}

	void storeTimesInDB(de.l3s.db.tables.Revision_DB dbRevision) {
		this.connect();
		System.out.println("Store " + this.dbAnnotations.size() + " annotations for revision "
				+ this.revision.getId() + " (" + this.revision.getLanguage() + ").");

		// for (Annotation_HeidelTime a : this.dbAnnotations)
		// System.out.println(a);

		this.dbget.storeDbObjectsPrepared(this.conn, this.dbAnnotations);
		dbRevision.addUpdateValueAndValue("loaded_heideltimes", 1);
		this.dbget.updateDbObjectPrepared(this.conn, dbRevision);
	}
}
