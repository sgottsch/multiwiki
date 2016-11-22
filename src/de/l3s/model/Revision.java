package de.l3s.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.model.links.ExternalLink;
import de.l3s.translate.Language;
import de.l3s.wiki.WikiWords;

public class Revision {

	private long id;

	// the whole article represented as a single annotation
	private Sentence articleAnnotation;

	private List<Sentence> sentences;
	private List<Sentence> unchangedAnnotations;
	private Date date;
	private String title;
	private Language language;
	private Map<Language, Map<String, Entity>> entities;
	private String articleUri;
	private String htmlText;
	private Article article;
	private Map<Integer, Sentence> sentenceIds;
	private Map<String, Integer> termFs;
	private Map<Integer, WikiParagraph> paragraphs;

	private Map<Author, Integer> authors;
	private Map<String, Author> authorNames;
	private Map<String, Integer> authorLocations;

	private Map<String, Integer> images;
	private Map<ExternalLink, Integer> externalLinks;
	private Map<String, ExternalLink> externalLinkUris;

	private Map<Entity, Integer> internalLinkEntities;
	private Map<Entity, Integer> dbpediaLinkEntities;
	private Map<Entity, Integer> internalAndDBpediaEntities;

	private int numberOfEdits;

	private LinkedHashMap<Date, Integer> numberOfEditsPerDate;
	private LinkedHashMap<Date, Integer> sizePerDate;

	private Integer lengthOfRawSentences;

	private Sentence firstSentence;
	private Map<WikiParagraph.ParagraphType, List<WikiParagraph>> paragraphsPerType;
	private Set<Entity> allEntities;

	public Revision(long number, Date date, String title, Article article, Language language) {
		this.id = number;
		this.date = date;
		this.title = title;
		this.article = article;
		this.language = language;
		this.entities = new HashMap<Language, Map<String, Entity>>();
		this.entities.put(this.language, new HashMap<String, Entity>());
		this.images = new HashMap<String, Integer>();
		this.authors = new HashMap<Author, Integer>();
		this.authorNames = new HashMap<String, Author>();
		this.authorLocations = new HashMap<String, Integer>();
		this.internalLinkEntities = new HashMap<Entity, Integer>();
		this.externalLinks = new HashMap<ExternalLink, Integer>();
		this.externalLinkUris = new HashMap<String, ExternalLink>();
		this.dbpediaLinkEntities = new HashMap<Entity, Integer>();
		this.internalAndDBpediaEntities = new HashMap<Entity, Integer>();
		this.paragraphs = new HashMap<Integer, WikiParagraph>();
		this.paragraphsPerType = new HashMap<WikiParagraph.ParagraphType, List<WikiParagraph>>();
		this.sentences = new ArrayList<Sentence>();
		this.allEntities = new HashSet<Entity>();
		this.getEntities().put(this.getLanguage(), new HashMap<String, Entity>());
	}

	public Revision(long number, Date date, String title, String htmlText, Language language, String articleUri) {
		this.id = number;
		this.date = date;
		this.title = title;
		this.htmlText = htmlText;
		this.language = language;
		this.articleUri = articleUri;
		this.entities = new HashMap<Language, Map<String, Entity>>();
		this.entities.put(this.language, new HashMap<String, Entity>());
		this.images = new HashMap<String, Integer>();
		this.authors = new HashMap<Author, Integer>();
		this.authorNames = new HashMap<String, Author>();
		this.authorLocations = new HashMap<String, Integer>();
		this.internalLinkEntities = new HashMap<Entity, Integer>();
		this.externalLinks = new HashMap<ExternalLink, Integer>();
		this.externalLinkUris = new HashMap<String, ExternalLink>();
		this.dbpediaLinkEntities = new HashMap<Entity, Integer>();
		this.internalAndDBpediaEntities = new HashMap<Entity, Integer>();
		this.paragraphs = new HashMap<Integer, WikiParagraph>();
		this.paragraphsPerType = new HashMap<WikiParagraph.ParagraphType, List<WikiParagraph>>();
		this.allEntities = new HashSet<Entity>();
		this.getEntities().put(this.getLanguage(), new HashMap<String, Entity>());
	}

	public void calculateNumberOfEdits() {
		this.numberOfEdits = 0;
		for (Integer numberOfEditsForAuthor : this.authors.values()) {
			this.numberOfEdits += numberOfEditsForAuthor.intValue();
		}
	}

	public Sentence getArticleAnnotation() {
		return this.articleAnnotation;
	}

	public void setArticleAnnotation(Sentence articleAnnotation) {
		this.articleAnnotation = articleAnnotation;
	}

	public Language getLanguage() {
		return this.language;
	}

	public Article getArticle() {
		return this.article;
	}

	public Map<Language, Map<String, Entity>> getEntities() {
		return this.entities;
	}

	public void setEntities(Map<Language, Map<String, Entity>> dbPediaEntities) {
		this.entities = dbPediaEntities;
		if (!this.entities.containsKey(this.language))
			this.entities.put(this.language, new HashMap<String, Entity>());
	}

