package net.javacoding.xsearch.core;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


/**
 * A class representing a single period of activity for a link or receiver.
 *
 * @author Robert Chalmers
 * @version 1.0
 */
@XStreamAlias("period-entry")
public class PeriodEntry {

    /** Starting timestamp */
	@XStreamAsAttribute
    public long start = -1;
    /** Ending timestamp */
	@XStreamAsAttribute
    public long stop  = -1;

    /**
     * Default constructor.
     */
    public PeriodEntry() {
    	super();
    }

    /**
     * Constructor, start = stop
     *
     * @param <code>long</code> starting timestamp
     */
    public PeriodEntry( long start ) {
      this.start = start;
      this.stop = start;
    }

    /**
     * Constructor, ensured start < stop in the function
     *
     * @param <code>long</code> starting timestamp
     * @param <code>long</code> ending timestamp
     */
    public PeriodEntry( long start, long stop ) {
      if(start < stop){
        this.start = start;
        this.stop = stop;
      }else{
        this.start = stop;
        this.stop = start;
      }
    }

    /**
     * Copy constructor.
     *
     * @param <code>PeriodEntry</code> entry to copt
     */
    public PeriodEntry( PeriodEntry other ) {
      start = other.start;
      stop = other.stop;
    }


    /**
     * Determine whether the given timestamp falls within this period.
     * start and stop time are inclusive
     *
     * @param <code>long</code> timestamp to check
     * @retun <code>boolean</code> whether period contains timestamp
     */
    public boolean contains( long theTime ) {
      return( theTime >= start && theTime <= stop );
    }

    /**
     * expand the current PeriodEntry to cover theTime
     *
     * @param <code>long</code> timestamp to cover
     * @retun <code>PeriodEntry</code> The instance itself
     */
    public PeriodEntry add( long theTime ) {
      if(theTime<0) return this;
      if( start == -1 || stop == -1 ){
          start = theTime;
          stop  = theTime;
      }else if(theTime < start){
          start = theTime;
      }else if (stop < theTime) {
          stop  = theTime;
      }
      return this;
    }


    /**
     * Calculate duration of period in terms of timestamp units.
     *
     * @return <code>long</code> duration of period
     */
    public long duration() {
        if( stop < Long.MAX_VALUE )
            return( Math.max( stop - start, 0 ) );
        else
            return( Long.MAX_VALUE );
    }


    /**
     * Calculate duration of period from the start to the given bound in
     * terms of timestamp units.
     *
     * @return <code>long</code> duration of bounded period
     */
    public long duration( long bound ) {
        if( bound <= stop )
            return( Math.max( bound - start, 0 ) );
        else
            return( duration() );
    }

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss z");
    public String toString() {
        return "["+formatter.print(start)+" ~ "+formatter.print(stop)+"]";
    }
}
