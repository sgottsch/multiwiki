package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.links.ExternalLink;
import translate.Language;

/**
 * An instance of this class represents a comparison of two or more articles in
 * different languages.
 */
public class NAryComparison {
	private List<Revision> revisions;
	private Map<Language, Revision> revisionsByLanguage;
	private FeatureFrequency<Author> authorFrequencies;
	private FeatureFrequency<Entity> entityFrequencies;
	private FeatureFrequency<String> imageFrequencies;
	private FeatureFrequency<ExternalLink> externalLinkFrequencies;
	private FeatureFrequency<String> externalLinkHostFrequencies;

	public NAryComparison(List<Revision> revisions) {
		this.revisions = revisions;
		this.revisionsByLanguage = new HashMap<Language, Revision>();
		for (Revision revision : revisions) {
			this.revisionsByLanguage.put(revision.getLanguage(), revision);
		}
	}

	public List<Revision> getRevisions() {
		return this.revisions;
	}

	public void setRevisions(List<Revision> revisions) {
		this.revisions = revisions;
	}

	public Map<Language, Revision> getRevisionsByLanguage() {
		return this.revisionsByLanguage;
	}

	public void setRevisionsByLanguage(Map<Language, Revision> revisionsByLanguage) {
		this.revisionsByLanguage = revisionsByLanguage;
	}

	public FeatureFrequency<Author> getAuthorFrequencies() {
		return this.authorFrequencies;
	}

	public FeatureFrequency<Entity> getEntityFrequencies() {
		return this.entityFrequencies;
	}

	public FeatureFrequency<String> getImageFrequencies() {
		return this.imageFrequencies;
	}

	public FeatureFrequency<ExternalLink> getExternalLinkFrequencies() {
		return this.externalLinkFrequencies;
	}

	public FeatureFrequency<String> getExternalLinkHostFrequencies() {
		return this.externalLinkHostFrequencies;
	}

	public void incrementImageFrequences(Revision revision, Set<String> images) {
		if (this.imageFrequencies == null) {
			this.imageFrequencies = new FeatureFrequency<String>();
		}
		this.imageFrequencies.incrementFrequencies(revision, images);
	}

	public void incrementAuthorFrequences(Revision revision, Set<Author> authors) {
		if (this.authorFrequencies == null) {
			this.authorFrequencies = new FeatureFrequency<Author>();
		}
		this.authorFrequencies.incrementFrequencies(revision, authors);
	}

	public void incrementExternalLinkFrequences(Revision revision, Set<ExternalLink> externalLinks) {
		if (this.externalLinkFrequencies == null) {
			this.externalLinkFrequencies = new FeatureFrequency<ExternalLink>();
		}
		this.externalLinkFrequencies.incrementFrequencies(revision, externalLinks);
	}

	public void incrementExternalLinkHostFrequences(Revision revision, Set<String> externalLinkHosts) {
		if (this.externalLinkHostFrequencies == null) {
			this.externalLinkHostFrequencies = new FeatureFrequency<String>();
		}
		this.externalLinkHostFrequencies.incrementFrequencies(revision, externalLinkHosts);
	}

	public void incrementEntityFrequences(Revision revision, Set<Entity> entities) {
		if (this.entityFrequencies == null) {
			this.entityFrequencies = new FeatureFrequency<Entity>();
		}
		this.entityFrequencies.incrementFrequencies(revision, entities);
	}

	public void sortFeatures() {
		this.imageFrequencies.createOrderedFeatureLists();
		this.authorFrequencies.createOrderedFeatureLists();
		this.externalLinkFrequencies.createOrderedFeatureLists();
		this.externalLinkHostFrequencies.createOrderedFeatureLists();
		this.entityFrequencies.createOrderedFeatureLists();
	}
}
