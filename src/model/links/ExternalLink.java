package model.links;

/**
 * An external link is an URI that refers to a web site that usually is not from
 * Wikipedia. These were extracted from the footnoes and assigned to their
 * belonging sentences.
 */
public class ExternalLink extends Link {
	private String link;
	private String host;

	public ExternalLink(String link, String host, int startPosition, int endPosition) {
		super(startPosition, endPosition);
		this.link = link;
		this.host = host;
	}

	public ExternalLink(String link, String host) {
		this.link = link;
		this.host = host;
	}

	public String getLink() {
		return this.link;
	}

	public String getHost() {
		return this.host;
	}
}
