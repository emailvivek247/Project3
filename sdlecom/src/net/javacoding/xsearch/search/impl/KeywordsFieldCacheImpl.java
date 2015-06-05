package net.javacoding.xsearch.search.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.javacoding.xsearch.lib.FlexibleIntArray;
import net.javacoding.xsearch.lib.LayeredIntArrayList;
import net.javacoding.xsearch.lib.LayeredStringArrayList;
import net.javacoding.xsearch.search.KeywordsFieldCache;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

public class KeywordsFieldCacheImpl implements KeywordsFieldCache {

	/** The internal cache. Maps Entry to array of interpreted term values. * */
	final Map<IndexReader, HashMap<String, Object>> cache = new WeakHashMap<IndexReader, HashMap<String, Object>>();

	/** See if an object is in the cache. */
	Object lookup(IndexReader reader, String field) {
		synchronized (this) {
			HashMap readerCache = cache.get(reader);
			if (readerCache == null)
				return null;
			return readerCache.get(field);
		}
	}

	/** Put an object into the cache. */
	Object store(IndexReader reader, String field, Object value) {
		synchronized (this) {
			HashMap<String, Object> readerCache = cache.get(reader);
			if (readerCache == null) {
				readerCache = new HashMap<String, Object>();
				cache.put(reader, readerCache);
			}
			return readerCache.put(field, value);
		}
	}

	/*
	 * modified according to FieldCacheImpl.getStrings Return an arrayList(size =
	 * maxdoc()) of a list of Keywords
	 */
	public synchronized LayeredStringArrayList getStrings(IndexReader reader, String field) throws IOException {
		field = field.intern();
		Object ret = lookup(reader, field);
		if (ret == null) {
			int maxSize = reader.maxDoc();
			LayeredStringArrayList retArray = new LayeredStringArrayList(maxSize, 8);
			if (maxSize > 0) {
				TermDocs termDocs = reader.termDocs();
				TermEnum termEnum = reader.terms(new Term(field, ""));
				try {
					if (termEnum.term() == null) {
						throw new RuntimeException("no terms in field " + field);
					}
					do {
						Term term = termEnum.term();
						if (term.field() != field)
							break;
						String termval = term.text().intern();
						termDocs.seek(termEnum);
						while (termDocs.next()) {
							int i = termDocs.doc();
							retArray.set(i, termval);
						}
					} while (termEnum.next());
				} finally {
					termDocs.close();
					termEnum.close();
				}
			}
			retArray.vacuum();
			store(reader, field, retArray);
			return retArray;
		}
		return (LayeredStringArrayList) ret;
	}

	public synchronized LayeredIntArrayList getInts(IndexReader reader, String field) throws IOException {
		field = field.intern();
		Object ret = lookup(reader, field);
		if (ret == null) {
			int maxSize = reader.maxDoc();
			LayeredIntArrayList retArray = new LayeredIntArrayList(maxSize, 8);
			if (maxSize > 0) {
				TermDocs termDocs = reader.termDocs();
				TermEnum termEnum = reader.terms(new Term(field, ""));
				try {
					if (termEnum.term() == null) {
						throw new RuntimeException("no terms in field " + field);
					}
					do {
						Term term = termEnum.term();
						if (term.field() != field)
							break;
						int termval = 0;
                        try{
                            if(!U.isEmpty(term.text())){
                                termval = (int) Float.parseFloat(term.text());
                            }
                        }catch(Exception e){continue;}
						termDocs.seek(termEnum);
						while (termDocs.next()) {
							int i = termDocs.doc();
							retArray.set(i, termval);
						}
					} while (termEnum.next());
				} finally {
					termDocs.close();
					termEnum.close();
				}
			}
			retArray.vacuum();
			store(reader, field, retArray);
			return retArray;
		}
		return (LayeredIntArrayList) ret;
	}

    public synchronized int[] getIntArray(IndexReader reader, String field) throws IOException {
        field = field.intern();
        int[] ret = (int[]) lookup(reader, field);
        if (ret == null) {
            int maxSize = reader.maxDoc();
            int[] retArray = new int[maxSize];
            Arrays.fill(retArray, FlexibleIntArray.EMPTY_INT);
            if (maxSize > 0) {
                TermDocs termDocs = reader.termDocs();
                TermEnum termEnum = reader.terms(new Term(field, ""));
                try {
                    if (termEnum.term() == null) {
                        throw new RuntimeException("no terms in field " + field);
                    }
                    do {
                        Term term = termEnum.term();
                        if (term.field() != field)
                            break;
                        int termval = 0;
                        try{
                            if(!U.isEmpty(term.text())){
                                termval = (int) Float.parseFloat(term.text());
                            }
                        }catch(Exception e){continue;}
                        termDocs.seek(termEnum);
                        while (termDocs.next()) {
                            retArray[termDocs.doc()] = termval;
                        }
                    } while (termEnum.next());
                } finally {
                    termDocs.close();
                    termEnum.close();
                }
            }
            store(reader, field, retArray);
            return retArray;
        }
        return ret;
    }
}
