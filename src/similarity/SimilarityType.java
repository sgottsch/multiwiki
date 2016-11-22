package similarity;

public enum SimilarityType {

    DbPediaLinkAllTypesSimilarity(SimilarityTypeGroup.OtherSimilarity),
    DbPediaLinkNERsOnlySimilarity(SimilarityTypeGroup.OtherSimilarity),
    EntitySimilarity(SimilarityTypeGroup.OtherSimilarity),
    InternalLinkSimilarity(SimilarityTypeGroup.OtherSimilarity),
    ExternalLinkHostSimilarity(SimilarityTypeGroup.OtherSimilarity),
    ExternalLinkSimilarity(SimilarityTypeGroup.OtherSimilarity),
    NGramSimilarity(SimilarityTypeGroup.TextSimilarity),
    TextLengthSimilarity(SimilarityTypeGroup.TextSimilarity),
    TextOverlapSimilarity(SimilarityTypeGroup.TextSimilarity),
    TimeSimilarity(SimilarityTypeGroup.OtherSimilarity),
    WordCosineSimilarity(SimilarityTypeGroup.TextSimilarity),
    EntityAllTypesSimilarity(SimilarityTypeGroup.OtherSimilarity),
    EntityAllTypesJaccardSimilarity(SimilarityTypeGroup.OtherSimilarity),
    LCSSimilarity(SimilarityTypeGroup.TextSimilarity),
    EntityAllTypesNewSimilarity(SimilarityTypeGroup.OtherSimilarity),
    EntityPairwiseSimilarity(SimilarityTypeGroup.OtherSimilarity),
    TextAndEntitySimilarity(SimilarityTypeGroup.OtherSimilarity),
    TimeStringsOverlap(SimilarityTypeGroup.OtherSimilarity),
    TimeUnweightedSimilarity(SimilarityTypeGroup.OtherSimilarity),
    EntityStandardCosineSimilarity(SimilarityTypeGroup.OtherSimilarity),
    RelativeDocumentPositionSimilarity(SimilarityTypeGroup.OtherSimilarity),
    InternalLinkNumberSimilarity(SimilarityTypeGroup.OtherSimilarity),
    LCSWordSimilarity(SimilarityTypeGroup.TextSimilarity),
    PreviousSentenceCosineSimilarity(SimilarityTypeGroup.NeighbourSimilarity),
    WikiParagraphEntitySimilarity(SimilarityTypeGroup.NeighbourSimilarity),
    HeidelTimeCosineSimilarity(SimilarityTypeGroup.OtherSimilarity),
    HeidelTimeSimilarity(SimilarityTypeGroup.OtherSimilarity),
    HeidelTimeStringsOverlap(SimilarityTypeGroup.OtherSimilarity),
    DictionaryOverlapSimilarity(SimilarityTypeGroup.TextSimilarity),
    DictionaryCosineSimilarity(SimilarityTypeGroup.TextSimilarity),
    ParagraphTimeSimilarity(SimilarityTypeGroup.OtherSimilarity),
    EntityOverlapSimilarity(SimilarityTypeGroup.OtherSimilarity),
    WordEmbeddingSimilarity(SimilarityTypeGroup.TextSimilarity);
	
	private SimilarityTypeGroup group;

	private SimilarityType(SimilarityTypeGroup similarityTypeGroup) {
		this.group = similarityTypeGroup;
	}

	public boolean isTextSimilarity() {
		if (this.group == SimilarityTypeGroup.TextSimilarity) {
			return true;
		}
		return false;
	}

	public static enum SimilarityTypeGroup {
		TextSimilarity, OtherSimilarity, NeighbourSimilarity;
	}

}
