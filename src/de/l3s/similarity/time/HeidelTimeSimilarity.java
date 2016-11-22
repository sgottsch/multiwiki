package de.l3s.similarity.time;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.l3s.model.BinaryComparison;
import de.l3s.model.Sentence;
import de.l3s.model.times.HeidelIntervalString;
import de.l3s.similarity.FeatureSimilarity;
import de.l3s.similarity.SimilarityType;

public class HeidelTimeSimilarity extends FeatureSimilarity {

	private static final boolean PRINT = false;

	private TimeSimilarityMode mode = TimeSimilarityMode.WEIGHTED;

	public HeidelTimeSimilarity() {
		this(false);
	}

	public HeidelTimeSimilarity(boolean isUnweighted) {
		super();
		if (isUnweighted)
			this.mode = TimeSimilarityMode.UNWEIGHTED;
		else
			this.mode = TimeSimilarityMode.WEIGHTED;
	}

	public String getName() {
		String name = "HeidelTimeSimilarity";
		if (mode == TimeSimilarityMode.UNWEIGHTED)
			name = "HeidelTimeUnweightedSimilarity";
		return name;
	}

	public String getEasyName() {
		return "HeidelTime";
	}

	public SimilarityType getSimilarityType() {
		return SimilarityType.HeidelTimeSimilarity;
	}

	public double calculateSimilarity(BinaryComparison comparison, Sentence annotation1, Sentence annotation2) {

		if (annotation1.getHeidelIntervals() == null) {
			System.err.println("Missing times for: " + annotation1.getRawText());
			this.isApplicable = false;
			return 0.0;
		}

		if (annotation2.getHeidelIntervals() == null) {
			System.err.println("Missing times for: " + annotation2.getRawText());
			this.isApplicable = false;
			return 0.0;
		}

		if (annotation1.getHeidelIntervals().size() == 0 || annotation2.getHeidelIntervals().size() == 0) {
			this.isApplicable = false;
			return 0.0;
		}

		this.isApplicable = true;

		if (PRINT) {
			System.out.println("");
			System.out.println("HeidelTimeSimilarity");

			System.out.println(annotation1.getEnglishRawText());
			System.out.println(annotation2.getEnglishRawText());
		}

		List<HeidelIntervalString> dates1 = annotation1.getHeidelIntervals();
		List<HeidelIntervalString> dates2 = annotation2.getHeidelIntervals();

		double similarity = 0.0;

		double weightSum1 = 0.0;
		Map<HeidelIntervalString, Double> connections1 = new HashMap<HeidelIntervalString, Double>();

		for (HeidelIntervalString date1 : dates1) {

			if (PRINT)
				System.out.println(date1.getCompleteIntervalStringRepresentation());

			connections1.put(date1, 0.0);

			// weightSum1 += date1.getWeight();
			weightSum1 += 1;

			for (HeidelIntervalString date2 : dates2) {

				try {
					boolean contains = intersect(date1, date2);

					if (contains) {

						if (PRINT) {
							System.out.println("Contains 1: [" + date1.getStartTime() + "," + date1.getEndTime()
									+ "] - [" + date2.getStartTime() + "," + date2.getEndTime() + "]");
						}

						double weight = 1;
						if (mode == TimeSimilarityMode.WEIGHTED)
							weight = Math.min(date1.getWeight(), date2.getWeight());

						// weight *= intersect2(date1, date2);

						if (connections1.get(date1) < weight) {
							connections1.put(date1, weight);
						}

					}
				} catch (NullPointerException e) {
					// No idea why this is happing. Just ignore it.
					e.printStackTrace();

				}

			}
		}

		double weightSum2 = 0.0;
		Map<HeidelIntervalString, Double> connections2 = new HashMap<HeidelIntervalString, Double>();

		for (HeidelIntervalString date1 : dates2) {

			connections2.put(date1, 0.0);

			// weightSum2 += date1.getWeight();
			weightSum2 += 1;

			for (HeidelIntervalString date2 : dates1) {

				try {
					boolean contains = intersect(date1, date2);
					if (contains) {

						if (PRINT) {
							System.out.println("Contains 2: [" + date1.getStartTime() + "," + date1.getEndTime()
									+ "] - [" + date2.getStartTime() + "," + date2.getEndTime() + "]");
						}

						double weight = 1;
						if (mode == TimeSimilarityMode.WEIGHTED)
							weight = Math.min(date1.getWeight(), date2.getWeight());

						// weight *= intersect2(date1, date2);

						if (connections2.get(date1) < weight) {
							connections2.put(date1, weight);
						}

					}
				} catch (NullPointerException e) {
					// No idea why this is happing. Just ignore it.
				}

			}
		}

		double connectedWeightSum1 = 0.0;
		for (Double weight : connections1.values())
			connectedWeightSum1 += weight;

		double connectedWeightSum2 = 0.0;
		for (Double weight : connections2.values())
			connectedWeightSum2 += weight;

		similarity = (connectedWeightSum1 + connectedWeightSum2) / (weightSum1 + weightSum2);

		if (weightSum1 == 0 && weightSum2 == 0)
			similarity = 0.0;
		else
			similarity = Math.min((connectedWeightSum1 + connectedWeightSum2) / (weightSum1 + weightSum2), 1);

		return similarity;
	}

