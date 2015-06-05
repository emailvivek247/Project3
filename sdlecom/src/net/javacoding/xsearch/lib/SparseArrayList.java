package net.javacoding.xsearch.lib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

public class SparseArrayList implements Serializable {

	public SparseArrayList() {
		//      sb = new SparseBitVector();
		ht = new Hashtable<Integer, Object>();
	}

	//  private SparseBitVector sb = null;
	private Hashtable<Integer, Object> ht = null;
	private int maxIndex = -1;

	public Integer[] getIndicies() {
		ArrayList<Integer> a = new ArrayList<Integer>();
		Integer[] go = null;

		a.addAll((Collection<Integer>) ht.keySet());
		go = new Integer[a.size()];
		go = a.toArray(go);
		return go;
	}

	public Integer[] getIndiciesSorted() {
		ArrayList<Integer> a = new ArrayList<Integer>();
		Integer[] go = null;

		a.addAll((Collection<Integer>) ht.keySet());
		go = new Integer[a.size()];
		go = a.toArray(go);
		Arrays.sort(go);
		return go;
	}

	public void set(int key, Object value) {
		//      sb.setBit( (long) key );
		if (key > maxIndex)
			maxIndex = key;
		ht.put(Integer.valueOf(key), value);
	}

	public boolean remove(int key) {
		//        if( !sb.isBitSet( (long) key ) )
		//            return false;
		//        else
		//        {
		//            sb.clearBit( (long) key );
		ht.remove(Integer.valueOf(key));
		if (maxIndex == key) {
			maxIndex = -1; // we'll calculate the new max index when needed
		}
		return true;
		//        }
	}

	public Object get(int key) {
		//        if( sb.isBitSet( (long) key ) )
		return ht.get(Integer.valueOf(key));
		//        else
		//            return null;
	}

	public int size() {
		//        if(maxIndex < 0 && sb.getPopCount() > 0){ //find max index
		if (maxIndex < 0 && ht.size() > 0) { //find max index
			Integer[] idx = getIndicies();
			Arrays.sort(idx);
			if (idx.length > 1) {
				maxIndex = idx[idx.length - 1].intValue();
			} else {
				maxIndex = -2;
			}
		}
		return (maxIndex + 1);
	}

	public int getPopCount() {
		//        return sb.getPopCount();
		return (ht.size());
	}

	public boolean contains(int key) {
		//        return sb.isBitSet( (long) key );
		return (ht.containsKey(Integer.valueOf(key)));
	}
}
