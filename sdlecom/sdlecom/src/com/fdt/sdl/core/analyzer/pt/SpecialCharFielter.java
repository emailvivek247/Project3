package com.fdt.sdl.core.analyzer.pt;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Avoid special chars in token terms. Special chars includes only accentuateds
 * letters like "á" , "Ó", etc, and "c" with a cedilla. Note that it is
 * case insensitive and it always replace with a lower case letter.
 *
 */
class SpecialCharFilter extends TokenFilter {

	private static final String[] REPLACES;
	private static final Pattern[] PATTERNS;

	static {

		REPLACES = new String[]{"a", "e", "i", "o", "u", "c"};

		PATTERNS = new Pattern[REPLACES.length];

		// pre compile patterns
		PATTERNS[0] = Pattern.compile("[âãáàä]", Pattern.CASE_INSENSITIVE);
		PATTERNS[1] = Pattern.compile("[éèêë]", Pattern.CASE_INSENSITIVE);
		PATTERNS[2] = Pattern.compile("[íìîï]", Pattern.CASE_INSENSITIVE);
		PATTERNS[3] = Pattern.compile("[óòôõö]", Pattern.CASE_INSENSITIVE);
		PATTERNS[4] = Pattern.compile("[úùûü]", Pattern.CASE_INSENSITIVE);
		PATTERNS[5] = Pattern.compile("ç", Pattern.CASE_INSENSITIVE);

	}

	/**
	 * @param in
	 */
	public SpecialCharFilter ( TokenStream in ) {

		super(in);

	}

	/**
	 * @see org.apache.lucene.analysis.TokenStream#next()
	 */
	public Token next() throws IOException {

		Token t = input.next();

		if (t == null) {

			return null;

		}

		String termText = replaceSpecial(t.termText());
		Token token = new Token(termText, t.startOffset(), t.endOffset());

		return token;

	}

	private String replaceSpecial( String text ) {

		String result = text;

		for (int i = 0; i < PATTERNS.length; i++) {

			Matcher matcher = PATTERNS[i].matcher(result);
			result = matcher.replaceAll(REPLACES[i]);

		}

		return result;

	}
}
