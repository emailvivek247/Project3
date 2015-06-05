package com.fdt.sdl.core.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

public class KeywordLowerCaseTokenizer extends Tokenizer {

	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private boolean done;

	private final char[] buffer;

	public KeywordLowerCaseTokenizer(Reader input) {
		this(input, DEFAULT_BUFFER_SIZE);
	}

	public KeywordLowerCaseTokenizer(Reader input, int bufferSize) {
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
			return new Token(text, 0, text.length());
		}
		return null;
	}

}
