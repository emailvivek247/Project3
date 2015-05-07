package net.javacoding.xsearch.lib;

import org.apache.lucene.util.OpenBitSet;



public class ShrinkableObjectArray {

    private Object[] array;
    private SparseArrayList sparseArray;
    private OpenBitSet hasData;
	int length;
	private float fullness = 0.2f;
    
    private boolean vacuumed = false;
    private boolean shrinked = false;
    
    private long count;
    
    public ShrinkableObjectArray(int length) {
        this.length = length;
        array = new Object[length];
        hasData = new OpenBitSet(length);
    }
    
    public void set(int n, Object v) {
    	if(v==null){
    		hasData.clear(n);
    		array[n] = null;
    	}else{
    		hasData.set(n);
            array[n] = v;
        }
    }
    
    /**
     * @param n
     * @param level starting from 0
     * @return
     */
    public Object get(int n) {
    	if(!vacuumed){
    		//vacuum();
    	}
        return shrinked ? (Object) ((SparseArrayList)sparseArray).get(n) : array[n];
    }
    
    public void vacuum(){
    	vacuumed = true;
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
        count = hasData.cardinality();
        //For object array, null means no data, so this is not needed
        //but for int array, hasData is still needed
        hasData = null;
    }
    public long count() {
        return this.count;
    }

}
