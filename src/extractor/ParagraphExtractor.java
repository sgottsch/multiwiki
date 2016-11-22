package extractor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import app.Configuration;
import db.tables.Article_DB;
import db.tables.Paragraph_DB;
import db.tables.Paragraph_Image_DB;
import db.tables.Revision_DB;
import db.tables.Revision_ExternalLink_DB;
import db.tables.Revision_Image_DB;
import db.tables.Revision_InternalLink_DB;
import db.tables.Sentence_DB;
import extractor.HTMLParagraph.Type;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import translate.Language;
import util.ParseUtil;
import util.WikiUtil;
import wiki.WikiTextGetter;

public class ParagraphExtractor extends SingleLanguageExtractor {

	// HTML objects
	private Source source;
	private HTMLParagraph topParagraph;
	private HTMLParagraph infoboxParagraph;
	private List<HTMLParagraph> contentParagraphs;

	// Database objects
	private LinkedHashSet<Paragraph_DB> paragraphs;

	private Set<Sentence_DB> annotations;

	private Map<String, List<Integer>> bordersOfHSegments;
	private Set<Integer> listsInTables;

	private int paragraphCounter;

	private ArrayList<Integer> allBorders;
	private boolean currentBlocksAreImportant = true;

	private Revision_DB revision;
	private Pattern footnotePattern;
	private Pattern wikiNotePattern;

	private Map<Paragraph_DB, HTMLParagraph> dbtoHtmlParagraphs;

	public ParagraphExtractor(String database, Revision_DB revision, List<Language> languages,
			ExtractionDataStore dataStore) {
		super(database, revision.getArticle().getLanguage(), languages, dataStore);
		this.revision = revision;
		this.listsInTables = new HashSet<Integer>();

		contentParagraphs = new ArrayList<HTMLParagraph>();
		paragraphCounter = 0;

		String footnoteRegex = "\\[\\d{1,3}\\]";
		this.footnotePattern = Pattern.compile(footnoteRegex);
		this.wikiNotePattern = ExtractionConfigThreadSafe.getInstance().getWikiNotePattern();
	}

