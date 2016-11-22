package model;

public class Author {
	private String name;
	private String countryCode;

	public Author(String name) {
		this.name = name;
	}

	public Author(String name, String countryCode) {
		this.name = name;
		this.countryCode = countryCode;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountryCode() {
		return this.countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
