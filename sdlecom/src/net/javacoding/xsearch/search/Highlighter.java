package net.javacoding.xsearch.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import net.javacoding.xsearch.search.highlight.LineFragmenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;

/** Implements hit summarization.
 *  A proxy of org.apache.lucene.search.highlight.Highlighter
*/
public class Highlighter {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private Analyzer m_analyzer;
    private org.apache.lucene.search.Query    m_query;
    private Object m_highlighter;
    private boolean isChinese;
    private Fragmenter fragmenter = null;
    private TagFormatter formatter = null;
    public Highlighter(Analyzer a, org.apache.lucene.search.Query q, String originalKeywords){
        m_analyzer = a;
        m_query = q;
        if(m_analyzer instanceof PerFieldAnalyzer){
        	PerFieldAnalyzer p = (PerFieldAnalyzer) m_analyzer;
        	isChinese = p.defaultAnalyzer instanceof  CJKAnalyzer || p.defaultAnalyzer instanceof  ChineseAnalyzer;
        }else{
        	isChinese = m_analyzer instanceof CJKAnalyzer || m_analyzer instanceof ChineseAnalyzer;
        }
        if( isChinese ){
            StringReader stringReader = new StringReader(originalKeywords);
            TokenStream tokenStream = m_analyzer.tokenStream("content", stringReader);
            ArrayList tokens = new ArrayList();
            try{
                for (Token token = tokenStream.next(); token != null;
                         token = tokenStream.next()
                    ) {
                    tokens.add(token.termText());
                }
            }catch(IOException ioe){
                logger.warn("IOException", ioe);
            }

            m_highlighter = new net.javacoding.xsearch.search.highlight.CJKHighlighter(tokens);
            String highlightTag = "b";
            ((net.javacoding.xsearch.search.highlight.CJKHighlighter)m_highlighter).setHighlightTag(highlightTag);
        }else{
            formatter = new TagFormatter();
            m_highlighter = new org.apache.lucene.search.highlight.Highlighter(formatter,new QueryScorer(m_query));
        }
    }
    /**
    * return highlighted text, if not found, return the original text
    */
    public String getHighlighted(String text){
    	return getHighlighted(text, "DEFAULT");
    }
    /*
     * Recommended highlighting function. With the filedName specified, 
     * customized analyzer for this field can be applied.
     */
    public String getHighlighted(String text, String fieldName){
        if(text == null|| text.trim().length()<=0) return "";
        if( isChinese) return getCJKHighlighted(text);
        TokenStream tokenStream=m_analyzer.tokenStream(fieldName,new StringReader(text));
        String ret = null;
        try{
            ((org.apache.lucene.search.highlight.Highlighter)m_highlighter).setTextFragmenter(new NullFragmenter());
            ret = ((org.apache.lucene.search.highlight.Highlighter)m_highlighter).getBestFragment(tokenStream,text);
            if(ret == null) ret = text;
        }catch(IOException ioe){
            logger.warn("IOException", ioe);
        }
        return ret;
    }
    public void setFragmenter(Fragmenter f) {
        fragmenter = f;
    }
    public void setSummaryLineMode() {
        fragmenter = new LineFragmenter();
    }
    private int fragmentsNumber = 4;
    public void setSummaryFragmentsNumber(int fragmentsNumber) {
        this.fragmentsNumber = fragmentsNumber;
    }
    private String fragmentsSeparator = "...";
    public void setSummaryFragmentSeparator(String sep) {
        this.fragmentsSeparator = sep;
    }
    public String getSummary(String text){
    	return getSummary(text, "DEFAULT");
    }
    public String getSummary(String text, String fieldName){
        if( isChinese) return getCJKSummary(text);
        if(text == null|| text.trim().length()<=0) return "";
        TokenStream tokenStream=m_analyzer.tokenStream(fieldName,new StringReader(text));
        ((org.apache.lucene.search.highlight.Highlighter)m_highlighter).setTextFragmenter((fragmenter==null?new SimpleFragmenter():fragmenter));
        try{
            String ret = null;
            ret = ((org.apache.lucene.search.highlight.Highlighter)m_highlighter).getBestFragments(tokenStream,text,fragmentsNumber, fragmentsSeparator);
            if(ret == null||ret.length()<=0) ret = text;
            return ret;
        }catch(IOException ioe){
            logger.warn("IOException", ioe);
        }
        return null;
    }
    private String getCJKHighlighted(String text){
        //if(!(m_analyzer instanceof CJKAnalyzer)) return "";
        return ((net.javacoding.xsearch.search.highlight.CJKHighlighter)m_highlighter).highlight(text,2560);
    }
    private String getCJKSummary(String text){
        //if(!(m_analyzer instanceof CJKAnalyzer)) return "";
        return ((net.javacoding.xsearch.search.highlight.CJKHighlighter)m_highlighter).highlight(text,512);
    }
    /**
     * @param highlightTag can be "u", "b", "i", or any simple html tag
     */
    public void setHighlightTag(String highlightTag) {
        if(isChinese){
            ((net.javacoding.xsearch.search.highlight.CJKHighlighter)m_highlighter).setHighlightTag(highlightTag);
        }else{
            formatter.setPreTag("<"+highlightTag+">");
            formatter.setPostTag("</"+highlightTag+">");
        }
    }
    /**
     * @param highlightPrefix can be <span style="background-color: #00ff00"> or any html start tag
     */
    public void setHighlightPrefix(String highlightPrefix) {
        if(isChinese){
            ((net.javacoding.xsearch.search.highlight.CJKHighlighter)m_highlighter).setHighlightPrefix(highlightPrefix);
        }else{
            formatter.setPreTag(highlightPrefix);
        }
    }
    /**
     * @param highlightSuffix can be </span> or any html end tag
     */
    public void setHighlightSuffix(String highlightSuffix) {
        if(isChinese){
            ((net.javacoding.xsearch.search.highlight.CJKHighlighter)m_highlighter).setHighlightSuffix(highlightSuffix);
        }else{
            formatter.setPostTag(highlightSuffix);
        }
    }
    
    public void setMaxDocBytesToAnalyze(int byteCount){
        if(isChinese){
        }else{
            ((org.apache.lucene.search.highlight.Highlighter)m_highlighter).setMaxDocBytesToAnalyze(byteCount);
        }
    }
    
}