	public void loadLinksOfRevision() {
		List<Element> links = source.getAllElements(HTMLElementName.A);

		Map<String, Revision_InternalLink_DB> foundInternalLinks = new HashMap<String, Revision_InternalLink_DB>();
		Map<String, Revision_ExternalLink_DB> foundExternalLinks = new HashMap<String, Revision_ExternalLink_DB>();

		for (Element htmlLink : links) {

			String linkText = htmlLink.getAttributeValue("href");
			String linkClass = htmlLink.getAttributeValue("class");

			if (linkText == null)
				continue;

			if (linkText.startsWith("/wiki")) {

				String lastPart = linkText.substring(linkText.lastIndexOf('/') + 1, linkText.length());

				if (foundInternalLinks.containsKey(lastPart)) {
					foundInternalLinks.get(lastPart).incrementNumberOfOccurences();
				} else {

					if (this.language == Language.RU && this.language == Language.RU && lastPart.contains("#")) {
						lastPart = lastPart.substring(0, lastPart.indexOf("#"));
					}
					try {
						lastPart = URLDecoder.decode(lastPart, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					Revision_InternalLink_DB revisionInternalLink = new Revision_InternalLink_DB(revision, lastPart,
							language);
					foundInternalLinks.put(lastPart, revisionInternalLink);
					dataStore.addRevisionInternalLink(revisionInternalLink);
				}

			} else if (linkClass != null && linkClass.contains("external")) {
				if (foundExternalLinks.containsKey(linkText)) {
					foundExternalLinks.get(linkText).incrementNumberOfOccurences();
				} else {

					// cut (because of DB constraints)
					if (linkText.length() > 252)
						linkText = linkText.substring(0, 250) + "...";

					Revision_ExternalLink_DB revisionExternalLink = new Revision_ExternalLink_DB(revision, linkText);
					foundExternalLinks.put(linkText, revisionExternalLink);
					dataStore.addRevisionExternalLink(revisionExternalLink);
				}
			}
		}
	}

	public Set<String> loadImagesOfRevision() {

		Map<String, Revision_Image_DB> foundImages = new HashMap<String, Revision_Image_DB>();

		List<Element> images = source.getAllElements(HTMLElementName.IMG);

		for (Element htmlImage : images) {

			String imageName = htmlImage.getAttributeValue("src");

			if (imageName == null)
				continue;

			if (imageName.startsWith(config.getWikiImageUrlBegin())) {

				String imageUrl = WikiUtil.getUrlOfThumb(imageName);
				if (imageUrl == null)
					continue;

				if (imageUrl.length() > 252)
					imageUrl = imageUrl.substring(0, 250) + "...";

				if (foundImages.containsKey(imageUrl)) {
					foundImages.get(imageUrl).incrementNumberOfOccurences();
				} else {
					Revision_Image_DB revision_image = new Revision_Image_DB(revision, imageUrl);
					dataStore.addRevisionImage(revision_image);
					foundImages.put(imageUrl, revision_image);
				}
			}
		}

		return foundImages.keySet();
	}

	private List<HTMLParagraph> findParagraphs(Segment segment, int level) {

		List<HTMLParagraph> ps = new ArrayList<HTMLParagraph>();

		if (level == 7)
			return ps;

		Type dbType = null;
		String htmlType = null;

		switch (level) {
		case 1:
			dbType = HTMLParagraph.Type.H1PARAGRAPH;
			htmlType = HTMLElementName.H1;
			break;
		case 2:
			dbType = HTMLParagraph.Type.H2PARAGRAPH;
			htmlType = HTMLElementName.H2;
			break;
		case 3:
			dbType = HTMLParagraph.Type.H3PARAGRAPH;
			htmlType = HTMLElementName.H3;
			break;
		case 4:
			dbType = HTMLParagraph.Type.H4PARAGRAPH;
			htmlType = HTMLElementName.H4;
			break;
		case 5:
			dbType = HTMLParagraph.Type.H5PARAGRAPH;
			htmlType = HTMLElementName.H5;
			break;
		case 6:
			dbType = HTMLParagraph.Type.H6PARAGRAPH;
			htmlType = HTMLElementName.H6;
			break;
		}

		List<Element> hLines = segment.getAllElements(htmlType);

		List<HTMLParagraph> blockParagraphs = new ArrayList<HTMLParagraph>();

		if (htmlType == HTMLElementName.H1 && blockParagraphs.isEmpty()) {

		}

		for (Element hLine : hLines) {

			List<HTMLParagraph> subParagraphs = new ArrayList<HTMLParagraph>();

			// Identify title and create title paragraph
			HTMLParagraph titleParagraph = buildTitleParagraph(hLine, dbType, htmlType);

			String title = null;

			if (titleParagraph != null && titleParagraph.getContentSegment() != null) {
				title = makeSentenceRaw(titleParagraph.getContentSegment().toString());
				if (config.getTitlesOfParagraphsNotToTranslate(language).contains(title)) {
					currentBlocksAreImportant = false;
					titleParagraph.setImportant(false);
				}
			}

			HTMLParagraph startParagraph = null;

			int innerEnd = getInnerEnd(hLine);
			int outerEnd = getOuterEnd(htmlType, hLine);
			Segment contentSegment = null;
			if (innerEnd > hLine.getEnd() + 1) {
				// otherwise, there is no content segment (text continues
				// directly with some other <h> title
				contentSegment = new Segment(source, hLine.getEnd(), innerEnd);
				startParagraph = new HTMLParagraph(contentSegment, HTMLParagraph.Type.STARTPARAGRAPH);
				startParagraph.setImportant(currentBlocksAreImportant);
				contentParagraphs.add(startParagraph);
				subParagraphs.add(startParagraph);
				// topParagraphs.add(startParagraph);

				List<HTMLParagraph> imageBoxes = identifyInnerImageBoxes(startParagraph);
				List<HTMLParagraph> seeAlsoLinks = identifyInnerSeeAlsoLinks(startParagraph);

				startParagraph.addSubParagraphs(seeAlsoLinks);
				startParagraph.addSubParagraphs(imageBoxes);

				// contentParagraphs.addAll(imageBoxes);
			}

			HTMLParagraph blockParagraph = null;

			Segment outerSegment = new Segment(source, hLine.getBegin(), outerEnd);

			blockParagraph = new HTMLParagraph(outerSegment, dbType);
			blockParagraph.setImportant(currentBlocksAreImportant);

			if (title != null && config.getTOCName(language).equals(title.trim())) {
				blockParagraph.setType(HTMLParagraph.Type.TOC);
				blockParagraph.setImportant(false);
				if (startParagraph != null) {
					startParagraph.setImportant(false);
					startParagraph.setType(HTMLParagraph.Type.STARTPARAGRAPH_TOC);
				}
			}

			if (topParagraph == null)
				topParagraph = blockParagraph;

			blockParagraphs.add(blockParagraph);

			if (titleParagraph != null) {
				blockParagraph.setTitleParagraph(titleParagraph);
			}
			if (outerSegment != null) {
				// Identify sub paragraphs
				if (!blockParagraph.getType().equals(HTMLParagraph.Type.TOC))
					subParagraphs.addAll(findParagraphs(outerSegment, level + 1));
				blockParagraph.setSubParagraphs(subParagraphs);
			}

		}

		return blockParagraphs;
	}

	/**
	 * Finds the infobox (if there is one) as an HTML paragraph. This meethod is
	 * not doing more (going deeper into the infobox etc.).
	 */
	private void loadInfoboxParagraph() {
		Element infobox = source.getFirstElementByClass("infobox");

		if (infobox != null) {
			infoboxParagraph = new HTMLParagraph(infobox, HTMLParagraph.Type.INFOBOX);
			infoboxParagraph.setContentSegment(null);
		}

	}

	/**
	 * Removes HTML tags and footnotes (e.g. "[3]") from string
	 * 
	 * @param sentence
	 * @return
	 */
	private String makeSentenceRaw(String sentence) {
		String rawSentence = Jsoup.parse(sentence).text();

		// Remove footnotes
		Matcher m = footnotePattern.matcher(rawSentence);
		rawSentence = m.replaceAll("");

		// Remove wikipedia notes
		if (language == Language.EN) {
			Matcher m2 = wikiNotePattern.matcher(rawSentence);
			rawSentence = m2.replaceAll("");
		}

		return rawSentence.trim();
	}

	/**
	 * For a H1/H2/... paragraph: Finds it end position in the HTML (end
	 * position as the end of its last sub paragraphs)
	 * 
	 * @param htmlType
	 *            HTML "H" type of the paragraphs (H1, H2, ...)
	 * @param hBlock
	 *            Paragraph to search the end for
	 * @return "outer" end position of the paragraph
	 */
	private int getOuterEnd(String htmlType, Element hBlock) {

		// outer end is the begin of the next element of the same type

		boolean nextIsTheOne = false;
		for (Integer i : bordersOfHSegments.get(htmlType)) {
			if (nextIsTheOne)
				return i;
			if (hBlock.getBegin() == i) {
				nextIsTheOne = true;
			}
		}

		// if there is none, it's the end of this element
		return hBlock.getEnd();
	}

	/**
	 * For a H1/H2/... paragraph: Finds it end position in the HTML (end
	 * position as the begin of its first sub paragraphs)
	 * 
	 * @param htmlType
	 *            HTML "H" type of the paragraphs (H1, H2, ...)
	 * @param hBlock
	 *            Paragraph to search the end for
	 * @return "inner" end position of the paragraph
	 */
	private int getInnerEnd(Element hBlock) {

		// inner end is the beginning of the next H block
		boolean nextIsTheOne = false;

		for (Integer i : allBorders) {
			if (nextIsTheOne)
				return i;
			if (hBlock.getBegin() == i) {
				nextIsTheOne = true;
			}
		}

		// should not happen
		return allBorders.get(allBorders.size() - 1);
	}

	/**
	 * For each H1/H2/... paragraph in the HTML: Finds its start and end
	 * position (divided into "inner" and "outer" end positions)
	 */
	private void buildBordersOfHSegments() {

		this.bordersOfHSegments = new HashMap<String, List<Integer>>();
		this.allBorders = new ArrayList<Integer>();

		List<String> hSegmentTypes = Arrays.asList(HTMLElementName.H1, HTMLElementName.H2, HTMLElementName.H3,
				HTMLElementName.H4, HTMLElementName.H5, HTMLElementName.H6);

		for (String hSegmentType : hSegmentTypes) {
			List<Integer> positions = new ArrayList<Integer>();

			List<Element> hBlocks = source.getAllElements(hSegmentType);
			for (Element hBlock : hBlocks) {
				positions.add(hBlock.getBegin());
				allBorders.add(hBlock.getBegin());
			}
			bordersOfHSegments.put(hSegmentType, positions);
		}

		// add end of whole article
		// depends on the html origin (whole page with <html>...</html> (then
		// stop at printfooter) or just the article text)
		int articleEnd;
		if (source.getFirstElementByClass("printfooter") != null)
			articleEnd = source.getFirstElementByClass("printfooter").getBegin() - 1;
		else
			articleEnd = source.getEnd();

		bordersOfHSegments.get(HTMLElementName.H1).add(articleEnd);
		allBorders.add(articleEnd);

		// for H1 paragraph: if there is a TOC: end it at the beginning of TOC
		if (source.getElementById("toc") != null)
			allBorders.add(source.getElementById("toc").getBegin() - 1);

		Collections.sort(allBorders);
	}

	private HTMLParagraph buildTitleParagraph(Element hLine, Type dbType, String htmlType) {
		HTMLParagraph titleParagraph = null;

		if (dbType == HTMLParagraph.Type.H1PARAGRAPH) {
			Element articleTitle = source.getElementById("firstHeading");
			titleParagraph = new HTMLParagraph(articleTitle, HTMLParagraph.Type.ARTICLE_TITLE);
			titleParagraph.setContentSegment(articleTitle.getFirstElement(HTMLElementName.SPAN).getContent());
			titleParagraph.setImportant(currentBlocksAreImportant);
		} else {
			Element titleElement = hLine.getFirstElement(htmlType);
			if (titleElement.getFirstElement(HTMLElementName.SPAN) != null)
				titleElement = titleElement.getFirstElement(HTMLElementName.SPAN);
			if (titleElement != null) {
				titleParagraph = new HTMLParagraph(titleElement, HTMLParagraph.Type.TITLE);
				titleParagraph.setContentSegment(titleElement.getContent());
				titleParagraph.setImportant(currentBlocksAreImportant);
			}
		}

		return titleParagraph;
	}

	private void extractParagraphsAndAnnotations() {

		String text = revision.getString(Revision_DB.original_html_text_attr);

		source = new Source(text);

		source.fullSequentialParse();

		loadInfoboxParagraph();

		buildBordersOfHSegments();

		findParagraphs(source, 1);

		// System.out.println("Images in the article:");
		// for(Element e:
		// topParagraph.getSegment().getAllElements(HTMLElementName.IMG)) {
		// String url =
		// e.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
		// System.out.println("URL: " + WikiUtil.getUrlOfThumb(url));
		// }
		//
		// System.out.println("thumb divs");
		// for(Element e:
		// topParagraph.getSegment().getAllElementsByClass("thumb")) {
		// // System.out.println(e);
		// String url =
		// e.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
		// String tx = e.getTextExtractor().toString();
		// System.out.println("TEXT: " + tx);
		// System.out.println("URL: " + WikiUtil.getUrlOfThumb(url));
		// }

		for (HTMLParagraph p : contentParagraphs) {

			if (p.getType() == HTMLParagraph.Type.STARTPARAGRAPH_TOC)
				continue;

			List<HTMLParagraph> subParagraphs = new ArrayList<HTMLParagraph>();

			List<HTMLParagraph> pBlockParagraphs = identifyInnerBlocks(p.getSegment(), p.isImportant());
			List<HTMLParagraph> tableParagraphs = identifyInnerTables(p.getSegment(), p.isImportant());
			List<HTMLParagraph> listParagraphs = identifyInnerLists(p.getSegment(), p.isImportant());

			subParagraphs.addAll(pBlockParagraphs);
			subParagraphs.addAll(tableParagraphs);
			subParagraphs.addAll(listParagraphs);

			for (HTMLParagraph subParagraph : subParagraphs) {
				if (infoboxParagraph != null && subParagraph.getBegin() >= infoboxParagraph.getBegin()
						&& subParagraph.getEnd() <= infoboxParagraph.getEnd()) {
					infoboxParagraph.addSubParagraph(subParagraph);
				} else
					p.addSubParagraph(subParagraph);
			}

		}

	}

	private void identifyInnerImages(HTMLParagraph paragraph) {
		Segment segment = paragraph.getSegment();

		for (Element e : segment.getAllElements(HTMLElementName.IMG)) {
			String imageUrl = e.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
			imageUrl = WikiUtil.getUrlOfThumb(imageUrl);

			if (imageUrl != null) {
				if (imageUrl.length() > 252)
					imageUrl = imageUrl.substring(0, 250) + "...";

				paragraph.addImageUrl(imageUrl);
			}
		}
	}

	private List<HTMLParagraph> identifyInnerSeeAlsoLinks(HTMLParagraph p) {
		Segment segment = p.getSegment();
		List<HTMLParagraph> seeAlsoLinks = new ArrayList<HTMLParagraph>();

		Set<String> seeAlsoLinkClasses = config.getSeeAlsoLinkClasses(language);

		for (String seeAlsoLinkClass : seeAlsoLinkClasses) {
			for (Element e : segment.getAllElementsByClass(seeAlsoLinkClass)) {

				List<Element> links = e.getAllElements(HTMLElementName.A);

				if (links.size() == 0)
					continue;

				// collect all links
				Segment linksSegment = new Segment(source, links.get(0).getBegin(),
						links.get(links.size() - 1).getEnd());

				HTMLParagraph seeAlsoParagraph = new HTMLParagraph(e, HTMLParagraph.Type.SEE_ALSO);
				seeAlsoParagraph.setImportant(p.isImportant());
				seeAlsoParagraph.setContentSegment(linksSegment);
				seeAlsoParagraph.setContent(linksSegment.toString());

				seeAlsoLinks.add(seeAlsoParagraph);
			}
		}

		return seeAlsoLinks;
	}

	private List<HTMLParagraph> identifyInnerImageBoxes(HTMLParagraph p) {
		Segment segment = p.getSegment();
		List<HTMLParagraph> imageBoxParagraphs = new ArrayList<HTMLParagraph>();
		for (Element e : segment.getAllElementsByClass("thumb")) {

			if (e.getFirstElement(HTMLElementName.IMG) == null) {
				continue;
			}

			String url = e.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
			String imageText = e.getTextExtractor().toString();

			String imageUrl = WikiUtil.getUrlOfThumb(url);

			if (imageUrl == null || imageText == null)
				continue;

			HTMLParagraph imageBoxParagraph = new HTMLParagraph(e, HTMLParagraph.Type.IMAGE_DIV);
			imageBoxParagraph.setImportant(p.isImportant());

			if (e.getFirstElementByClass("thumbcaption") == null)
				continue;

			Segment thumbCaptionDiv = e.getFirstElementByClass("thumbcaption").getContent();

			if (thumbCaptionDiv == null || thumbCaptionDiv.getFirstElement() == null)
				continue;

			thumbCaptionDiv = new Segment(source, thumbCaptionDiv.getFirstElement().getEnd(), thumbCaptionDiv.getEnd());

			// Sometimes, another div follows the caption. Ignore it.
			if (thumbCaptionDiv.getFirstElement(HTMLElementName.DIV) != null) {
				thumbCaptionDiv = new Segment(source, thumbCaptionDiv.getBegin(),
						thumbCaptionDiv.getFirstElement(HTMLElementName.DIV).getBegin());
			}

			imageBoxParagraph.setContentSegment(thumbCaptionDiv);
			imageBoxParagraph.setContent(thumbCaptionDiv.toString());

			imageBoxParagraph.addImageUrl(imageUrl);
			imageBoxParagraphs.add(imageBoxParagraph);
		}

		return imageBoxParagraphs;
	}

	public void printContents() {
		if (infoboxParagraph != null)
			printContents(infoboxParagraph, 0);

		printContents(topParagraph, 0);
	}

	public void printContents(HTMLParagraph p, int level) {

		// text indent
		for (int i = 0; i < level; i++)
			System.out.print("\t");

		System.out.print(p.getType() + ": ");

		if (p.getTitleParagraph() != null) {
			System.out.print(p.getTitleParagraph().getContentSegment().toString());
		}

		if (p.getContent() != null) {
			System.out.println(p.getContent());
		} else
			System.out.println("");

		if (p.getSubParagraphs() != null) {
			for (HTMLParagraph subP : p.getSubParagraphs()) {
				printContents(subP, level + 1);
			}
		}

	}

	private List<HTMLParagraph> identifyInnerBlocks(Segment segment, boolean isImportant) {
		List<HTMLParagraph> pBlockParagraphs = new ArrayList<HTMLParagraph>();
		List<Element> pBlocks = segment.getAllElements(HTMLElementName.P);

		for (Element pBlock : pBlocks) {
			Segment inner = pBlock.getContent();
			String text = inner.toString();

			if (text.isEmpty())
				continue;

			HTMLParagraph paragraph = new HTMLParagraph(pBlock, HTMLParagraph.Type.PBLOCK);
			paragraph.setImportant(isImportant);
			paragraph.setContent(text);
			paragraph.setContentSegment(pBlock.getContent());
			pBlockParagraphs.add(paragraph);
		}
		return pBlockParagraphs;
	}

	private List<HTMLParagraph> identifyInnerTables(Segment segment, boolean isImportant) {
		List<HTMLParagraph> tableParagraphs = new ArrayList<HTMLParagraph>();

		List<Element> tables = segment.getAllElements(HTMLElementName.TABLE);

		// Keep track of the end position of the table before to avoid storing
		// of tables within other tables
		Integer endOfTableBefore = null;

		for (Element table : tables) {

			Integer endOfTableBeforeStored = endOfTableBefore;
			endOfTableBefore = table.getEnd();

			// Only take "wikitable" tables or those without any class
			if (table.getAttributeValue("class") != null && !table.getAttributeValue("class").contains("wikitable"))
				continue;

			if (table.getAttributeValue("class") != null && table.getAttributeValue("class").contains("collapsed"))
				continue;

			if (endOfTableBeforeStored != null && table.getBegin() < endOfTableBeforeStored) {
				endOfTableBefore = endOfTableBeforeStored;
				continue;
			}

			HTMLParagraph tableParagraph = new HTMLParagraph(table, HTMLParagraph.Type.TABLE);
			tableParagraph.setImportant(isImportant);
			Element tableHeaderRow = table.getFirstElement(HTMLElementName.TR);

			if (ParseUtil.getChildren(tableHeaderRow, HTMLElementName.TH) != null) {
				tableParagraphs.add(tableParagraph);

				HTMLParagraph tableHeaderRowParagraph = new HTMLParagraph(tableHeaderRow,
						HTMLParagraph.Type.TABLEHEADERROW);
				tableHeaderRowParagraph.setImportant(isImportant);
				tableParagraph.addSubParagraph(tableHeaderRowParagraph);

				// Table header
				List<Element> tableHeaderCells = ParseUtil.getChildren(tableHeaderRow, HTMLElementName.TH);
				for (Element tableCell : tableHeaderCells) {
					Segment inner = tableCell.getContent();
					String text = inner.toString();
					if (text.isEmpty())
						continue;
					HTMLParagraph tableCellParagraph = new HTMLParagraph(tableHeaderRow,
							HTMLParagraph.Type.TABLEHEADERCELL);
					tableCellParagraph.setImportant(isImportant);

					// Tables often contain lists!
					List<HTMLParagraph> listParagraphs = identifyInnerLists(tableCell, isImportant);
					if (listParagraphs.isEmpty())
						tableCellParagraph.setContent(text);
					else {
						for (HTMLParagraph list : listParagraphs) {
							// don't insert lists that are already in tables
							if (listsInTables.contains(list.getSegment().getBegin()))
								continue;

							tableCellParagraph.addSubParagraph(list);
							listsInTables.add(list.getSegment().getBegin());
						}
					}

					tableHeaderRowParagraph.addSubParagraph(tableCellParagraph);
				}
			}

			// Table content

			List<Element> tableRows = ParseUtil.getChildren(table, HTMLElementName.TR);
			for (Element tableRow : tableRows) {
				HTMLParagraph tableRowParagraph = new HTMLParagraph(tableRow, HTMLParagraph.Type.TABLEROW);
				tableRowParagraph.setImportant(isImportant);

				List<Element> tableCells = ParseUtil.getChildren(tableRow, HTMLElementName.TD);

				if (tableCells.isEmpty())
					continue;

				tableParagraph.addSubParagraph(tableRowParagraph);

				for (Element tableCell : tableCells) {
					Segment inner = tableCell.getContent();
					String text = inner.toString();
					if (text.trim().isEmpty())
						continue;
					HTMLParagraph tableCellParagraph = new HTMLParagraph(tableCell, HTMLParagraph.Type.TABLECELL);
					tableCellParagraph.setImportant(isImportant);

					identifyInnerImages(tableCellParagraph);

					tableRowParagraph.addSubParagraph(tableCellParagraph);
				}
			}

		}

		return tableParagraphs;
	}

	private List<HTMLParagraph> identifyInnerLists(Segment segment, boolean isImportant) {
		List<HTMLParagraph> listParagraphs = new ArrayList<HTMLParagraph>();
		listParagraphs.addAll(identifyInnerLists(segment, HTMLParagraph.Type.LIST, isImportant));
		listParagraphs.addAll(identifyInnerLists(segment, HTMLParagraph.Type.OL_LIST, isImportant));
		return listParagraphs;
	}

	private List<HTMLParagraph> identifyInnerLists(Segment segment, HTMLParagraph.Type type, boolean isImportant) {

		String listType = HTMLElementName.UL;
		if (type == HTMLParagraph.Type.OL_LIST) {
			listType = HTMLElementName.OL;
		}

		List<HTMLParagraph> listParagraphs = new ArrayList<HTMLParagraph>();

		// Keep track of the end position of the list before to avoid storing
		// of lists within other tables (they will instead be stored as
		// subparagraphs)
		Integer endOfListBefore = null;

		for (Element ulList : segment.getAllElements(listType)) {

			Integer endOfListBeforeStored = endOfListBefore;
			endOfListBefore = ulList.getEnd();

			List<Element> liChildren = ParseUtil.getChildren(ulList, HTMLElementName.LI);

			if (liChildren.size() == 0)
				continue;

			if (endOfListBeforeStored != null && ulList.getBegin() < endOfListBeforeStored) {
				endOfListBefore = endOfListBeforeStored;
				continue;
			}

			HTMLParagraph listParagraph = new HTMLParagraph(ulList, type);
			listParagraph.setImportant(isImportant);

			// Find title
			Element previousSibling = source.getPreviousTag(ulList.getBegin() - 1).getElement();
			if (previousSibling.getFirstStartTag().getName().equals(HTMLElementName.DL)
					&& previousSibling.getAllElements(HTMLElementName.DT) != null) {
				try {
					String title = previousSibling.getFirstElement(HTMLElementName.DT).getContent().toString();
					HTMLParagraph titleParagraph = new HTMLParagraph(previousSibling, HTMLParagraph.Type.TITLE);
					titleParagraph.setImportant(isImportant);

					titleParagraph.setContentSegment(previousSibling.getFirstElement(HTMLElementName.DT).getContent());
					listParagraph.setTitle(title);
					listParagraph.setTitleParagraph(titleParagraph);
				} catch (NullPointerException e) {
					continue;
				}
			}

			for (Element liChild : liChildren) {

				Segment inner;
				List<HTMLParagraph> subLists = new ArrayList<HTMLParagraph>();

				if (liChild.getAllElements(HTMLElementName.UL).size() > 0
						|| liChild.getAllElements(HTMLElementName.OL).size() > 0) {

					Integer innerEnd = null;
					// don't include the inner list or a div
					if (liChild.getContent().getFirstElement(HTMLElementName.UL) != null)
						innerEnd = liChild.getContent().getFirstElement(HTMLElementName.UL).getBegin();
					if (innerEnd == null || (liChild.getContent().getFirstElement(HTMLElementName.OL) != null
							&& liChild.getContent().getFirstElement(HTMLElementName.OL).getBegin() < innerEnd))
						innerEnd = liChild.getContent().getFirstElement(HTMLElementName.OL).getBegin();
					if (liChild.getContent().getFirstElement(HTMLElementName.DIV) != null
							&& liChild.getContent().getFirstElement(HTMLElementName.DIV).getBegin() < innerEnd)
						innerEnd = liChild.getContent().getFirstElement(HTMLElementName.DIV).getBegin();

					inner = new Segment(source, liChild.getContent().getBegin(), innerEnd);

					// find list in list
					subLists = identifyInnerLists(liChild, isImportant);
				} else {
					inner = liChild.getContent();
				}
				String text = inner.toString();

				if (text.isEmpty() && subLists.size() == 0)
					continue;

				HTMLParagraph listElementParagraph = new HTMLParagraph(inner, HTMLParagraph.Type.LISTELEMENT);
				listElementParagraph.setImportant(isImportant);

				listElementParagraph.setContent(text);
				listElementParagraph.setContentSegment(inner);

				listParagraph.addSubParagraph(listElementParagraph);

				if (subLists.size() > 0)
					listParagraph.addSubParagraphs(subLists);
			}

			listParagraphs.add(listParagraph);
		}

		return listParagraphs;
	}

	public void convertToDBParagraphs() {

		this.dbtoHtmlParagraphs = new HashMap<Paragraph_DB, HTMLParagraph>();

		convertToDBParagraphs(topParagraph, null, false);
		if (infoboxParagraph != null)
			convertToDBParagraphs(infoboxParagraph, null, true);

		// correct the paragraph numbers
		// List<Paragraph> paragraphList = new ArrayList<Paragraph>();
		// paragraphList.addAll(paragraphs);
		// Collections.sort(paragraphList);
		//
		// int correctedParagraphNumber = 0;
		// for (Paragraph paragraph : paragraphList) {
		// paragraph.setValue(Paragraph.paragraph_id_attr,
		// correctedParagraphNumber);
		// correctedParagraphNumber += 1;
		// }

	}

	private Paragraph_DB convertToDBParagraphs(HTMLParagraph htmlParagraph, Paragraph_DB paragraphAbove,
			boolean inInfobox) {

		// Don't translate any paragraph and its sub paragraph that e.g. have
		// the title "See also"

		Paragraph_DB dbp = new Paragraph_DB(revision, paragraphCounter, htmlParagraph.getType(),
				htmlParagraph.isImportant(), htmlParagraph.getBegin(), htmlParagraph.getEnd(),
				htmlParagraph.getContentSegment());

		this.dbtoHtmlParagraphs.put(dbp, htmlParagraph);

		if (htmlParagraph.getImageUrls() != null) {
			for (String imageUrl : htmlParagraph.getImageUrls()) {
				dataStore.addParagraphImage(new Paragraph_Image_DB(dbp, imageUrl));
			}
		}

		if (htmlParagraph.isImportant()) {
			dbp.setToTranslate(true);
		}

		paragraphCounter += 1;

		if (paragraphAbove != null) {
			dbp.setAboveParagraph(paragraphAbove);
		}

		this.paragraphs.add(dbp);

		if (htmlParagraph.getTitleParagraph() != null) {
			Paragraph_DB a = convertToDBParagraphs(htmlParagraph.getTitleParagraph(), dbp, inInfobox);
			this.paragraphs.add(a);
		}

		if (htmlParagraph.getSubParagraphs() != null) {
			for (HTMLParagraph subHtmlParagraph : htmlParagraph.getSubParagraphs()) {
				this.paragraphs.add(convertToDBParagraphs(subHtmlParagraph, dbp, inInfobox));
			}
		}

		return dbp;
	}

	/**
	 * Extracts from the revision's html all the external links that are
	 * mentioned in the article in a footnote and returns a mapping of all
	 * footnote ids to their external link.
	 * 
	 * @param revision
	 *            Revision whose footnotes are looked for.
	 * @return HashMap that contains the footnote ids and the belonging external
	 *         link.
	 */
	public HashMap<String, String> loadFootnoteIds() {

		HashMap<String, String> footnotesWithLinks = new HashMap<String, String>();

		String text = revision.getValue(Revision_DB.original_html_text_attr).toString();

		Document doc = Jsoup.parse(text);

		Elements footnoteLis = doc.select("li[id^=cite_note-]");

		for (org.jsoup.nodes.Element footnoteLi : footnoteLis) {
			String liId = footnoteLi.attr("id");
			String refId = liId.substring(liId.indexOf("-") + 1, liId.length());

			String url = footnoteLi.select("a.external").attr("href");

			if (!url.isEmpty())
				footnotesWithLinks.put(refId, url);
		}

		return footnotesWithLinks;
	}

	public void loadAnnotations() {

		this.annotations = new LinkedHashSet<Sentence_DB>();

		HashMap<String, String> footnoteIds = loadFootnoteIds();
		Map<Paragraph_DB, AnnotationExtractor> aes = new HashMap<Paragraph_DB, AnnotationExtractor>();

		AnnotationExtractorData aeData = new AnnotationExtractorData(database, revision, languages, footnoteIds);

		for (Paragraph_DB p : paragraphs) {
			if (p.getBoolean(Paragraph_DB.to_translate_attr)) {
				AnnotationExtractor ae = new AnnotationExtractor(aeData, p, dbtoHtmlParagraphs.get(p), dataStore);
				List<Sentence_DB> annotationsOfParagraph = ae.buildAnnotations();
				annotations.addAll(annotationsOfParagraph);
				aes.put(p, ae);
			}
		}

		// correct the sentence numbers
		List<Sentence_DB> annotationList = new ArrayList<Sentence_DB>();
		annotationList.addAll(annotations);
		Collections.sort(annotationList);

		int correctedSentenceNumber = 0;
		for (Sentence_DB annotation : annotationList) {
			annotation.setValue(Sentence_DB.annotation_id_attr, correctedSentenceNumber);
			correctedSentenceNumber += 1;
		}

		for (Paragraph_DB p : aes.keySet()) {
			AnnotationExtractor ae = aes.get(p);

			for (Sentence_DB a : ae.getAnnotations()) {
				a.setContainingParagraph(p);
			}

			ae.extractInformation();
		}

		dataStore.addAnnotations(annotations);

	}

	public Set<Paragraph_DB> extractAll() {
		this.paragraphs = new LinkedHashSet<Paragraph_DB>();

		extractParagraphsAndAnnotations();
		convertToDBParagraphs();

		dataStore.addParagraphs(this.paragraphs);

		loadAnnotations();

		return this.paragraphs;
	}

	public static void main(String[] args) throws Exception {

		String url = "http://ru.wikipedia.org/wiki/Museum_of_Old_and_New_Art";

		Language language = Language.RU;

		WikiTextGetter wikiTextGetter = new WikiTextGetter();
		// String text = wikiTextGetter.getHTMLTextOfRevision(language,
		// 634153739, "Museum_of_Old_and_New_Art");
		String text = wikiTextGetter.getHTMLTextOfRevision(language, 71061766, "\u0420\u0438\u043a\u0448\u0430");
		List<Language> languages = Arrays.asList(new Language[] { Language.RU, Language.EN });

		// Build example revision
		Revision_DB rev = new Revision_DB();
		rev.setValue(Revision_DB.article_uri_attr, url);
		rev.setValue(Revision_DB.revision_id_attr, 12);
		rev.setValue(Revision_DB.original_html_text_attr, text);

		Article_DB article = new Article_DB();
		article.setValue(Article_DB.language_attr, language.toString());
		rev.setArticle(article);

		ExtractionConfigThreadSafe.getInstance().init(languages);
		ParagraphExtractor extractor = new ParagraphExtractor(Configuration.DATABASE1, rev, languages, new ExtractionDataStore());

		// extractor.extractParagraphsAndAnnotations();
		// extractor.convertToDBParagraphs();
		//
		// extractor.printContents();
		// extractor.loadAnnotations();

		extractor.extractAll();
		extractor.printContents();

		// System.out.println("\nparagraphs\n");
		//
		// for (Paragraph p : extractor.paragraphs)
		// System.out.println("Paragraph: " + p.toString());
		//
		// System.out.println("\nannotations\n");
		//
		// for (Annotation p : extractor.annotations) {
		// if (p.getBoolean(Annotation.in_infobox_attr))
		// System.out.println("INFOBOX");
		// System.out.println("Annotation: " + p.toString());
		// }
		//
		// System.out.println("\nHTML Text\n");
		//
		// for (Annotation p : extractor.annotations) {
		// if ((Boolean) p.getValue(Annotation.to_translate_attr))
		// System.out.println(p.getValue(Annotation.original_text_html_attr).toString());
		// }
		//
		// System.out.println("\nText\n");
		//
		// for (Annotation p : extractor.annotations) {
		// if ((Boolean) p.getValue(Annotation.to_translate_attr)) {
		// String infobox = "";
		// if (p.getBoolean(Annotation.in_infobox_attr))
		// infobox = "Infobox: ";
		//
		// System.out.println(infobox +
		// p.getValue(Annotation.annotation_id_attr) + " "
		// + p.getValue(Annotation.original_text_attr).toString());
		// }
		// }

		System.out.println("\nComplete annotations\n");

		for (Sentence_DB p : extractor.annotations) {
			if (p.getBoolean(Sentence_DB.in_infobox_attr))
				System.out.println("INFOBOX");
			System.out.println("Annotation: " + p.getString(Sentence_DB.annotation_type_attr) + " -> "
					+ p.getString(Sentence_DB.original_text_attr));
		}

		System.out.println(extractor.annotations.size() + " annotations.");

		System.out.println(extractor.paragraphs.size() + " annotation.");

		// System.out.println(extractor.revision.size() +
		// " revision_internal_link.");

		extractor.loadLinksOfRevision();

		for (Revision_InternalLink_DB ri : extractor.dataStore.getRevisionInternalLinks()) {
			System.out.println(ri.getString(Revision_InternalLink_DB.wiki_link_attr));
		}
		System.out.println(extractor.dataStore.getRevisionInternalLinks().size() + " ris");
		// TextReplacer.markSentences(text, extractor.annotations);
	}
}
