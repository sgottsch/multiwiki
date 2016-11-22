package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Given from the Wikipedia markup, it is possible to construct a bottom-up
 * paragraph structure from the article. Each paragraph has a title, sentences
 * and a reference to its containing paragraph.
 * 
 * This has nothing to do with the paragraphs constructing during the paragraph
 * alignment process.
 */
public class WikiParagraph {
	private int id;
	private List<Sentence> annotations;
	private Sentence titleAnnotation;
	private WikiParagraph aboveParagraph;
	private List<WikiParagraph> containedParagraphs;
	private Integer topParagraphId;
	private ParagraphType paragraphType;
	private int startPosition;
	private int endPosition;
	private int startPositionContent;
	private int endPositionContent;
	private TimeInterval timeInterval;

	public WikiParagraph(int paragraphId, int topParagraphId, String paragraphType, int startPosition, int endPosition,
			int startPositionContent, int endPositionContent) {
		this.id = paragraphId;
		this.topParagraphId = topParagraphId;
		this.paragraphType = ParagraphType.valueOf(paragraphType);
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.startPositionContent = startPositionContent;
		this.endPositionContent = endPositionContent;
		this.containedParagraphs = new ArrayList<WikiParagraph>();
	}

	public WikiParagraph(int paragraphId, Integer topParagraphId, String paragraphType, int startPosition,
			int endPosition) {
		this.id = paragraphId;
		this.topParagraphId = topParagraphId;
		this.paragraphType = ParagraphType.valueOf(paragraphType);
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.containedParagraphs = new ArrayList<WikiParagraph>();
	}

	public WikiParagraph(int paragraphId, Integer topParagraphId, String paragraphType) {
		this.id = paragraphId;
		this.topParagraphId = topParagraphId;
		this.paragraphType = ParagraphType.valueOf(paragraphType);
	}

	public List<Sentence> getAnnotations() {
		return this.annotations;
	}

	public void setAnnotations(List<Sentence> annotations) {
		this.annotations = annotations;
	}

	public void addAnnotation(Sentence annotation) {
		if (this.annotations == null) {
			this.annotations = new ArrayList<Sentence>();
		}
		this.annotations.add(annotation);
	}

	public Sentence getTitleAnnotation() {
		return this.titleAnnotation;
	}

	public void setTitleAnnotation(Sentence titleAnnotation) {
		this.titleAnnotation = titleAnnotation;
	}

	public WikiParagraph getAboveParagraph() {
		return this.aboveParagraph;
	}

	public void setAboveParagraph(WikiParagraph aboveParagraph) {
		this.aboveParagraph = aboveParagraph;
		if (aboveParagraph != null) {
			aboveParagraph.addContainedParagraph(this);
		}
	}

	public int getId() {
		return this.id;
	}

	public Integer getTopParagraphId() {
		return this.topParagraphId;
	}

	public ParagraphType getParagraphType() {
		return this.paragraphType;
	}

	public int getStartPosition() {
		return this.startPosition;
	}

	public int getEndPosition() {
		return this.endPosition;
	}

	public int getStartPositionContent() {
		return this.startPositionContent;
	}

	public int getEndPositionContent() {
		return this.endPositionContent;
	}

	public String getIdString() {
		return String.valueOf(this.startPosition) + "-" + this.endPosition;
	}

	private void addContainedParagraph(WikiParagraph wikiParagraph) {
		this.containedParagraphs.add(wikiParagraph);
	}

	public List<WikiParagraph> getContainedParagraphs() {
		return this.containedParagraphs;
	}

	public TimeInterval getTimeInterval() {
		return this.timeInterval;
	}

	public void setTimeInterval(TimeInterval timeInterval) {
		this.timeInterval = timeInterval;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	public static enum ParagraphType {
		PBLOCK, STARTPARAGRAPH, H1PARAGRAPH, H2PARAGRAPH, H3PARAGRAPH, H4PARAGRAPH, H5PARAGRAPH, H6PARAGRAPH, TABLE, LIST, LISTELEMENT, TABLEROW, TABLECELL, TITLE, TABLEHEADERROW, TABLEHEADERCELL, OL_LIST, TOC, IMAGE_DIV, STARTPARAGRAPH_TOC, SEE_ALSO, IMAGE_DIV_CAPTION, INFOBOX, ARTICLE_TITLE;
	}

}
