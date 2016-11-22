package de.l3s.heideltime;

import java.util.Date;
import java.util.Set;

import de.unihd.dbs.uima.types.heideltime.Timex3Interval;

public interface IHeidelTimeExtractor {

	public Set<Timex3Interval> getTimeExpressions(String text, Date documentCreationTime);
	
}
