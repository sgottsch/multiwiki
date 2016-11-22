package de.l3s.extractor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;

import de.l3s.db.tables.Paragraph_DB;
import de.l3s.db.tables.Revision_DB;
import de.l3s.db.tables.Sentence_DB;
import de.l3s.db.tables.Sentence_ExternalLink_DB;
import de.l3s.db.tables.Sentence_InternalLink_DB;
import de.l3s.nlp.LuceneUtils;
import de.l3s.translate.Language;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

public class AnnotationExtractor extends SingleLanguageExtractor {

	// private Paragraph paragraph;
	private HTMLParagraph htmlParagraph;

	private ArrayList<Sentence_DB> annotations;

	private Revision_DB revision;
	private LuceneUtils luceneUtils;

	private AnnotationExtractorData data;

	public AnnotationExtractor(AnnotationExtractorData data, Paragraph_DB paragraph, HTMLParagraph htmlParagraph,
			ExtractionDataStore dataStore) {

		// Important: At first, build the annotations, THEN correct their
		// sentence numbers in the paragraph extractor and then extract internal
		// links etc.

		super(data.getDatabase(), paragraph.getRevision().getArticle().getLanguage(), data.getLanguages(), dataStore);
		this.htmlParagraph = htmlParagraph;
		this.revision = data.getRevision();
		this.luceneUtils = this.config.getLuceneUtils();
		this.data = data;
	}

	public AnnotationExtractor(String database, Language language, List<Language> languages,
			ExtractionDataStore dataStore) {
		super(database, language, languages, dataStore);
	}

	public List<Sentence_DB> buildAnnotations() {

		annotations = new ArrayList<Sentence_DB>();

		if (this.htmlParagraph.getType() == HTMLParagraph.Type.TITLE
				|| this.htmlParagraph.getType() == HTMLParagraph.Type.ARTICLE_TITLE) {
			Sentence_DB titleAnnotation = this.buildAnnotationsForTitleParagraph();
			titleAnnotation.setToTranslate(this.htmlParagraph.isImportant());
			this.annotations.add(titleAnnotation);
		}

		if (this.htmlParagraph.getSubParagraphs() == null && this.htmlParagraph.getContent() != null) {
			Set<Sentence_DB> sentences = this.buildSentenceAnnotations();
			this.annotations.addAll(sentences);
			for (Sentence_DB sentence : sentences) {
				sentence.setToTranslate(this.htmlParagraph.isImportant());
			}
		}

		return this.annotations;
	}

