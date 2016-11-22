package wiki;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import translate.Language;

public class WikiWords {

	private static WikiWords instance;

	private Map<Language, Set<String>> forbiddenLinks;
	private Map<Language, Set<String>> forbiddenInternalLinks;

	public static WikiWords getInstance() {
		if (instance == null) {
			instance = new WikiWords();
		}
		return instance;
	}

	public Set<String> getForbiddenInternalLinks(Language language) {
		if (this.forbiddenInternalLinks == null) {
			this.forbiddenInternalLinks = new HashMap<Language, Set<String>>();
			this.forbiddenInternalLinks.put(Language.EN, this.initForbiddenInternalLinks(Language.EN));
			this.forbiddenInternalLinks.put(Language.DE, this.initForbiddenInternalLinks(Language.DE));
			this.forbiddenInternalLinks.put(Language.PT, this.initForbiddenInternalLinks(Language.PT));
			this.forbiddenInternalLinks.put(Language.NL, this.initForbiddenInternalLinks(Language.NL));
			this.forbiddenInternalLinks.put(Language.RU, this.initForbiddenInternalLinks(Language.RU));
			this.forbiddenInternalLinks.put(Language.FR, this.initForbiddenInternalLinks(Language.FR));
			this.forbiddenInternalLinks.put(Language.IT, this.initForbiddenInternalLinks(Language.IT));
			this.forbiddenInternalLinks.put(Language.ES, this.initForbiddenInternalLinks(Language.ES));
		}
		return this.forbiddenInternalLinks.get(language);
	}

