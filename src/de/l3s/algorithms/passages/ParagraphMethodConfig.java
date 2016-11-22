package de.l3s.algorithms.passages;

import de.l3s.model.passages.StructureFreedom;

public class ParagraphMethodConfig {

	private Double threshold;

	private Integer gapSize;

	private StructureFreedom limit;

	private String name;

	private boolean alignFirstParagraph = false;

	private static final double DEFAULT_THRESHOLD = 0.2;

	private static final int DEFAULT_GAP_SIZE = 2;

	public ParagraphMethodConfig() {
		super();
		this.threshold = DEFAULT_THRESHOLD;
		this.alignFirstParagraph = false;
	}

	public ParagraphMethodConfig(StructureFreedom limit, int gapSize, double threshold) {
		super();
		this.limit = limit;
		this.threshold = threshold;
		this.gapSize = gapSize;
		this.alignFirstParagraph = false;
	}

	public ParagraphMethodConfig(StructureFreedom limit, int gapSize, double threshold, boolean alignFirstParagraph) {
		super();
		this.limit = limit;
		this.threshold = threshold;
		this.gapSize = gapSize;
		this.alignFirstParagraph = alignFirstParagraph;
	}

	public ParagraphMethodConfig(StructureFreedom limit) {
		super();
		this.limit = limit;
		this.threshold = DEFAULT_THRESHOLD;
		this.gapSize = DEFAULT_GAP_SIZE;
		this.alignFirstParagraph = false;
	}

	public ParagraphMethodConfig(StructureFreedom limit, Integer gapSize) {
		super();
		this.limit = limit;
		this.gapSize = gapSize;
		this.threshold = DEFAULT_THRESHOLD;
		this.alignFirstParagraph = false;
	}

	public ParagraphMethodConfig(StructureFreedom limit, Integer gapSize, boolean alignFirstParagraph) {
		super();
		this.limit = limit;
		this.gapSize = gapSize;
		this.threshold = DEFAULT_THRESHOLD;
		this.alignFirstParagraph = alignFirstParagraph;
	}

	public static ParagraphMethodConfig getDefaultParagraphMethodConfig() {
		return new ParagraphMethodConfig(StructureFreedom.MID, 99);
	}

	public ParagraphMethodConfig(String name) {

		String[] parts = name.split("_");

		boolean isBaseline = false;

		this.alignFirstParagraph = false;

		if (!isBaseline && parts.length > 1) {
			this.limit = StructureFreedom.valueOf(parts[1]);
			if (parts.length > 2) {
				this.gapSize = Integer.parseInt(parts[2]);
				if (parts.length > 3) {
					this.threshold = Double.parseDouble(parts[3]);
					if (parts.length > 4 && parts[4].equals("FP")) {
						this.setAlignFirstParagraph(true);
					}
				}
			}
		}

	}

	public ParagraphMethodConfig(double threshold) {
		this.threshold = threshold;
	}

	public Double getThreshold() {
		return threshold;
	}

	public Integer getGapSize() {
		return gapSize;
	}

	public StructureFreedom getStructureFreedom() {
		return limit;
	}

	public String toString() {

		if (this.name == null) {

			if (this.getStructureFreedom() != null)
				this.name += "_" + this.limit;

			if (this.getGapSize() != null)
				this.name += "_" + this.gapSize;

			if (this.getThreshold() != null)
				this.name += "_" + this.getThreshold();

			if (alignFirstParagraph)
				this.name += "_" + "FP";
		}

		return this.name;
	}

	public boolean isDefault() {
		ParagraphMethodConfig defaultConfig = getDefaultParagraphMethodConfig();

		if (this.getStructureFreedom() == defaultConfig.getStructureFreedom()
				&& this.getThreshold() == defaultConfig.getThreshold()
				&& this.getGapSize() == defaultConfig.getGapSize()
				&& this.isAlignFirstParagraph() == defaultConfig.isAlignFirstParagraph())
			return true;
		else
			return false;
	}

	public boolean isAlignFirstParagraph() {
		return alignFirstParagraph;
	}

	public void setAlignFirstParagraph(boolean alignFirstParagraph) {
		this.alignFirstParagraph = alignFirstParagraph;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

}
