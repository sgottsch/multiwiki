package de.l3s.extractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;

public class HTMLParagraph {

	public enum Type {
		PBLOCK, STARTPARAGRAPH, H1PARAGRAPH, H2PARAGRAPH, H3PARAGRAPH, H4PARAGRAPH, H5PARAGRAPH, H6PARAGRAPH, TABLE, LIST, LISTELEMENT, TABLEROW, TABLECELL, TITLE, TABLEHEADERROW, TABLEHEADERCELL, OL_LIST, TOC, IMAGE_DIV, STARTPARAGRAPH_TOC, SEE_ALSO, IMAGE_DIV_CAPTION, INFOBOX, ARTICLE_TITLE;
	}

	private Segment segment;

	private Type type;

	private List<HTMLParagraph> subParagraphs;

	private String content;

	private boolean important = false;

	private Segment contentSegment;

	private String title;

	private HTMLParagraph titleParagraph;

	private Set<String> imageUrls;
	
	private boolean inInfobox = false;

	public HTMLParagraph(Segment segment, Type type) {
		this.segment = segment;
		this.type = type;
	}

	public HTMLParagraph(Element segment, Type type) {
		this.segment = segment;
		this.type = type;
		this.contentSegment = segment.getContent();
	}

	public HTMLParagraph getTitleParagraph() {
		return titleParagraph;
	}

	public Segment getContentSegment() {
		return contentSegment;
	}

	public void setContentSegment(Segment contentSegment) {
		this.contentSegment = contentSegment;
	}

	public void setTitleParagraph(HTMLParagraph titleParagraph) {
		this.titleParagraph = titleParagraph;
	}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getBegin() {
		return segment.getBegin();
	}

	public int getEnd() {
		return segment.getEnd();
	}

	public int getContentBegin() {
		return contentSegment.getBegin();
	}

	public int getContentEnd() {
		return contentSegment.getEnd();
	}

	public List<HTMLParagraph> getSubParagraphs() {
		return subParagraphs;
	}

	public void setSubParagraphs(List<HTMLParagraph> subParagraphs) {
		this.subParagraphs = subParagraphs;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void addSubParagraph(HTMLParagraph subParagraph) {
		if (this.subParagraphs == null)
			this.subParagraphs = new ArrayList<HTMLParagraph>();
		this.subParagraphs.add(subParagraph);
	}

	public void addSubParagraphs(List<HTMLParagraph> subParagraphs) {
		if (this.subParagraphs == null)
			this.subParagraphs = new ArrayList<HTMLParagraph>();
		this.subParagraphs.addAll(subParagraphs);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	public Set<String> getImageUrls() {
		return this.imageUrls;
	}

	public void setImageUrls(Set<String> imageUrls) {
		this.imageUrls = imageUrls;
	}

	public void addImageUrl(String imageUrl) {
		if (this.imageUrls == null)
			imageUrls = new HashSet<String>();
		imageUrls.add(imageUrl);
	}

	public boolean isInInfobox() {
		return inInfobox;
	}

	public void setInInfobox(boolean inInfobox) {
		this.inInfobox = inInfobox;
	}

}