	private Set<String> initForbiddenInternalLinks(Language language) {

		Set<String> forbiddenNameSpaces = new HashSet<String>();
		Set<String> forbiddenInternalLinks = new HashSet<String>();

		String talkPrefix = null;
		String talkSuffix = null;

		if (language == Language.DE) {
			forbiddenNameSpaces.add("Diskussion");
			forbiddenNameSpaces.add("Benutzer");
			forbiddenNameSpaces.add("Benutzerin");
			forbiddenNameSpaces.add("Wikipedia");
			forbiddenNameSpaces.add("Datei");
			forbiddenNameSpaces.add("MediaWiki");
			forbiddenNameSpaces.add("Vorlage");
			forbiddenNameSpaces.add("Hilfe");
			forbiddenNameSpaces.add("Kategorie");
			forbiddenNameSpaces.add("Portal");
			forbiddenNameSpaces.add("Modul");
			forbiddenNameSpaces.add("Spezial");
			forbiddenNameSpaces.add("Medium");
			talkSuffix = "Diskussion";
			forbiddenInternalLinks.add("WP:");
			forbiddenInternalLinks.add("H:");
			forbiddenInternalLinks.add("P:");
			forbiddenInternalLinks.add("WD:");
			forbiddenInternalLinks.add("HD:");
			forbiddenInternalLinks.add("PD:");
		} else if (language == Language.NL) {
			forbiddenNameSpaces.add("Gebruiker");
			forbiddenNameSpaces.add("Wikipedia");
			forbiddenNameSpaces.add("Help");
			forbiddenNameSpaces.add("Bestand");
			forbiddenNameSpaces.add("Categorie");
			forbiddenNameSpaces.add("MediaWiki");
			forbiddenNameSpaces.add("Overleg");
			forbiddenNameSpaces.add("Sjabloon");
			forbiddenNameSpaces.add("Speciaal");
			forbiddenNameSpaces.add("Portaal");
			talkPrefix = "Overleg";
			forbiddenInternalLinks.add("WP:");
			forbiddenInternalLinks.add("H:");
		} else if (language == Language.PT) {
			forbiddenNameSpaces.add("Media");
			forbiddenNameSpaces.add("Especial");
			forbiddenNameSpaces.add("Discuss\u00e3o");
			forbiddenNameSpaces.add("Usu\u00e1rio");
			forbiddenNameSpaces.add("Wikip\u00e9dia");
			forbiddenNameSpaces.add("Ficheiro");
			forbiddenNameSpaces.add("MediaWiki");
			forbiddenNameSpaces.add("Predefini\u00e7\u00e3o");
			forbiddenNameSpaces.add("Ajuda");
			forbiddenNameSpaces.add("Categoria");
			forbiddenNameSpaces.add("Portal");
			forbiddenNameSpaces.add("Anexo");
			forbiddenNameSpaces.add("Imagem");
			forbiddenNameSpaces.add("Utilizador");
			talkSuffix = "Discuss\u00e3o";
			forbiddenInternalLinks.add("WP:");
			forbiddenInternalLinks.add("Discuss\u00e3o Portal:");
		} else if (language == Language.RU) {
			forbiddenNameSpaces.add("\u041e\u0431\u0441\u0443\u0436\u0434\u0435\u043d\u0438\u0435");
			forbiddenNameSpaces.add("\u0423\u0447\u0430\u0441\u0442\u043d\u0438\u043a");
			forbiddenNameSpaces.add("\u0412\u0438\u043a\u0438\u043f\u0435\u0434\u0438\u044f");
			forbiddenNameSpaces.add("\u0424\u0430\u0439\u043b");
			forbiddenNameSpaces.add("MediaWiki");
			forbiddenNameSpaces.add("\u0428\u0430\u0431\u043b\u043e\u043d");
			forbiddenNameSpaces.add("\u0421\u043f\u0440\u0430\u0432\u043a\u0430");
			forbiddenNameSpaces.add("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f");
			forbiddenNameSpaces.add("\u041f\u043e\u0440\u0442\u0430\u043b");
			forbiddenNameSpaces.add("\u0418\u043d\u043a\u0443\u0431\u0430\u0442\u043e\u0440");
			forbiddenNameSpaces.add("\u0418\u043d\u043a\u0443\u0431\u0430\u0442\u043e\u0440");
			forbiddenNameSpaces.add("\u041f\u0440\u043e\u0435\u043a\u0442");
			forbiddenNameSpaces.add("\u0410\u0440\u0431\u0438\u0442\u0440\u0430\u0436");
			forbiddenNameSpaces.add(
					"\u041e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u043f\u0440\u043e\u0433\u0440\u0430\u043c\u043c\u0430");
			forbiddenNameSpaces.add("\u041c\u043e\u0434\u0443\u043b\u044c");
			talkPrefix = "\u041e\u0431\u0441\u0443\u0436\u0434\u0435\u043d\u0438\u0435";
			forbiddenInternalLinks.add("WP:");
		} else if (language == Language.IT) {
			forbiddenNameSpaces.add("File");
			forbiddenNameSpaces.add("Wikipedia");
			forbiddenNameSpaces.add("Utente");
			forbiddenNameSpaces.add("MediaWiki");
			forbiddenNameSpaces.add("Speciale");
			forbiddenNameSpaces.add("Media");
			forbiddenNameSpaces.add("Aiuto");
			forbiddenNameSpaces.add("Categoria");
			forbiddenNameSpaces.add("Portale");
			forbiddenNameSpaces.add("Progetto");
			forbiddenNameSpaces.add("Modulo");
			talkPrefix = "Discussioni";
			forbiddenInternalLinks.add("WP:");
		} else if (language == Language.ES) {
			forbiddenNameSpaces.add("Usuario");
			forbiddenNameSpaces.add("Wikipedia");
			forbiddenNameSpaces.add("Archivo");
			forbiddenNameSpaces.add("MediaWiki");
			forbiddenNameSpaces.add("Plantilla");
			forbiddenNameSpaces.add("Ayuda");
			forbiddenNameSpaces.add("Categor\u00eda");
			forbiddenNameSpaces.add("Portal");
			forbiddenNameSpaces.add("Wikiproyecto");
			forbiddenNameSpaces.add("Anexo");
			forbiddenNameSpaces.add("Programa educativo");
			forbiddenNameSpaces.add("M\u00f3dulo");
			talkSuffix = "Discusi\u00f3n";
			forbiddenInternalLinks.add("WP:");
		} else if (language == Language.FR) {
			forbiddenNameSpaces.add("Utilisateur ");
			forbiddenNameSpaces.add("Mod\u00e8le");
			forbiddenNameSpaces.add("Projet");
			forbiddenNameSpaces.add("Wikip\u00e9dia");
			forbiddenNameSpaces.add("Aide");
			forbiddenNameSpaces.add("R\u00e9f\u00e9rence");
			forbiddenNameSpaces.add("Fichier");
			forbiddenNameSpaces.add("Cat\u00e9gorie");
			forbiddenNameSpaces.add("Module");
			talkPrefix = "Discussion";
			forbiddenInternalLinks.add("WT:");
			forbiddenInternalLinks.add("H:");
			forbiddenInternalLinks.add("CAT:");
		} else if (language == Language.EN) {
			forbiddenNameSpaces.add("Talk");
			forbiddenNameSpaces.add("User");
			forbiddenNameSpaces.add("Wikipedia");
			forbiddenNameSpaces.add("WP");
			forbiddenNameSpaces.add("File");
			forbiddenNameSpaces.add("MediaWiki");
			forbiddenNameSpaces.add("Template");
			forbiddenNameSpaces.add("Help");
			forbiddenNameSpaces.add("Category");
			forbiddenNameSpaces.add("Portal");
			forbiddenNameSpaces.add("Draft");
			forbiddenNameSpaces.add("Education Program");
			forbiddenNameSpaces.add("TimedText");
			forbiddenNameSpaces.add("Module");
			forbiddenNameSpaces.add("Topic");
			forbiddenNameSpaces.add("Special");
			forbiddenNameSpaces.add("Media");
			forbiddenNameSpaces.add("Image");
			talkSuffix = "talk";
			forbiddenInternalLinks.add("WT:");
			forbiddenInternalLinks.add("H:");
			forbiddenInternalLinks.add("CAT:");
		}
		for (String forbiddenNameSpace : forbiddenNameSpaces) {
			forbiddenInternalLinks.add(String.valueOf(forbiddenNameSpace) + ":");
			if (talkSuffix != null) {
				forbiddenInternalLinks.add(String.valueOf(forbiddenNameSpace) + " " + talkSuffix + ":");
				continue;
			}
			if (talkPrefix == null)
				continue;
			forbiddenInternalLinks.add(String.valueOf(talkPrefix) + " " + forbiddenNameSpace + ":");
			forbiddenInternalLinks.add(String.valueOf(talkPrefix) + " "
					+ forbiddenNameSpace.substring(0, 1).toLowerCase() + forbiddenNameSpace.substring(1) + ":");
		}
		return forbiddenInternalLinks;
	}

