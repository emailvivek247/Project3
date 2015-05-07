/*
 * Created on May 15, 2005
 */
package net.javacoding.xsearch.search.highlight;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.highlight.Fragmenter;

/**
 * 
 *
 * @
 */
public class LineFragmenter implements Fragmenter {

    private int nextLineEnd = 0;
    private String text = null;
    public void start(String originalText) {
        nextLineEnd = 0;
        text = originalText;
    }

    /**
     * Set to a new Fragment if it's a new line
     * UNIX text files have lines delimited by a single line-feed character (0A hex), 
     * whereas DOS text-mode files are delimited with a carriage-return/line-feed pair (0D/OA hex).
     * @see org.apache.lucene.search.highlight.Fragmenter#isNewFragment(org.apache.lucene.analysis.Token)
     */
    public boolean isNewFragment(Token nextToken) {
        int nt = nextToken.startOffset();
        if(nt<nextLineEnd || nextLineEnd == -1 ) return false;
        nextLineEnd = text.indexOf(10,nt);
        return true;
    }

}
