package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.RealVector;

import dbpedia_spotlight.SpotlightLink;
import model.links.DbPediaLink;
import model.links.ExternalLink;
import model.links.InternalLink;
import model.times.HeidelIntervalString;
import tfidf.CollectionType;
import translate.Language;

/**
 * An instance of this model represents a single sentence in any language.
 * 
 * Due to the paragraph alignment, this can be the combination of several
 * sentences as well.
 */
public class Sentence implements Comparable<Sentence> {

	private String articleUri;
	protected Revision revision;
	private long revisionId;
	private int number;

	private Language language;

	private String htmlText;
	private String rawText;

	private String englishHtmlText;
	private String englishRawText;
	private List<String> englishStemmedText;

	private int startPosition;
	private int endPosition;

	private List<String> nGrams;
	// NGrams as proposed in the paper
	// "Building Bilingual Parallel Corpora based on Wikipedia" (2010)
	private List<String> dictionaryNGrams;

	private Set<InternalLink> internalLinks;
	private Set<ExternalLink> externalLinks;
	private Set<DbPediaLink> dbPediaLinks;
	private boolean isImportant;
	private boolean isInInfobox;
	private SentenceType type;
	private String englishStemmedTextConcatenated;
	private String entityString;
	private String entityAllString;
	private String externalLinksString;
	private String externalLinkHostString;
	private String dbpediaLinksNERString;
	private String dbpediaLinksAllString;
	private String dbpediaLinksNoString;
	private String internalLinksString;
	private Map<CollectionType, RealVector> termVectors;
	private WikiParagraph paragraph;
	public List<Sentence> subAnnotations;
	public List<Sentence> allSubAnnotations;
	public List<Sentence> gapSentences;
	private Map<String, Integer> entityNames;
	private double relativePosition;
	private Sentence seqSentence;
	private Sentence preSentence;
	private List<Sentence> partSentences = new ArrayList<Sentence>();
	private List<Integer> dividePositions;
	private boolean isTranslated;
	private int numberOfWords;
	private List<HeidelIntervalString> heidelIntervals;
	private List<SpotlightLink> spotlightLinks;
	private List<Set<String>> dictionaryTranslations;
	private List<String> snowballStemmedWords;
	private double probability;
	private Map<Sentence, SentencePair> involvedInSentencePairs;

	public Sentence(Language language, int number, String rawText, String englishRawText,
			String englishStemmedTextConcatenated) {
		this.language = language;
		this.rawText = rawText;
		this.englishRawText = englishRawText;
		this.number = number;
		if (englishStemmedTextConcatenated != null) {
			this.englishStemmedTextConcatenated = englishStemmedTextConcatenated = englishStemmedTextConcatenated
					.trim();
			this.englishStemmedText = Sentence.splitStemmedText(englishStemmedTextConcatenated);
		}
		this.termVectors = new HashMap<CollectionType, RealVector>();
		this.heidelIntervals = new ArrayList<HeidelIntervalString>();
		this.spotlightLinks = new ArrayList<SpotlightLink>();
	}

	public Sentence(int number, Language language, String htmlText, String rawText, String englishText,
			String englishRawText, String englishStemmedTextConcatenated, int startPosition, int endPosition,
			boolean isImportant, boolean isInInfobox, String type) {
		this.number = number;
		this.language = language;
		this.htmlText = htmlText;
		this.rawText = rawText;
		this.englishHtmlText = englishText;
		this.englishRawText = englishRawText;
		if (englishStemmedTextConcatenated != null) {
			this.englishStemmedTextConcatenated = englishStemmedTextConcatenated = englishStemmedTextConcatenated
					.trim();
			this.englishStemmedText = Sentence.splitStemmedText(englishStemmedTextConcatenated);
		}
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.isImportant = isImportant;
		this.isInInfobox = isInInfobox;
		this.dbPediaLinks = new HashSet<DbPediaLink>();
		this.type = SentenceType.valueOf(type);
		this.termVectors = new HashMap<CollectionType, RealVector>();
		this.heidelIntervals = new ArrayList<HeidelIntervalString>();
		this.spotlightLinks = new ArrayList<SpotlightLink>();
	}

	public static List<String> splitStemmedText(String text) {
		return Arrays.asList(text.split(" "));
	}

