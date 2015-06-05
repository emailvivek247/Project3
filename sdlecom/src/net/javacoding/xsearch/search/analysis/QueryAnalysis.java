/* Generated By:JavaCC: Do not edit this line. QueryAnalysis.java */
package net.javacoding.xsearch.search.analysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Set;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.query.DbsClause;
import net.javacoding.xsearch.search.query.DbsQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/** The JavaCC-generated Nutch lexical analyzer and query parser. */
public class QueryAnalysis implements QueryAnalysisConstants {
  private static Logger logger = LoggerFactory.getLogger(QueryAnalysis.class.getName());
  private DatasetConfiguration dc;
  private Analyzer analyzer;
  public boolean debug = false;

  public QueryAnalysis(CharStream stream,DatasetConfiguration dc,Analyzer a) {
    this(stream);
    this.dc = dc;
    //changing from a to WhitespaceAnalyzer is to prevent applying analyzer twice, especially SoundEx analyzer
    //but this caused wildcard search not being stripped for cases when under minimal length, effectively allowing wildcard searches
    this.analyzer = new WhitespaceAnalyzer();
  }
  private static final String[] STOP_WORDS = {};

  public static final Set STOP_SET = StopFilter.makeStopSet(STOP_WORDS);

  private String queryString;
  /** True iff word is a stop word.  Stop words are only removed from queries.
   * Every word is indexed.  */
  public static boolean isStopWord(String word) {
    return STOP_SET.contains(word);
  }

  /** Construct a query parser for the text in a reader. */
  public static DbsQuery parseQuery(String queryString) throws ParseException {
    QueryAnalysis parser =
      new QueryAnalysis(new FastCharStream(new StringReader(queryString)));
    parser.queryString = queryString;
    return parser.parse();
  }

  /** Construct a query parser for the text in a reader.
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  public static DbsQuery parseQueryDebug(String queryString,DatasetConfiguration dc) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    QueryAnalysis parser = new QueryAnalysis(new FastCharStream(new StringReader(queryString)),dc,dc.getAnalyzer());
    parser.debug = true;
    parser.queryString = queryString;
    return parser.parse();
  }
  public static DbsQuery parseQueryDebug(String queryString,DatasetConfiguration dc, Analyzer a) throws ParseException {
    QueryAnalysis parser = new QueryAnalysis(new FastCharStream(new StringReader(queryString)),dc,a);
    parser.debug = true;
    parser.queryString = queryString;
    return parser.parse();
  }


/** Construct a query parser for the text in a reader. */
  public static DbsQuery parseQuery(String queryString,DatasetConfiguration dc,Analyzer a) throws ParseException {
    QueryAnalysis parser = new QueryAnalysis(new FastCharStream(new StringReader(queryString)),dc,a);
    parser.queryString = queryString;
    return parser.parse();
  }
  public static DbsQuery parseQuery(String queryString,DatasetConfiguration dc) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    QueryAnalysis parser = new QueryAnalysis(new FastCharStream(new StringReader(queryString)),dc,dc.getAnalyzer());
    parser.queryString = queryString;
    return parser.parse();
  }

  /**
 *  use dataset info and analyzer to process input
*/
private boolean invalidField(String field){
 return (!DbsClause.DEFAULT_FIELD.equals(field)&& dc!= null && dc.findColumn(field)==null);
}
private void customizedProcess(String input, String field, ArrayList<String> result,boolean wildcard){
    if (dc!= null && dc.isRawField(field))  {
      result.clear();
      result.add(input);
    } else if (analyzer!=null){
      try {
        //if invalid field, restore the input and field
        if (invalidField(field)) {
            if (debug) logger.info(field+" is invalid");
                input = field+":"+input;
                field =DbsClause.DEFAULT_FIELD;
            if (debug) logger.info("input="+input);
        }
        result.clear();

        //wild card case
       if (( dc == null || dc.getIsWildcardAllowed())&& wildcard){
           int prefix = 5;
           if (dc!=null) prefix=dc.getMinWildcardPrefixLength();
           int starIndex = input.indexOf("*");
           int questionIndex =input.indexOf("?");
           if (starIndex>=prefix&&questionIndex<0 || questionIndex>=prefix&&starIndex<0 ||questionIndex>=prefix&&starIndex>=prefix ){
              if(dc!=null&&dc.getIsWildcardLowercaseNeeded()){
                      result.add(input.toLowerCase());
              }else{
                      result.add(input);
                  }
              return;
           }
        }

        String columnName = field;
        if (!DbsClause.DEFAULT_FIELD.equals(field)) {
            columnName = dc.findColumn(field).getColumnName();
        }
        if (debug) logger.info("columnName="+columnName);
        //regular case
        TokenStream stream = analyzer.tokenStream(columnName, new StringReader(input));
        while (true) {
            org.apache.lucene.analysis.Token t = stream.next();
            if (t == null) break;
            if (debug) logger.info("token="+t.termText());
            result.add(t.termText());
        }
      }catch (Exception e){
        e.printStackTrace();
      }
    }
}


  /** For debugging. */
  public static void main(String[] args) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    Analyzer analyzer = new StandardAnalyzer();
    while (true) {
      System.out.print("Query: ");
      String line = in.readLine();
      System.out.println(parseQueryDebug(line,null, analyzer));
    }
  }

