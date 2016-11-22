package de.l3s.dbloader;

import java.io.File;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import de.l3s.db.DBGetter;
import de.l3s.db.DbObject;
import de.l3s.db.QueryParam;
import de.l3s.db.tables.Revision_Author_DB;
import de.l3s.db.tables.Revision_ExternalLink_DB;
import de.l3s.db.tables.Revision_Image_DB;
import de.l3s.model.Author;
import de.l3s.model.Entity;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.links.DbPediaLink;
import de.l3s.model.links.ExternalLink;
import de.l3s.wiki.WikiWords;

public class RevisionDataLoader {

	private Connection conn;
	private DBGetter dbget;

	public RevisionDataLoader(Connection conn, DBGetter dbget) {
		this.conn = conn;
		this.dbget = dbget;
	}

	protected void loadExternalLinksOfRevision(Revision revision, Revision otherRevisionFromComparison) {

		long revisionId = revision.getId();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(de.l3s.db.tables.Revision_DB.revision_id_attr, revisionId));

		Set<DbObject> dbRevisionExternalLinks = dbget.retrieveSelected(conn, new de.l3s.db.tables.Revision_ExternalLink_DB(),
				null, qparam_and, null);

		for (DbObject dbExternalLinkObj : dbRevisionExternalLinks) {

			de.l3s.db.tables.Revision_ExternalLink_DB dbExternalLink = (de.l3s.db.tables.Revision_ExternalLink_DB) dbExternalLinkObj;

			String externalLinkUri = dbExternalLink.getString(Revision_ExternalLink_DB.external_link_uri_attr);

			ExternalLink externalLink;
			if (otherRevisionFromComparison != null
					&& otherRevisionFromComparison.getExternalLinkUris().containsKey(externalLinkUri)) {
				externalLink = otherRevisionFromComparison.getExternalLinkUris().get(externalLinkUri);
			} else if (revision.getExternalLinkUris().containsKey(externalLinkUri)) {
				externalLink = revision.getExternalLinkUris().get(externalLinkUri);
			} else {
				externalLink = new ExternalLink(
						dbExternalLink.getString(de.l3s.db.tables.Sentence_ExternalLink_DB.external_link_uri_attr),
						dbExternalLink.getString(de.l3s.db.tables.Sentence_ExternalLink_DB.external_link_host_attr));
			}
			revision.addExternalLink(externalLink,
					dbExternalLink.getInt(de.l3s.db.tables.Revision_ExternalLink_DB.number_of_occurrences_attr));
		}
	}

	protected void loadInternalLinksOfRevision(Revision revision) {
		long revisionId = revision.getId();
		try {
			String query1 = "SELECT number_of_occurrences, wiki_link " + " FROM revision_internal_link al "
					+ " WHERE al.language = ? AND revision_id = ?  AND wiki_link NOT LIKE 'Wikipedia:%' ";
			PreparedStatement pstmt = this.conn.prepareStatement(query1);
			pstmt.setString(1, revision.getLanguage().getLanguage());
			pstmt.setLong(2, revisionId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String wikiLink = rs.getString("wiki_link");
				wikiLink = URLDecoder.decode(wikiLink, "UTF8");
				int numberOfOccurences = rs.getInt("number_of_occurrences");
				Entity entity = DataLoader.buildEntity(revision, wikiLink);
				if (entity == null)
					continue;
				revision.addInternalLinkEntity(DataLoader.buildEntity(revision, wikiLink), numberOfOccurences);
				revision.addInternalLinkEntity(entity, numberOfOccurences);
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void buildDBpediaLinksOfRevision(Revision revision) {
		for (Sentence annotation : revision.getSentences()) {
			for (DbPediaLink dbpediaLink : annotation.getDbPediaLinks()) {
				revision.addDBPediaLink(dbpediaLink.getEntity());
			}
		}
	}

	protected void loadRevisionHistory(Revision revision) {

		LinkedHashMap<Date, Integer> editsPerDate = new LinkedHashMap<Date, Integer>();
		LinkedHashMap<Date, Integer> sizePerDate = new LinkedHashMap<Date, Integer>();

		String query = "SELECT DATE(date) date, COUNT(*) cnt, size FROM revision_history WHERE article_uri = ? "
				+ " GROUP BY DATE(date)" + " ORDER BY DATE(date) ASC";

		try {
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, revision.getArticleUri());
			ResultSet res = pstmt.executeQuery();

			while (res.next()) {
				int numberOfEdits = res.getInt("cnt");
				int size = res.getInt("size");
				Date date = res.getDate("date");
				editsPerDate.put(date, numberOfEdits);
				sizePerDate.put(date, size);
			}
			res.close();

			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		revision.setNumberOfEditsPerDate(editsPerDate);
		revision.setSizePerDate(sizePerDate);
	}

	protected void loadAuthors(Revision revision, Revision otherRevisionFromComparison) {
		long revisionId = revision.getId();

		try {

			String query1 = "SELECT " + de.l3s.db.tables.Author_DB.name_attr + ", " + de.l3s.db.tables.Author_DB.location_attr + ", "
					+ Revision_Author_DB.edits_attr + " FROM " + new Revision_Author_DB().getTableName() + " JOIN "
					+ new de.l3s.db.tables.Author_DB().getTableName() + " ON(" + Revision_Author_DB.author_attr + "="
					+ de.l3s.db.tables.Author_DB.name_attr + ") WHERE " + Revision_Author_DB.revision_number_attr + "=?";

			PreparedStatement pstmt = conn.prepareStatement(query1);
			pstmt.setLong(1, revisionId);

			ResultSet res = pstmt.executeQuery();

			while (res.next()) {

				Author author = null;
				String authorName = res.getString(de.l3s.db.tables.Author_DB.name_attr);
				// Map same authors to same instances (over different
				// revisions)?
				if (otherRevisionFromComparison != null
						&& otherRevisionFromComparison.getAuthorNames().containsKey(authorName)) {
					author = otherRevisionFromComparison.getAuthorNames().get(authorName);
				} else if (res.getString(de.l3s.db.tables.Author_DB.location_attr) == null)
					author = new Author(res.getString(de.l3s.db.tables.Author_DB.name_attr));
				else
					author = new Author(res.getString(de.l3s.db.tables.Author_DB.name_attr),
							res.getString(de.l3s.db.tables.Author_DB.location_attr));

				revision.addAuthor(author, res.getInt(Revision_Author_DB.edits_attr));

			}

			res.close();

			pstmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		revision.calculateNumberOfEdits();

	}

	protected void loadImages(Revision revision) {

		Set<String> forbiddenImages = WikiWords.getForbiddenImages();

		long revisionId = revision.getId();

		Vector<QueryParam> qparam_and = new Vector<QueryParam>();
		qparam_and.add(new QueryParam(de.l3s.db.tables.Revision_Image_DB.revision_number_attr, revisionId));

		Set<DbObject> dbImages = dbget.retrieveSelected(conn, new de.l3s.db.tables.Revision_Image_DB(), null, qparam_and,
				null);

		for (DbObject dboImage : dbImages) {
			Revision_Image_DB dbImage = (de.l3s.db.tables.Revision_Image_DB) dboImage;

			// No mapping to same image instances, because they are just strings
			String imageUri = dbImage.getString(de.l3s.db.tables.Revision_Image_DB.image_uri_attr);

			String fileName = new File(imageUri).getName();

			// ignore "standard" Wikipedia images that are not about the article
			// itself
			if (!forbiddenImages.contains(fileName))
				revision.addImage(imageUri, dbImage.getInt(de.l3s.db.tables.Revision_Image_DB.number_of_occurrences_attr));
		}
	}

}
