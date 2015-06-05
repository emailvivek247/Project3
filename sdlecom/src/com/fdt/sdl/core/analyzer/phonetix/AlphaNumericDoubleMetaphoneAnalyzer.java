package com.fdt.sdl.core.analyzer.phonetix;

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

public class AlphaNumericDoubleMetaphoneAnalyzer extends PhoneticAnalyzer {

	public AlphaNumericDoubleMetaphoneAnalyzer() {
		super(new DoubleMetaphone());
	}

	public TokenStream tokenStream (String fieldname, final Reader reader)  {
		Reader outputReader = AnalyzerUtil.removeSpecialCharactersReader(reader);
        TokenStream result = new StandardTokenizer(outputReader);
        result = new StandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopTable);
        result = new PhoneticFilter(result,encoder);
        return result;
    }
}