/** Parse a query. */
  final public DbsQuery parse() throws ParseException {
  DbsQuery query = new DbsQuery();
  ArrayList<String> terms;
  Token token;
  String field;
  boolean stop;
  boolean prohibited;
  boolean required;
  String slop;
    nonOpOrTerm();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case WORD:
      case INCHES:
      case ACRONYM:
      case SIGRAM:
      case PLUS:
      case MINUS:
      case QUOTE:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      stop=true; required = false; prohibited=false; field = DbsClause.DEFAULT_FIELD; slop=null;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case PLUS:
          jj_consume_token(PLUS);
              stop=false;required=true;
          break;
        case MINUS:
          jj_consume_token(MINUS);
                                                      stop=false;prohibited=true;
          break;
        default:
          jj_la1[1] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[2] = jj_gen;
        ;
      }
      if (jj_2_1(2147483647)) {
        token = jj_consume_token(WORD);
        jj_consume_token(COLON);
      field = token.image;
      if (debug) logger.info("field="+field);
      } else {
        ;
      }
      if (jj_2_3(2147483647)) {
        terms = phrase(field);
                           stop=false;
        if (jj_2_2(2147483647)) {
          jj_consume_token(SLOP);
          token = jj_consume_token(WORD);
            if (debug)  logger.info("slop exist="+token.image );
            slop = token.image;
        } else {
          ;
        }
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case WORD:
        case INCHES:
        case ACRONYM:
        case SIGRAM:
          // quoted terms or
                terms = compound(field);
          break;
        default:
          jj_la1[3] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
      nonOpOrTerm();
      String[] array = terms.toArray(new String[terms.size()]);

      if (stop && terms.size()==1 && isStopWord(array[0])) {
        // ignore stop words only when single, unadorned terms
      } else {
       if (invalidField(field))
           field = DbsClause.DEFAULT_FIELD;
       query.addPhrase(array, field, slop, required, prohibited);
      }
    }
    jj_consume_token(0);
    {if (true) return query;}
    throw new Error("Missing return statement in function");
  }

