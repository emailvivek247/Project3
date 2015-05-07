package com.fdt.sdl.core.analyzer;

import com.fdt.sdl.core.analyzer.phonetix.Soundex;
import com.fdt.sdl.core.analyzer.phonetix.lucene.PhoneticAnalyzer;

public class SoundExAnalyzer extends PhoneticAnalyzer {

	public SoundExAnalyzer() {
		super(new Soundex(true));
	}

}
