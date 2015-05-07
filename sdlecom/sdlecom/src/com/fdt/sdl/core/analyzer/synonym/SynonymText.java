package com.fdt.sdl.core.analyzer.synonym;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;

public class SynonymText {

	public String[] getSynonyms(String key) {
		String[] str = PageStyleUtil.getSystemValues("system", key);
		return str;
	}

}