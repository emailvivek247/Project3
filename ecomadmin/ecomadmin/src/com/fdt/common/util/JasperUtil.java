package com.fdt.common.util;

import java.util.Date;

import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class JasperUtil {

    public static String convertCamelCase(String s) {
    	return WordUtils.capitalizeFully(s);
    }

    public static String getDateInTimezone(Date date, String timeZone) {
    	DateTime dateTime = new DateTime(date);
    	DateTime dateTimeInTimezone = dateTime.withZoneRetainFields(DateTimeZone.forID(timeZone));
    	DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a zzz");
    	String dateAsString = format.print(dateTimeInTimezone);
    	return dateAsString;
    }
    
    public static int getDocumentsPurchased(int count){
    	if(count < 0){
    		return 0;
    	}
    	return count;
    }
}