package com.fdt.sdl.core.analyzer;

import java.io.Reader;
import java.util.Collections;

import net.javacoding.xsearch.utility.AnalyzerUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Class Removes All Non Alpha Numeric Characters from Input.  **/
public class AlphaNumericAnalyzer extends Analyzer {

	 private static Logger logger = LoggerFactory.getLogger(AlphaNumericAnalyzer.class);

	 private static final Analyzer STANDARD = new StandardAnalyzer(Collections.emptySet());

	 public final TokenStream tokenStream(String paramString, Reader paramReader) {
		Reader outputReader = AnalyzerUtil.removeSpecialCharactersReader(paramReader);
		TokenStream tk = STANDARD.tokenStream(paramString, outputReader);
		return tk;
	 }
}
