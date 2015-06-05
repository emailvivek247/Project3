package net.javacoding.xsearch.core;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import net.javacoding.xsearch.config.XMLSerializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * A class representing the indexed document's time range
 *
 * @version 1.0
 */
@XStreamAlias("period-table")
public class PeriodTable extends XMLSerializable{

    /** List of period entries */
	@XStreamAlias("indexed-periods")
    protected Vector<PeriodEntry> entries = new Vector<PeriodEntry>( 3, 5 );

    /**
     * Default constructor.
     */
    public PeriodTable() {
    	super();
    }


    /**
     * Retrieve the earliest time in the period table.
     *
     * @return <code>long</code> earliest time in the table or Long.MAX_VALUE if no earliest time available
     */
    public long getEarliest() {
        if(entries.size()>0) {
            return( ((PeriodEntry)entries.firstElement()).start );
        }else{
            return -1;
        }
    }

    /**
     * Retrieve the latest time in the period table.
     *
     * @return <code>long</code> latest time in the table or Long.MAX_VALUE if no latest time available
     */
    public long getLatest() {
        if(entries.size()>0) {
            return( ((PeriodEntry)entries.lastElement()).stop );
        }else{
            return -1;
        }
    }

    private void clean() {
        if(entries.size()>0) {
            PeriodEntry pe = (PeriodEntry) entries.firstElement();
            if(pe.start<0&&pe.stop<0){
                entries.remove( 0 );
            }
        }
    }

    /**
     * Retrieve the total active time in the period table.
     *
     * @param <code>bound</code> maximum stop time considered
     * @return <code>long</code> total active time
     */
    public long getDuration( long bound ) {
        long duration = 0;
    	for (PeriodEntry e : entries) {
            duration += e.duration( bound );
		}
        return( duration );
    }


    /**
     * Check whether a specific time falls within the table periods.
     *
     * @param <code>long</code> time to check for
     * @return <code>boolean</code> whether the time fell withing the tables periods
     */
    public boolean contains( long time ) {
        if(entries.size()<=0) return false;
    	for (PeriodEntry e : entries) {
            if( e.contains( time ) )
                // it's in this entry's range
                return( true );
		}
        return( false );
    }

    public void deleteOlderThan( long start, long stop) {
        for( int i = 0; i < entries.size(); i++ ){
            PeriodEntry entry = (PeriodEntry)entries.get( i );
            //find the first periodEntry where start<entry.start
            //because the list is ordered by entry's start time ascending
            if(entry.stop<= stop){
                entries.remove(i);
                i--;
            }else if(entry.start < stop){
                entry.start = stop+1;
            }
        }
    }
    public void delete( long start, long stop) {
        for( int i = 0; i < entries.size(); i++ ){
            PeriodEntry entry = (PeriodEntry)entries.get( i );
            //find the first periodEntry where start<entry.start
            //because the list is ordered by entry's start time ascending
            if( start <= entry.start ) {
                if(entry.stop<= stop){
                    entries.remove(i);
                    i--;
                }else if(entry.start<=stop){
                    entry.start = stop+1;
                }
            } else if( entry.start < start) {
                if(start <= entry.stop){
                    entry.stop = start-1;
                }
            }
        }
    }
    public void deleteOlderThan( PeriodEntry pe) {
        if(pe==null) return;
        deleteOlderThan(pe.start, pe.stop);
    }
    public void delete( PeriodEntry pe) {
        if(pe==null) return;
        delete(pe.start, pe.stop);
    }

    public long add( PeriodEntry pe) {
        if(pe==null) return -1;
        return add(pe.start, pe.stop);
    }
    /**
     * Add a new time period into the table, without merging adjacent entries.
     * Start times need to be kept so that further adds will respect them.
     *
     * @param <code>long</code> start time of period
     * @param <code>long</code> stop time of period
     * @return <code>long</code> actual stop time, possibly reduced due to earlier explicit start times
     */
    public long add( long start, long stop ) {
        PeriodEntry prior = null, entry = null;
        for( int i = 0; i < entries.size(); i++ ){
            prior = entry;
            entry = (PeriodEntry)entries.get( i );
            //find the first periodEntry where start<entry.start
            //because the list is ordered by entry's start time ascending
            if( start < entry.start ) {
                // it should go before this entry
                // possibly bound the stop time by the next known start time
                stop = Math.min( stop, entry.start );
                // add the entry
                entries.add( i, new PeriodEntry( start, stop ) );
                if( prior != null )
                // possibly bound the prior entry's stop by our start time
                prior.stop =  Math.min( prior.stop, start );
                // return the actual stop time used
                return( stop );
            } else if( start == entry.start ) {
                // a repeat of this entry, so bound the stop time
                return( entry.stop = Math.max( entry.stop, stop ) );
            }
        }
        // it's beyond all known periods, so add it
        entries.add( new PeriodEntry( start, stop ) );
        if( entry != null && entry.stop > start )
            // the last entry of the list may need to be bounded
            entry.stop = start;
        // return the actual stop time
        return( stop );
    }