/** Parse an explcitly quoted phrase query.  Note that this may return a single
 * term, a trivial phrase.*/
  final public ArrayList<String> phrase(String field) throws ParseException {
  int start;
  int end;
  ArrayList<String> result = new ArrayList<String>();
  String term;
    jj_consume_token(QUOTE);
    start = token.endColumn;
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
      case COLON:
      case SLOP:
      case WHITE:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_2;
      }
      nonTerm();
    }
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case WORD:
      case INCHES:
      case ACRONYM:
      case SIGRAM:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_3;
      }
      term = term();
                    result.add(term);
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case PLUS:
        case MINUS:
        case COLON:
        case SLOP:
        case WHITE:
          ;
          break;
        default:
          jj_la1[6] = jj_gen;
          break label_4;
        }
        nonTerm();
      }
    }
    end = token.endColumn;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case QUOTE:
      jj_consume_token(QUOTE);
      break;
    case 0:
      jj_consume_token(0);
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    String phrase = queryString.substring(start, end);
    if (debug)  logger.info("phrase="+ phrase);
    customizedProcess(phrase,field,result,false);
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

/** Parse a compound term that is interpreted as an implicit phrase query.
 * Compounds are a sequence of terms separated by infix characters.  Note that
 * htis may return a single term, a trivial compound. */
  final public ArrayList<String> compound(String field) throws ParseException {
  int start;
  ArrayList<String> result = new ArrayList<String>();
  String term;
    start = token.endColumn;
    term = term();
                  result.add(term);
    label_5:
    while (true) {
      if (jj_2_4(2147483647)) {
        ;
      } else {
        break label_5;
      }
      label_6:
      while (true) {
        infix();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case PLUS:
        case MINUS:
        case COLON:
        case SLOP:
          ;
          break;
        default:
          jj_la1[8] = jj_gen;
          break label_6;
        }
      }
      term = term();
                    result.add(term);
    }
    String compound = queryString.substring(start, token.endColumn);
    if (debug)  logger.info("compound="+ compound);
    customizedProcess(compound,field,result,true);
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

/** Parse a single term. */
  final public String term() throws ParseException {
  Token token;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case WORD:
      token = jj_consume_token(WORD);
      break;
    case ACRONYM:
      token = jj_consume_token(ACRONYM);
      break;
    case SIGRAM:
      token = jj_consume_token(SIGRAM);
      break;
    case INCHES:
      token = jj_consume_token(INCHES);
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  // logger.info("term="+token.image);
  {if (true) return token.image;}
    throw new Error("Missing return statement in function");
  }

/** Parse anything but a term or a quote. */
  final public void nonTerm() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case WHITE:
      jj_consume_token(WHITE);
      break;
    case PLUS:
    case MINUS:
    case COLON:
    case SLOP:
      infix();
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

