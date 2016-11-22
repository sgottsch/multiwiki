package tfidf;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import model.BinaryComparison;
import model.Revision;
import model.Sentence;
import model.links.ExternalLink;

public class ExternalLinkCollection extends TfIdfCollection implements Collection {

	public ExternalLinkCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		super(comparison, revision1, revision2);
	}

	@Override
	String createTerms(Sentence annotation, Revision revision) {
		String linkText = "";
		HashSet<String> externalLinkUris = new HashSet<String>();
		for (ExternalLink dbl : annotation.getExternalLinks()) {
			externalLinkUris.add(dbl.getLink());
		}
		linkText = StringUtils.join(externalLinkUris, (String) " ");
		annotation.setExternalLinksString(linkText);
		return linkText;
	}

	@Override
	String getTermName() {
		return "externalLinks";
	}

	@Override
	CollectionType getCollectionType() {
		return CollectionType.ExternalLinkCollection;
	}
}
