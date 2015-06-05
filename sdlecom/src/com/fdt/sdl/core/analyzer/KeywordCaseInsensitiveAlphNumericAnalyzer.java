package com.fdt.sdl.core.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

public class KeywordCaseInsensitiveAlphNumericAnalyzer extends Analyzer {

	public TokenStream tokenStream(String fieldName, Reader reader) {
	    return new KeywordCaseInsensitiveAlphaNumericTokenizer(reader);
	}
	
	
	public class KeywordCaseInsensitiveAlphaNumericTokenizer extends Tokenizer {

		private static final int DEFAULT_BUFFER_SIZE = 1024;

		private boolean done;

		private final char[] buffer;

		public KeywordCaseInsensitiveAlphaNumericTokenizer(Reader input) {
			this(input, DEFAULT_BUFFER_SIZE);
		}

		public KeywordCaseInsensitiveAlphaNumericTokenizer(Reader input, int bufferSize) {
		    super(input);
		    this.buffer = new char[bufferSize];
		    this.done = false;
		}

		public Token next() throws IOException {
			if (!done) {
				done = true;
				StringBuffer buffer = new StringBuffer();
				int length;
				while (true) {
					length = input.read(this.buffer);
					if (length == -1)
						break;

					buffer.append(this.buffer, 0, length);
				}
				String text = buffer.toString().toLowerCase();
				String regularExpresssion ="[^A-Z a-z0-9]";
	            Pattern replace = Pattern.compile(regularExpresssion);
	            Matcher matcher2 = replace.matcher(text);
	            text =  matcher2.replaceAll("");
				return new Token(text, 0, text.length());
			}
			return null;
		}
	}	
}
