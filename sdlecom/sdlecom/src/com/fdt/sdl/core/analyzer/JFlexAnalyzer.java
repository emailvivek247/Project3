package com.fdt.sdl.core.analyzer;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

/**
 * 
 */
public class JFlexAnalyzer extends Analyzer {
    public JFlexAnalyzer() {}

    public TokenStream tokenStream(String fieldName, Reader aReader) {
        return new JFlexTokenStream(aReader);
    }

    class JFlexTokenStream extends TokenStream {
        JFlexParser tokenizer;

        public JFlexTokenStream(Reader aReader) {
            tokenizer = new JFlexParser(aReader);
        }

        public void close() throws IOException {}

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.lucene.analysis.TokenStream#next()
         */
        public Token next() throws IOException {
            try {
                int t = tokenizer.getNextToken();

                if (t == JFlexParser.YYEOF) { return null; }

 /*               if (tokenTypeHolder != null)
                {
                    switch (t)
                    {
                        case JFlexWordBasedParserImpl.EMAIL:
                            tokenTypeHolder[0] = TYPE_EMAIL;
                            break;
                            
                        case JFlexWordBasedParserImpl.FULL_URL:
                        case JFlexWordBasedParserImpl.FILE:
                            tokenTypeHolder[0] = TYPE_URL;
                            break;

                        case JFlexWordBasedParserImpl.TERM:
                        case JFlexWordBasedParserImpl.HYPHTERM:
                        case JFlexWordBasedParserImpl.ACRONYM:
                        case JFlexWordBasedParserImpl.BARE_URL:
                            tokenTypeHolder[0] = TYPE_TERM;
                            break;

                        case JFlexWordBasedParserImpl.SENTENCEMARKER:
                            tokenTypeHolder[0] = TYPE_SENTENCEMARKER;
                            break;

                        case JFlexWordBasedParserImpl.PUNCTUATION:
                            tokenTypeHolder[0] = TYPE_PUNCTUATION;
                            break;

                        case JFlexWordBasedParserImpl.NUMERIC:
                            tokenTypeHolder[0] = TYPE_NUMERIC;
                            break;

                        default:
                            throw new RuntimeException("Unexpected token type: "
                                + t);
                    }
                }*/

                String image = tokenizer.yytext().toLowerCase();
                return new Token(image, tokenizer.yyposition(), tokenizer.yyposition()+image.length());
            } catch (NullPointerException e) {
                // catching exception costs nothing
                if (tokenizer == null) { throw new RuntimeException("Initialize tokenizer first."); }

                throw e;
            } catch (IOException e) {
                throw new RuntimeException("Parser exception: ", e);
            }
        }
    }
    public static void main(String[] arg){
        String text = "The quick brown fox jumped over the lazy dogs, X.Y.Z., AB&C Corporation - xyz@example.com, article1234";
        JFlexAnalyzer analyzer = new JFlexAnalyzer();
        try {
        TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
        while (true) {
            Token token = stream.next();
            if (token == null) break;
            System.out.print(" "+token.termText());
        }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

