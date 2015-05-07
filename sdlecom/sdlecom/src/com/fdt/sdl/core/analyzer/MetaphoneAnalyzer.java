package com.fdt.sdl.core.analyzer;

import com.fdt.sdl.core.analyzer.phonetix.Metaphone;
import com.fdt.sdl.core.analyzer.phonetix.lucene.PhoneticAnalyzer;


public class MetaphoneAnalyzer extends PhoneticAnalyzer {

	public MetaphoneAnalyzer() {
		super(new Metaphone());
	}
}
