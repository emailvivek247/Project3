package com.fdt.sdl.core.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class KeywordLowerCaseAnalyzer extends Analyzer {

	public TokenStream tokenStream(String fieldName, Reader reader) {
	    return new KeywordLowerCaseTokenizer(reader);
	}

}
