package com.fdt.sdl.core.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;

public class CommaSemicolonAnalyzer extends Analyzer {

    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new CommaSemicolonTokenizer(reader);
    }
    
    private class CommaSemicolonTokenizer extends CharTokenizer{

        boolean previousCharacterIsBoundary = true;

        public CommaSemicolonTokenizer(Reader input) {
            super(input);
        }

        protected boolean isTokenChar(char c) {
            if(c==';'||c==',') {
                previousCharacterIsBoundary = true;
                return false;
            }
            if(Character.isWhitespace(c)&&previousCharacterIsBoundary) {
                return false;
            }
            previousCharacterIsBoundary = false;
            return true;
        }

		protected char normalize(char c) {
			return new String(new char[]{c}).toLowerCase().charAt(0);
		}
        
    }

}
