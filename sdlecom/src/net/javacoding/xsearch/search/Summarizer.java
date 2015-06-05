package net.javacoding.xsearch.search;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import net.javacoding.xsearch.search.Summary.*;
import net.javacoding.xsearch.search.query.DbsQuery;

/** Implements hit summarization. */
public class Summarizer {

  /** The number of context terms to display preceding and following matches.*/
  private static final int SUM_CONTEXT = 5;

  /** The total number of terms to display in a summary.*/
  private static final int SUM_LENGTH = 20;

  /** Converts text to tokens. */
  private static final Analyzer ANALYZER = new StandardAnalyzer();

  /**
   * Class Excerpt represents a single passage found in the
   * document, with some appropriate regions highlit.
   */
  static class Excerpt {
      ArrayList<Fragment> passages = new ArrayList<Fragment>();
      SortedSet<String> tokenSet = new TreeSet<String>();
      int numTerms = 0;

      /**
       */
      public Excerpt() {
      }

      /**
       */
      public void addToken(String token) {
          tokenSet.add(token);
      }

      /**
       * Return how many unique toks we have
       */
      public int numUniqueTokens() {
          return tokenSet.size();
      }

      /**
       * How many fragments we have.
       */
      public int numFragments() {
          return passages.size();
      }

      public void setNumTerms(int numTerms) {
          this.numTerms = numTerms;
      }

      public int getNumTerms() {
          return numTerms;
      }

      /**
       * Add a frag to the list.
       */
      public void add(Fragment fragment) {
          passages.add(fragment);
      }

      /**
       * Return an Enum for all the fragments
       */
      public Fragment[] toArray() {
          return passages.toArray(new Fragment[0]);
      }
  }

