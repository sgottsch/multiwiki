package de.l3s.dbpopulate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.l3s.algorithms.Comparator;
import de.l3s.algorithms.passages.ParagraphMethodConfig;
import de.l3s.app.Configuration;
import de.l3s.db.DbObject;
import de.l3s.db.QueryParam;
import de.l3s.db.QueryParam.QueryOperator;
import de.l3s.db.tables.ComparisonSimilarity_DB;
import de.l3s.db.tables.Comparison_DB;
import de.l3s.dbloader.DataLoader;
import de.l3s.extractor.Extractor;
import de.l3s.model.Article;
import de.l3s.model.BinaryComparison;
import de.l3s.model.Revision;
import de.l3s.tfidf.EntityCollection;
import de.l3s.tfidf.WordCollection;
import de.l3s.translate.Language;

/**
 * To efficiently display the history chart, the similarity value for every
 * revision pair in the data base has to be precalculated. This is done with
 * this class.
 */

public class ComparisonSimilarityCollector extends Extractor {

	private DataLoader dl;

	private Map<BinaryComparison, Long> comparisonIds;

	public ComparisonSimilarityCollector(String database) {
		super(database);
		this.dl = new DataLoader(database);
		this.comparisonIds = new HashMap<BinaryComparison, Long>();
	}

	public List<BinaryComparison> fetchAllBinaryComparisons() {

		connect();

		// create list of already stored revision pairs
		Set<DbObject> storedComparisons = dbget.retrieveAll(conn, new ComparisonSimilarity_DB());

		// TODO: Add Identifiers (with date) to that list
		Set<String> alreadyFoundRevisions = new HashSet<String>();

		for (DbObject dbo : storedComparisons) {
			ComparisonSimilarity_DB cs = (ComparisonSimilarity_DB) dbo;
			String identifier = cs.getLong("revision1_id") + "-" + cs.getString("language2")
					+ cs.getLong("revision2_id") + "-" + cs.getDate("date");
			alreadyFoundRevisions.add(identifier);
		}

		List<BinaryComparison> comparisons = new ArrayList<BinaryComparison>();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(de.l3s.db.tables.Comparison_DB.unused_attr, QueryOperator.IS_FALSE));
		Set<DbObject> comparisonsTmp = dbget.retrieveSelected(conn, new Comparison_DB(), null, qparam_and, null);
		for (DbObject comparisonTmp : comparisonsTmp) {

			Comparison_DB comparison = (Comparison_DB) comparisonTmp;

			Date comparisonDate = comparison.getDate(Comparison_DB.date_attr);
			long comparisonId = comparison.getLong(Comparison_DB.comparison_id_attr);

			Language language1 = Language.valueOf(comparison.getString(Comparison_DB.language1_attr).toUpperCase());
			Language language2 = Language.valueOf(comparison.getString(Comparison_DB.language2_attr).toUpperCase());

			Language language3 = null;

			String articleUri1 = comparison.getString(Comparison_DB.article1_uri_attr);
			String articleUri2 = comparison.getString(Comparison_DB.article2_uri_attr);

			if (comparison.getValue(Comparison_DB.language3_attr) != null)
				language3 = Language.valueOf(comparison.getString(Comparison_DB.language3_attr).toUpperCase());

			Long revisionId1 = comparison.getLong(Comparison_DB.revision1_id_attr);
			Long revisionId2 = comparison.getLong(Comparison_DB.revision2_id_attr);

			Long revisionId3 = null;
			String articleUri3 = null;
			if (language3 != null) {
				revisionId3 = comparison.getLong(Comparison_DB.revision3_id_attr);
				articleUri3 = comparison.getString(Comparison_DB.article3_uri_attr);
			}

			// Build all possible binary comparisons with at least one English
			// revision

			// Identify english revision
			long revisionIdEn;
			String articleUriEn = null;
			if (language1 == Language.EN) {
				revisionIdEn = revisionId1;
				articleUriEn = articleUri1;
				language1 = language2;
				articleUri1 = articleUri2;
				revisionId1 = revisionId2;
				language2 = language3;
				articleUri2 = articleUri3;
				revisionId2 = revisionId3;
			} else if (language2 == Language.EN) {
				articleUriEn = articleUri2;
				revisionIdEn = revisionId2;
				language2 = language3;
				articleUri2 = articleUri3;
				revisionId2 = revisionId3;
			} else if (language3 == Language.EN) {
				articleUriEn = articleUri3;
				revisionIdEn = revisionId3;
			} else {
				throw new NullPointerException("There is no English revision in comparison "
						+ comparison.getString(Comparison_DB.article1_uri_attr));
			}

			language3 = null;
			revisionId3 = null;

			Article articleEn = new Article(articleUriEn, Language.EN);
			Article article1 = new Article(articleUri1, language1);
			Article article2 = new Article(articleUri2, language2);

			Revision revisionEn = new Revision(revisionIdEn, null, null, articleEn, Language.EN);
			Revision revision1 = new Revision(revisionId1, null, null, article1, language1);
			Revision revision2 = null;
			if (language2 != null)
				revision2 = new Revision(revisionId2, null, null, article2, language2);

			BinaryComparison comparison1 = new BinaryComparison(revisionEn, revision1);

			String comparison1Identifier = revisionEn.getId() + "-" + revision1.getLanguage()
					+ revision1.getId() + "-" + comparison.getDate("date");

			if (!alreadyFoundRevisions.contains(comparison1Identifier)) {
				comparison1.setDate(comparisonDate);
				comparisons.add(comparison1);
				comparisonIds.put(comparison1, comparisonId);

				// alreadyFoundRevisions.add(comparison1Identifier);
			}

			if (revision2 != null) {
				BinaryComparison comparison2 = new BinaryComparison(revisionEn, revision2);

				String comparison2Identifier = revisionEn.getId() + "-" + revision2.getLanguage()
						+ revision2.getId() + "-" + comparison.getDate("date");

				if (!alreadyFoundRevisions.contains(comparison2Identifier)) {
					comparison2.setDate(comparisonDate);
					comparisons.add(comparison2);
					comparisonIds.put(comparison2, comparisonId);

					// alreadyFoundRevisions.add(comparison1Identifier);
				}
			}

		}

