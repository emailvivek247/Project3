package net.javacoding.xsearch.lib;

import org.apache.lucene.util.OpenBitSet;



public class ShrinkableIntArray {
	
    private int[] array;
    private SparseArrayList sparseArray;
    private OpenBitSet hasData;
	private int length;
	private float fullness = 0.08f;
    
    private boolean vacuumed = false;
    private boolean shrinked = false;
    
    public static final int EMPTY_INT = -32765;
    
    public ShrinkableIntArray(int length) {
        this.length = length;
        array = new int[length];
        hasData = new OpenBitSet(length);
    }
    
    public void set(int n, int v) {
    	hasData.set(n);
        array[n] = v;
    }
    
    /**
     * @param n
     * @param level starting from 0
     * @return
     */
    public int get(int n) {
    	if(shrinked){
    		if(hasData.get(n)){
        		Integer i = (Integer) sparseArray.get(n);
        		if(i==null){
        			return EMPTY_INT;
        		}
        		return i;
    		}
    	}else{
    		if(hasData.get(n)){
        		return array[n];
    		}
    	}
		return EMPTY_INT;
    }
    
    public void vacuum(){
    	vacuumed = true;
    	//logger.debug("fullness: "+ hasData.cardinality() + "/" + length);
        if(hasData.cardinality() < fullness * length){
            sparseArray = new SparseArrayList();
            for(int i=array.length-1;i>=0;i--) {
            	if(hasData.get(i)){
                	sparseArray.set(i, array[i]);
            	}
            }
            array = null;
            shrinked = true;
        }
    }
    
    public long count() {
        return this.hasData.cardinality();
    }
}
