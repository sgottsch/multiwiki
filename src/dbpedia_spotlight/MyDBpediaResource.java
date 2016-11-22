package dbpedia_spotlight;

public class MyDBpediaResource {
	private String uri;
	private int support;

	public MyDBpediaResource(String uri, int support) {
		this.uri = uri;
		this.support = support;
	}

	public String getUri() {
		return this.uri;
	}

	public int getSupport() {
		return support;
	}

}