	/**
	 * Creates an annotation for every sentence in the text in the given
	 * paragraph.
	 */
	private Set<Sentence_DB> buildSentenceAnnotations() {

		Set<Sentence_DB> sentenceAnnotations = new LinkedHashSet<Sentence_DB>();

		String text = this.htmlParagraph.getContent();

		// sometimes, an element e.g. just contains of an image and has no text
		// -> ignore that here

		if (this.makeSentenceRaw(text).trim().isEmpty()) {
			return sentenceAnnotations;
		}

		String htmlLine = text;

		// Remove everything within a tag that is not the tag name itself
		// (Example: <span id="test"> becomes <span>). But: Memorize the
		// removed strings to re-insert them after the sentence splitting.
		Matcher matcher = this.config.getPatternTagAttributeRemoval().matcher(text);
		StringBuffer sb = new StringBuffer(text.length());

		int tagIdentifier = 0;

		Map<Integer, String> tags = new HashMap<Integer, String>();

		while (matcher.find()) {
			tags.put(tagIdentifier, matcher.group(2));
			// To reassign the removed string to the tag again: Append a
			// number to the tag name representing the removed string in the
			// "tags" hash map (Example: <span id="test"> indeed becomes
			// <span2> with 2-> id="test")
			matcher.appendReplacement(sb, "<" + matcher.group(1) + tagIdentifier + ">");
			tagIdentifier += 1;
		}
		matcher.appendTail(sb);
		text = sb.toString();

		String[] sentencesTmp = this.config.getLangTool(this.language).sentenceSplitterAndJoinSmallOnes(text, 12);

		// never break <a> tags!
		List<String> sentencesList = new ArrayList<String>();

		String current = "";
		for (String s : sentencesTmp) {

			if (current.isEmpty())
				current = s;
			else
				current = current + " " + s;

			Matcher aTagMatcher = config.getATagPattern().matcher(current);

			int numberOfOpeningATags = 0;
			while (aTagMatcher.find())
				numberOfOpeningATags++;

			Matcher aEndTagMatcher = config.getAEndTagPattern().matcher(current);

			int numberOfClosingATags = 0;
			while (aEndTagMatcher.find())
				numberOfClosingATags++;

			if (numberOfOpeningATags == numberOfClosingATags) {
				sentencesList.add(current);
				current = "";
			}
			// else: sentences with number of opening != number of closing a
			// tags are joined
		}

		if (!current.isEmpty())
			sentencesList.add(current);

		String[] sentences = sentencesList.toArray(new String[sentencesList.size()]);

		int htmlPositionOfTheLine = this.htmlParagraph.getContentBegin();

		for (String sentence : sentences) {

			// rawSentence: text only, sentence: text with tags,
			// htmlSentence: text with tags and attributes

			String rawSentence = makeSentenceRaw(sentence);

			if (rawSentence.isEmpty())
				continue;

			// Reinsert removed HTML attributes
			Matcher matcherHtmlReinsert = config.getPatternHtmlReinsert().matcher(sentence);
			StringBuffer sb2 = new StringBuffer(sentence.length());

			while (matcherHtmlReinsert.find()) {
				int tagIdentifier2 = Integer.parseInt(matcherHtmlReinsert.group(2));
				try {
					matcherHtmlReinsert.appendReplacement(sb2,
							"<" + matcherHtmlReinsert.group(1) + tags.get(tagIdentifier2) + ">");
				} catch (IllegalArgumentException e) {
					System.err.println("Unknown problem while parsing sentences. Ignore sentence and continue.");
				}
			}
			matcherHtmlReinsert.appendTail(sb2);
			String htmlSentence = sb2.toString();

			// Find out the position of the text in the whole article
			int positionOfSentenceInLine = htmlLine.indexOf(htmlSentence);

			int startPosition = positionOfSentenceInLine + htmlPositionOfTheLine;
			int endPosition = positionOfSentenceInLine + htmlSentence.length() + htmlPositionOfTheLine;

			String stemmed;
			if (language == Language.EN)
				stemmed = luceneUtils.stemmedEnglishText(rawSentence);
			else
				stemmed = "NULL";

			String annotationType;
			switch (htmlParagraph.getType()) {
			case PBLOCK:
				annotationType = Sentence_DB.SENTENCE_TYPE;
				break;
			case H1PARAGRAPH:
				annotationType = Sentence_DB.IN_PARAGRAPH_TYPE;
				break;
			case H2PARAGRAPH:
				annotationType = Sentence_DB.IN_PARAGRAPH_TYPE;
				break;
			case H3PARAGRAPH:
				annotationType = Sentence_DB.IN_PARAGRAPH_TYPE;
				break;
			case H4PARAGRAPH:
				annotationType = Sentence_DB.IN_PARAGRAPH_TYPE;
				break;
			case H5PARAGRAPH:
				annotationType = Sentence_DB.IN_PARAGRAPH_TYPE;
				break;
			case H6PARAGRAPH:
				annotationType = Sentence_DB.IN_PARAGRAPH_TYPE;
				break;
			case IMAGE_DIV_CAPTION:
				annotationType = Sentence_DB.IMAGE_DESCRIPTION_TYPE;
				break;
			case LISTELEMENT:
				annotationType = Sentence_DB.LIST_ELEMENT_TYPE;
				break;
			case SEE_ALSO:
				annotationType = Sentence_DB.SEE_ALSO_TYPE;
				break;
			case TABLECELL:
				annotationType = Sentence_DB.TABLE_CELL_TYPE;
				break;
			case TABLEHEADERCELL:
				annotationType = Sentence_DB.TABLE_HEADER_CELL_TYPE;
				break;
			case TITLE:
				annotationType = Sentence_DB.TITLE_TYPE;
				break;
			case IMAGE_DIV:
				annotationType = Sentence_DB.IMAGE_DESCRIPTION_TYPE;
				break;
			default:
				annotationType = Sentence_DB.MISC_TYPE;
			}

			Sentence_DB annotation = new Sentence_DB(revision, 0, annotationType, startPosition, endPosition,
					htmlSentence, rawSentence, "NULL", "NULL", stemmed);

			annotation.setInInfobox(htmlParagraph.isInInfobox());

			sentenceAnnotations.add(annotation);

			htmlLine = htmlLine.substring(positionOfSentenceInLine + htmlSentence.length());

			htmlPositionOfTheLine += positionOfSentenceInLine + htmlSentence.length();
		}

		return sentenceAnnotations;

	}

