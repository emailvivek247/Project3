package com.fdt.sdl.core.analyzer;

import java.io.Reader;

import net.javacoding.xsearch.utility.AnalyzerUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  A lucene Analyzer that filters LetterTokenizer with LowerCaseFilter
 */
public class NumberLowerCaseAnalyzer extends Analyzer {
    /**
     * The tokenStream class to process the tokens.
     * 
     * @param fieldName Needed for override?
     * @param reader The reader of tokens to normalize.
     * @return The token stream with this tokenizer.
     */
	
	private static Logger logger = LoggerFactory.getLogger(NumberLowerCaseAnalyzer.class);
	
    public final TokenStream tokenStream(String fieldName, Reader reader) {
    	Reader trimmedReader = AnalyzerUtil.trimReader(reader);
		return new NumberLowerCaseTokenizer(trimmedReader);
    }

    private class NumberLowerCaseTokenizer extends CharTokenizer {
        /**
         * Construct a new LetterTokenizer.
         * 
         * @param in The reader to get characters from.
         */
        public NumberLowerCaseTokenizer(Reader in) {
            super(in);
        }

        /**
         * Puts chars to lower case.
         * 
         * @param c The char to lower.
         * @return The lowercase version of c.
         */
        protected char normalize(char c) {
            return Character.toLowerCase(c);
        }

        /**
         * Collects only characters which satisfy {@linkjava.lang.Character#isLetter(char)} or {@link
         * java.lang.Character#isDigit(char)}
         * 
         * @param c The character to test.
         * @return <tt>true</tt> if c is a letter or digit.
         */
        protected boolean isTokenChar(char c) {
            return (Character.isLetter(c) || Character.isDigit(c));
        }
    }

}
