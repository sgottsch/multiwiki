package util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {

	public static List<Date> getMonthsBetween(Date startDate, Date endDate) {
		List<Date> dates = new ArrayList<Date>();
		// Calendar calendar = new GregorianCalendar();
		Calendar calendar = Calendar.getInstance();

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);

		calendar.set(Calendar.YEAR, startCal.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, startCal.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		System.out.println(calendar.getTime());

		while (calendar.getTime().before(endDate)) {
			Date result = calendar.getTime();
			dates.add(result);
			calendar.add(Calendar.MONTH, 1);
		}

		return dates;
	}

	public int numberOfDaysBetween(Date startDate, Date endDate) {
		if (startDate.after(endDate)) {
			throw new IllegalArgumentException("End date should be grater or equals to start date");
		}

		long startDateTime = startDate.getTime();
		long endDateTime = endDate.getTime();
		long milPerDay = 1000 * 60 * 60 * 24;

		int numOfDays = (int) ((endDateTime - startDateTime) / milPerDay);

		return (numOfDays + 1); // add one day to include start date in interval
	}

	/**
	 * Returns the date that is one day before the given one.
	 */
	public static Date decreaseByOneDay(Date date) {

		if (date == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -1);
		date = c.getTime();

		return date;
	}

	/**
	 * Returns the date that is one day after the given one.
	 */
	public static Date increaseByOneDay(Date date) {

		if (date == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		date = c.getTime();

		return date;
	}

	public static boolean dateWithin(Date date, Date intervalBegin, Date intervalEnd) {

		if (date.after(intervalEnd))
			return false;

		if (date.before(intervalBegin))
			return false;

		return true;
	}

}
