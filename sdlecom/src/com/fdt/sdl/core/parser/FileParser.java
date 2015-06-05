package com.fdt.sdl.core.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tika.Tika;

public class FileParser {
	
	private static Logger logger = LoggerFactory.getLogger(FileParser.class);	

    public String parse(InputStream is) {
    	Tika tika = new Tika();
    	String text = null;
    	try {
    		text = tika.parseToString(is);
		} catch (Exception e) {
			logger.error("Error Parsing the File", e);
		}
		return text;
    }
    
    public String parse(String inputString) {
    	Tika tika = new Tika();
    	String text = null;
    	try {
    		InputStream is = new ByteArrayInputStream(inputString.getBytes("UTF-8"));    		
    		text = tika.parseToString(is);
		} catch (Exception e) {
			logger.error("Error Parsing the File", e);
		}
		return text;
    }    	
}
