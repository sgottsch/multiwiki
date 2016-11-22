package extractor;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import nlp.LuceneUtils;
import nlp.OpenNLPutils;
import translate.Language;
import wiki.WikiWords;

public class ExtractionConfigThreadSafe {

	private static ExtractionConfigThreadSafe instance;

	private Map<Language, Set<String>> forbiddenInternalLinks;
	private Map<Language, Set<String>> titlesOfParagraphsNotToTranslate;
	private Map<Language, Set<String>> seeAlsoLinkClasses;
	private Map<Language, String> tocName;

	private LuceneUtils luceneUtils;
	private Pattern footnotePattern;
	private Pattern aTagPattern;
	private Pattern patternHtmlReinsert;
	private Pattern patternTagAttributeRemoval;
	private Pattern aEndTagPattern;
	private Pattern wikiNotePattern;

	private Map<Language, OpenNLPutils> langTools;

	private Pattern footnotePatternHtmlEn;
	private Pattern footnotePatternHtmlDe;

	private Pattern wikiNotePatternHtml;

	private Pattern titlePatternHtml;

	private Pattern specialCharPatternHtml;

	private ExtractionConfigThreadSafe() {
	}

	public static ExtractionConfigThreadSafe getInstance() {
		if (instance == null) {
			instance = new ExtractionConfigThreadSafe();
		}
		return instance;
	}

	public void init(Collection<Language> languages) {
		Set<Language> languagesSet = new HashSet<Language>();
		languagesSet.addAll(languages);
		init(languagesSet);
	}

	public void init(Set<Language> languages) {
		forbiddenInternalLinks = new HashMap<Language, Set<String>>();
		for (Language language : languages)
			this.forbiddenInternalLinks.put(language, WikiWords.getInstance().getForbiddenInternalLinks(language));

		titlesOfParagraphsNotToTranslate = new HashMap<Language, Set<String>>();
		for (Language language : languages)
			this.titlesOfParagraphsNotToTranslate.put(language,
					WikiWords.getTitlesOfParagraphsNotToTranslate(language));

		seeAlsoLinkClasses = new HashMap<Language, Set<String>>();
		for (Language language : languages)
			this.seeAlsoLinkClasses.put(language, WikiWords.getSeeAlsoLinkClasses(language));

		tocName = new HashMap<Language, String>();
		for (Language language : languages)
			this.tocName.put(language, WikiWords.getTOCName(language));

		luceneUtils = new LuceneUtils();

		String footnoteRegex = "\\[\\d{1,3}\\]";
		footnotePattern = Pattern.compile(footnoteRegex);

		String wikiNoteRegex = "\\[([a-zA-Z]+ needed|note \\d{1,3})\\]";
		wikiNotePattern = Pattern.compile(wikiNoteRegex);

		String wikiNoteRegexHtml = "\\[[^\\]]*([a-zA-Z]+ needed|note \\d{1,3})[^\\[]*\\]";
		wikiNotePatternHtml = Pattern.compile(wikiNoteRegexHtml);

		String wikiTitleRegex = "\\<h\\d{1,6}\\>.+\\</h\\d{1,6}\\>"; // [^>]+
		titlePatternHtml = Pattern.compile(wikiTitleRegex);

		String wikispecialCharRegex = "\\&\\#.{3}\\;";
		specialCharPatternHtml = Pattern.compile(wikispecialCharRegex);

		aTagPattern = Pattern.compile("<a[0-9]+>");

		String regexHtmlReinsert = "<([a-zA-Z]*?)(\\d{1,3})>";
		patternHtmlReinsert = Pattern.compile(regexHtmlReinsert);

		String regexTagAttributeRemoval = "<([a-zA-Z]*?)( .*?)>";
		patternTagAttributeRemoval = Pattern.compile(regexTagAttributeRemoval);

		aEndTagPattern = Pattern.compile("</a>");

		langTools = new HashMap<Language, OpenNLPutils>();
		for (Language language : languages) {
			try {
				OpenNLPutils langTool = new OpenNLPutils(language);
				langTools.put(language, langTool);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		for (Language language : languages) {
			// TODO: Is this used? Add more languages.
			if (language != Language.DE && this.footnotePatternHtmlEn == null) {
				String footnoteRegexHtml = "\\[</span>\\d{1,3}<span>\\]";
				footnotePatternHtmlEn = Pattern.compile(footnoteRegexHtml);
			} else if (language == Language.DE && this.footnotePatternHtmlDe == null) {
				String footnoteRegexHtmlDe = "\\[\\d{1,3}\\]";
				footnotePatternHtmlDe = Pattern.compile(footnoteRegexHtmlDe);
			}
		}

	}

	public Set<String> getForbiddenInternalLinks(Language language) {
		return this.forbiddenInternalLinks.get(language);
	}

	public Set<String> getTitlesOfParagraphsNotToTranslate(Language language) {
		return this.titlesOfParagraphsNotToTranslate.get(language);
	}

	public Set<String> getSeeAlsoLinkClasses(Language language) {
		return this.seeAlsoLinkClasses.get(language);
	}

	public String getTOCName(Language language) {
		return this.tocName.get(language);
	}

	public LuceneUtils getLuceneUtils() {
		return luceneUtils;
	}

	public Pattern getFootnotePattern() {
		return footnotePattern;
	}

	public Pattern getFootnotePatternHtml(Language language) {
		// TODO (see above): Is this used? Add more languages.
		if (language != Language.DE)
			return footnotePatternHtmlEn;
		else
			return footnotePatternHtmlDe;
	}

	public Pattern getWikiNotePattern() {
		return wikiNotePattern;
	}

	public Pattern getWikiNotePatternHtml() {
		return wikiNotePatternHtml;
	}

	public Pattern getTitlePatternHtml() {
		return titlePatternHtml;
	}

	public Pattern getSpecialCharPatternHtml() {
		return specialCharPatternHtml;
	}

	public Pattern getATagPattern() {
		return aTagPattern;
	}

	public Pattern getPatternHtmlReinsert() {
		return patternHtmlReinsert;
	}

	public Pattern getPatternTagAttributeRemoval() {
		return patternTagAttributeRemoval;
	}

	public Pattern getAEndTagPattern() {
		return aEndTagPattern;
	}

	public OpenNLPutils getLangTool(Language language) {
		return langTools.get(language);
	}

	public String getWikiImageUrlBegin() {
		return WikiWords.getWikiImageUrlBegin();
	}

}
