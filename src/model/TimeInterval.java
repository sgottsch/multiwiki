package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import util.DateUtil;

public class TimeInterval implements Comparable<TimeInterval> {
	private Date begin;
	private Date end;
	private String stringRepresentation;

	public TimeInterval(Date begin, Date end) throws IllegalArgumentException {
		if (begin != null && end != null && end.before(begin)) {
			throw new IllegalArgumentException("begin of time interval " + begin + " is after the end " + end);
		}
		this.begin = begin;
		this.end = end;
		this.createStringRepresentation();
	}

	public TimeInterval(String timeIntervalString) throws IllegalArgumentException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		timeIntervalString = timeIntervalString.substring(1, timeIntervalString.length() - 1);
		String[] parts = timeIntervalString.split(",");
		String beginString = parts[0];
		String endString = parts[1];
		if (endString.length() == 4) {
			endString = String.valueOf(endString) + "-12-31";
		}
		if (beginString.length() == 4) {
			beginString = String.valueOf(beginString) + "-01-01";
		}
		try {
			Date begin = formatter.parse(beginString);
			Date end = formatter.parse(endString);
			if (begin != null && end != null && end.before(begin)) {
				throw new IllegalArgumentException("begin of time interval " + begin + " is after the end " + end);
			}
			this.begin = begin;
			this.end = end;
			this.createStringRepresentation();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Date getBegin() {
		return this.begin;
	}

	public Date getEnd() {
		return this.end;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
		this.createStringRepresentation();
	}

	public void setEnd(Date end) {
		this.end = end;
		this.createStringRepresentation();
	}

	/**
	 * Returns true, if both time intervals overlap. If an interval has an
	 * undefined interval limit, this is set to the remaining defined limit.
	 */
	public boolean overlaps(TimeInterval interval2) {
		Date b1 = this.begin;
		Date e1 = this.end;
		Date b2 = interval2.getBegin();
		Date e2 = interval2.getEnd();

		if (b1 == null)
			b1 = e1;
		if (e1 == null)
			e1 = b1;
		if (b2 == null)
			b2 = e2;
		if (e2 == null)
			e2 = b2;

		if (b1.equals(b2) || b1.equals(e2) || e1.equals(b2) || e1.equals(e2))
			return true;

		// http://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
		return (b1.before(e2)) && (e1.after(b2));
	}

	/**
	 * Returns true, if both time intervals overlap, but a difference of one day
	 * is allowed between the intervals. If an interval has an undefined
	 * interval limit, this is set to the remaining defined limit.
	 */
	public boolean overlapsWithOneDayTolerance(TimeInterval interval2) {

		Date b1 = this.begin;
		Date e1 = this.end;
		Date b2 = interval2.getBegin();
		Date e2 = interval2.getEnd();

		if (b1 == null)
			b1 = e1;
		if (e1 == null)
			e1 = b1;
		if (b2 == null)
			b2 = e2;
		if (e2 == null)
			e2 = b2;

		// extend only one of the time intervals by one day. Otherwise:
		// Tolerance of two days.
		b1 = DateUtil.decreaseByOneDay(b1);
		e1 = DateUtil.increaseByOneDay(e1);

		if (b1.equals(b2) || b1.equals(e2) || e1.equals(b2) || e1.equals(e2))
			return true;

		// http://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
		return (b1.before(e2)) && (e1.after(b2));
	}

	/**
	 * Returns the union of this time interval and the given one. Only works if
	 * both time intervals overlap (with a tolerance of one day).
	 * 
	 * @param otherTimeInterval
	 * @return Union of both time intervals
	 */
	public TimeInterval union(TimeInterval otherTimeInterval) {
		if (!overlapsWithOneDayTolerance(otherTimeInterval))
			return null;

		Date minBegin = this.begin;
		if (minBegin == null || (otherTimeInterval.getBegin() != null && otherTimeInterval.getBegin().before(minBegin)))
			minBegin = otherTimeInterval.getBegin();

		Date maxEnd = this.end;
		if (maxEnd == null || (otherTimeInterval.getEnd() != null && otherTimeInterval.getEnd().after(maxEnd)))
			maxEnd = otherTimeInterval.getEnd();

		TimeInterval unionInterval = new TimeInterval(minBegin, maxEnd);

		return unionInterval;
	}

	public TimeInterval intersect(TimeInterval otherTimeInterval) {
		if (!overlapsLoosely(otherTimeInterval))
			return null;

		Date maxBegin = this.begin;
		if (maxBegin == null || (otherTimeInterval.getBegin() != null && otherTimeInterval.getBegin().after(maxBegin)))
			maxBegin = otherTimeInterval.getBegin();

		Date minEnd = this.end;
		if (minEnd == null || (otherTimeInterval.getEnd() != null && otherTimeInterval.getEnd().before(minEnd)))
			minEnd = otherTimeInterval.getEnd();

		TimeInterval intersectionInterval = new TimeInterval(maxBegin, minEnd);

		return intersectionInterval;
	}

	/**
	 * Returns true, if both time intervals overlap. If an interval has an
	 * undefined interval limit, this is set to the minimum (or maximum)
	 * possible value.
	 */
	public boolean overlapsLoosely(TimeInterval interval2) {

		Date b1 = this.begin;
		Date e1 = this.end;
		Date b2 = interval2.getBegin();
		Date e2 = interval2.getEnd();

		// if the time intervals have an open end: take the min/max of both time
		// intervals as border
		Set<Date> notNullDates = new HashSet<Date>();
		if (b1 != null)
			notNullDates.add(b1);
		if (e1 != null)
			notNullDates.add(e1);
		if (b2 != null)
			notNullDates.add(b2);
		if (e2 != null)
			notNullDates.add(e2);

		if (notNullDates.size() != 4) {
			Date earliestDate = null;
			Date latestDate = null;
			for (Date date : notNullDates) {
				if (earliestDate == null || date.before(earliestDate))
					earliestDate = date;
				if (latestDate == null || date.after(latestDate))
					latestDate = date;
			}

			if (b1 == null)
				b1 = earliestDate;
			if (b2 == null)
				b2 = earliestDate;
			if (e1 == null)
				e1 = latestDate;
			if (e2 == null)
				e2 = latestDate;
		}

		if (b1.equals(b2) || b1.equals(e2) || e1.equals(b2) || e1.equals(e2))
			return true;

		// http://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
		return (b1.before(e2)) && (e1.after(b2));
	}

	public String toString() {
		return this.stringRepresentation;
	}

	// TODO: REMOVE!
	@Override
	public int compareTo(TimeInterval timeInterval2) {
		Date begin2;
		Date begin1 = this.begin;
		if (begin1 == null) {
			begin1 = this.end;
		}
		if ((begin2 = timeInterval2.getBegin()) == null) {
			begin2 = timeInterval2.getEnd();
		}
		int compare = 0;
		if (begin1 != null && begin2 != null) {
			compare = begin1.compareTo(begin2);
		}
		if (compare == 0) {
			Date end2;
			Date end1 = this.end;
			if (end1 == null) {
				end1 = this.end;
			}
			if ((end2 = timeInterval2.getEnd()) == null) {
				end2 = timeInterval2.getEnd();
			}
			if (end1 != null && end2 != null) {
				compare = end1.compareTo(end2);
			}
		}

		// TODO: Do this sorting thing different
		if (compare == 0) {
			if (this.hashCode() < timeInterval2.hashCode()) {
				return 1;
			}
			if (this.hashCode() > timeInterval2.hashCode()) {
				return -1;
			}
			return 0;
		}
		return compare;
	}

	private void createStringRepresentation() {
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
		String beginDay = "?";
		if (this.begin != null) {
			beginDay = dayFormat.format(this.begin);
		}
		String endDay = "?";
		if (this.end != null) {
			endDay = dayFormat.format(this.end);
		}
		this.stringRepresentation = "[";
		if (!beginDay.equals(endDay)) {
			this.stringRepresentation = this.begin != null ? String.valueOf(this.stringRepresentation) + beginDay
					: String.valueOf(this.stringRepresentation) + "?";
			this.stringRepresentation = String.valueOf(this.stringRepresentation) + ",";
			this.stringRepresentation = this.end != null ? String.valueOf(this.stringRepresentation) + endDay
					: String.valueOf(this.stringRepresentation) + "?";
		} else {
			this.stringRepresentation = String.valueOf(this.stringRepresentation) + beginDay;
		}
		this.stringRepresentation = String.valueOf(this.stringRepresentation) + "]";
	}

	public TimeInterval copy() {
		return new TimeInterval(this.begin, this.end);
	}

	public boolean contains(Date date) {
		if ((this.begin == null || this.begin.equals(date) || this.begin.before(date))
				&& (this.end == null || this.end.equals(date) || this.end.after(date))) {
			return true;
		}
		return false;
	}

	public boolean contains(TimeInterval otherTimeInterval) {
		boolean isBefore = false;
		boolean isAfter = false;
		if (this.getBegin() == null) {
			isBefore = true;
		}
		if (!isBefore && otherTimeInterval.getBegin() != null && this.getBegin().before(otherTimeInterval.getBegin())) {
			isBefore = true;
		}
		if (!isBefore) {
			return false;
		}
		if (this.getEnd() == null) {
			isAfter = true;
		}
		if (!isAfter && otherTimeInterval.getEnd() != null && this.getEnd().after(otherTimeInterval.getEnd())) {
			isAfter = true;
		}
		return isAfter;
	}

	public TimeInterval expandByDays(int days) {
		Date newBegin = this.begin;
		Date newEnd = this.end;
		if (days > 0) {
			if (newBegin != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(newBegin);
				cal.add(5, -days);
				newBegin = cal.getTime();
			}
			if (newEnd != null) {
				Calendar calEnd = Calendar.getInstance();
				calEnd.setTime(newEnd);
				calEnd.add(5, days);
				newEnd = calEnd.getTime();
			}
		}
		return new TimeInterval(newBegin, newEnd);
	}
}
