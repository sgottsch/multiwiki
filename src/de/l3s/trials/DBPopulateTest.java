package de.l3s.trials;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.l3s.app.Configuration;
import de.l3s.dbpopulate.DBPopulator;
import de.l3s.dbpopulate.EnglishStemmer;
import de.l3s.dbpopulate.heideltime.HeidelTimePopulator;
import de.l3s.dbpopulate.spotlight.SpotlightPopulator;
import de.l3s.dbpopulate.translation.Translator;
import de.l3s.translate.Language;

public class DBPopulateTest {

	public static void main(String[] args) throws ParseException {

		DBPopulator dbpDe = new DBPopulator(Configuration.DATABASE1, Language.EN, Language.DE);
		DBPopulator dbpRu = new DBPopulator(Configuration.DATABASE1, Language.EN, Language.RU);

		SimpleDateFormat textFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date comparisonDate1 = textFormat.parse("2015-06-20");
		Date comparisonDate2 = textFormat.parse("2016-01-15");

		dbpDe.storeArticle("Battle of Messkirch", comparisonDate1);
		dbpDe.storeArticle("Arthur Seyss-Inquart", comparisonDate1);
		dbpRu.storeArticle("She Wants Revenge", comparisonDate2);

		HeidelTimePopulator timePopulator = new HeidelTimePopulator(Configuration.DATABASE1);
		timePopulator.populate(Language.EN);
		timePopulator.populate(Language.DE);
		timePopulator.populate(Language.RU);

		SpotlightPopulator spotlightPopulator = new SpotlightPopulator(Configuration.DATABASE1);
		spotlightPopulator.populate(Language.EN);
		spotlightPopulator.populate(Language.DE);
		spotlightPopulator.populate(Language.RU);

		EnglishStemmer stemmer = new EnglishStemmer(Configuration.DATABASE1);
		stemmer.stemAnnotations();

		Translator translator = new Translator();
		translator.translate();

	}

}
