import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fdt.common.util.SystemUtil;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String keystorePath = System.getProperty("javax.net.ssl.keyStore");
		String keystoreType = System.getProperty("javax.net.ssl.keyStoreType");
		String keystorePassword =System.getProperty("javax.net.ssl.keyStorePassword");


		System.out.println("javax.net.ssl.keyStore: " + keystorePath);
		System.out.println("javax.net.ssl.keyStoreType: " + keystoreType);
		System.out.println("javax.net.ssl.keyStorePassword: " + keystorePassword);

		Date date = SystemUtil.changeTimeZone(new Date(), TimeZone.getTimeZone("America/Los_Angeles"));

		System.out.println(date);
	}

	public static Date getDateInTimezone(Date date, String timeZone) {

		DateTime dateTime = new DateTime(date);
    	DateTime dateTimeInTimezone = dateTime.withZone(DateTimeZone.forID(timeZone));
    	DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a");
    	String dateAsString = format.print(dateTimeInTimezone);

    	System.out.println("dateTimeInTimezone: " + dateAsString);

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		TimeZone tz = TimeZone.getTimeZone(timeZone);
		sdf.setTimeZone(tz);
		Date newDate = null;
		try {
			newDate = sdf.parse(dateAsString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return newDate;
    }

	public static Date changeTimeZone(Date date, TimeZone zone) {
        Calendar first = Calendar.getInstance(zone);
        first.setTimeInMillis(date.getTime());
        Calendar output = Calendar.getInstance();
        output.set(Calendar.YEAR, first.get(Calendar.YEAR));
        output.set(Calendar.MONTH, first.get(Calendar.MONTH));
        output.set(Calendar.DAY_OF_MONTH, first.get(Calendar.DAY_OF_MONTH));
        output.set(Calendar.HOUR_OF_DAY, first.get(Calendar.HOUR_OF_DAY));
        output.set(Calendar.MINUTE, first.get(Calendar.MINUTE));
        output.set(Calendar.SECOND, first.get(Calendar.SECOND));
        output.set(Calendar.MILLISECOND, first.get(Calendar.MILLISECOND));

        return output.getTime();
    }

	/*public static Date getDateInTimezone(Date date, String timeZone) {
		TimeZone tz = TimeZone.getTimeZone(timeZone);
		Calendar cal = Calendar.getInstance(tz);
		cal.setTime(date);
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS zzz");
		format.setCalendar(cal);
		String dateAsString = format.format(cal.getTime());
		Date newDate = null;
		try {
			System.out.println(dateAsString);
			format.setTimeZone(TimeZone.getTimeZone(timeZone));
			newDate = format.parse(dateAsString);
		} catch (ParseException e) {
			System.out.println("Date Parsing Exception");
		}
		return newDate;
	}*/
}
