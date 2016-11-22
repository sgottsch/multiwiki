package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;

public class FormatUtil {

	/**
	 * When converting a double into a string, the result may be something like
	 * "5.73017397E8". With this method, you get the double without any "." or
	 * "E".
	 * 
	 * @param number
	 *            double format number to conver
	 * @return double number as String without any special conversions
	 */
	public static String doubleToString(Object number) {
		
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(0);

		return df.format(number);
	}

	public static Date convertWikiTimestampToMySQLDate(String timestamp) {

		// Example wiki timestamp: 2014-08-21T18:35:01Z
		// Target format: yyyy-[m]m-[d]d

		String date = timestamp.substring(0, timestamp.indexOf("T"));

		Date sqlDate = Date.valueOf(date);

		return sqlDate;
	}

	public static Timestamp convertWikiTimestampToMySQLDateTime(String timestamp) {
		
		// Example wiki timestamp: 2014-08-21T18:35:01Z
		// Target format: yyyy-mm-dd hh:mm:ss
		
		if(timestamp == null)
			return null;
		
		String date = timestamp.substring(0, timestamp.indexOf("T"));

		String time = timestamp.substring(timestamp.indexOf("T") + 1, timestamp.length() - 1);
		
		String datetime = date + " " + time;
			
		Timestamp sqlTimestamp = Timestamp.valueOf(datetime);
		
		return sqlTimestamp;
	}
	
	// Source: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

}
