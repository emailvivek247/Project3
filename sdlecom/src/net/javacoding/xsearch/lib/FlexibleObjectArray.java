package net.javacoding.xsearch.lib;

import java.util.Arrays;



public class FlexibleObjectArray {

    public Object[] values;
    private int[] indexes;
    
    private boolean vacuumed = false;
    private boolean shrinked = false;
    private int counter = 0;
    
    public FlexibleObjectArray(int length) {
        values = new Object[length];
    }
    
    public void set(int n, Object v) {
        if(values[n]==null&&v!=null) {
            counter++;
        }
        if(values[n]!=null&&v==null) {
            counter--;
        }
        values[n] = v;
    }
    
    /**
     * @param n
     * @param level starting from 0
     * @return
     */
    public Object get(int n) {
        if(shrinked){
            int k = Arrays.binarySearch(indexes, n);
            if(k<0) return null;
            return values[k];
        }else{
            return values[n];
        }
    }
    
    public void vacuum(){
        if(vacuumed) return;
        vacuumed = true;
        if(counter * 2 < values.length){
            Object[] t = new Object[counter];
            indexes = new int[counter];
            int k = 0;
            for(int i=0;i<values.length;i++) {
                if(values[i]!=null) {
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