	public Set<String> getForbiddenLinks(Language language) {
		if (this.forbiddenLinks == null) {
			this.forbiddenLinks = new HashMap<Language, Set<String>>();
			this.forbiddenLinks.put(Language.EN, new HashSet<String>());
			this.forbiddenLinks.get(Language.EN).add("Wikipedia:Citation_needed");
			this.forbiddenLinks.put(Language.DE, new HashSet<String>());
			this.forbiddenLinks.get(Language.DE).add("Viertelgeviertstrich");
			this.forbiddenLinks.put(Language.PT, new HashSet<String>());
			this.forbiddenLinks.put(Language.NL, new HashSet<String>());
			this.forbiddenLinks.put(Language.RU, new HashSet<String>());
			this.forbiddenLinks.put(Language.FR, new HashSet<String>());
			this.forbiddenLinks.put(Language.IT, new HashSet<String>());
			this.forbiddenLinks.put(Language.ES, new HashSet<String>());
		}
		return this.forbiddenLinks.get(language);
	}

	public static Set<String> getTitlesOfParagraphsNotToTranslate(Language language) {
		HashSet<String> titlesNotToTranslate = new HashSet<String>();
		if (language == Language.DE) {
			titlesNotToTranslate.add("Ver\u00f6ffentlichungen");
			titlesNotToTranslate.add("Literatur");
			titlesNotToTranslate.add("Weblinks");
			titlesNotToTranslate.add("Einzelnachweise");
			titlesNotToTranslate.add("Werke");
			titlesNotToTranslate.add("Werke (Auswahl)");
		} else if (language == Language.NL) {
			titlesNotToTranslate.add("Zie ook");
			titlesNotToTranslate.add("Externe links");
			titlesNotToTranslate.add("Literatuur");
			titlesNotToTranslate.add("Bronnen, noten en referenties");
		} else if (language == Language.PT) {
			titlesNotToTranslate.add("Ver tamb\u00e9m");
			titlesNotToTranslate.add("Refer\u00eancias");
			titlesNotToTranslate.add("Liga\u00e7\u00f5es externas");
			titlesNotToTranslate.add("Notas");
			titlesNotToTranslate.add("Bibliografia");
		} else if (language == Language.EN) {
			titlesNotToTranslate.add("See also");
			titlesNotToTranslate.add("References");
			titlesNotToTranslate.add("External links");
			titlesNotToTranslate.add("Further reading");
			titlesNotToTranslate.add("Bibliography");
		} else if (language == Language.RU) {
			titlesNotToTranslate.add("\u0421\u043c. \u0442\u0430\u043a\u0436\u0435");
			titlesNotToTranslate.add("\u041f\u0440\u0438\u043c\u0435\u0447\u0430\u043d\u0438\u044f");
			titlesNotToTranslate.add("\u0412\u043d\u0435\u0448\u043d\u0438\u0435 \u0441\u0441\u044b\u043b\u043a\u0438");
			titlesNotToTranslate.add("\u0421\u0441\u044b\u043b\u043a\u0438");
			titlesNotToTranslate.add("\u0413\u0430\u043b\u0435\u0440\u0435\u044f");
		} else if (language == Language.FR) {
			titlesNotToTranslate.add("Voir aussi");
			titlesNotToTranslate.add("Annexes");
			titlesNotToTranslate.add("Notes et r\u00e9f\u00e9rences");
			titlesNotToTranslate.add("R\u00e9f\u00e9rences");
			titlesNotToTranslate.add("Compl\u00e9ments");
		} else if (language == Language.IT) {
			titlesNotToTranslate.add("Bibliografia");
			titlesNotToTranslate.add("Altri progetti");
			titlesNotToTranslate.add("Documentari");
			titlesNotToTranslate.add("Note");
			titlesNotToTranslate.add("Collegamenti esterni");
			titlesNotToTranslate.add("Voci correlate");
		} else if (language == Language.ES) {
			titlesNotToTranslate.add("V\u00e9ase tambi\u00e9n");
			titlesNotToTranslate.add("Referencias");
			titlesNotToTranslate.add("Bibliograf\u00eda");
			titlesNotToTranslate.add("Enlaces externos");
		}
		return titlesNotToTranslate;
	}

