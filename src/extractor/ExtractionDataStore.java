package extractor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import db.DBGetter;
import db.tables.Article_DB;
import db.tables.Author_DB;
import db.tables.Comparison_DB;
import db.tables.Paragraph_DB;
import db.tables.Paragraph_Image_DB;
import db.tables.RevisionHistory_DB;
import db.tables.Revision_Author_DB;
import db.tables.Revision_DB;
import db.tables.Revision_ExternalLink_DB;
import db.tables.Revision_Image_DB;
import db.tables.Revision_InternalLink_DB;
import db.tables.Sentence_DB;
import db.tables.Sentence_ExternalLink_DB;
import db.tables.Sentence_InternalLink_DB;

public class ExtractionDataStore {

	// Db objects

	// Paragraph
	private Set<Paragraph_DB> paragraphs;
	private Set<Paragraph_Image_DB> paragraph_images;

	private Set<Sentence_InternalLink_DB> annotation_internalLinks;
	private Set<Sentence_ExternalLink_DB> annotation_externalLinks;
	private LinkedHashSet<Sentence_DB> annotations;

	private Set<Article_DB> articles;

	private Set<RevisionHistory_DB> revisionHistories = new HashSet<RevisionHistory_DB>();
	private Set<Revision_DB> revisions = new HashSet<Revision_DB>();

	private Set<Comparison_DB> comparisons = new HashSet<Comparison_DB>();

	private Set<Author_DB> authors = new HashSet<Author_DB>();
	private Set<String> existingAuthors;

	private Set<Revision_Author_DB> revision_authors = new HashSet<Revision_Author_DB>();

	private Set<Revision_Image_DB> revision_images = new HashSet<Revision_Image_DB>();

	private Set<Revision_InternalLink_DB> revision_internalLinks = new HashSet<Revision_InternalLink_DB>();
	private Set<Revision_ExternalLink_DB> revision_externalLinks = new HashSet<Revision_ExternalLink_DB>();

	private Set<RevisionHistory_DB> revisionsHistoriesToUpdate;

	public ExtractionDataStore() {
		revisionHistories = new HashSet<RevisionHistory_DB>();
		revisions = new HashSet<Revision_DB>();

		articles = new HashSet<Article_DB>();

		comparisons = new HashSet<Comparison_DB>();

		authors = new HashSet<Author_DB>();
		revision_authors = new HashSet<Revision_Author_DB>();

		revision_images = new HashSet<Revision_Image_DB>();

		annotations = new LinkedHashSet<Sentence_DB>();

		annotation_internalLinks = new HashSet<Sentence_InternalLink_DB>();
		annotation_externalLinks = new HashSet<Sentence_ExternalLink_DB>();

		paragraphs = new HashSet<Paragraph_DB>();
		paragraph_images = new HashSet<Paragraph_Image_DB>();

		revision_internalLinks = new HashSet<Revision_InternalLink_DB>();
		revision_externalLinks = new HashSet<Revision_ExternalLink_DB>();

		existingAuthors = new HashSet<String>();
	}

	public void addAuthors(Set<Author_DB> authors) {

		for (Author_DB author : authors) {
			String authorName = author.getString(Author_DB.name_attr);

			if (!existingAuthors.contains(authorName))
				this.authors.add(author);
		}

	}

	public void addArticles(Collection<Article_DB> articles) {
		this.articles.addAll(articles);
	}

	public void addRevisions(Set<Revision_DB> revisions) {
		this.revisions.addAll(revisions);
	}

	public void addRevisionHistories(Set<RevisionHistory_DB> revisionHistories) {
		this.revisionHistories.addAll(revisionHistories);
	}

	public void addRevisionHistories(List<RevisionHistory_DB> revisionHistories) {
		this.revisionHistories.addAll(revisionHistories);
	}

	public void addComparisons(Set<Comparison_DB> comparisons) {
		this.comparisons.addAll(comparisons);
	}

	public void addComparison(Comparison_DB comparison) {
		this.comparisons.add(comparison);
	}

	public void addRevisionAuthors(Set<Revision_Author_DB> revision_authors) {
		this.revision_authors.addAll(revision_authors);
	}

	public void addAuthor(Author_DB author) {
		this.authors.add(author);
	}

	public void addRevisionAuthor(Revision_Author_DB revision_author) {
		this.revision_authors.add(revision_author);
	}

	public void addRevisionImages(Set<Revision_Image_DB> revision_images) {
		this.revision_images.addAll(revision_images);
	}

	public Set<Revision_Image_DB> getRevision_images() {
		return this.revision_images;
	}

	public void addRevisionImage(Revision_Image_DB revision_image) {
		this.revision_images.add(revision_image);
	}

	public void addParagraphs(Set<Paragraph_DB> paragraphs) {
		this.paragraphs.addAll(paragraphs);
	}

	public void addAnnotations(Set<Sentence_DB> annotations) {
		this.annotations.addAll(annotations);
	}

	public void addParagraphImages(Set<Paragraph_Image_DB> paragraph_images) {
		this.paragraph_images.addAll(paragraph_images);
	}

	public void addInternalLinks(Set<Sentence_InternalLink_DB> annotation_internalLinks) {
		this.annotation_internalLinks.addAll(annotation_internalLinks);
	}