	private Sentence_DB buildAnnotationsForTitleParagraph() {
		int startPosition = htmlParagraph.getContentBegin();
		int endPosition = htmlParagraph.getContentEnd();

		String htmlSentence = htmlParagraph.getContentSegment().toString();

		String rawSentence = makeSentenceRaw(htmlSentence);

		String stemmed;
		if (language == Language.EN)
			stemmed = luceneUtils.stemmedEnglishText(rawSentence);
		else
			stemmed = "NULL";

		String annotationType = Sentence_DB.TITLE_TYPE;

		if (htmlParagraph.getType() == HTMLParagraph.Type.ARTICLE_TITLE)
			annotationType = Sentence_DB.ARTICLE_TITLE_TYPE;

		Sentence_DB annotation = new Sentence_DB(revision, 0, annotationType, startPosition, endPosition, htmlSentence,
				rawSentence, "NULL", "NULL", stemmed);

		annotation.setInInfobox(htmlParagraph.isInInfobox());

		return annotation;
	}

	/**
	 * Removes HTML tags and footnotes (e.g. "[3]") from string
	 * 
	 * @param sentence
	 * @return
	 */
	public String makeSentenceRaw(String sentence) {
		String rawSentence = Jsoup.parse(sentence).text();

		// Remove footnotes
		Matcher m = config.getFootnotePattern().matcher(rawSentence);
		rawSentence = m.replaceAll("");

		// Remove wikipedia notes
		if (language == Language.EN) {
			Matcher m2 = config.getWikiNotePattern().matcher(rawSentence);
			rawSentence = m2.replaceAll("");
		}

		return rawSentence.trim();
	}

	public void extractInformation() {
		for (Sentence_DB annotation : this.annotations) {
			this.loadLinks(annotation);
		}
	}

	public void loadInternalLinks(Sentence_DB annotation) {
		this.loadLinks(annotation, true, false);
	}

	public void loadLinks(Sentence_DB annotation) {
		this.loadLinks(annotation, true, true);
	}

	public void loadLinks(Sentence_DB annotation, boolean internal, boolean external) {
		Source source = new Source(annotation.getValue(Sentence_DB.original_text_html_attr).toString());

		source.fullSequentialParse();

		List<Element> links = source.getAllElements(HTMLElementName.A);

		for (Element htmlLink : links) {

			String linkText = htmlLink.getAttributeValue("href");

			if (linkText == null)
				continue;

			int start = htmlLink.getBegin();
			int end = htmlLink.getEnd();

			if (internal && linkText.startsWith("/wiki")) {

				String lastPart = linkText.substring(linkText.lastIndexOf('/') + 1, linkText.length());

				try {
					// Transform e.g. "Departamento_R%C3%ADo_Senguer" to
					// "Departamento_Río_Senguer"
					lastPart = URLDecoder.decode(lastPart, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				Sentence_InternalLink_DB annotationInternalLink = new Sentence_InternalLink_DB(annotation, lastPart,
						language, start, end);
				dataStore.addInternalLink(annotationInternalLink);

			}

			else if (external && linkText.startsWith("#cite_note")) {

				String footnoteId = linkText.substring("#cite_note-".length(), linkText.length());

				if (data.getFootnoteIds().containsKey(footnoteId)) {

					String externalLinkName = data.getFootnoteIds().get(footnoteId);

					// cut (because of DB constraints)
					if (externalLinkName.length() > 252)
						externalLinkName = externalLinkName.substring(0, 250) + "...";

					Sentence_ExternalLink_DB annotationExternalLink = new Sentence_ExternalLink_DB(annotation,
							externalLinkName, start, end);
					dataStore.addExternalLink(annotationExternalLink);
				}

			}

		}
	}

	public List<Sentence_DB> getAnnotations() {
		return this.annotations;
	}

}