	public Sentence(int number, Language language, String htmlText, String rawText, String stemmedTextConcatenated,
			int startPosition, int endPosition, boolean isImportant, boolean isInInfobox, String type) {
		this.number = number;
		this.language = language;
		this.htmlText = htmlText;
		this.rawText = rawText;
		this.englishStemmedTextConcatenated = stemmedTextConcatenated;
		this.englishStemmedText = Sentence.splitStemmedText(stemmedTextConcatenated);
		if (language == Language.EN) {
			this.englishHtmlText = htmlText;
			this.englishRawText = rawText;
		}
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.isImportant = isImportant;
		this.isInInfobox = isInInfobox;
		this.dbPediaLinks = new HashSet<DbPediaLink>();
		this.type = SentenceType.valueOf(type);
		this.termVectors = new HashMap<CollectionType, RealVector>();
		this.heidelIntervals = new ArrayList<HeidelIntervalString>();
		this.spotlightLinks = new ArrayList<SpotlightLink>();
	}

	public Sentence() {
		this.dbPediaLinks = new HashSet<DbPediaLink>();
		this.heidelIntervals = new ArrayList<HeidelIntervalString>();
		this.termVectors = new HashMap<CollectionType, RealVector>();
	}

	public Sentence(Sentence a) {
		this(a.getNumber(), a.getLanguage(), a.getHtmlText(), a.getRawText(), a.getEnglishStemmedTextConcatenated(),
				a.getStartPosition(), a.getEndPosition(), a.isImportant(), a.isInInfobox(), a.getType().toString());
		this.englishHtmlText = a.getEnglishText();
		this.englishRawText = a.getEnglishRawText();
		this.paragraph = a.getParagraph();
		this.dbPediaLinks = a.getDbPediaLinks();
		this.entityNames = a.getEntityNames();
		this.internalLinks = a.getInternalLinks();
		this.termVectors = new HashMap<CollectionType, RealVector>();
		this.termVectors = a.getTermVectors();
		this.revision = a.getRevision();
		this.heidelIntervals = new ArrayList<HeidelIntervalString>();
		this.spotlightLinks = new ArrayList<SpotlightLink>();
	}

	public void setType(SentenceType type) {
		this.type = type;
	}

	private Map<CollectionType, RealVector> getTermVectors() {
		return this.termVectors;
	}

	public String getArticleUri() {
		if (this.articleUri != null) {
			return this.articleUri;
		}
		return this.getRevision().getArticleUri();
	}

	public void setArticleUri(String articleUri) {
		this.articleUri = articleUri;
	}

	public long getRevisionId() {
		if (this.revision != null) {
			return this.getRevision().getId();
		}
		return this.revisionId;
	}

