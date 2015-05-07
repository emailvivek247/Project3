package net.javacoding.xsearch.lib;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayeredIntArrayList {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private int length;
	private int level;

    private FlexibleIntArray[] array;
    private FlexibleObjectArray overflow;
    
    private boolean vacuumed = false;
    
    public static final int EMPTY_INT = FlexibleIntArray.EMPTY_INT;

    public LayeredIntArrayList(int length,int level) {
        this.length = length;
        array = new FlexibleIntArray[level];
        for(int i=0;i<level;i++){
            array[i] = new FlexibleIntArray(length);
        }
        overflow = new FlexibleObjectArray(length);
        this.level = level;
    }
    
    public void set(int n, int v) {
        for(int i=0;i<level;i++){
        	if(array[i].get(n) == FlexibleIntArray.EMPTY_INT){
        		array[i].set(n, v);
        		return;
        	}
        	if(array[i].get(n) == v){
        		return;
        	}
        }

        //overflown
        ArrayList<Integer> a = (ArrayList<Integer>) overflow.get(n);
        if(a ==null) {
          a = new ArrayList<Integer>(1);
          overflow.set(n, a);
        }
        a.add(v);
    }
    
    /**
     * @param n
     * @param l starting from 0
     * @return
     */
    public int get(int n, int l) {
    	if(l<level){
    		return array[l].get(n);
    	}
    	if(overflow==null) {
    	    return EMPTY_INT;
    	}
        l = l - level;
        int[] a = (int[]) overflow.get(n); 
        if(a == null || l >= a.length) {
            return EMPTY_INT;
        } else {
            return a[l];
        }
    }
    
    public void vacuum(){
    	vacuumed = true;
    	//logger.debug("re-structuring overflow to int[] ...");
        for(int i=overflow.values.length-1;i>=0;i--) {
            ArrayList a = (ArrayList) overflow.values[i];
            if( a !=null ){
                int[] ints = new int[a.size()];
                for(int j=0;j<ints.length;j++){
                	ints[j] = ((Integer)a.get(j)).intValue();
                }
                overflow.values[i] = ints;
            }
        }
    	//logger.debug("Vacumming primary storage...");
        for(int i=0;i<level;i++){
            array[i].vacuum();
        }
    	//logger.debug("Vacumming overflow storage...");
        overflow.vacuum();
        if(overflow.count()<=0) {
            this.overflow = null;
            for(int i=level-1;i>=0;i--) {
                if(array[i].count()<=0) {
                    array[i] = null;
                    this.level--;
                }else {
                    break;
                }
            }
        }
    }

}