	private boolean intersect(HeidelIntervalString time1, HeidelIntervalString time2) {

		Date b1 = time1.getStartTime();
		Date e1 = time1.getEndTime();
		Date b2 = time2.getStartTime();
		Date e2 = time2.getEndTime();

		if (b1.equals(e2) || b1.equals(b2) || e1.equals(e2) || e1.equals(b2))
			return true;

		// if (b1.before(e2) && b2.before(e1)) {
		// System.out.println("[" + b1 + "," + e1 + "] intersects [" + b2 + ","
		// + e2 + "]");
		// }

		return b1.before(e2) && b2.before(e1);
	}

	// private double intersect2(HeidelIntervalString time1,
	// HeidelIntervalString time2) {
	//
	// Date b1 = time1.getStartTime();
	// Date e1 = time1.getEndTime();
	// Date b2 = time2.getStartTime();
	// Date e2 = time2.getEndTime();
	//
	// if (!(b1.equals(e2) || b1.equals(b2) || e1.equals(e2) || e1.equals(b2)))
	// return 0;
	//
	// // if (b1.before(e2) && b2.before(e1)) {
	// // System.out.println("[" + b1 + "," + e1 + "] intersects [" + b2 + ","
	// // + e2 + "]");
	// // }
	//
	// if (!(b1.before(e2) && b2.before(e1)))
	// return 0;
	//
	// // Take the later begin and the earlier begin
	// Date startDateCommon = null;
	// Date endDateCommon = null;
	//
	// Date startDateLong = null;
	// Date endDateLong = null;
	//
	// if (b1.before(b2)) {
	// startDateCommon = b2;
	// startDateLong = b1;
	// } else {
	// startDateCommon = b1;
	// startDateLong = b2;
	// }
	//
	// if (e1.before(e2)) {
	// endDateCommon = e1;
	// endDateLong = e2;
	// } else {
	// endDateCommon = e2;
	// endDateLong = e1;
	// }
	//
	// int numberOfDaysCommon = DateUtil.numberOfDaysBetween(startDateCommon,
	// endDateCommon);
	// int numberOfDaysLong = DateUtil.numberOfDaysBetween(startDateLong,
	// endDateLong);
	//
	// double dayOverlap = (double) (((double) numberOfDaysCommon) / ((double)
	// numberOfDaysLong));
	//
	// System.out.println("Day overlap: " + dayOverlap);
	//
	// return dayOverlap;
	// }

	private enum TimeSimilarityMode {
		WEIGHTED, UNWEIGHTED;
	}

}