	public void setRevisionId(long revisionId) {
		this.revisionId = revisionId;
	}

	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public String getHtmlText() {
		return this.htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	public String getRawText() {
		return this.rawText;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	public String getEnglishText() {
		return this.englishHtmlText;
	}

	public void setEnglishText(String englishText) {
		this.englishHtmlText = englishText;
	}

	public int getStartPosition() {
		return this.startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getEndPosition() {
		return this.endPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	public Set<InternalLink> getInternalLinks() {
		if (this.internalLinks == null) {
			this.internalLinks = new HashSet<InternalLink>();
		}
		return this.internalLinks;
	}

	public void addInternalLink(InternalLink internalLink) {
		if (this.internalLinks == null) {
			this.internalLinks = new HashSet<InternalLink>();
		}
		this.internalLinks.add(internalLink);
		this.revision.addEntity(internalLink.getEntity());
	}

	public void addDbPediaLink(DbPediaLink dbPediaLink) {
		if (this.dbPediaLinks == null) {
			this.dbPediaLinks = new HashSet<DbPediaLink>();
		}
		this.dbPediaLinks.add(dbPediaLink);
		this.revision.addEntity(dbPediaLink.getEntity());
	}

	public Set<ExternalLink> getExternalLinks() {
		if (this.externalLinks == null) {
			this.externalLinks = new HashSet<ExternalLink>();
		}
		return this.externalLinks;
	}

	public void setExternalLinks(Set<ExternalLink> externalLinks) {
		this.externalLinks = externalLinks;
	}

	public void setInternalLinks(Set<InternalLink> internalLinks) {
		this.internalLinks = internalLinks;
	}

	public void setDbPediaLinks(Set<DbPediaLink> dbPediaLinks) {
		this.dbPediaLinks = dbPediaLinks;
	}

	public Set<DbPediaLink> getDbPediaLinks() {
		return this.dbPediaLinks;
	}

	public String getEnglishRawText() {
		return this.englishRawText;
	}

	public void setEnglishRawText(String englishRawText) {
		this.englishRawText = englishRawText;
	}

	public List<String> getEnglishStemmedText() {
		return this.englishStemmedText;
	}

	public boolean isImportant() {
		return this.isImportant;
	}

	public void setImportant(boolean isImportant) {
		this.isImportant = isImportant;
	}

	public boolean isInInfobox() {
		return this.isInInfobox;
	}

	public void setInInfobox(boolean isInInfobox) {
		this.isInInfobox = isInInfobox;
	}

	public List<String> getNGrams() {
		return this.nGrams;
	}

	public void setNGrams(List<String> nGrams) {
		this.nGrams = nGrams;
	}

	public SentenceType getType() {
		return this.type;
	}

	public String getEntityString() {
		return this.entityString;
	}

	public void setEntityString(String entityString) {
		this.entityString = entityString;
	}

	public Revision getRevision() {
		return this.revision;
	}

	public void setRevision(Revision revision) {
		this.revision = revision;
	}

	public String getEnglishStemmedTextConcatenated() {
		return this.englishStemmedTextConcatenated;
	}

	public boolean hasTermVector(CollectionType collectionType) {
		return this.termVectors.containsKey(collectionType);
	}

	public RealVector getTermVector(CollectionType collectionType) {
		return this.termVectors.get(collectionType);
	}

	public void setTermVector(CollectionType collectionType, RealVector termVector) {
		this.termVectors.put(collectionType, termVector);
	}

	public WikiParagraph getParagraph() {
		return this.paragraph;
	}

	public void setParagraph(WikiParagraph paragraph) {
		this.paragraph = paragraph;
	}

	public String getExternalLinksString() {
		return this.externalLinksString;
	}

	public void setExternalLinksString(String externalLinksString) {
		this.externalLinksString = externalLinksString;
	}

	public String getExternalLinkHostString() {
		return this.externalLinkHostString;
	}

	public void setExternalLinkHostString(String externalLinkHostString) {
		this.externalLinkHostString = externalLinkHostString;
	}

	@Override
	public int compareTo(Sentence a) {
		if (this.getStartPosition() < a.getStartPosition()) {
			return -1;
		}
		if (this.getStartPosition() != a.getStartPosition()) {
			return 1;
		}
		return 0;
	}

	public String getDbpediaLinksNERString() {
		return this.dbpediaLinksNERString;
	}

	public void setDbpediaLinksNERString(String dbpediaLinksNERString) {
		this.dbpediaLinksNERString = dbpediaLinksNERString;
	}

	public String getDbpediaLinksAllString() {
		return this.dbpediaLinksAllString;
	}

	public void setDbpediaLinksAllString(String dbpediaLinksAllString) {
		this.dbpediaLinksAllString = dbpediaLinksAllString;
	}

	public String getInternalLinksString() {
		return this.internalLinksString;
	}

	public void setInternalLinksString(String internalLinksString) {
		this.internalLinksString = internalLinksString;
	}

	public String getEntityAllString() {
		return this.entityAllString;
	}

	public void setEntityAllString(String entityAllString) {
		this.entityAllString = entityAllString;
	}

	public String getDbpediaLinksNoString() {
		return this.dbpediaLinksNoString;
	}

	public void setDbpediaLinksNoString(String dbpediaLinksNoString) {
		this.dbpediaLinksNoString = dbpediaLinksNoString;
	}

	public List<Sentence> getSubAnnotations() {
		if (this.subAnnotations == null) {
			this.subAnnotations = new ArrayList<Sentence>();
			this.subAnnotations.add(this);
		}
		return this.subAnnotations;
	}

	public boolean isSubAnnotationsNull() {
		if (this.subAnnotations == null) {
			return true;
		}
		return false;
	}

	public boolean isAllSubAnnotationsNull() {
		if (this.allSubAnnotations == null) {
			return true;
		}
		return false;
	}

	public void setSubAnnotations(List<Sentence> subAnnotations) {
		this.subAnnotations = subAnnotations;
		this.allSubAnnotations = new ArrayList<Sentence>();
		for (Sentence sub : subAnnotations) {
			this.allSubAnnotations.addAll(sub.getAllSubAnnotations());
		}
	}

	public List<Sentence> getAllSubAnnotations() {
		if (this.allSubAnnotations == null) {
			this.allSubAnnotations = new ArrayList<Sentence>();
			this.allSubAnnotations.add(this);
		}
		return this.allSubAnnotations;
	}

	/**
	 * Returns the k sentences that are directly before this sentence in the
	 * text.
	 */
	public List<Sentence> getSentencesBefore(int k) {

		List<Sentence> sentencesBefore = new ArrayList<Sentence>();

		if (k == 0)
			return sentencesBefore;

		CircularFifoQueue<Sentence> sentencesBeforeQueue = new CircularFifoQueue<Sentence>(k);

		// find the first sentence (if this sentence is composed of multiple
		// ones)
		Sentence firstSentence = getFirstSentence();

		// start from the beginning of the article and add sentences to the
		// queue until the first sentence is found
		for (Sentence sentence : this.revision.getSentences()) {
			if (sentence == firstSentence)
				break;
			if (sentence.getType() == SentenceType.SENTENCE)
				sentencesBeforeQueue.add(sentence);
		}

		while (!sentencesBeforeQueue.isEmpty()) {
			sentencesBefore.add(sentencesBeforeQueue.poll());
		}

		return sentencesBefore;
	}

	/**
	 * Returns the k sentences that are directly after this sentence in the
	 * text.
	 */
	public List<Sentence> getSentencesAfter(int k) {

		List<Sentence> sentencesAfter = new ArrayList<Sentence>(k);

		if (k == 0)
			return sentencesAfter;

		// find the last sentence (if this sentence is composed of multiple
		// ones)
		Sentence lastSentence = getLastSentence();

		boolean foundLastSentence = false;
		for (Sentence sentence : this.revision.getSentences()) {
			if (!foundLastSentence) {
				if (sentence == lastSentence)
					foundLastSentence = true;
			} else {
				if (sentence.getType() == SentenceType.SENTENCE) {
					sentencesAfter.add(sentence);
					if (sentencesAfter.size() == k)
						break;
				}
			}
		}

		return sentencesAfter;
	}

	public Sentence getFirstSentence() {
		Sentence firstSentence = this;
		if (this.allSubAnnotations != null) {
			firstSentence = this.allSubAnnotations.get(0);
		}
		return firstSentence;
	}

	public Sentence getLastSentence() {
		Sentence lastSentence = this;
		if (this.allSubAnnotations != null) {
			lastSentence = this.allSubAnnotations.get(this.allSubAnnotations.size() - 1);
		}
		return lastSentence;
	}

	public List<String> getDictionaryNGrams() {
		return this.dictionaryNGrams;
	}

	public void setDictionaryNGrams(List<String> paperNGrams) {
		this.dictionaryNGrams = paperNGrams;
	}

	public Map<String, Integer> getEntityNames() {
		return this.entityNames;
	}

	public void setEntityNames(Map<String, Integer> entityNames) {
		this.entityNames = entityNames;
	}

	public List<Sentence> getGapSentences() {
		return this.gapSentences;
	}

	public void setGapSentences(List<Sentence> gapSentences) {

		// for (Annotation s : gapSentences)
		// System.out.println(s.getRawText());

		this.gapSentences = gapSentences;
	}

	public boolean isGapSentence(Sentence sentence) {
		if (this.gapSentences != null && this.gapSentences.contains(sentence)) {
			return true;
		}
		return false;
	}

	public double getRelativePosition() {
		return this.relativePosition;
	}

	public void setRelativePosition(double relativePosition) {
		this.relativePosition = relativePosition;
	}

	// public Sentence smallCopy() {
	// Sentence a = new Sentence();
	// a.setRawText(this.rawText);
	// a.setNumber(this.number);
	// a.setRawText(this.rawText);
	// a.setStartPosition(startPosition);
	// a.setEndPosition(endPosition);
	// WikiParagraph p = new
	// WikiParagraph(this.getParagraph().getStartPosition(),
	// this.getParagraph().getEndPosition(),
	// this.getParagraph().getParagraphType().toString());
	// WikiParagraph aboveP = new
	// WikiParagraph(this.getParagraph().getAboveParagraph().getStartPosition(),
	// this.getParagraph().getAboveParagraph().getEndPosition(),
	// this.getParagraph().getAboveParagraph().getParagraphType().toString());
	// p.setAboveParagraph(aboveP);
	// a.setParagraph(p);
	//
	// return a;
	// }

	public Sentence smallCopy2() {
		Sentence a = new Sentence();
		a.setEntityAllString(this.entityAllString);
		a.setRawText(this.rawText);
		a.setNumber(this.number);
		a.setRawText(this.rawText);
		a.setRevisionId(this.revisionId);
		a.setRevision(this.revision);
		a.setStartPosition(this.startPosition);
		a.setEndPosition(this.endPosition);
		a.setHeidelIntervals(this.heidelIntervals);
		a.setArticleUri(this.articleUri);
		a.setEnglishRawText(this.englishRawText);
		a.setParagraph(this.paragraph);
		a.setType(this.type);
		a.setImportant(this.isImportant);
		a.setInInfobox(this.isInInfobox);
		a.setInternalLinks(this.internalLinks);
		a.setDbPediaLinks(this.dbPediaLinks);
		a.setTermVectors(this.termVectors);
		a.setLanguage(this.language);
		a.setEnglishStemmedText(this.englishStemmedText);
		a.setEnglishHtmlText(this.englishHtmlText);
		a.setEnglishRawText(this.englishRawText);
		a.setNGrams(this.dictionaryNGrams);
		a.setEnglishStemmedTextConcatenated(this.englishStemmedTextConcatenated);
		a.setSnowballStemmedWords(this.snowballStemmedWords);
		a.setDictionaryTranslations(this.dictionaryTranslations);
		this.revision.getSentenceIds().remove(this.number);
		this.revision.getSentenceIds().put(this.number, a);
		return a;
	}

	public void setAllSubAnnotations(ArrayList<Sentence> subsTmp1) {
		this.allSubAnnotations = subsTmp1;
	}

	public void addGapSentences(List<Sentence> newGapSentences) {
		for (Sentence gapSentence : newGapSentences) {
			if (this.gapSentences.contains(gapSentence))
				continue;
			this.gapSentences.addAll(newGapSentences);
		}
	}

	public void addGapSentence(Sentence newGapSentence) {
		this.gapSentences.add(newGapSentence);
	}

	public boolean containsSameSentenceMultipleTimes() {
		HashSet<Sentence> subSentences = new HashSet<Sentence>();
		for (Sentence subSentence : this.getAllSubAnnotations()) {
			if (subSentences.contains(subSentence)) {
				return true;
			}
			subSentences.add(subSentence);
		}
		return false;
	}

	public Sentence getSeqSentence() {
		return this.seqSentence;
	}

	public void setSeqSentence(Sentence seqSentence) {
		this.seqSentence = seqSentence;
	}

	public Sentence getPreSentence() {
		return this.preSentence;
	}

	public void setPreSentence(Sentence preSentence) {
		this.preSentence = preSentence;
	}

	public boolean isInFirstParagraph() {
		if (this.paragraph == this.revision.getFirstWikiParagraph()) {
			return true;
		}
		return false;
	}

	public void setTermVectors(Map<CollectionType, RealVector> termVectors) {
		this.termVectors = termVectors;
	}

	public String getEnglishHtmlText() {
		return this.englishHtmlText;
	}

	public void setEnglishHtmlText(String englishHtmlText) {
		this.englishHtmlText = englishHtmlText;
	}

	public List<String> getnGrams() {
		return this.nGrams;
	}

	public void setnGrams(List<String> nGrams) {
		this.nGrams = nGrams;
	}

	public void setEnglishStemmedText(List<String> englishStemmedText) {
		this.englishStemmedText = englishStemmedText;
	}

	public void setEnglishStemmedTextConcatenated(String englishStemmedTextConcatenated) {
		this.englishStemmedTextConcatenated = englishStemmedTextConcatenated;
	}

	public List<Integer> findDividePositions() {

		List<Integer> positions = new ArrayList<Integer>();

		Set<Character> betweenChars = new HashSet<Character>();
		betweenChars.add('-');
		betweenChars.add('.');
		betweenChars.add(',');
		betweenChars.add(';');
		betweenChars.add(' ');
		betweenChars.add('–');
		betweenChars.add(':');

		Set<Character> previousChars = new HashSet<Character>();
		Character previousChar = null;

		for (int i = 0; i < rawText.length(); i++) {
			char character = rawText.charAt(i);
			if (betweenChars.contains(character) && (previousChar == null || !betweenChars.contains(previousChar))) {
				// Don't divide e.g. "U.S"
				Character nextChar = null;
				if (i < rawText.length() - 1)
					nextChar = rawText.charAt(i + 1);
				if (previousChar != null) {
					if (nextChar == null || !((character == ':' || character == '.' || character == ',')
							&& previousChar != ' ' && nextChar != ' '))
						positions.add(i + 1);
				}
			}
			previousChars.add(character);
			previousChar = character;
		}

		this.dividePositions = positions;
		return positions;
	}

	public List<Sentence> getPartSentences() {
		return partSentences;
	}

	public List<Integer> getDividePositions() {
		return dividePositions;
	}

	public boolean isTranslated() {
		return isTranslated;
	}

	public void setTranslated(boolean isTranslated) {
		this.isTranslated = isTranslated;
	}

	public int getNumberOfWords() {
		return numberOfWords;
	}

	public void setNumberOfWords(int numberOfWords) {
		this.numberOfWords = numberOfWords;
	}

	public List<HeidelIntervalString> getHeidelIntervals() {
		return this.heidelIntervals;
	}

	public List<SpotlightLink> getSpotlightLinks() {
		return this.spotlightLinks;
	}

	public void addHeidelIntervalString(HeidelIntervalString heidelIntervalString) {
		if (this.heidelIntervals == null) {
			this.heidelIntervals = new ArrayList<HeidelIntervalString>();
		}
		this.heidelIntervals.add(heidelIntervalString);
	}

	public void addSpotlightLink(SpotlightLink spotlightLink) {
		if (this.spotlightLinks == null) {
			this.spotlightLinks = new ArrayList<SpotlightLink>();
		}
		this.spotlightLinks.add(spotlightLink);
	}

	public void setHeidelIntervals(List<HeidelIntervalString> heidelIntervals) {
		this.heidelIntervals = heidelIntervals;
	}

	public void setDictionaryTranslations(List<Set<String>> translations) {
		this.dictionaryTranslations = translations;
	}

	public List<Set<String>> getDictionaryTranslations() {
		return dictionaryTranslations;
	}

	public void setSnowballStemmedWords(List<String> snowballStemmedWords) {
		this.snowballStemmedWords = snowballStemmedWords;
	}

	public List<String> getSnowballStemmedWords() {
		return snowballStemmedWords;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public void addSentencePair(Sentence otherSentence, SentencePair sp) {
		if (this.involvedInSentencePairs == null)
			this.involvedInSentencePairs = new HashMap<Sentence, SentencePair>();
		this.involvedInSentencePairs.put(otherSentence, sp);
	}

	public SentencePair getSentencePair(Sentence otherSentence) {
		if (this.involvedInSentencePairs == null)
			return null;
		return this.involvedInSentencePairs.get(otherSentence);
	}

	public void printRepresentationWithFeatures() {

		System.out.println(rawText);

		List<String> times = new ArrayList<String>();
		for (HeidelIntervalString time : this.heidelIntervals) {
			times.add(time.getStartTime() + "-" + time.getEndTime());
		}
		if (!times.isEmpty())
			System.out.println("\tTimes: " + StringUtils.join(times, "; "));

		List<String> entityNames = new ArrayList<String>();
		for (DbPediaLink link : getDbPediaLinks()) {
			entityNames.add(link.getEntity().getName(this.language));
		}
		if (this.internalLinks != null) {
			for (InternalLink link : getInternalLinks()) {
				entityNames.add(link.getEntity().getName(this.language));
			}
		}
		if (!entityNames.isEmpty())
			System.out.println("\tLinks: " + StringUtils.join(entityNames, "; "));

	}

}