	public static String getTOCName(Language language) {
		System.out.println(language);
		if (language == Language.EN)
			return "Contents";
		else if (language == Language.DE)
			return "Inhaltsverzeichnis";
		else if (language == Language.NL)
			return "Inhoud";
		else if (language == Language.PT)
			return "\u00cdndice";
		else if (language == Language.RU)
			return "\u0421\u043e\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435";
		else if (language == Language.FR)
			return "Sommaire";
		else if (language == Language.ES)
			return "\u00cdndice";
		else if (language == Language.IT)
			return "Indice";

		throw new NullPointerException("No name for table of contents in that language.");
	}

	public static Set<String> getSeeAlsoLinkClasses(Language language) {
		HashSet<String> seeAlsoLinkClasses = new HashSet<String>();
		if (language == Language.DE) {
			seeAlsoLinkClasses.add("sieheauch");
			seeAlsoLinkClasses.add("hauptartikel");
		} else if (language == Language.EN) {
			// seeAlsoLinkClasses.add("seealso");
			// seeAlsoLinkClasses.add("mainarticle");
			seeAlsoLinkClasses.add("hatnote");
		} else if (language == Language.FR) {
			seeAlsoLinkClasses.add("bandeau-niveau-detail");
		} else if (language == Language.NL) {
			// won't work (is in "noprint" table structure)
		} else if (language == Language.PT) {
			seeAlsoLinkClasses.add("dablink");
		} else if (language == Language.RU) {
			seeAlsoLinkClasses.add("dablink");
		} else if (language != Language.IT && language == Language.ES) {
			seeAlsoLinkClasses.add("rellink");
		}

		return seeAlsoLinkClasses;
	}

	public static Set<String> getForbiddenImages() {
		Set<String> forbiddenImages = new HashSet<String>();

		// Source: http://wikimediafoundation.org/wiki/Wikimedia_trademarks

		forbiddenImages.add("Commons-logo.svg");
		forbiddenImages.add("WiktionaryEn.svg");
		forbiddenImages.add("Wiktionary-logo-en.svg");
		forbiddenImages.add("Wikiquote-logo.svg");
		forbiddenImages.add("WiktionaryEn.svg");
		forbiddenImages.add("Wikiquote-logo-en.svg");
		forbiddenImages.add("Wikibooks-logo.svg");
		forbiddenImages.add("Wikibooks-logo-en-noslogan.svg");
		forbiddenImages.add("Wikisource-logo.svg");
		forbiddenImages.add("Wikisource-newberg-de.png");
		forbiddenImages.add("Wikinews-logo.svg");
		forbiddenImages.add("WikiNews-Logo-en.svg");
		forbiddenImages.add("Wikiversity-logo.svg");
		forbiddenImages.add("Wikiversity-logo-en.svg");
		forbiddenImages.add("Wikispecies-logo.svg");
		forbiddenImages.add("WikiSpecies.svg");
		forbiddenImages.add("MediaWiki-notext.svg");
		forbiddenImages.add("MediaWiki.svg");
		forbiddenImages.add("Commons-logo.svg");
		forbiddenImages.add("Commons-logo-en.svg");
		forbiddenImages.add("Wikidata-logo.svg");
		forbiddenImages.add("Wikidata-logo-en.svg");
		forbiddenImages.add("Wikivoyage-Logo-v3-icon.svg");
		forbiddenImages.add("Wikivoyage-Logo-v3-en.svg");
		forbiddenImages.add("Incubator-notext");
		forbiddenImages.add("Incubator-text.svg");
		forbiddenImages.add("Wikimedia_labs_logo.svg");
		forbiddenImages.add("Wikimedia_labs_logo_with_text.svg");
		forbiddenImages.add("Wikimedia-logo.svg");
		forbiddenImages.add("Wmf_logo_vert_pms.svg");
		forbiddenImages.add("Wikimania.svg");
		forbiddenImages.add("Wikimania_logo_with_text_2.svg");

		// Others
		// TODO: Continue...

		forbiddenImages.add("Ambox_important.svg");
		forbiddenImages.add("Question_book.svg");
		forbiddenImages.add("Portal_icon.svg");

		return forbiddenImages;
	}

	public static String getWikiImageUrlBegin() {
		return "//upload.wikimedia.org";
	}
}
