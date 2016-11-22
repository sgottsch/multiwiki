package similarity.time;

import java.util.ArrayList;
import java.util.List;

import model.BinaryComparison;
import model.Sentence;
import model.times.HeidelIntervalString;
import similarity.FeatureSimilarity;
import similarity.SimilarityType;
import util.TextUtil;

public class HeidelTimeStringsOverlapSimilarity extends FeatureSimilarity {

	private static final boolean PRINT = false;

	public String getName() {
		return "HeidelTimeStringsOverlap";
	}

	public String getEasyName() {
		return "Time";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.HeidelTimeStringsOverlap;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		if (annotation1.getHeidelIntervals().isEmpty() || annotation2.getHeidelIntervals().isEmpty()
				|| annotation1.getHeidelIntervals() == null || annotation2.getHeidelIntervals() == null) {
			this.isApplicable = false;
			return 0.0;
		}

		this.isApplicable = true;

		if (PRINT) {
			System.out.println("HeidelTimeStringsOverlap");

			System.out.println(annotation1.getEnglishRawText());
			System.out.println(annotation2.getEnglishRawText());
		}

		List<HeidelIntervalString> dates1 = annotation1.getHeidelIntervals();
		List<HeidelIntervalString> dates2 = annotation2.getHeidelIntervals();

		List<String> dateStrings1 = new ArrayList<String>();
		List<String> dateStrings2 = new ArrayList<String>();

		for (HeidelIntervalString date1 : dates1) {
			dateStrings1.add(date1.getCompleteIntervalStringRepresentation());
		}

		for (HeidelIntervalString date2 : dates2) {
			dateStrings2.add(date2.getCompleteIntervalStringRepresentation());
		}

		return TextUtil.getJaccardSimilarity(dateStrings1, dateStrings2);
	}

}
