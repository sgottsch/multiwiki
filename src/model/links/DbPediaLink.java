package model.links;

public class DbPediaLink extends EntityLink {
	private long dbId;
	private int originalStartPosition;
	private int originalEndPosition;
	private boolean hasType;

	public DbPediaLink(boolean hasType) {
		this.hasType = hasType;
	}

	public DbPediaLink(Integer startPosition, Integer endPosition, boolean hasType) {
		super(startPosition, endPosition);
		this.hasType = hasType;
	}

	public int getOriginalStartPosition() {
		return this.originalStartPosition;
	}

	public void setOriginalStartPosition(int originalStartPosition) {
		this.originalStartPosition = originalStartPosition;
	}

	public int getOriginalEndPosition() {
		return this.originalEndPosition;
	}

	public void setOriginalEndPosition(int originalEndPosition) {
		this.originalEndPosition = originalEndPosition;
	}

	public boolean hasType() {
		return this.hasType;
	}

	public void setHasType(boolean hasType) {
		this.hasType = hasType;
	}

	public long getDbId() {
		return this.dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}
}