    public int size() {
        return( entries.size() );
    }

    /**
     * Merge the activity table
     *
     */
    public void merge() {
        PeriodEntry entry = null, next = null;
        for( int i = 0; i < entries.size(); ) {
            entry = (next == null) ? (PeriodEntry)entries.get( i ) : next;
            next = (i + 1 < entries.size()) ? (PeriodEntry)entries.get( i + 1 ) : null;
            if( next != null && entry.stop >= next.start ) {
                // our stop time overlap the next entry, so combine
                next.start = entry.start;
                entries.remove( i );
            /*
            } else if( entry.start == entry.stop ) {
                // our start and stop times match (null time period), so remove
                entries.remove( i );
                // if we removed the last element then set entry back one so that
                //   its stop time can be bounded(PeriodEntry)entries.get( i )
                if( i == entries.size() && i > 0 )
                    entry = (PeriodEntry)entries.get( i - 1 );
                else
                    entry = null;
            */
            } else {
                // move on to the next entry
                i++;
            }
        }
    }


    /**
     * Merge periods from another table into this one.
     * This is used to build a single table representing the total activity on all links of a node.
     * This total activity is then used by its parents to bound their tables.
     *
     * @param <code>PeriodTable</code> table to merge into this one
     */
    public void merge( PeriodTable merger ) {
        if(merger==null) return;
        PeriodEntry prev = null, entry = null, merge = null;
        int m = 0;
        for( int i = 0; i < entries.size() && (merge != null || m < merger.entries.size()); ){
            prev = (entry != null) ? entry : prev;
            entry = (PeriodEntry)entries.get( i );
            merge = (merge == null) ? (PeriodEntry)merger.entries.get( m++ ) : merge;

            if( prev != null && prev.stop > entry.stop ) {
                // the previous entry stops later than the this one, so just yank it
                entries.remove( i );
                entry = null;
                continue;
            } else if( merge.stop < entry.start ) {
                // the new period takes place prior to this entry, so insert it
                prev = new PeriodEntry( merge );
                entries.add( i, prev );
                entry = merge = null;
            } else if( merge.start < entry.stop ) {
                // now we have overlapping periods
                if( merge.start < entry.start )
                // the new one is earlier, so use its start time
                entry.start = merge.start;
                // use the latest stop time
                entry.stop = Math.max( entry.stop, merge.stop );
                merge = null;
            }
            i++;
        }

        // fill in any left over periods
        for( m = (merge == null) ? m : m - 1; m < merger.entries.size(); m++ )
            entries.add( new PeriodEntry( (PeriodEntry)merger.entries.get( m ) ) );

        // compact the table
        merge();
    }


    /**
     * Create a new table that contains the total activity of all component tables.
     *
     * @param <code>PeriodTable[]</code> list of component tables to merge
     * @return <code>PeriodTable</code> new table with total activity
     */
    public static PeriodTable merge( PeriodTable[] tables ) {
        PeriodTable merged = new PeriodTable();
        // merge each table into the new one
        for( int t = 0; t < tables.length; t++ )
            merged.merge( tables[t] );
        return( merged );
    }


    /**
     * Load a period table from a file.
     *
     * @param <code>File</code> reader to use to parse file
     */
    public static PeriodTable load( File theFile ) throws IOException {
    	return (PeriodTable)fromXML(theFile);
    }

    /**
     * save a period table to a file.
     *
     * @param <code>File</code> reader to use to parse file
     */
    public void save( File theFile ){
        clean();
        merge();
        toXML(theFile);
    }

    /**
     * Print the contents of the period table.
     */
    public String toString() {
        clean();
        merge();
        StringBuffer sb = new StringBuffer();
        try {
            if(entries.size()>2) {
                sb.append(entries.get(0));
                sb.append("...");
                sb.append(entries.get(entries.size()-1));
            }else {
                for (PeriodEntry e : entries) {
                    sb.append(e);
                }
            }
        } catch( Exception e ) {
            sb.append( "\tError printing: " + e.getMessage() );
        }
        return sb.toString();
    }

}
