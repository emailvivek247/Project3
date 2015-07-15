package net.javacoding.xsearch.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
/**
  * created for loading some java classes can not be loaded by velocity tool loader
  * since some java class new instance function are private
  */

public final class VMTool{
    public static Date stringToDate(String s) {
        try{
            return new Date(Long.parseLong(s));
        }catch(NumberFormatException nfe){
            return null;
        }
    }
    /** Converts a string-encoded date into a Date object. */
    public static Date storedStringToDate(String s) {
        try{
            return new Date(Long.parseLong(s)-DATE_LONG_DELTA);
        }catch(NumberFormatException nfe){
            return null;
        }
        //for performance, use radix=10, instread of standard lucene Charactor.MAX_RADIX
        //return new Date(org.apache.lucene.document.DateField.stringToTime(s));
    }
    public static Long storedStringToLong(String s) {
        try{
            return Long.valueOf(s)-DATE_LONG_DELTA;
        }catch(NumberFormatException nfe){
            return null;
        }
    }
    public static long stringToLongValue(String s) {
        try{
            return Long.parseLong(s);
        }catch(NumberFormatException nfe){
            return 0L;
        }
    }
    public static long storedStringToLongValue(String s) {
        try{
            return Long.parseLong(s)-DATE_LONG_DELTA;
        }catch(NumberFormatException nfe){
            return 0L;
        }
    }

