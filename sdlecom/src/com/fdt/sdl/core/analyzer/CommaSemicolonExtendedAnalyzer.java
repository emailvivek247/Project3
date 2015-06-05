package com.fdt.sdl.core.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class CommaSemicolonExtendedAnalyzer extends Analyzer {

    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new MultipleWordedKeywordTokenizer(new CommaSemicolonTokenizer(reader));
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
    
    private class MultipleWordedKeywordTokenizer extends TokenFilter{

        private Stack<Token> wordStack;

        public MultipleWordedKeywordTokenizer(TokenStream in) {
            super(in);
            this.wordStack = new Stack<Token>();
        }

        public Token next() throws IOException {
            if (wordStack.size() > 0) {
                return (Token) wordStack.pop();
            }
            Token token = input.next();
            if (token == null) {
                return null;
            }

            if(token.termText().indexOf(" ")>0) {
                String[] words = token.termText().split(" ");
                for (int i = 0; i < words.length; i++) {
                    Token partialToken = new Token(words[i],
                                               token.startOffset(),
                                               token.endOffset(),
                                               "Multi-Worded Keyword");
                    partialToken.setPositionIncrement(0);

                    wordStack.push(partialToken);
                }
            }

            return token;
        }
        
    }

}
