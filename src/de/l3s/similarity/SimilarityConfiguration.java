package de.l3s.similarity;

public class SimilarityConfiguration {

	protected boolean alwaysApplicable = false;
	
	private Double entityAlpha = null;
	
	private Double entityK = null;
	
	private boolean loadTypes = false;

	private boolean onlyLoadWikiAnnotationsWithTypes = false;
	
	private static SimilarityConfiguration instance;

	private SimilarityConfiguration() {
	}

	public static SimilarityConfiguration getInstance() {
		if (SimilarityConfiguration.instance == null) {
			SimilarityConfiguration.instance = new SimilarityConfiguration();
		}
		return SimilarityConfiguration.instance;
	}

	public boolean isAlwaysApplicable() {
		return alwaysApplicable;
	}

	public void setAlwaysApplicable(boolean alwaysApplicable) {
		this.alwaysApplicable = alwaysApplicable;
	}

	public Double getEntityAlpha() {
		return entityAlpha;
	}

	public void setEntityAlpha(Double entityAlpha) {
		this.entityAlpha = entityAlpha;
	}

	public Double getEntityK() {
		return entityK;
	}

	public void setEntityK(Double entityK) {
		this.entityK = entityK;
	}

	public boolean isLoadTypes() {
		return loadTypes;
	}

	public void setLoadTypes(boolean loadTypes) {
		this.loadTypes = loadTypes;
	}

	public boolean isOnlyLoadWikiAnnotationsWithTypes() {
		return onlyLoadWikiAnnotationsWithTypes;
	}

	public void setOnlyLoadWikiAnnotationsWithTypes(boolean onlyLoadWikiAnnotationsWithTypes) {
		this.onlyLoadWikiAnnotationsWithTypes = onlyLoadWikiAnnotationsWithTypes;
	}

	public static void setInstance(SimilarityConfiguration instance) {
		SimilarityConfiguration.instance = instance;
	}

}
