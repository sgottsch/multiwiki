package dbpopulate;

import java.util.Set;
import java.util.Vector;

import app.Configuration;
import db.DbObject;
import db.QueryParam;
import db.QueryParam.QueryOperator;
import db.tables.Sentence_DB;
import extractor.Extractor;
import nlp.LuceneUtils;

/**
 * For each English sentence in the data base that has not been stemmed so far,
 * its stemmed text is stored in the database with this class.
 */
public class EnglishStemmer extends Extractor {

	private LuceneUtils luceneUtils;

	public EnglishStemmer(String database) {
		super(database);

		luceneUtils = new LuceneUtils();
	}

	public void stemAnnotations() {
		connect();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(Sentence_DB.language_attr, "en", java.sql.Types.VARCHAR));
		qparam_and.add(new QueryParam(Sentence_DB.stemmed_english_text_attr, QueryOperator.IS_NULL));
		qparam_and.add(new QueryParam(Sentence_DB.original_text_attr, QueryOperator.IS_NOT_NULL));

		Set<DbObject> dbAnnotations = dbget.retrieveSelected(conn, new Sentence_DB(), null, qparam_and, null);
		int i = 0;
		for (DbObject dbo : dbAnnotations) {
			Sentence_DB a = (Sentence_DB) dbo;

			String stemmed = luceneUtils.stemmedEnglishText(a.getString(Sentence_DB.original_text_attr));

			if (i % 1000 == 0)
				System.out.println(i + "/" + dbAnnotations.size() + ": " + stemmed + "...");

			a.addUpdateValue(Sentence_DB.stemmed_english_text_attr, stemmed);

			dbget.updateDbObjectPrepared(conn, a);
			i += 1;
		}
	}

	public static void main(String[] args) {
		EnglishStemmer es = new EnglishStemmer(Configuration.DATABASE1);
		es.stemAnnotations();
	}

}
