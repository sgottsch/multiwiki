package model.links;

public abstract class Link {
	
	private Integer startPosition;
	private Integer endPosition;
	private int annotationNumber;

	public Link(Integer startPosition, Integer endPosition) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	public Link() {
	}

	public Integer getStartPosition() {
		return this.startPosition;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	public Integer getEndPosition() {
		return this.endPosition;
	}

	public void setEndPosition(Integer endPosition) {
		this.endPosition = endPosition;
	}

	public int getAnnotationNumber() {
		return this.annotationNumber;
	}

	public void setAnnotationNumber(int annotationNumber) {
		this.annotationNumber = annotationNumber;
	}
}
