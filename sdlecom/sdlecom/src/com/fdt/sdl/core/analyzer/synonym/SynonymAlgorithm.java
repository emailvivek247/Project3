package com.fdt.sdl.core.analyzer.synonym;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;

public class SynonymAlgorithm extends Analyzer {

	private SynonymText synonym =  new SynonymText();

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new SynonymFilter(new LowerCaseFilter(new StandardFilter(new NumberLowerCaseTokenizer(
						reader))),  synonym);
		return result;
	}
	
	public void setSynonym(SynonymText synonym) {
		this.synonym = synonym;
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
        	if (c == '&') {
        		return true;
        	} else {
        		return (Character.isLetter(c) || Character.isDigit(c));
        	}
        }
    }
}