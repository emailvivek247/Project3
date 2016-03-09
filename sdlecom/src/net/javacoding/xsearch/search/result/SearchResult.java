package net.javacoding.xsearch.search.result;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.api.Document;
import net.javacoding.xsearch.api.SDLIndexDocument;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.config.XMLSerializable;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.search.Highlighter;
import net.javacoding.xsearch.search.HitDocument;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.result.filter.FilteredColumn;
import net.javacoding.xsearch.utility.EscapeChars;
import net.javacoding.xsearch.utility.HttpUtil;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import org.apache.lucene.search.Query;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fdt.sdl.admin.ui.action.constants.IndexType;
import com.fdt.sdl.core.ui.action.search.MultiSearchAction.MultiSearchContext;
import com.fdt.sdl.core.ui.action.search.SearchAction.SearchContext;
import com.fdt.sdl.styledesigner.Template;
import com.fdt.sdl.styledesigner.util.DeviceDetectorUtil;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class holds all information passed from Controller to View in Struts MVC framework.
 * 
 * So you can use this class to render search result page in any way you want.
 * This was originally for users preferring their own JSP renderer.
 * But you can use it for Velocity, Freemarker, etc
 * 
 * To retrieve the SearchResult value,
 * <ol>
 *   <li> In JSP: &lt;%=request.getAttribute("searchResult")%&gt;
 *   <li> In Freemarker: ${searchResult}
 *   <li> In Velocity: ${searchResult}
 * </ol>
 * 
 * Two most important objects are:
 *   <ol>
 *    <li> a list of HitDocument holding the matched results
 *    <li> a FilterResult object, holding narrowBy results.
 *   </ol>  
 */
@XStreamAlias("SearchResult")
public class SearchResult extends XMLSerializable{

    private static final long serialVersionUID = -5095050639345513273L;

    transient DatasetConfiguration datasetConfiguration;
    transient Template template;
    @XStreamAlias("indexName")
    public String indexName;
    @XStreamAlias("templateName")
    public String templateName;
    private transient HttpServletRequest request;
    private transient HttpServletResponse response;
    @XStreamAlias("q")
    public String q;
    @XStreamAlias("lq")
    public String lq;
    @XStreamAlias("sortBys")
    public List<SearchSort> sortBys;
    @XStreamAlias("docs")
    public List<? extends SDLIndexDocument> docs;

    @XStreamAlias("searchTime")
    public long searchTime;
    @XStreamAlias("searchTimeString")
    public String searchTimeString;

    @XStreamAlias("total")
    public int total;
    @XStreamAlias("start")
    public int start;
    @XStreamAlias("length")
    public int length;

    transient Highlighter summarizer;
    transient String beginTag = "<B>";
    transient String endTag = "</B>";

    @XStreamAlias("filterResult")
    public FilterResult filterResult;

    @XStreamAlias("defaultDocs")
    public List<HitDocument> defaultDocs;

    public String userInput;

    /**
     * Get current DatasetConfiguration instance
     */
    public DatasetConfiguration getDatasetConfiguration() {
        return datasetConfiguration;
    }

    public Template getTemplate() {
        return this.template;
    }

