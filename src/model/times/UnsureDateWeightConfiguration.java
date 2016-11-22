package model.times;

public class UnsureDateWeightConfiguration {
	private static UnsureDateWeightConfiguration instance = null;
	private double weightDay = 1.0;
	private double weightWeek = 0.85;
	private double weightMonth = 0.85;
	private double weightYear = 0.75;
	private double weightDecade = 0.6;
	private double weightCentury = 0.5;
	private double weightToday = 0.25;

	protected UnsureDateWeightConfiguration() {
		// Exists only to defeat instantiation.
	}

	public void setWeights(double weightDay, double weightWeek, double weightMonth, double weightYear,
			double weightDecade, double weightCentury, double weightToday) {
		this.weightDay = weightDay;
		this.weightWeek = weightWeek;
		this.weightMonth = weightMonth;
		this.weightYear = weightYear;
		this.weightDecade = weightDecade;
		this.weightCentury = weightCentury;
		this.weightToday = weightToday;
	}

	public static UnsureDateWeightConfiguration getInstance() {
		if (instance == null) {
			instance = new UnsureDateWeightConfiguration();
		}
		return instance;
	}

	public double getWeight(long numberOfDays, boolean isToday) {
		if (isToday) {
			return this.weightToday;
		}
		if (numberOfDays == 0) {
			return this.weightDay;
		}
		if (numberOfDays <= 7) {
			return this.weightWeek;
		}
		if (numberOfDays <= 31) {
			return this.weightMonth;
		}
		if (numberOfDays <= 366) {
			return this.weightYear;
		}
		if (numberOfDays <= 3653) {
			return this.weightDecade;
		}
		if (numberOfDays <= 36525) {
			return this.weightCentury;
		}
		return 0.0;
	}
}
