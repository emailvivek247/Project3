package com.fdt.sdl.core.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;

public class SimpleLowerCaseAnalyzer extends Analyzer {
	
	private SimpleAnalyzer simpleAnalyzer = new SimpleAnalyzer();

	public SimpleLowerCaseAnalyzer() {
	}

	public TokenStream reusableTokenStream(String fieldName, Reader reader)
			throws IOException {
		return simpleAnalyzer.reusableTokenStream(fieldName, reader);
	}

	public TokenStream tokenStream(String fieldName, Reader reader) {
		return simpleAnalyzer.tokenStream(fieldName, reader);

	}
}