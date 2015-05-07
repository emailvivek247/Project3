package net.javacoding.xsearch.lib;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayeredStringArrayList {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private int length;
	private int level;

    private FlexibleObjectArray[] array;
    private FlexibleObjectArray overflow;
    
    private boolean vacuumed = false;
    
    public LayeredStringArrayList(int length,int level) {
        this.length = length;
        array = new FlexibleObjectArray[level];
        for(int i=0;i<level;i++){
            array[i] = new FlexibleObjectArray(length);
        }
        overflow = new FlexibleObjectArray(length);
        this.level = level;
    }
    
    public void set(int n, String v) {
        for(int i=0;i<level;i++){
        	if(array[i].get(n) == null){
        		array[i].set(n, v);
        		return;
        	}
        	if(array[i].get(n) == v){
        		return;
        	}
        }

        //overflown
        ArrayList<String> a = (ArrayList<String>) overflow.get(n);
        if(a ==null) {
          a = new ArrayList<String>(1);
          overflow.set(n, a);
        }
        a.add(v);
    }
    
    /**
     * @param n
     * @param level starting from 0
     * @return
     */
    public String get(int n, int l) {
    	if(l<level){
    		return (String)array[l].get(n);
    	}
    	if(!vacuumed){
    		vacuum();
    	}
        l = l - level;
        String[] a = (String[]) overflow.get(n);
        if(a == null || l >= a.length) {
            return null;
        } else {
            return a[l];
        }
    }
    
    public void vacuum(){
    	vacuumed = true;
    	//logger.debug("re-structuring overflow to string[] ...");
        for(int i=overflow.values.length-1;i>=0;i--) {
            ArrayList a = (ArrayList) overflow.values[i];
            if( a !=null ){
                String[] strings = new String[a.size()];
                for(int j=0;j<strings.length;j++){
                	strings[j] = (String)a.get(j);
                }
                overflow.values[i]=strings;
            }
        }
    	//logger.debug("Vacumming primary storage...");
        for(int i=0;i<level;i++){
            array[i].vacuum();
        }
    	//logger.debug("Vacumming overflow storage...");
        overflow.vacuum();
    }

}
