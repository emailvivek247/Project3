/*
 */
package net.javacoding.xsearch.search.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.indexer.textfilter.ZipToGeoConvertor;
import net.javacoding.xsearch.indexer.textfilter.ZipToGeoConvertor.GeoPosition;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.result.filter.FilteredColumn;
import net.javacoding.xsearch.search.result.filter.StringRangeFilterValue;
import net.javacoding.xsearch.utility.NumberUtils;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.regex.JakartaRegexpCapabilities;
import org.apache.lucene.search.regex.RegexQuery;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class AdvancedQueryAnalysis {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.analysis.AdvancedQueryAnalysis");

    public static Pattern yearPattern = Pattern.compile("\\d\\d\\d\\d");
    public static Pattern yearAndMonthPattern = Pattern.compile("\\d\\d\\d\\d\\/\\d\\d");

    private String originalQ;
    private String q;
    private Query advancedQuery = null;
    private boolean isAllNegative;
    private FilterResult filterResult;
    private DatasetConfiguration dc;

    public AdvancedQueryAnalysis(DatasetConfiguration dc, String q, FilterResult filterResult) {
        this.originalQ = q;
        this.q = q;
        this.dc = dc;
        this.filterResult = filterResult;
        this.isAllNegative = true;
        if (dc != null) {
            analyzeZipRangeQuery();
            analyzeRangeQuery();
            analyzeRegexQuery();
        }
    }
    /**
     * Created mostly for unit testing purpose
     * @param columns
     * @param q
     */
    public AdvancedQueryAnalysis(ArrayList<Column> columns, String q) {
        this.originalQ = q;
        this.q = q;
        analyzeZipRangeQuery();
        analyzeRangeQueryByColumns(columns);
        analyzeRegexQueryByColumns(columns);
    }

    //     original: ([\\[\\(])\\s*(\\S*?)\\s*,\\s*(\\d+)\\s*(km|mi|mile|miles|nm)?([\\]\\)])
    //     java see: ([ \[ \(]) \s*(\S*?)  \s*, \s*( \d+) \s*(km|mi|mile|miles|nm)?([ \] \)])
    //                [or(       asdfasdf  ,  100 mile        ]or )
    public static Pattern zipRangePattern = Pattern.compile("(\\+|-|\\s|^)zip:([\\[\\(])\\s*(\\S*?)\\s*,\\s*(\\d+) \\s*(km|mi|mile|miles|nm)?([\\]\\)])", Pattern.CASE_INSENSITIVE|Pattern.COMMENTS);

    /**
     * Distance Search support:
     *  zip:[94403, 5 mi)
     *  zip:[94403, 5 sm)   statute miles
     * +zip:[94403, 5 mile) statute miles
     * +zip:[94403, 80miles) statute miles
     * -zip:[94403, 15km)  kilometers
     *  zip:[94403, 5 nm)  nautical miles 
     */
    private void analyzeZipRangeQuery() {
        if (q.toLowerCase().indexOf("zip:") >= 0) {
            Matcher matcherRange = zipRangePattern.matcher(q);
            if (matcherRange.find()) {
                boolean mustNotOccur = "-".equals(matcherRange.group(1));
                if(isAllNegative) {isAllNegative = mustNotOccur;}
                String zipCode = U.isEmpty(matcherRange.group(3)) ? null : matcherRange.group(3);
                GeoPosition geo = ZipToGeoConvertor.lookup(zipCode);
                if(geo==null) return;
                float distanceValue = U.getFloat(matcherRange.group(4),-1);
                if(distanceValue<0) return;
                String distanceUnitValue = U.isEmpty(matcherRange.group(5)) ? null : matcherRange.group(5).intern();
                float distancePerLongitudeOrLatitude = 69;
                if(distanceUnitValue=="km") {
                    distancePerLongitudeOrLatitude = 111;
                }else if(distanceUnitValue=="nm") {
                    distancePerLongitudeOrLatitude = 60;
                }
                float delta = distanceValue / distancePerLongitudeOrLatitude;

                BooleanQuery query = new BooleanQuery();
                RangeQuery rq = new RangeQuery("_long",NumberUtils.float2sortableStr(geo.getLongitude()-delta),NumberUtils.float2sortableStr(geo.getLongitude()+delta),true,true);
                rq.setConstantScoreRewrite(true);
                query.add(rq, Occur.MUST);
                rq = new RangeQuery("_lat",NumberUtils.float2sortableStr(geo.getLatitude()-delta),NumberUtils.float2sortableStr(geo.getLatitude()+delta),true,true);
                rq.setConstantScoreRewrite(true);
                query.add(rq, Occur.MUST);
                
                //logger.debug(query);
                advancedQuery = appendQuery(advancedQuery, query, mustNotOccur);
                q = matcherRange.replaceAll("");
                //logger.debug("Remaining:"+q);
            }
        }
    }
    private void analyzeRangeQueryByColumns(ArrayList<Column> kColumns) {
        if(kColumns==null) return;
        for (Column c : kColumns) {
            if (q.toLowerCase().indexOf(c.getColumnName().toLowerCase() + ":") >= 0) {
                //     original: ([\\[\\(])\\s*(\\S*?)\\s*,\\s*(\\S*?)\\s*([\\]\\)])
                //     java see: ([ \[ \(]) \s*(\S*?)  \s*, \s*( \S*?) \s*([ \] \)])
                //                [or(       asdfasdf  ,  asdfasdf        ]or )
                Pattern patternRange = Pattern.compile("(\\+|-|\\s|^)" + c.getColumnName() + ":([\\[\\(])\\s*(\\S*?)\\s*,\\s*(\\S*?)\\s*([\\]\\)])", Pattern.CASE_INSENSITIVE|Pattern.COMMENTS);
                Matcher matcherRange = patternRange.matcher(q);
                if (matcherRange.find()) {
                    boolean mustNotOccur = "-".equals(matcherRange.group(1));
                    if(isAllNegative) {isAllNegative = mustNotOccur;}
                    String lowValue = U.isEmpty(matcherRange.group(3)) ? null : matcherRange.group(3);
                    String highValue = U.isEmpty(matcherRange.group(4)) ? null : matcherRange.group(4);
                    String sortableLowValue=null, sortableHighValue=null;
                    boolean includeLow = "[".equals(matcherRange.group(2));
                    boolean includeHigh = "]".equals(matcherRange.group(5));
                    RangeQuery rq = null;
                    if (c.getIsNumber()) {
                        if(c.getIsFilterable()&&dc!=null) {
                            this.filterResult.addFilteredColumn(new FilteredColumn(dc, c.getName(), new StringRangeFilterValue(lowValue,includeLow,highValue,includeHigh)));
                        }
                        if (lowValue != null) sortableLowValue = NumberUtils.double2sortableStr(lowValue);
                        if (highValue != null) sortableHighValue = NumberUtils.double2sortableStr(highValue);
                        rq= new RangeQuery("s" + c.getColumnName(), sortableLowValue, sortableHighValue, includeLow, includeHigh);
                    } else if (c.getIsDate()) {
                        if(c.getIsFilterable()&&dc!=null) {
                            this.filterResult.addFilteredColumn(new FilteredColumn(dc, c.getName(), new StringRangeFilterValue(lowValue,includeLow,highValue,includeHigh)));
                        }
                        if(IndexFieldType.KEYWORD_DATE_HIERARCHICAL == c.getIndexFieldType()) {
                            if(bothMatches(lowValue, highValue, yearPattern)) {
                                rq = new RangeQuery("y"+c.getColumnName(), lowValue, highValue, includeLow, includeHigh);
                            }else if(bothMatches(lowValue, highValue, yearAndMonthPattern)) {
                                rq = new RangeQuery("ym"+c.getColumnName(), lowValue, highValue, includeLow, includeHigh);
                            }else {
                                sortableLowValue = formatDateStringToIndexedFormat(lowValue);
                                sortableHighValue= formatDateStringToIndexedFormat(highValue);
                                rq = new RangeQuery(c.getColumnName(), sortableLowValue, sortableHighValue, includeLow, includeHigh);
                            }
                        }else {
                            sortableLowValue = formatDateStringToIndexedFormat(lowValue);
                            sortableHighValue= formatDateStringToIndexedFormat(highValue);
                            rq = new RangeQuery(c.getColumnName(), sortableLowValue, sortableHighValue, includeLow, includeHigh);
                        }
                    } else {
                        rq = new RangeQuery("s" + c.getColumnName(), lowValue, highValue, includeLow, includeHigh);
                    }
                    rq.setConstantScoreRewrite(true);
                    // logger.debug(csrq);
                    advancedQuery = appendQuery(advancedQuery, rq, mustNotOccur);
                    q = matcherRange.replaceAll("");
                    // logger.debug("Remaining:"+q);
                }
            }
        }
    }
    private boolean bothMatches(String a, String b, Pattern pattern) {
        if(a==null) {
            if(b==null) {
                return false;
            }else {
                return pattern.matcher(b).matches();
            }
        }else{
            if(b==null) {
                return pattern.matcher(a).matches();
            }else {
                return pattern.matcher(a).matches() && pattern.matcher(b).matches();
            }
        }
    }
    private void analyzeRangeQuery() {
    	ArrayList<Column> kColumns = dc.findColumnsByFieldType(IndexFieldType.KEYWORD);
    	analyzeRangeQueryByColumns(kColumns);
    }
    private void analyzeRegexQueryByColumns(ArrayList<Column> kColumns) {
        if(kColumns==null) return;
        for (Column c : kColumns) {
            if (q.toLowerCase().indexOf(c.getColumnName().toLowerCase() + ":") >= 0) {
                Pattern regexPattern = Pattern.compile("(\\+|-|\\s|^)" + c.getColumnName() + ":\\{(.*?)\\}(\\s|$)", Pattern.CASE_INSENSITIVE);
                boolean foundMatch = true;
                while(foundMatch){
                    Matcher rm = regexPattern.matcher(q);
                    if (rm.find()) {
                        boolean mustNotOccur = "-".equals(rm.group(1));
                        if(isAllNegative) {isAllNegative = mustNotOccur;}
                        String regex = rm.group(2);
                        RegexQuery regexQuery = new RegexQuery(new Term(c.getColumnName(), regex));
                        regexQuery.setRegexImplementation(new JakartaRegexpCapabilities());
                        // logger.debug(csrq);
                        advancedQuery = appendQuery(advancedQuery, regexQuery, mustNotOccur);
                        q = rm.replaceAll(" ");
                        // logger.debug("Remaining:"+q);
                    }else{
                    	foundMatch = false;
                    }
                }
            }
        }
    }
    
    private void analyzeRegexQuery() {
    	ArrayList<Column> kColumns = dc.getSearchableColumns();
    	analyzeRegexQueryByColumns(kColumns);
    }

    public String getRemainingQueryString() {
        return q;
    }

    public String getOriginalQueryString() {
        return originalQ;
    }

    public Query getAdvancedQuery() {
        return advancedQuery;
    }
    public boolean getIsAllNegative() {
        return isAllNegative;
    }

    public static Query andQuery(Query a, Query b) {
        if (b == null) return a;
        if (a == null) return b;
        BooleanQuery c = new BooleanQuery(true);
        c.add(a, Occur.MUST);
        c.add(b, Occur.MUST);
        return c;
    }
    public static Query appendQuery(Query a, Query b) {
        return appendQuery(a,b,false);
    }
    public static Query appendQuery(Query a, Query b, boolean mustNotOccur) {
        return appendQuery(a, b, (mustNotOccur? Occur.MUST_NOT : Occur.MUST ) );
    }
    /*
     * only a is boolean queury:
     * only b is boolean query: (+a) (occur)b
     * neither is boolean query: +a (occur)b 
     */
    public static Query appendQuery(Query a, Query b, Occur occur) {
        if (b == null) return a;
        if (a == null && occur != Occur.MUST_NOT) return b;
        BooleanQuery c;
        if(a!=null) {
            if (a instanceof BooleanQuery) {
                if(b instanceof BooleanQuery) {
                    if(isEmptyBooleanQuery((BooleanQuery)b)) {
                        c = (BooleanQuery)a;
                    }else {
                        if(isEmptyBooleanQuery((BooleanQuery)a)) {
                            c = new BooleanQuery(true);
                            c.add(b, occur);
                        }else {
                            if(isAllNegativeBooleanQuery((BooleanQuery)a)) {
                                c = new BooleanQuery(true);
                                ((BooleanQuery)a).add(new MatchAllDocsQuery(), Occur.MUST);
                                c.add(a, Occur.MUST);
                                c.add(b, occur);
                            }else {
                                c = new BooleanQuery(true);
                                c.add(a, Occur.MUST);
                                c.add(b, occur);
                            }
                        }
                    }
                } else if(b instanceof MatchAllDocsQuery){
                    c = (BooleanQuery)a;
                    c.add(b, occur);
                } else {
                    c = new BooleanQuery(true);
                    c.add(a, Occur.MUST);
                    c.add(b, occur);
                }
            } else {
                c = new BooleanQuery(true);
                c.add(a, Occur.MUST);
                if(b instanceof BooleanQuery) {
                    appendBooleanClauses(c,(BooleanQuery)b);
                } else {
                    c.add(b, occur);
                }
            }
        } else {
            c = new BooleanQuery(true);
            c.add(b, occur);
        }
        return c;
    }
    private static boolean isEmptyBooleanQuery(BooleanQuery y) {
        return y==null||y.clauses().size()<=0;
    }
    private static boolean isAllNegativeBooleanQuery(BooleanQuery y) {
        for(BooleanClause x: (List<BooleanClause>)y.clauses()) {
            if(Occur.MUST_NOT!=x.getOccur()) {
                return false;
            }
        }
        return true;
    }
    private static void appendBooleanClauses(BooleanQuery ret, BooleanQuery y) {
        if(!isEmptyBooleanQuery(y)) {
            for(BooleanClause x: (List<BooleanClause>)y.clauses()) {
                ret.add(x);
            }
        }
    }
    private static DateTimeFormatter rangeDateFormat = DateTimeFormat.forPattern("yyyy/MM/dd");
    private static String formatDateStringToIndexedFormat(String dString) {
        if(dString==null) return null;
        try {
            return VMTool.timeToStoredString(rangeDateFormat.parseMillis(dString));
        } catch (Exception ex) {
            logger.warn("Exception", ex);
        }
        return null;
    }
}

/*
test cases:

total 1354512
-fairchild 1354402
fairchild 110
fairchild year:[1985,1990) 4
fairchild -year:[1985,1990) 106
fairchild TRUDI year:[1985,1990) 0
fairchild  TRUDI 1

36567 year:[1985,1990)
1317945 -year:[1985,1990)

4 fairchild jelly 
1 fairchild jelly year:[1985,1990)
3 fairchild jelly -year:[1985,1990)
3 fairchild -jelly year:[1985,1990)
4 fairchild year:[1985,1990)
36563 -fairchild year:[1985,1990)
36567 year:[1985,1990)
106 fairchild -jelly

2 love created_at:[2007/11,2007/12)
463 -love created_at:[2007/11,2007/12)
83 love -created_at:[2007/11,2007/12)
85 love
*/
