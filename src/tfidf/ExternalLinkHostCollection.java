package tfidf;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import model.BinaryComparison;
import model.Revision;
import model.Sentence;
import model.links.ExternalLink;

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