/** Parse anything but a term or an operator (plur or minus or quote). */
  final public void nonOpOrTerm() throws ParseException {
    label_7:
    while (true) {
      if (jj_2_5(2)) {
        ;
      } else {
        break label_7;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case WHITE:
        jj_consume_token(WHITE);
        break;
      case COLON:
      case SLOP:
        nonOpInfix();
        break;
      case PLUS:
      case MINUS:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case PLUS:
          jj_consume_token(PLUS);
          break;
        case MINUS:
          jj_consume_token(MINUS);
          break;
        default:
          jj_la1[11] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        nonTerm();
        break;
      default:
        jj_la1[12] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

/** Characters which can be used to form compound terms. */
  final public void infix() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PLUS:
      jj_consume_token(PLUS);
      break;
    case MINUS:
      jj_consume_token(MINUS);
      break;
    case COLON:
    case SLOP:
      nonOpInfix();
      break;
    default:
      jj_la1[13] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

/** Parse infix characters except plus and minus. */
  final public void nonOpInfix() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case COLON:
      jj_consume_token(COLON);
      break;
    case SLOP:
      jj_consume_token(SLOP);
      break;
    default:
      jj_la1[14] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  final private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  final private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  final private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  final private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  final private boolean jj_3R_18() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(6)) {
    jj_scanpos = xsp;
    if (jj_scan_token(7)) {
    jj_scanpos = xsp;
    if (jj_3R_23()) return true;
    }
    }
    return false;
  }

  final private boolean jj_3R_8() {
    if (jj_3R_10()) return true;
    return false;
  }

  final private boolean jj_3R_15() {
    if (jj_3R_12()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_21()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  final private boolean jj_3R_24() {
    if (jj_3R_18()) return true;
    return false;
  }

  final private boolean jj_3_2() {
    if (jj_scan_token(SLOP)) return true;
    if (jj_scan_token(WORD)) return true;
    return false;
  }

  final private boolean jj_3_5() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(19)) {
    jj_scanpos = xsp;
    if (jj_3R_13()) {
    jj_scanpos = xsp;
    if (jj_3R_14()) return true;
    }
    }
    return false;
  }

  final private boolean jj_3_3() {
    if (jj_3R_10()) return true;
    return false;
  }

  final private boolean jj_3R_14() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(6)) {
    jj_scanpos = xsp;
    if (jj_scan_token(7)) return true;
    }
    if (jj_3R_20()) return true;
    return false;
  }

  final private boolean jj_3R_20() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(19)) {
    jj_scanpos = xsp;
    if (jj_3R_24()) return true;
    }
    return false;
  }

  final private boolean jj_3_1() {
    if (jj_scan_token(WORD)) return true;
    if (jj_scan_token(COLON)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_8()) {
    jj_scanpos = xsp;
    if (jj_3R_9()) return true;
    }
    return false;
  }

  final private boolean jj_3R_22() {
    if (jj_3R_20()) return true;
    return false;
  }

  final private boolean jj_3R_17() {
    if (jj_3R_12()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_22()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  final private boolean jj_3R_16() {
    if (jj_3R_20()) return true;
    return false;
  }

  final private boolean jj_3R_12() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(1)) {
    jj_scanpos = xsp;
    if (jj_scan_token(4)) {
    jj_scanpos = xsp;
    if (jj_scan_token(5)) {
    jj_scanpos = xsp;
    if (jj_scan_token(2)) return true;
    }
    }
    }
    return false;
  }

  final private boolean jj_3R_23() {
    if (jj_3R_19()) return true;
    return false;
  }

  final private boolean jj_3R_13() {
    if (jj_3R_19()) return true;
    return false;
  }

  final private boolean jj_3R_10() {
    if (jj_scan_token(QUOTE)) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_16()) { jj_scanpos = xsp; break; }
    }
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_17()) { jj_scanpos = xsp; break; }
    }
    xsp = jj_scanpos;
    if (jj_scan_token(8)) {
    jj_scanpos = xsp;
    if (jj_scan_token(0)) return true;
    }
    return false;
  }

  final private boolean jj_3R_11() {
    if (jj_3R_18()) return true;
    return false;
  }

  final private boolean jj_3_4() {
    Token xsp;
    if (jj_3R_11()) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_11()) { jj_scanpos = xsp; break; }
    }
    if (jj_3R_12()) return true;
    return false;
  }

  final private boolean jj_3R_9() {
    if (jj_3R_15()) return true;
    return false;
  }

  final private boolean jj_3R_19() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(9)) {
    jj_scanpos = xsp;
    if (jj_scan_token(12)) return true;
    }
    return false;
  }

  final private boolean jj_3R_25() {
    if (jj_3R_18()) return true;
    return false;
  }

  final private boolean jj_3R_21() {
    Token xsp;
    if (jj_3R_25()) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_25()) { jj_scanpos = xsp; break; }
    }
    if (jj_3R_12()) return true;
    return false;
  }

  public QueryAnalysisTokenManager token_source;
  public Token token, jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  public boolean lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[15];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x1f6,0xc0,0xc0,0x36,0x812c0,0x36,0x812c0,0x101,0x12c0,0x36,0x812c0,0xc0,0x812c0,0x12c0,0x1200,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[5];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  public QueryAnalysis(CharStream stream) {
    token_source = new QueryAnalysisTokenManager(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(CharStream stream) {
    token_source.ReInit(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public QueryAnalysis(QueryAnalysisTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(QueryAnalysisTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration<int[]> e = jj_expentries.elements(); e.hasMoreElements();) {
        int[] oldentry = (e.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[24];
    for (int i = 0; i < 24; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 15; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 24; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 5; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
