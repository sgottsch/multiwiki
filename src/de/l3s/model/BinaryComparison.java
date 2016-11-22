package de.l3s.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.model.links.ExternalLink;
import de.l3s.similarity.revision.AuthorLocationSimilarity;
import de.l3s.tfidf.CollectionType;
import de.l3s.tfidf.TfIdfCollection;
import de.l3s.translate.Language;
import edu.stanford.nlp.util.StringUtils;

/**
 * Two revisions in different languages that are to be compared are treated as a
 * binary comparison.
 */
public class BinaryComparison {

	private Set<Revision> revisions;

	private Date date;

	private long id1;

	private long id2;

	private String title;

	private Revision revision1;

	private Revision revision2;

	private Set<SentencePair> similarParagraphs;

	private Map<Language, Revision> revisionsOfLanguage;

	// private ExternalLinkCollection externalLinkCollection;
	// private ExternalLinkHostCollection externalLinkHostCollection;
	// private WordCollection wordCollection;
	// private InternalLinkCollection internalLinkCollection;
	// private DbpediaLinkCollection dbpediaLinkCollection;
	// private DbpediaLinkCollectionAllTypes dbpediaLinkCollectionAllTypes;
	// private DbpediaLinkCollectionNoTypes dbpediaLinkCollectionNoTypes;
	// private EntityCollection entityCollectionAllTypesNew;
	// private HeidelTimeCollection heidelTimeCollection;

	// private Map<CollectionType, Collection> collections;

	private FeatureComparison<String> imageComparison;
	private FeatureComparison<ExternalLink> externalLinkComparison;
	private FeatureComparison<String> externalLinkHostComparison;
	private FeatureComparison<Author> authorComparison;
	private FeatureComparison<Entity> entityComparison;
	private AuthorLocationSimilarity authorLocationSimilarity;
	private double textLengthSimilarity;
	private double textCoverSimilarity;
	private double textOverlapSimilarity;
	private double overallSimilarity;
	private String overallSimilarityString;

	private Map<CollectionType, TfIdfCollection> tfIdfCollections;

	public BinaryComparison(String title, long id1, long id2) {
		this.title = title;
		this.id1 = id1;
		this.id2 = id2;
		this.revisionsOfLanguage = new HashMap<Language, Revision>();
	}

