package de.l3s.tfidf;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Revision;
import de.l3s.model.Sentence;
import de.l3s.model.links.ExternalLink;

public class ExternalLinkHostCollection extends TfIdfCollection implements Collection {
	public ExternalLinkHostCollection(BinaryComparison comparison, Revision revision1, Revision revision2) {
		super(comparison, revision1, revision2);
	}

	@Override
	String createTerms(Sentence annotation, Revision revision) {
		String linkText = "";
		Set<String> ExternalLinkHostUris = new HashSet<String>();
		for (ExternalLink dbl : annotation.getExternalLinks()) {
			String host = dbl.getHost();
			ExternalLinkHostUris.add(host);
		}
		linkText = StringUtils.join(ExternalLinkHostUris, (String) " ");
		annotation.setExternalLinkHostString(linkText);
		return linkText;
	}

	@Override
	String getTermName() {
		return "externalLinkHosts";
	}

	@Override
	CollectionType getCollectionType() {
		return CollectionType.ExternalLinkHostCollection;
	}
}
