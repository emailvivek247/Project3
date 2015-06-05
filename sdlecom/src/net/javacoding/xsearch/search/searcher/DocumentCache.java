package net.javacoding.xsearch.search.searcher;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

import net.javacoding.xsearch.foundation.LRUCache;

public class DocumentCache {

	private static Map _caches = new WeakHashMap();
	
	public static LRUCache getCache(IndexReader ir) throws IOException{
		LRUCache _cache = (LRUCache) _caches.get(ir);
		if(_cache==null){
			_cache = new LRUCache(1024*128);
		}
		return _cache;
	}
	
}