    /**
     * Get all DatasetConfigurations
     */
    public ArrayList<DatasetConfiguration> getDatasetConfigurations() {
        try {
            return ServerConfiguration.getDatasetConfigurations(false);
        } catch (ConfigurationException e) {
            // Ignore
        }
        return null;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTemplateName() {
        return templateName;
    }

	/**
	 * Exact query that the user typed in
	 */
	public String getUserQueryString() { return q;}
	/**
	 * URLEncoder encoded q string in "UTF-8"
	 * Convenience method, You can directly append it to any URL
	 */
	public String getURLEncodedQuery() throws UnsupportedEncodingException{
		String searchString = java.net.URLEncoder.encode(q, "utf-8"); 
		if (lq != null) {
			searchString = searchString + "&lq=" + java.net.URLEncoder.encode(lq, "utf-8");
		}
		return  searchString;
	}
	
	/**
	 * HTML escaped q string
	 * Convenience method, You can print it out to HTML
	 */
	public String getHTMLEncodedQuery() { return HTMLEntities.encode(q);}
    /**
     * URLEncoder encoded q string in "UTF-8", stripping all field-specific part, like field:abc
     * Convenience method, You can directly append it to any URL
     */
    public String getURLEncodedClearQuery() { return FilteredColumn.clearFields(q, filterResult.filteredColumns);}
	
	/**
	 * Found documents
	 */
	public List<? extends SDLIndexDocument> getDocs(){return docs;}
	
    /**
     * If no matches found for query q, this will be the matches for defaultQuery
     */
    public List<HitDocument> getDefaultDocs(){return defaultDocs;}
    public SearchResult setDefaultDocs(List<HitDocument> documents){defaultDocs = documents; return this;}

    /**
	 * Search Time in milliseconds
	 */
	public long getSearchTime(){return searchTime;}
	/**
	 * Search Time in seconds, formatted string
	 */
	public String getSearchTimeString(){return VMTool.timeFormat.get().format(searchTime * .001);}
	
	public Integer getTotal(){ return total;}
	public Integer getStart(){ return start;}
	public Integer getLength(){ return length;}
	
    /**
     * Used for summarize, or highlight a piece of text
     */
    public Highlighter getSummarizer() {
        return summarizer;
    }

    /**
     * Convenience function to get the request's query string
     */
    public String getQueryString() {
        return request.getQueryString();
    }

    /**
     * Convenience function to remove "start=xxx" from the request's query
     * string And also removing "fileName=xxx".
     */
    public String getQueryStringWithoutStart() {
        return HttpUtil.addOrSetQuery(HttpUtil.addOrSetQuery(request.getQueryString(), "start", ""), "fileName", "");
    }

	public class RequestQuery{
	    String requestQueryString;
	    public RequestQuery(String requestQueryString) {
	        this.requestQueryString = requestQueryString;
	    }
	    public RequestQuery set(String name, String value) {
	        this.requestQueryString = HttpUtil.addOrSetQuery(this.requestQueryString,name,value);
	        return this;
	    }
        public RequestQuery remove(String name) {
            this.requestQueryString = HttpUtil.addOrSetQuery(this.requestQueryString,name,"");
            return this;
        }
        public String toString() {
            return this.requestQueryString;
        }
	}
	/**
	 * Return a RequestQuery object to conveniently set/remove parameters
	 * @return
	 */
	public RequestQuery getRequestQuery() {
	    return new RequestQuery(request.getQueryString());
	}
	
	public HttpServletRequest getRequest() { return request; }
    public HttpServletResponse getResponse() { return response; }
    /**
	 * return the encoding set in web.xml, by ApplicationInitServlet
	 */
	public String getEncoding(){ return WebserverStatic.getEncoding();}

	/**
	 * This contains all narrowBy results for filteralbe columns
	 */
	public FilterResult getFilterResult(){ return filterResult;}
	
	public List<SearchSort> getSearchSorts(){ return sortBys;}

	public String getBeginTag() {
	    return beginTag;
	}

	public String getEndTag() {
	    return endTag;
	}

	/**
	 * This is used to hold results from search.do
	 */
	public SearchResult(SearchContext sc, 
			String q,
			String lq,
			org.apache.lucene.search.Query query, 
			List<HitDocument> docs, 
            List<HitDocument> defaultDocs, 
			long searchTime, 
			int total, 
			int start, 
			int length,
			List<SearchSort> sortBys,
			FilterResult filterResult,
			HttpServletRequest request,
			HttpServletResponse response
			) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
	    init(sc,q,lq,query,docs,defaultDocs,searchTime,total,start,length,sortBys,filterResult,request,response);
	}
	
    /**
     * This is used to hold results from multiSearch.do
     */
    public SearchResult(MultiSearchContext msc, int i, 
            String q,
            String lq,
            org.apache.lucene.search.Query query, 
            List<HitDocument> docs, 
            long searchTime, 
            int total, 
            int start, 
            int length,
            List<SearchSort> sortBys,
            FilterResult filterResult,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
        this.datasetConfiguration = msc.dcs.get(i);
        this.q = q;
        this.lq = lq;
        this.indexName = msc.dcs.get(i).getName();
        this.templateName = DeviceDetectorUtil.identifyDevice(msc.dcs.get(i), request);
        this.template = null;
        this.docs = docs;
        this.searchTime = searchTime;
        this.total = total;
        this.start = start;
        this.length = length;
        if (query != null) {
            this.summarizer = new Highlighter(msc.dcs.get(i).getAnalyzer(),
                    query.rewrite(msc.irss.get(i).getIndexReader()), q);
        }
        this.sortBys = sortBys;
        this.filterResult = filterResult;
        this.request = request;
        this.response = response;
    }

    public void init(SearchContext sc, 
            String q, 
            String lq,
            Query query, 
            List<HitDocument> docs, 
            List<HitDocument> defaultDocs, 
            long searchTime, 
            int total, 
            int start, 
            int length,
            List<SearchSort> sortBys,
            FilterResult filterResult,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
        this.datasetConfiguration = sc.dc;
        this.q = q;
        this.lq = lq;
        this.indexName = sc.dc.getName();
        this.templateName = sc.templateName;
        this.template = sc.template;
        this.docs = docs;
        this.defaultDocs = defaultDocs;
        this.searchTime = searchTime;
        this.total = total;
        this.start = start;
        this.length = length;
        if (query != null) {
            this.summarizer = new Highlighter(sc.dc.getAnalyzer(), query.rewrite(sc.irs.getIndexReader()), q);
        }
        this.sortBys = sortBys;
        this.filterResult = filterResult;
        this.request = request;
        this.response = response;
    }
    
