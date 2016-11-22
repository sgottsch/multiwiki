package de.l3s.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.algorithms.passages.ParagraphMethodConfig;
import de.l3s.algorithms.passages.SimilarParagraphFinder;
import de.l3s.algorithms.sentences.SimilarSentenceFinder;
import de.l3s.model.Author;
import de.l3s.model.BinaryComparison;
import de.l3s.model.Entity;
import de.l3s.model.FeatureComparison;
import de.l3s.model.NAryComparison;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.SentenceType;
import de.l3s.model.links.ExternalLink;
import de.l3s.similarity.revision.AuthorLocationSimilarity;
import de.l3s.similarity.text.TextLengthSimilarity;
import de.l3s.similarity.text.TextOverlapSimilarity;

/**
 * Class that controls the comparison of articles. That means, for an input of a
 * comparison as a article pair (or triple), several operations are started,e.g.
 * the computation of overall similarity and the paragraph alignment.
 */
public class Comparator {

	private NAryComparison nAryComparison;

	private ParagraphMethodConfig method;

	public Comparator() {
		this(ParagraphMethodConfig.getDefaultParagraphMethodConfig());
	}

	public Comparator(ParagraphMethodConfig method) {
		this.method = method;
	}

	public NAryComparison compare(BinaryComparison binaryComparison) {
		List<Revision> revisions = new ArrayList<Revision>();
		revisions.add(binaryComparison.getRevision1());
		revisions.add(binaryComparison.getRevision2());
		this.nAryComparison = new NAryComparison(revisions);
		compareNAry();
		compareBinary(binaryComparison);

		return nAryComparison;
	}

	private void compareNAry() {

		for (Revision revision : nAryComparison.getRevisions()) {
			// images
			nAryComparison.incrementImageFrequences(revision, revision.getImages().keySet());

			// external links
			nAryComparison.incrementExternalLinkFrequences(revision, revision.getExternalLinks().keySet());

			// external link hosts
			Set<String> externalLinkHosts = new HashSet<String>();
			for (ExternalLink link : revision.getExternalLinks().keySet())
				externalLinkHosts.add(link.getHost());

			nAryComparison.incrementExternalLinkHostFrequences(revision, externalLinkHosts);

			// authors
			nAryComparison.incrementAuthorFrequences(revision, revision.getAuthors().keySet());

			// Entities
			nAryComparison.incrementEntityFrequences(revision, revision.getInternalAndDBpediaEntities().keySet());

			createWholeArticleAnnotation(revision);
		}

		nAryComparison.sortFeatures();
	}