	public BinaryComparison(Revision revision1, Revision revision2) {
		this.title = revision1.getTitle();
		if (revision2.getLanguage() == Language.EN)
			this.title = revision2.getTitle();
		this.id1 = revision1.getId();
		this.id2 = revision2.getId();
		this.revision1 = revision1;
		this.revision2 = revision2;
		this.revisionsOfLanguage = new HashMap<Language, Revision>();
		revisionsOfLanguage.put(revision1.getLanguage(), revision1);
		revisionsOfLanguage.put(revision2.getLanguage(), revision2);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Revision getRevision1() {
		return revision1;
	}

	public void setRevision1(Revision revision1) {
		this.revision1 = revision1;
		revisionsOfLanguage.put(revision1.getLanguage(), revision1);
	}

	public Revision getRevision2() {
		return revision2;
	}

	public void setRevision2(Revision revision2) {
		this.revision2 = revision2;
		revisionsOfLanguage.put(revision2.getLanguage(), revision2);
	}

	public Set<Revision> getRevisions() {
		return revisions;
	}

	public void setRevisions(Set<Revision> revisions) {
		this.revisions = revisions;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getId1() {
		return this.id1;
	}

	public long getId2() {
		return this.id2;
	}

	// public WordCollection getWordCollection() {
	// return wordCollection;
	// }
	//
	// public void setWordCollection(WordCollection wordCollection) {
	// this.wordCollection = wordCollection;
	// }
	//
	// public ExternalLinkHostCollection getExternalLinkHostCollection() {
	// return externalLinkHostCollection;
	// }
	//
	// public void setExternalLinkHostCollection(ExternalLinkHostCollection
	// externalLinkHostCollection) {
	// this.externalLinkHostCollection = externalLinkHostCollection;
	// }
	//
	// public ExternalLinkCollection getExternalLinkCollection() {
	// return externalLinkCollection;
	// }
	//
	// public void setExternalLinkCollection(ExternalLinkCollection
	// externalLinkCollection) {
	// this.externalLinkCollection = externalLinkCollection;
	// }
	//
	// public InternalLinkCollection getInternalLinkCollection() {
	// return internalLinkCollection;
	// }
	//
	// public void setInternalLinkCollection(InternalLinkCollection
	// internalLinkCollection) {
	// this.internalLinkCollection = internalLinkCollection;
	// }
	//
	// public DbpediaLinkCollection getDbpediaLinkCollection() {
	// return dbpediaLinkCollection;
	// }
	//
	// public void setDbpediaLinkCollection(DbpediaLinkCollection
	// dbpediaLinkCollection) {
	// this.dbpediaLinkCollection = dbpediaLinkCollection;
	// }
	//
	// public DbpediaLinkCollectionAllTypes getDbpediaLinkCollectionAllTypes() {
	// return dbpediaLinkCollectionAllTypes;
	// }
	//
	// public void
	// setDbpediaLinkCollectionAllTypes(DbpediaLinkCollectionAllTypes
	// dbpediaLinkCollectionAllTypes) {
	// this.dbpediaLinkCollectionAllTypes = dbpediaLinkCollectionAllTypes;
	// }
	//
	// public EntityCollection getEntityCollectionAllTypesNew() {
	// return entityCollectionAllTypesNew;
	// }
	//
	// public void setEntityCollectionAllTypesNew(EntityCollection
	// entityCollectionAllTypesNew) {
	// this.entityCollectionAllTypesNew = entityCollectionAllTypesNew;
	// }
	//
	// public DbpediaLinkCollectionNoTypes getDbpediaLinkCollectionNoTypes() {
	// return dbpediaLinkCollectionNoTypes;
	// }
	//
	// public void setDbpediaLinkCollectionNoTypes(DbpediaLinkCollectionNoTypes
	// dbpediaLinkCollectionNoTypes) {
	// this.dbpediaLinkCollectionNoTypes = dbpediaLinkCollectionNoTypes;
	// }
	//
	// public void setHeidelTimeCollection(HeidelTimeCollection
	// heidelTimeCollection) {
	// this.heidelTimeCollection = heidelTimeCollection;
	// }
	//
	// public HeidelTimeCollection getHeidelTimeCollection() {
	// return this.heidelTimeCollection;
	// }

	public FeatureComparison<String> getImageComparison() {
		return imageComparison;
	}

	public void setImageComparison(FeatureComparison<String> imageComparison) {
		this.imageComparison = imageComparison;
	}

	public FeatureComparison<ExternalLink> getExternalLinkComparison() {
		return externalLinkComparison;
	}

	public void setExternalLinkComparison(FeatureComparison<ExternalLink> externalLinkComparison) {
		this.externalLinkComparison = externalLinkComparison;
	}

	public FeatureComparison<String> getExternalLinkHostComparison() {
		return externalLinkHostComparison;
	}

	public void setExternalLinkHostComparison(FeatureComparison<String> externalLinkHostComparison) {
		this.externalLinkHostComparison = externalLinkHostComparison;
	}

	public FeatureComparison<Author> getAuthorComparison() {
		return authorComparison;
	}

	public void setAuthorComparison(FeatureComparison<Author> authorComparison) {
		this.authorComparison = authorComparison;
	}

	public FeatureComparison<Entity> getEntityComparison() {
		return entityComparison;
	}

	public void setEntityComparison(FeatureComparison<Entity> entityComparison) {
		this.entityComparison = entityComparison;
	}

	public AuthorLocationSimilarity getAuthorLocationSimilarity() {
		return authorLocationSimilarity;
	}

	public void setAuthorLocationSimilarity(AuthorLocationSimilarity authorLocationSimilarity) {
		this.authorLocationSimilarity = authorLocationSimilarity;
	}

	public double getTextLengthSimilarity() {
		return textLengthSimilarity;
	}

	public void setTextLengthSimilarity(double textLengthSimilarity) {
		this.textLengthSimilarity = textLengthSimilarity;
	}

	public double getTextCoverSimilarity() {
		return textCoverSimilarity;
	}

	public void setTextCoverSimilarity(double textCoverSimilarity) {
		this.textCoverSimilarity = textCoverSimilarity;
	}

	public double getTextOverlapSimilarity() {
		return textOverlapSimilarity;
	}

	public void setTextOverlapSimilarity(double textSimilarity) {
		this.textOverlapSimilarity = textSimilarity;
	}

	public double getOverallSimilarity() {
		return overallSimilarity;
	}

	public void setOverallSimilarity(double overallSimilarity) {
		this.overallSimilarity = overallSimilarity;
	}

	public void createOverallSimilarityString() {
		if (this.overallSimilarityString == null) {
			List<String> overallSimilarityStringValues = new ArrayList<String>();
			overallSimilarityStringValues.add("TextLengthSimilarity:" + textLengthSimilarity);
			overallSimilarityStringValues.add("TextCoverSimilarity:" + textCoverSimilarity);
			overallSimilarityStringValues.add("TextOverlapSimilarity:" + textOverlapSimilarity);
			overallSimilarityStringValues.add("AuthorSimilarity:" + authorComparison.getSimilarity());
			overallSimilarityStringValues.add("AuthorLocationSimilarity:" + authorLocationSimilarity.getSimilarity());
			overallSimilarityStringValues.add("TextLengthSimilarity:" + textLengthSimilarity);
			overallSimilarityStringValues.add("ImageSimilarity:" + imageComparison.getSimilarity());
			overallSimilarityStringValues.add("ExternalLinkSimilarity:" + externalLinkComparison.getSimilarity());
			overallSimilarityStringValues
					.add("ExternalLinkHostSimilarity:" + externalLinkHostComparison.getSimilarity());
			overallSimilarityStringValues.add("EntitySimilarity:" + entityComparison.getSimilarity());
			this.overallSimilarityString = StringUtils.join(overallSimilarityStringValues, ";");
		}
	}

	public String getOverallSimilarityString() {
		return overallSimilarityString;
	}

	public void setOverallSimilarityString(String overallSimilarityString) {
		this.overallSimilarityString = overallSimilarityString;
	}

	public Set<SentencePair> getSimilarParagraphs() {
		return similarParagraphs;
	}

	public void setSimilarParagraphs(Set<SentencePair> similarParagraphs) {
		this.similarParagraphs = similarParagraphs;
	}

	public Revision getRevisionOfLanguage(Language lang) {
		return this.revisionsOfLanguage.get(lang);
	}

	public void addTfIdfCollection(CollectionType collectionType, TfIdfCollection tfIdfCollection) {
		if (tfIdfCollections == null)
			tfIdfCollections = new HashMap<CollectionType, TfIdfCollection>();
		this.tfIdfCollections.put(collectionType, tfIdfCollection);
	}

	public TfIdfCollection getTfIdfCollection(CollectionType collectionType) {
		return this.tfIdfCollections.get(collectionType);
	}

}
