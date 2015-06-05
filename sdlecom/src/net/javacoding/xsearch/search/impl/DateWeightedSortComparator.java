/*
 * Created on Mar 22, 2007
 */
package net.javacoding.xsearch.search.impl;

import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.TimeWeight;
import net.javacoding.xsearch.utility.VMTool;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortComparator;
import org.apache.lucene.search.SortField;

public class DateWeightedSortComparator extends SortComparator {

    private DatasetConfiguration dc = null;
    
    /*
     * Create the time-based weight when initializing the cache
     */
    private long nowTime;
    private TimeWeight[] timeWeights;
    public DateWeightedSortComparator(DatasetConfiguration dc) {
        this.dc = dc;
        this.nowTime = System.currentTimeMillis()/1000L;
        this.timeWeights = dc.getTimeWeights();
    }

    public ScoreDocComparator newComparator(final IndexReader reader, final String fieldname) throws IOException {
        final String field = fieldname.intern();
        final Comparable[] cachedValues = FieldCache.DEFAULT.getCustom(reader, field, DateWeightedSortComparator.this);

        return new ScoreDocComparator() {

            public int compare(ScoreDoc i, ScoreDoc j) {
            	try{
                	if(cachedValues[i.doc]==null) return -1;
                	if(cachedValues[j.doc]==null) return 1;
	                float iWeight=((Float)cachedValues[i.doc]).floatValue();
	                float jWeight=((Float)cachedValues[j.doc]).floatValue();
	                float ret = i.score * iWeight - j.score * jWeight;
	                return (ret>0? 1: (ret==0? 0: -1 ));
            	}catch(Throwable t){
            		return 0;
            	}
            }

            public Comparable sortValue(ScoreDoc i) {
                return cachedValues[i.doc];
            }

            public int sortType() {
                return SortField.CUSTOM;
            }
        };
    }

    protected Comparable getComparable(String termtext) {        
        //calculate the weight when creating the cache
        //the cache is initialized when each time the index is updated
        long time = nowTime - VMTool.storedStringToLongValue(termtext)/1000L;
        double weight=1;
        for(int k=0;k<timeWeights.length;k++) {
            if(time < timeWeights[k].getTime()) {
            	if(k>0){
                	double ratio = (time - timeWeights[k-1].getTime())*1.0/(timeWeights[k].getTime() - timeWeights[k-1].getTime());
                    weight = (timeWeights[k].getWeight()-timeWeights[k-1].getWeight())*ratio + timeWeights[k-1].getWeight();
            	}else{
            		weight = timeWeights[k].getWeight();
            	}
                break;
            }
        }

        
        return new Float(weight);
    }

    public int hashCode() {
        return dc.getName().hashCode();
    }

    public boolean equals(Object obj) {
        try {
            if(obj instanceof DateWeightedSortComparator) {
                return this.dc.getName().equals(((DateWeightedSortComparator)obj).dc.getName());
            }
        }catch(Exception e) {}
        return false;
    }

}