	public void addEntityMapping(Map<Language, Map<String, Entity>> dbPediaEntities) {
		if (this.entities == null) {
			this.entities = new HashMap<Language, Map<String, Entity>>();
		}
		for (Language l : dbPediaEntities.keySet()) {
			this.entities.put(l, dbPediaEntities.get(l));
		}
	}

	public String getArticleUri() {
		if (this.article != null) {
			return this.article.getUri();
		}
		return this.articleUri;
	}

	public List<Sentence> getAnnotations() {
		return this.sentences;
	}

	public List<Sentence> getSentences() {
		return this.sentences;
	}

	public void setSentences(List<Sentence> annotations) {
		this.sentences = annotations;
	}

	public void addSentence(Sentence annotation) {
		this.sentences.add(annotation);
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHtmlText() {
		return this.htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	/**
	 * @deprecated use getId() instead
	 */
	@Deprecated
	public long getNumber() {
		return this.id;
	}

	public long getId() {
		return this.id;
	}

	public void setNumber(long number) {
		this.id = number;
	}

	public void addAuthor(Author author, int numberOfEditsOfAuthor) {
		this.authors.put(author, numberOfEditsOfAuthor);
		this.authorNames.put(author.getName(), author);
		String countryCode = author.getCountryCode();
		if (countryCode != null && !countryCode.isEmpty() && !countryCode.equals("-")) {
			if (!this.authorLocations.containsKey(countryCode)) {
				this.authorLocations.put(countryCode, 1);
			} else {
				this.authorLocations.put(countryCode, this.authorLocations.get(countryCode) + 1);
			}
		}
	}

	public Map<Author, Integer> getAuthors() {
		return this.authors;
	}

	public void setAuthors(Map<Author, Integer> authors) {
		this.authors = authors;
	}

	public Map<String, Author> getAuthorNames() {
		return this.authorNames;
	}

	public Map<String, Integer> getAuthorLocations() {
		return this.authorLocations;
	}

	public void setAuthorLocations(Map<String, Integer> authorLocations) {
		this.authorLocations = authorLocations;
	}

	public void addImage(String imageUri, int numberOfOccurences) {
		this.images.put(imageUri, numberOfOccurences);
	}

	public Map<String, Integer> getImages() {
		return this.images;
	}

	public void addExternalLink(ExternalLink externalLink, int numberOfOccurences) {
		this.externalLinks.put(externalLink, numberOfOccurences);
		this.externalLinkUris.put(externalLink.getLink(), externalLink);
	}

	public void setExternalLinks(Map<ExternalLink, Integer> externalLinks) {
		this.externalLinks = externalLinks;
	}

	public Map<ExternalLink, Integer> getExternalLinks() {
		return this.externalLinks;
	}

	public Map<String, ExternalLink> getExternalLinkUris() {
		return this.externalLinkUris;
	}

	public void addInternalLinkEntity(Entity internalLinkEntity, int numberOfOccurences) {
		this.internalLinkEntities.put(internalLinkEntity, numberOfOccurences);
		this.addInternalAndDBpediaEntities(internalLinkEntity, numberOfOccurences);
	}

	public void addInternalAndDBpediaEntities(Entity anyEntity, int numberOfOccurences) {
		if (!this.internalAndDBpediaEntities.containsKey(anyEntity)) {
			this.internalAndDBpediaEntities.put(anyEntity, numberOfOccurences);
		} else {
			this.internalAndDBpediaEntities.put(anyEntity,
					this.internalAndDBpediaEntities.get(anyEntity) + numberOfOccurences);
		}
	}

	public Map<Entity, Integer> getInternalLinkEntities() {
		return this.internalLinkEntities;
	}

	public Map<Entity, Integer> getInternalAndDBpediaEntities() {
		return this.internalAndDBpediaEntities;
	}

	public void setInternalAndDBpediaEntities(Map<Entity, Integer> internalAndDBpediaEntities) {
		this.internalAndDBpediaEntities = internalAndDBpediaEntities;
	}

	public void addDBPediaLink(Entity dbPediaLinkEntity) {
		if (!this.dbpediaLinkEntities.containsKey(dbPediaLinkEntity)) {
			this.dbpediaLinkEntities.put(dbPediaLinkEntity, 0);
		}
		this.dbpediaLinkEntities.put(dbPediaLinkEntity, this.dbpediaLinkEntities.get(dbPediaLinkEntity) + 1);
		this.addInternalAndDBpediaEntities(dbPediaLinkEntity, 1);
	}

	public Map<Entity, Integer> getDBPediaLinkEntitiess() {
		return this.dbpediaLinkEntities;
	}

	private void insertEntity(Language lang, String name, Entity entity) {
		if (!this.entities.containsKey(lang)) {
			this.entities.put(lang, new HashMap<String, Entity>());
		}
		this.entities.get(lang).put(name, entity);
	}

	public Entity buildEntity(Map<Language, String> langLinks) {

		// check whether entity link is allowed and not something like
		// "Wikipedia:Citation_needed"
		for (Language language : langLinks.keySet()) {
			if (WikiWords.getInstance().getForbiddenLinks(language).contains(langLinks.get(language)))
				return null;
		}

		Entity entity = null;

		// Find out whether this entity already exists
		for (Language lang : langLinks.keySet()) {
			String name = langLinks.get(lang);

			if (lang == null || name == null || name.equals("-"))
				continue;

			// Ignore entities like "File: ...", "Template: ..." etc.
			for (String prefix : WikiWords.getInstance().getForbiddenInternalLinks(lang)) {
				if (name.startsWith(prefix)) {
					return null;
				}
			}

			if (this.entities.containsKey(lang) && this.entities.get(lang).containsKey(langLinks.get(lang))) {
				entity = this.entities.get(lang).get(name);
				break;
			}
		}

		// Entity did not exist -> Create it.
		if (entity == null) {
			entity = new Entity();
		}

		for (Language lang : langLinks.keySet()) {
			if (entity.getName(lang) == null) {
				String name = langLinks.get(lang);
				entity.addName(lang, name);
				insertEntity(lang, name, entity);
			}
		}

		return entity;
	}

	public Sentence getSentenceById(int sentenceId) {
		return this.sentenceIds.get(sentenceId);
	}

	public void setSentenceIds(Map<Integer, Sentence> sentenceIds) {
		this.sentenceIds = sentenceIds;
	}

	public Map<String, Integer> getTermFs() {
		if (this.termFs == null) {
			this.termFs = new HashMap<String, Integer>();
		}
		return this.termFs;
	}

	public void setTermFs(Map<String, Integer> termFs) {
		this.termFs = termFs;
	}

	public void addTermF(String name, Integer number) {
		if (this.termFs == null) {
			this.termFs = new HashMap<String, Integer>();
		}
		this.termFs.put(name, number);
	}

	public Map<Integer, WikiParagraph> getParagraphs() {
		return this.paragraphs;
	}

	public void addParagraph(WikiParagraph paragraph) {
		this.paragraphs.put(paragraph.getId(), paragraph);
		if (!this.paragraphsPerType.containsKey(paragraph.getParagraphType())) {
			this.paragraphsPerType.put(paragraph.getParagraphType(), new ArrayList<WikiParagraph>());
		}
		this.paragraphsPerType.get(paragraph.getParagraphType()).add(paragraph);
	}

	public int getNumberOfEdits() {
		return this.numberOfEdits;
	}

	public void setNumberOfEdits(int numberOfEdits) {
		this.numberOfEdits = numberOfEdits;
	}

	public LinkedHashMap<Date, Integer> getNumberOfEditsPerDate() {
		return this.numberOfEditsPerDate;
	}

	public void setNumberOfEditsPerDate(LinkedHashMap<Date, Integer> numberOfEditsPerDate) {
		this.numberOfEditsPerDate = numberOfEditsPerDate;
	}

	public LinkedHashMap<Date, Integer> getSizePerDate() {
		return this.sizePerDate;
	}

	public void setSizePerDate(LinkedHashMap<Date, Integer> sizePerDate) {
		this.sizePerDate = sizePerDate;
	}

	public String getWikiLink() {
		return String.valueOf(this.getArticleUri()) + "?oldid=" + this.id;
	}

	public void storeAnnotations() {
		this.unchangedAnnotations = new ArrayList<Sentence>();
		for (Sentence a : this.sentences) {
			Sentence b = new Sentence(a);
			this.unchangedAnnotations.add(b);
		}
	}

	public void resetAnnotations() {
		this.sentences.clear();
		for (Sentence a : this.unchangedAnnotations) {
			this.sentences.add(a);
		}
	}

	public int getLengthofRawSentences() {
		if (this.lengthOfRawSentences == null) {
			this.lengthOfRawSentences = 0;
			for (Sentence sentence : this.getSentences()) {
				if (sentence.getType() != SentenceType.SENTENCE)
					continue;
				this.lengthOfRawSentences = this.lengthOfRawSentences + sentence.getRawText().length();
			}
		}
		return this.lengthOfRawSentences;
	}

	public void buildSentenceGraph() {
		Sentence sentenceBefore = null;
		for (Sentence sentence : this.sentences) {
			if (sentence.getType() != SentenceType.SENTENCE)
				continue;
			if (sentenceBefore != null) {
				sentenceBefore.setSeqSentence(sentence);
				sentence.setPreSentence(sentenceBefore);
			}
			sentenceBefore = sentence;
		}
	}

	public WikiParagraph getFirstWikiParagraph() {
		return this.firstSentence.getParagraph();
	}

	public Sentence getFirstSentence() {
		return this.firstSentence;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public List<WikiParagraph> getPBlockParagraphs() {
		return this.paragraphsPerType.get(WikiParagraph.ParagraphType.PBLOCK);
	}

	public List<WikiParagraph> getParagraphsByType(WikiParagraph.ParagraphType type) {
		return this.paragraphsPerType.get(type);
	}

	public Map<Integer, Sentence> getSentenceIds() {
		return this.sentenceIds;
	}

	public void addEntity(Entity entity) {
		this.allEntities.add(entity);
	}
}
