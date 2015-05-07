package com.fdt.sdl.core.analyzer;

import java.io.Reader;

import net.javacoding.xsearch.utility.AnalyzerUtil;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;


import com.fdt.sdl.core.analyzer.phonetix.DoubleMetaphone;
import com.fdt.sdl.core.analyzer.phonetix.lucene.PhoneticAnalyzer;
import com.fdt.sdl.core.analyzer.phonetix.lucene.PhoneticFilter;

public class DoubleMetaphoneAnalyzer extends PhoneticAnalyzer {

	public DoubleMetaphoneAnalyzer() {
		super(new DoubleMetaphone());
	}
	
	public TokenStream tokenStream (String fieldname, final Reader reader)
    {
		Reader trimmedReader = AnalyzerUtil.trimReader(reader);
		TokenStream result = new StandardTokenizer(trimmedReader);
        result = new StandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopTable);
        result = new PhoneticFilter(result,encoder);
        return result;
    }

}