  /** Returns a string highlighted for the given pre-tokenized text. */
  public static String getHighlighted(String text, DbsQuery query) throws IOException {
    Token[] tokens = getTokens(text);             // parse text to token array
    if (tokens==null || tokens.length == 0) return "";

    String[] terms = query.getTerms();
    HashSet<String> highlight = new HashSet<String>();            // put query terms in table
    for (int i = 0; i < terms.length; i++)
      highlight.add(terms[i]);

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < tokens.length; i++) {
      //anything before the token
      if(i==0){
        sb.append(text.substring(0,tokens[i].startOffset()));
      }else{
        sb.append(text.substring(tokens[i-1].endOffset(),tokens[i].startOffset()));
      }
      //the token itself
      if (highlight.contains(tokens[i].termText())) {
        sb.append("<b>").append(text.substring(tokens[i].startOffset(),tokens[i].endOffset())).append("</b>");
      }else{
        sb.append(text.substring(tokens[i].startOffset(),tokens[i].endOffset()));
      }
    }
    sb.append(text.substring(tokens[tokens.length-1].endOffset()));
    return sb.toString();
  }

  /** Returns a summary for the given pre-tokenized text. */
  public static Summary getSummary(String text, DbsQuery query) throws IOException {
      return getSummary(text, query, SUM_LENGTH);
  }
  /** Returns a summary for the given pre-tokenized text. */
  public static Summary getSummary(String text, DbsQuery query, int summaryLength) throws IOException {

    // Simplistic implementation.  Finds the first fragments in the document
    // containing any query terms.
    //
    // TODO: check that phrases in the query are matched in the fragment

    Token[] tokens = getTokens(text);             // parse text to token array

    if (tokens==null || tokens.length == 0) return new Summary();

    String[] terms = query.getTerms();
    HashSet<String> highlight = new HashSet<String>();            // put query terms in table
    for (int i = 0; i < terms.length; i++)
      highlight.add(terms[i]);

    //
    // Create a SortedSet that ranks excerpts according to
    // how many query terms are present.  An excerpt is
    // a Vector full of Fragments and Highlights
    //
    SortedSet<Excerpt> excerptSet = new TreeSet<Excerpt>(new Comparator() {
        public int compare(Object o1, Object o2) {
            Excerpt excerpt1 = (Excerpt) o1;
            Excerpt excerpt2 = (Excerpt) o2;

            if (excerpt1 == null && excerpt2 != null) {
                return -1;
            } else if (excerpt1 != null && excerpt2 == null) {
                return 1;
            } else if (excerpt1 == null && excerpt2 == null) {
                return 0;
            }

            int numToks1 = excerpt1.numUniqueTokens();
            int numToks2 = excerpt2.numUniqueTokens();

            if (numToks1 < numToks2) {
                return -1;
            } else if (numToks1 == numToks2) {
                int result = excerpt1.numFragments() - excerpt2.numFragments();
                if (result == 0) {
                    return excerpt1.hashCode() - excerpt2.hashCode();
                } else {
                    return result;
                }
            } else {
                return 1;
            }
        }
    }
        );

    //
    // Iterate through all terms in the document
    //
    int lastExcerptPos = 0;
    for (int i = 0; i < tokens.length; i++) {
      //
      // If we find a term that's in the query...
      //
      if (highlight.contains(tokens[i].termText())) {
        //
        // Start searching at a point SUM_CONTEXT terms back,
        // and move SUM_CONTEXT terms into the future.
        //
        int startToken = (i > SUM_CONTEXT) ? i-SUM_CONTEXT : 0;
        int endToken = Math.min(i+SUM_CONTEXT, tokens.length);
        int offset = tokens[startToken].startOffset();
        int j = startToken;

        //
        // Iterate from the start point to the finish, adding
        // terms all the way.  The end of the passage is always
        // SUM_CONTEXT beyond the last query-term.
        //
        Excerpt excerpt = new Excerpt();
        if (i != 0) {
            excerpt.add(new Summary.Ellipsis());
        }

        //
        // Iterate through as long as we're before the end of
        // the document and we haven't hit the max-number-of-items
        // -in-a-summary.
        //
        while ((j < endToken) && (j - startToken < summaryLength)) {
          //
          // Now grab the hit-element, if present
          //
          //TODO: chris: need to highlight other terms in the same text
          Token t = tokens[j];
          if (highlight.contains(t.termText())) {
            excerpt.addToken(t.termText());
            excerpt.add(new Fragment(text.substring(offset, t.startOffset())));
            excerpt.add(new Highlight(text.substring(t.startOffset(),t.endOffset())));
            offset = t.endOffset();
            endToken = Math.min(j+SUM_CONTEXT, tokens.length);
          }

          j++;
        }

        lastExcerptPos = endToken;

        //
        // We found the series of search-term hits and added
        // them (with intervening text) to the excerpt.  Now
        // we need to add the trailing edge of text.
        //
        // So if (j < tokens.length) then there is still trailing
        // text to add.  (We haven't hit the end of the source doc.)
        // Add the words since the last hit-term insert.
        //
        if (j < tokens.length) {
          excerpt.add(new Fragment(text.substring(offset,tokens[j].endOffset())));
        }

        //
        // Remember how many terms are in this excerpt
        //
        excerpt.setNumTerms(j - startToken);

        //
        // Store the excerpt for later sorting
        //
        excerptSet.add(excerpt);

        //
        // Start SUM_CONTEXT places away.  The next
        // search for relevant excerpts begins at i-SUM_CONTEXT
        //
        i = j+SUM_CONTEXT;
      }
    }

    //
    // If the target text doesn't appear, then we just
    // excerpt the first summaryLength words from the document.
    //
    if (excerptSet.size() == 0) {
        Excerpt excerpt = new Excerpt();
        int excerptLen = Math.min(summaryLength, tokens.length);
        lastExcerptPos = excerptLen;

        excerpt.add(new Fragment(text.substring(tokens[0].startOffset(), tokens[excerptLen-1].startOffset())));
        excerpt.setNumTerms(excerptLen);
        excerptSet.add(excerpt);
    }

    //
    // Now choose the best items from the excerpt set.
    // Stop when our Summary grows too large.
    //
    double tokenCount = 0;
    Summary s = new Summary();
    while (tokenCount <= summaryLength && excerptSet.size() > 0) {
        Excerpt excerpt = excerptSet.last();
        excerptSet.remove(excerpt);

        double tokenFraction = (1.0 * excerpt.getNumTerms()) / excerpt.numFragments();
        Fragment[] fs = excerpt.toArray();
        for (int i=0;i<fs.length;i++) {
            Fragment f = fs[i];
            // Don't add fragments if it takes us over the max-limit
            if (tokenCount + tokenFraction <= summaryLength) {
                s.add(f);
            }
            tokenCount += tokenFraction;
        }
    }

    if (tokenCount > 0 && lastExcerptPos < tokens.length)
      s.add(new Ellipsis());
    return s;
  }

  private static Token[] getTokens(String text) throws IOException {
    if(text==null || text.trim().length()==0) return null;
    ArrayList<Token> result = new ArrayList<Token>();
    TokenStream ts = ANALYZER.tokenStream("content", new StringReader(text));
    for (Token token = ts.next(); token != null; token = ts.next()) {
      result.add(token);
    }
    return result.toArray(new Token[result.size()]);
  }

    /**
     * Tests Summary-generation.  User inputs the name of a
     * text file and a query string
     */
    public static void main(String argv[]) throws Exception {
        // Test arglist
        if (argv.length < 2) {
            System.out.println("Usage: java net.javacoding.xsearch.search.Summarizer <textfile> <queryStr>");
            return;
        }

        //
        // Parse the args
        //
        File textFile = new File(argv[0]);
        StringBuffer queryBuf = new StringBuffer();
        for (int i = 1; i < argv.length; i++) {
            queryBuf.append(argv[i]);
            queryBuf.append(" ");
        }

        //
        // Load the text file into a single string.
        //
        StringBuffer body = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(textFile));
        try {
            System.out.println("About to read " + textFile + " from " + in);
            String str = in.readLine();
            while (str != null) {
                body.append(str);
                str = in.readLine();
            }
        } finally {
            in.close();
        }

        // Convert the query string into a proper Query
        DbsQuery query = DbsQuery.parse(queryBuf.toString());
        System.out.println("Summary: '" + Summarizer.getSummary(body.toString(), query) + "'");
    }
}