	public void addExternalLinks(Set<Sentence_ExternalLink_DB> annotation_externalLinks) {
		this.annotation_externalLinks.addAll(annotation_externalLinks);
	}

	public Set<Sentence_ExternalLink_DB> getAnnotation_externalLinks() {
		return this.annotation_externalLinks;
	}

	public void addRevision(Revision_DB revision) {
		this.revisions.add(revision);
	}

	public void store(Connection conn, DBGetter dbget) {

		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (this.revisionsHistoriesToUpdate != null) {
			for (RevisionHistory_DB rh : revisionsHistoriesToUpdate) {
				dbget.updateDbObjectPrepared(conn, rh);
			}
		}

		dbget.storeDbObjectsPrepared(conn, articles, true);
		dbget.storeDbObjectsPrepared(conn, revisionHistories, true);
		dbget.storeDbObjectsPrepared(conn, revisions, true);
		dbget.storeDbObjectsPrepared(conn, comparisons, false);
		dbget.storeDbObjectsPrepared(conn, authors, true);

		dbget.storeDbObjectsPrepared(conn, revision_authors, true); // false
		dbget.storeDbObjectsPrepared(conn, revision_images, true); // false
		dbget.storeDbObjectsPrepared(conn, revision_internalLinks, true); // false
		dbget.storeDbObjectsPrepared(conn, revision_externalLinks, true); // false

		// Store the paragraphs
		dbget.storeDbObjectsPrepared(conn, paragraphs, true);

		// Store the relation paragraph -> images
		dbget.storeDbObjectsPrepared(conn, paragraph_images, true);

		// Store the annotations
		dbget.storeDbObjectsPrepared(conn, annotations, true);

		// Store the external links
		dbget.storeDbObjectsPrepared(conn, annotation_externalLinks, true);

		// Store the internal link relations
		// Store the wiki links (don't overwrite!!!)
		dbget.storeDbObjectsPrepared(conn, annotation_internalLinks, true);

		// fill "text" table with the content of the annotations and add
		// "text id" to the annotations.
		System.out.println("move_annotations_to_texts");
		dbget.callProcedure(conn, "move_annotations_to_texts");

		// create annotation_dbpedia link connetions for those sentences that
		// have already been translated some time before
		// System.out.println("move_dbpedia_links_to_annotations");
		// dbget.callProcedure(conn, "move_dbpedia_links_to_annotations");

		try {
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		flush();
	}

	public void flush() {

		revisionsHistoriesToUpdate = new HashSet<RevisionHistory_DB>();

		revisionHistories = new HashSet<RevisionHistory_DB>();
		revisions = new HashSet<Revision_DB>();

		articles = new HashSet<Article_DB>();

		comparisons = new HashSet<Comparison_DB>();

		authors = new HashSet<Author_DB>();
		revision_authors = new HashSet<Revision_Author_DB>();

		revision_images = new HashSet<Revision_Image_DB>();

		annotations = new LinkedHashSet<Sentence_DB>();
		annotation_internalLinks = new HashSet<Sentence_InternalLink_DB>();
		annotation_externalLinks = new HashSet<Sentence_ExternalLink_DB>();

		paragraphs = new HashSet<Paragraph_DB>();
		paragraph_images = new HashSet<Paragraph_Image_DB>();

		revision_internalLinks = new HashSet<Revision_InternalLink_DB>();
		revision_externalLinks = new HashSet<Revision_ExternalLink_DB>();

		existingAuthors = new HashSet<String>();
	}

	public void addParagraphImage(Paragraph_Image_DB paragraph_image) {
		this.paragraph_images.add(paragraph_image);
	}

	public void addParagraph(Paragraph_DB paragraph) {
		this.paragraphs.add(paragraph);
	}

	public void addExternalLink(Sentence_ExternalLink_DB annotationExternalLink) {
		this.annotation_externalLinks.add(annotationExternalLink);

	}

	public void addInternalLink(Sentence_InternalLink_DB annotationInternalLink) {
		this.annotation_internalLinks.add(annotationInternalLink);
	}

	public void addRevisionsHistoriesToUpdate(RevisionHistory_DB revisionsHistoryToUpdate) {
		this.revisionsHistoriesToUpdate.add(revisionsHistoryToUpdate);
	}

	public void addRevisionInternalLink(Revision_InternalLink_DB revision_internalLink) {
		this.revision_internalLinks.add(revision_internalLink);
	}

	public void addRevisionExternalLink(Revision_ExternalLink_DB revision_externalLink) {
		this.revision_externalLinks.add(revision_externalLink);
	}

	public Set<Revision_InternalLink_DB> getRevisionInternalLinks() {
		return revision_internalLinks;
	}

	public Set<Revision_DB> getRevisions() {
		return revisions;
	}

	public Set<Comparison_DB> getComparisons() {
		return comparisons;
	}

	public Set<RevisionHistory_DB> getRevisionHistories() {
		return revisionHistories;
	}

	public Set<Sentence_DB> getAnnotations() {
		return annotations;
	}

	public Set<Sentence_InternalLink_DB> getAnnotation_internalLinks() {
		return annotation_internalLinks;
	}

	public Set<Paragraph_DB> getParagraphs() {
		return paragraphs;
	}

}