	private void compareBinary(BinaryComparison binaryComparison) {

		Revision revision1 = binaryComparison.getRevision1();
		Revision revision2 = binaryComparison.getRevision2();

		SimilarSentenceFinder ssf = new SimilarSentenceFinder();
		// ssf.loadWeightConfigurationDict();

		ssf.loadDefaultTranslationSimilarityFunction();

		binaryComparison.setSimilarParagraphs(ssf.findSimilarParagraphs(binaryComparison, method));

		// images
		binaryComparison
				.setImageComparison(new FeatureComparison<String>(revision1.getImages(), revision2.getImages()));

		// authors
		binaryComparison
				.setAuthorComparison(new FeatureComparison<Author>(revision1.getAuthors(), revision2.getAuthors()));

		// external links
		binaryComparison.setExternalLinkComparison(
				new FeatureComparison<ExternalLink>(revision1.getExternalLinks(), revision2.getExternalLinks()));

		// external link hosts
		Map<String, Integer> externalLinkHosts1 = new HashMap<String, Integer>();
		Map<String, Integer> externalLinkHosts2 = new HashMap<String, Integer>();

		for (ExternalLink link : revision1.getExternalLinks().keySet()) {
			String host = link.getHost();
			int numberOfOccurences = revision1.getExternalLinks().get(link);

			if (!externalLinkHosts1.containsKey(host))
				externalLinkHosts1.put(host, numberOfOccurences);
			else
				externalLinkHosts1.put(host, externalLinkHosts1.get(host) + numberOfOccurences);
		}
		for (ExternalLink link : revision2.getExternalLinks().keySet()) {
			String host = link.getHost();
			int numberOfOccurences = revision2.getExternalLinks().get(link);

			if (!externalLinkHosts2.containsKey(host))
				externalLinkHosts2.put(host, numberOfOccurences);
			else
				externalLinkHosts2.put(host, externalLinkHosts2.get(host) + numberOfOccurences);
		}

		binaryComparison
				.setExternalLinkHostComparison(new FeatureComparison<String>(externalLinkHosts1, externalLinkHosts2));

		// Entities
		// only take those entities existing in both languages
		Map<Entity, Integer> entities1 = new HashMap<Entity, Integer>();
		for (Entity entity : revision1.getInternalAndDBpediaEntities().keySet()) {
			if (entity.linkExistsInBothLanguages(revision1.getLanguage(), revision2.getLanguage())) {
				int numberOfOccurences = revision1.getInternalAndDBpediaEntities().get(entity);
				if (!entities1.containsKey(entity))
					entities1.put(entity, numberOfOccurences);
				else
					entities1.put(entity, entities1.get(entity) + numberOfOccurences);
			}
		}

		Map<Entity, Integer> entities2 = new HashMap<Entity, Integer>();
		for (Entity entity : revision2.getInternalAndDBpediaEntities().keySet()) {
			if (entity.linkExistsInBothLanguages(revision2.getLanguage(), revision2.getLanguage())) {
				int numberOfOccurences = revision2.getInternalAndDBpediaEntities().get(entity);
				if (!entities2.containsKey(entity))
					entities2.put(entity, numberOfOccurences);
				else
					entities2.put(entity, entities2.get(entity) + numberOfOccurences);
			}
		}

		binaryComparison.setEntityComparison(new FeatureComparison<Entity>(entities1, entities2));

		// author location similarity
		AuthorLocationSimilarity authorLocationSimilarity = new AuthorLocationSimilarity();
		authorLocationSimilarity.calculateSimilarity(revision1, revision2);
		binaryComparison.setAuthorLocationSimilarity(authorLocationSimilarity);

		// Text length
		TextLengthSimilarity textLengthSimilarityMeasure = new TextLengthSimilarity();
		double textLengthSimilarity = textLengthSimilarityMeasure.calculateSimilarity(binaryComparison,
				revision1.getArticleAnnotation(), revision2.getArticleAnnotation());
		binaryComparison.setTextLengthSimilarity(textLengthSimilarity);

		// Text Overlap
		TextOverlapSimilarity textOverlapSimilarityMeasure = new TextOverlapSimilarity();
		double textOverlapSimilarity = textOverlapSimilarityMeasure.calculateSimilarity(binaryComparison,
				revision1.getArticleAnnotation(), revision2.getArticleAnnotation());
		binaryComparison.setTextOverlapSimilarity(textOverlapSimilarity);

		// Text Coverage: Percentage of text occurring in the other revision
		binaryComparison.setTextCoverSimilarity(ssf.getTotalCoverage());

		double overallSimilarity = 0d;

		// 50% text
		double overallTextSimilarity = 0d;
		overallTextSimilarity += (1d / 3) * binaryComparison.getTextLengthSimilarity();
		overallTextSimilarity += (1d / 3) * binaryComparison.getTextOverlapSimilarity();
		overallTextSimilarity += (1d / 3) * binaryComparison.getTextCoverSimilarity();

		// 50% rest: authors, external links, entities, images
		double overallMetaSimilarity = 0d;

		double authorSimilarity = 0d;
		authorSimilarity += (3d / 4) * binaryComparison.getAuthorLocationSimilarity().getSimilarity();
		authorSimilarity += (1d / 4) * binaryComparison.getAuthorComparison().getSimilarity();

		double externalLinkSimilarity = 0d;
		externalLinkSimilarity += (1d / 4) * binaryComparison.getExternalLinkComparison().getSimilarity();
		externalLinkSimilarity += (3d / 4) * binaryComparison.getExternalLinkHostComparison().getSimilarity();

		overallMetaSimilarity += (1d / 4) * authorSimilarity;
		overallMetaSimilarity += (1d / 4) * externalLinkSimilarity;
		overallMetaSimilarity += (1d / 4) * binaryComparison.getEntityComparison().getSimilarity();
		overallMetaSimilarity += (1d / 4) * binaryComparison.getImageComparison().getSimilarity();

		overallSimilarity += (1d / 2) * overallTextSimilarity;
		overallSimilarity += (1d / 2) * overallMetaSimilarity;

		binaryComparison.setOverallSimilarity(overallSimilarity);
		binaryComparison.createOverallSimilarityString();
	}

	private void createWholeArticleAnnotation(Revision revision) {

		SimilarParagraphFinder spf = new SimilarParagraphFinder(null, null, null);

		Sentence articleAnnotation = null;

		for (Sentence annotation : revision.getSentences()) {

			if (!annotation.isImportant() || annotation.isInInfobox()
					|| (annotation.getType() != SentenceType.SENTENCE && annotation.getType() != SentenceType.TITLE
					// && annotation.getType() != AnnotationType.LIST_ELEMENT
					))
				continue;

			if (articleAnnotation == null)
				articleAnnotation = annotation;
			else {
				List<Sentence> annotationsToCombine = new ArrayList<Sentence>();
				annotationsToCombine.add(articleAnnotation);
				annotationsToCombine.add(annotation);
				articleAnnotation = spf.combineSentences(annotationsToCombine);
			}
		}
		revision.setArticleAnnotation(articleAnnotation);
	}

}
