package de.l3s.util;

public class TimeLogger {

	private long firstTime = 0;
	private long lastTime = 0;
	private boolean print = true;
	private boolean active = true;

	private static TimeLogger instance;

	private TimeLogger() {
	}

	public static TimeLogger getInstance() {
		if (TimeLogger.instance == null) {
			TimeLogger.instance = new TimeLogger();
		}
		return TimeLogger.instance;
	}

	public void start() {
		start(null);
	}

	public void start(String comment) {
		firstTime = System.nanoTime();
		lastTime = firstTime;
		if (comment == null)
			System.out.println("Start logging.");
		else
			System.out.println("Start logging: " + comment + ".");
	}

	public void start(boolean active) {
		this.active = active;
		firstTime = System.nanoTime();
		lastTime = firstTime;
		if (active)
			System.out.println("Start logging.");
	}

	public void logTime() {
		logTime("");
	}

	public void logTime(String string) {
		long currentTime = System.nanoTime();
		double difference = (currentTime - lastTime) / 1e6;
		if (print && active)
			System.out.println("LOG (" + string + "): " + difference + " ms");
		lastTime = currentTime;
	}

	public void logTotalTime() {
		logTotalTime("");
	}

	public void logTotalTime(String string) {
		long currentTime = System.nanoTime();
		double difference = (currentTime - firstTime) / 1e6;
		double differenceSeconds = (currentTime - firstTime) / 1e9;

		if (print && active)
			System.out.println("LOG TOTAL (" + string + "): " + difference + " ms (" + differenceSeconds + " s)");
		lastTime = currentTime;
	}

	public double getTotalTimeInSeconds() {
		long currentTime = System.nanoTime();
		double differenceSeconds = (currentTime - firstTime) / 1e9;
		return differenceSeconds;
	}

}