		return comparisons;
	}

	public boolean calculateSimilarity(BinaryComparison comp) {

		Revision revision1 = loadRevision(comp.getRevision1(), comp.getRevision2());
		Revision revision2 = loadRevision(comp.getRevision2(), revision1);

		// if (revision1.getHtmlText().length() < 300000)
		// return false;

		comp.setRevision1(revision1);
		comp.setRevision2(revision2);

		WordCollection wc = new WordCollection(comp, revision1, revision2);
		wc.createLuceneIndex();
		EntityCollection ec = new EntityCollection(comp, revision1, revision2);
		ec.createLuceneIndex();

		Comparator comparator = new Comparator(ParagraphMethodConfig.getDefaultParagraphMethodConfig());
		// try {
		// comparator.compare(comp);
		// } catch (Exception e) {
		// return false;
		// }
		comparator.compare(comp);

		return true;
	}

	private Revision loadRevision(Revision revision, Revision otherRevisionFromComparison) {
		Language language = revision.getLanguage();
		long revisionId = revision.getId();

		Revision loadedRevision = dl.loadRevision(revisionId, language, otherRevisionFromComparison,
				otherRevisionFromComparison.getLanguage());
		// if (language == Language.EN)
		// loadedEnglishRevisions.put(revisionId, loadedRevision);

		return loadedRevision;
	}

	public void storeSimilarity(BinaryComparison comp) {
		ComparisonSimilarity_DB compSim = new ComparisonSimilarity_DB(comp, comparisonIds.get(comp));
		System.out.println(compSim);
		System.out.println("Overall comparison: " + comp.getOverallSimilarity());
		System.out.println(comp.getDate());
		dbget.storeDbObjectPrepared(conn, compSim);
	}

	public static void main(String[] args) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("Start time: " + dateFormat.format(new Date()));

		ComparisonSimilarityCollector collector = new ComparisonSimilarityCollector(Configuration.DATABASE1);
		List<BinaryComparison> comparisons = collector.fetchAllBinaryComparisons();

		int i = 0;
		int numberOfRevisionPairs = comparisons.size();

		for (Iterator<BinaryComparison> it = comparisons.iterator(); it.hasNext();) {
			BinaryComparison comparison = it.next();
			i += 1;
			System.out.println(i + "/" + numberOfRevisionPairs);
			System.out.println(comparison.getRevision1().getLanguage() + " - " + comparison.getRevision2().getLanguage()
					+ ": " + comparison.getRevision1().getArticleUri() + " - "
					+ comparison.getRevision2().getArticleUri() + ": " + comparison.getRevision1().getId() + " - "
					+ comparison.getRevision2().getId());

			if (!collector.calculateSimilarity(comparison)) {
				System.out.println("--- skipped or error ---");
				it.remove();
				collector.comparisonIds.remove(comparison);
				continue;
			}

			collector.storeSimilarity(comparison);
			// Remove comparison from list to free some heap space (otherwise, a
			// heap space error message appears after some comparisons)
			it.remove();
			collector.comparisonIds.remove(comparison);
		}

		System.out.println("Stored " + numberOfRevisionPairs + " comparisons.");

		System.out.println("End time: " + dateFormat.format(new Date()));

	}
}