    public void initFor3Tier(SearchContext sc, 
            String q, 
            String lq,
            Query query, 
            List<Document> docs, 
            List<HitDocument> defaultDocs, 
            long searchTime, 
            int total, 
            int start, 
            int length,
            List<SearchSort> sortBys,
            FilterResult filterResult,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        this.datasetConfiguration = sc.dc;
        this.q = q;
        this.lq = lq;
        this.indexName = sc.dc.getName();
        this.templateName = sc.templateName;
        this.template = sc.template;
        this.docs = docs;
        this.defaultDocs = defaultDocs;
        this.searchTime = searchTime;
        this.total = total;
        this.start = start;
        this.length = length;
        if (query != null) {
            this.summarizer = new Highlighter(sc.dc.getAnalyzer(), query.rewrite(sc.irs.getIndexReader()), q);
        }
        this.sortBys = sortBys;
        this.filterResult = filterResult;
        this.request = request;
        this.response = response;
    }

    public SearchResult() {
        super();
    }

    /**
     * set these attributes for backward compatibility
     * @deprecated
     */
    public void setAttributes() throws IOException {
        request.setAttribute("dc", datasetConfiguration);
        request.setAttribute("template", template);
        request.setAttribute("indexName", indexName);
        request.setAttribute("templateName", templateName);
        request.setAttribute("q", q);
        request.setAttribute("lq", lq);
        request.setAttribute("docs", docs);
        request.setAttribute("searchTime", getSearchTimeString());
        request.setAttribute("total", new Integer(total));
        request.setAttribute("start", new Integer(start));
        request.setAttribute("length", new Integer(length));
        request.setAttribute("summarizer", summarizer); // show summary
        request.setAttribute("URLEncodedQuery", getURLEncodedQuery()); // encode query in url format
        request.setAttribute("HTMLEncodedQuery", getHTMLEncodedQuery()); // encode query in html format
        request.setAttribute("request", request);
        request.setAttribute("response", response);
        request.setAttribute("queryString", getQueryString());
        request.setAttribute("noStartQueryString", getQueryStringWithoutStart());
        request.setAttribute("encoding", getEncoding());
        request.setAttribute("dcs", getDatasetConfigurations());

        // set sortBy to the first sortBy column
        if (sortBys != null && sortBys.size() > 0) {
            request.setAttribute("sortBy", sortBys.get(0).field);
        } else {
            request.removeAttribute("sortBy");
        }
        if (!U.getBoolean(request.getParameter("desc"), "Y", true)) {
            request.setAttribute("desc", "N");
        }
    }

    public JSONObject toJSONObject() {
        JSONObject self = new JSONObject();
        try {
            self.put("indexName", indexName);
            self.put("q", q);
            self.put("lq", lq);
            self.put("searchTime", searchTime);
            self.put("searchTimeString", getSearchTimeString());
            self.put("total", total);
            self.put("start", start);
            self.put("length", length);
            self.put("sortBys", SearchSort.toJSONArray(sortBys));
            this.summarizer.setHighlightPrefix(beginTag);
            this.summarizer.setHighlightSuffix(endTag);
            self.put("docs", PageStyleUtil.toJSONArray(docs));
            self.put("filterResult", filterResult.toJSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return self;
    }

    public void setHighlightTag(String beginTag, String endTag) {
        this.beginTag = beginTag;
        this.endTag = endTag;
    }

    /**
     * Not ready yet. Too verbose. Directly using the template is recommended
     * for more flexible control of the format.
     */
    public String toJson() {
        return toJSONObject().toString();
    }

    /**
     * Usage: searchResult.hightlight(doc, "id") <br/>
     * Convenient methods to replace
     * searchResult.summarizer.getHighlighted(doc.get("id")?html,"id")
     */
    public String highlight(HitDocument doc, String field) {
        String t = EscapeChars.forHTMLTag(doc.get(field));
        if (summarizer == null) {
            return t;
        } else {
            return summarizer.getHighlighted(t, field);
        }
    }

    public String highlight(Document doc, String field) {
        String result = null;
        IndexType indexType = datasetConfiguration.getIndexType();
        if (indexType == null || indexType == IndexType.LUCENE) {
            String t = EscapeChars.forHTMLTag(doc.getString(field));
            if (summarizer == null) {
                result = t;
            } else {
                result = summarizer.getHighlighted(t, field);
            }
        } else if (indexType == IndexType.ELASTICSEARCH) {
            String value = doc.getString(field, true);
            if (value.contains("hl-tag-replace")) {
                value = value.replaceAll("<hl-tag-replace>", beginTag);
                value = value.replaceAll("</hl-tag-replace>", endTag);
            } else {
                value = EscapeChars.forHTMLTag(value);
            }
            result = value;
        }
        return result;
    }

    public String highlight(String value, String field) {
        String t = EscapeChars.forHTMLTag(value);
        if (summarizer == null) {
            return t;
        } else {
            return summarizer.getHighlighted(t, field);
        }
    }

    /**
     * Usage: searchResult.directSummarize(doc, "id")
     * <br/>
     * Convenient methods to replace
     * searchResult.summarizer.getSummary(doc.get("id"),"id")
     */
    public String directSummarize(HitDocument doc, String field) {
        if (summarizer == null) {
            return doc.get(field);
        } else {
            return summarizer.getSummary(doc.get(field), field);
        }
    }

    public String directSummarize(Document doc, String field) {
        if (summarizer == null) {
            return doc.get(field);
        } else {
            return summarizer.getSummary(doc.get(field), field);
        }
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

}