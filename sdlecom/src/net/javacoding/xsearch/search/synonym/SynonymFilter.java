/*
 * Created on Apr 20, 2007
 */
package net.javacoding.xsearch.search.synonym;

import java.io.IOException;
import java.util.Stack;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class SynonymFilter extends TokenFilter {

    public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";

    private Stack<Token> synonymStack; // synonym buffer
    private SynonymEngine engine;

    public SynonymFilter(TokenStream in, SynonymEngine engine) {
        super(in);
        synonymStack = new Stack<Token>();
        this.engine = engine;
    }

    public Token next() throws IOException {
        // pop buffered synonyms
        if (synonymStack.size() > 0) {
            return (Token) synonymStack.pop();
        }

        // read next token
        Token token = input.next();
        if (token == null) {
            return null;
        }

        // push synonyms of current token onto stack
        addAliasesToStack(token);

        // return current token
        return token;
    }

    private void addAliasesToStack(Token token) throws IOException {
        
        if (engine == null ) return;
        // retrieve synonyms
        String[] synonyms = engine.getSynonyms(token.termText());

        if (synonyms == null) return;

        for (int i = 0; i < synonyms.length; i++) {
            Token synToken = new Token(synonyms[i],
                                       token.startOffset(),
                                       token.endOffset(),
                                       TOKEN_TYPE_SYNONYM);
            synToken.setPositionIncrement(0);

            synonymStack.push(synToken);
        }
    }
}