    // Copied from lucene, except use radix = 10
    // make date strings long enough to last a millenium
    private static int DATE_LEN = Long.toString(10000L*365*24*60*60*1000).length();
    private static long DATE_LONG_DELTA = -(new DateTime(0,1,1,0,0,0,0).getMillis());
    /**
     * Copied from lucene, except use radix = 10
     * Converts a millisecond time to a string suitable for indexing.
     * @throws RuntimeException if the time specified in the
     * method argument is negative, that is, before 1970
     */
    public static String timeToString(long time) {
        //return NumberUtils.long2sortableStr(time);
        if (time < 0)
            throw new RuntimeException("time too early");

        String s = Long.toString(time);

        if (s.length() > DATE_LEN)
            throw new RuntimeException("time too late");

        // Pad with leading zeros
        if (s.length() < DATE_LEN) {
            StringBuffer sb = new StringBuffer(s);
            while (sb.length() < DATE_LEN)
                sb.insert(0, 0);
            s = sb.toString();
         }

         return s;
    }
    //time of storedString is used in 
    // 1. cache for subsequent query, via DBTool.getString()
    // 2. stored in TextDocument
    // 3. stored in Lucene Document
    // 4. time based ranking, when reading from the doc, need to remove the delta
    public static String timeToStoredString(long time) {
         return timeToString(time+DATE_LONG_DELTA);
    }
    public static Date longToDate(long l) {
        return new Date(l);
    }
    public static int floatToInt(float f) {
        int i = (int)f;
        return i;
    }
    /**
    *  search a file for a pattern on each line
    *  @return If not found, return null, otherwise, return line number, and the found match groups
    */
    public static ArrayList<ArrayList<Comparable>> match(String filename, String patternStr) {
        try {
            return match(new FileReader(filename),patternStr);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
    public static ArrayList<ArrayList<Comparable>> match(File _file, String patternStr) {
        try {
            return match(new FileReader(_file),patternStr);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
    private static ArrayList<ArrayList<Comparable>> match(FileReader _fileReader, String patternStr) {
        ArrayList<ArrayList<Comparable>> al = null;
        LineNumberReader rd = null;
        try {
            rd = new LineNumberReader(_fileReader);
            // Create the pattern
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher("");

            // Retrieve all lines that match pattern
            String line = null;
            while ((line = rd.readLine()) != null) {
                matcher.reset(line);
                if (matcher.find()) {
                    ArrayList<Comparable> oneRow = new ArrayList<Comparable>(2);
                    oneRow.add(new Integer(rd.getLineNumber()));
                    //0 group is for the whole matched string
                    for(int i=0;i<=matcher.groupCount();i++){
                        oneRow.add(matcher.group(i));
                    }
                    if(al==null) al = new ArrayList<ArrayList<Comparable>>();
                    al.add(oneRow);
                }
            }
        } catch (IOException e) {
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (Exception e) {
                }
            }
        }
        return al;
    }

    public static final ThreadLocal<DecimalFormat> timeFormat = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("#,###,###0.00");
        }
    };

    public static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy MMM dd hh:mm");
    public static String longDateToString(long l) {
        return dateFormatter.print(l);
    }
    /**
     * @param f
     * @return false if the file doesn't exists and can not be created 
     */
    public static boolean checkExists(File f) {
        if(f==null) return false;
        if (!f.exists()) {
            f.mkdirs();
        }
        if (!f.exists()) {
            return false;
        }
        return true;
    }

    public static Link makeLink(String href, String text){
        return new Link(href, text);
    }
    
    private static long dayTime   = 24*60*60*1000L;
    private static long hourTime  = 60*60*1000L;
    private static long minuteTime= 60*1000L;
    private static long secondTime= 1000L;
    /**
     * @param elapsedTimeMillis
     * @return long[] of 4 values: day, hour, minute, second
     */
    private static long[] getElapsedTime(long elapsedTimeMillis){
        long[] ret = new long[4];
        // Get elapsed time in days
        ret[0] = elapsedTimeMillis/dayTime;
        elapsedTimeMillis-=ret[0]*dayTime;
        // Get elapsed time in hours
        ret[1] = elapsedTimeMillis/hourTime;
        elapsedTimeMillis-=ret[1]*hourTime;
        // Get elapsed time in minutes
        ret[2] = elapsedTimeMillis/minuteTime;
        elapsedTimeMillis-=ret[2]*minuteTime;
        // Get elapsed time in seconds
        ret[3] = elapsedTimeMillis/secondTime;
        //elapsedTimeMillis-=ret[3]*secondTime;
        return ret;
    }
    /**
     * @param previousDate one previous Date
     * @return an array of integers, which velocity takes(not long[], or Long[])
     */
    public static Integer[] getElapsedTime(Date previousDate){
        if(previousDate==null) return null;
        long elapsed = System.currentTimeMillis()-previousDate.getTime();
        //logger.debug("delta time:"+elapsed);
        long[] ls = getElapsedTime(elapsed);
        Integer[] ret = new Integer[ls.length];
        for(int i=0;i<ls.length;i++){
            ret[i] = new Integer((int)ls[i]);
        }
        return ret;
    }

    // used by RSS
    private static final DateTimeFormatter dateFormatRfc822 = DateTimeFormat.forPattern("EEE, d MMM yyyy hh:mm:ss z");
    public static String formatRfc822Date(Date date) {
        if (date == null) return "";
        return dateFormatRfc822.print(date.getTime());
    }
    @Deprecated
    public static String formatRfc822Date(String lTime) {
        return formatRfc822Date(stringToDate(lTime));
    }

    // used by ATOM
    private static final DateTimeFormatter DateFormatRfc8601 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static String formatRfc8601Date(Date date) {
        if (date == null) return "";
        String s = DateFormatRfc8601.print(date.getTime());
        // convert yyyy-MM-ddTHH:mm:ss+HH00 to yyyy-MM-ddTHH:mm:ss+HH:00
        StringBuffer sb = new StringBuffer();
        sb.append(s.substring(0, s.length()-2)).append(":").append(s.substring(s.length()-2));
        return sb.toString();
    }
    @Deprecated
    public static String formatRfc8601Date(String lTime) {
        return formatRfc8601Date(stringToDate(lTime));
    }
    
    public static String formatToOneWord(String s){
        if(s!=null && s.indexOf(" ")>=0){
            return s.replaceAll(" ", "");
        }
        return s;
    }
    public static boolean contains(List list, Object item){
        if(list==null)return false;
        for(Object x : list) {
            if(item.equals(x)) {
                return true;
            }
        }
        return false;
    }

}
