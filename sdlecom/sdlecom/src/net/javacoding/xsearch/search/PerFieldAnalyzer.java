package net.javacoding.xsearch.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;

/** A wrapper of PerFieldAnalyzerWrapper.java, to get the default analyzer
*/
public class PerFieldAnalyzer extends PerFieldAnalyzerWrapper {

	Analyzer defaultAnalyzer;
	public PerFieldAnalyzer(Analyzer defaultAnalyzer) {
		super(defaultAnalyzer);
		this.defaultAnalyzer = defaultAnalyzer;
	}
	public Analyzer getDefaultAnalyzer() {
		return defaultAnalyzer;
	}
}
