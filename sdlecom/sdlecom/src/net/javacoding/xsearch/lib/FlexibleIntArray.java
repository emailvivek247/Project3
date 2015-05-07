package net.javacoding.xsearch.lib;

import java.util.Arrays;



public class FlexibleIntArray {
	
    private int[] values;
    private int[] indexes;
    
    private boolean vacuumed = false;
    private boolean shrinked = false;
    private int counter = 0;
    
    public static final int EMPTY_INT = Integer.MIN_VALUE;
    
    public FlexibleIntArray(int length) {
        values = new int[length];
        Arrays.fill(values, EMPTY_INT);
    }
    
    public void set(int n, int v) {
        values[n] = v;
        if(v!=EMPTY_INT) {
            counter++;
        }
    }
    
    /**
     * @param n
     * @param level starting from 0
     * @return
     */
    public int get(int n) {
    	if(shrinked){
    	    int k = Arrays.binarySearch(indexes, n);
    	    if(k<0) return EMPTY_INT;
    	    return values[k];
    	}else{
    		return values[n];
    	}
    }
    
    public synchronized void vacuum(){
        if(vacuumed) return;
    	vacuumed = true;
    	//logger.debug("fullness: "+ hasData.cardinality() + "/" + length);
        if(counter * 2 < values.length){
            int[] t = new int[counter];
            indexes = new int[counter];
            int k = 0;
            for(int i=0;i<values.length;i++) {
                if(values[i]!=EMPTY_INT) {
                    t[k] = values[i];
                    indexes[k] = i;
                    k++;
                }
            }
            values = t;
            shrinked = true;
        }
    }
    
    public int count() {
        return this.counter;
    }
}
