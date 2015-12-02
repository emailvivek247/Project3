package net.javacoding.xsearch.search;

import java.util.StringTokenizer;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.analysis.QueryAnalysis;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.utility.EscapeChars;
import net.javacoding.xsearch.utility.U;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/** Implements parsing a query for search
*/
public class SearchQueryParser {

    private static final  Logger logger = LoggerFactory.getLogger(SearchQueryParser.class);

    public static String elasticsearchParse(String text) {
        text = reformatQuery(text);
        text = reformatRangeQuery(text);
        return text;
    }

    public static Query luceneParse(Analyzer analyzer, String text) throws Exception{
        BooleanQuery.setMaxClauseCount(16384);
        QueryParser parser = new QueryParser("content", analyzer);
        parser.setAllowLeadingWildcard(true);
        text = reformatQuery(text);
        text = reformatRangeQuery(text);
        Query query = parser.parse(text);
        logger.debug("Query: " + query.toString("content"));
        logger.debug("Query: " + HTMLEntities.encode(query.toString("content")));
        return query;
    }

    private static String reformatQuery(String originalText) {
      StringBuffer formattedText = new StringBuffer();
        String[] text = StringUtils.split(originalText, "#");
        if (text.length == 1) {
            String decodedString = EscapeChars.decodeURL(originalText);
            text =  decodedString.split("#");
        }
        StringBuffer formattedWildCardText = new StringBuffer();
      logger.debug("Input String is : " + originalText);
        for (String splitText:text) {
            if (!splitText.trim().isEmpty()) {
                  if (splitText.contains("*") || splitText.contains("?")) {
                        splitText = splitText.replace("~5", "");
                        String wildCardColumnName = null;
                        formattedWildCardText = new StringBuffer();
                        
                        for (String wildCardSplitText: splitText.split("\"")) {
                              if (wildCardColumnName == null) {
                                    wildCardColumnName = wildCardSplitText;
                              } else {
                                    StringTokenizer st = new StringTokenizer(wildCardSplitText);
                                    while (st.hasMoreTokens()) {
                                          formattedWildCardText = formattedWildCardText.append(wildCardColumnName);
                                          formattedWildCardText = formattedWildCardText.append(st.nextToken());
                                          if (st.hasMoreTokens()) {
                                                formattedWildCardText = formattedWildCardText.append(" AND ");
                                          }
                                  }
                              }
                        }
                        formattedText.append(formattedWildCardText.toString());
                        } else {
                              formattedText.append(splitText);
                        }
                  } 
            }
            logger.debug("Wildcard Formatted String is : " + formattedText.toString());
            return formattedText.toString();
    } 
    
    public static void main(String[] args) {
      String originalText ="aaaa4555aaaaa TAXAMT:[|134| TO |135|]";
        System.out.println("The Reformated Query is " + reformatRangeQuery(originalText));
    }
    
    public static String reformatRangeQuery(String originalRangeQuery) {
      logger.debug("Original Range Query String is : " + originalRangeQuery);
      if (!StringUtils.contains(originalRangeQuery, "|")) {
            return originalRangeQuery ;
      } else {
            String[] spliTextArray = StringUtils.split(originalRangeQuery, "|");
            StringBuffer formattedRangeQueryText = new StringBuffer();
            for (String splitText:spliTextArray) {
                  formattedRangeQueryText.append(splitText);
            }
            logger.debug("Range Query Formatted String is : " + formattedRangeQueryText.toString());
            return formattedRangeQueryText.toString();
      }
    }    
    
    

    public static Query parse(DatasetConfiguration dc, String text) throws Exception{
        BooleanQuery.setMaxClauseCount(16384);
        return    parse(dc,text,null);
    }

    public static Query parse(DatasetConfiguration dc, String text,FilterResult filterResult) throws Exception{
        if(U.isEmpty(text)) return null;
        QueryTranslator translator = new QueryTranslator(dc.getColumns());
        /*
         for (int i = 0; i < dc.getColumns().size(); i++) {
            Column c = (Column)dc.getColumns().get(i);
            if (c != null) {
                translator.add(c.getColumnName(), c.getSearchWeight());
            }
        }
        */
        //logger.debug("Start parse query: "+HTMLEntities.encode(text));
        net.javacoding.xsearch.search.query.DbsQuery myQuery = QueryAnalysis.parseQuery(text,dc);
        //logger.debug("parsed query: "+HTMLEntities.encode(myQuery.toString()));
        translator.setSlop(5);
        Query query = translator.translate(dc.getAnalyzer(), myQuery, filterResult,dc);
        //String suggestion = SpellChecker.checkSpell(dc,myQuery,text);
        //logger.debug("translated query: "+HTMLEntities.encode(query.toString()));

        return query;
    }


}
