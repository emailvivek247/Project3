package net.javacoding.xsearch.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.api.protocol.SearchProtocol;

/**
 * This class uses Google's Protocol Buffer to interact with SDL efficiently.
 * 
 * Passing results in XML or JSON could actually be too verbose. The interaction with SDL 
 * is defined in Google's Protocol Buffer, as below. So you could use other languages 
 * like python, C++, etc to query SLD via this protocol.
 * 
 * Example usage:
 * <pre>
 * import net.javacoding.xsearch.api.Document;
 * import net.javacoding.xsearch.api.FacetChoice;
 * import net.javacoding.xsearch.api.FacetCount;
 * import net.javacoding.xsearch.api.Field;
 * import net.javacoding.xsearch.api.Result;
 * import net.javacoding.xsearch.api.SearchConnection;
 * import net.javacoding.xsearch.api.SearchQuery;
 *  ...
 *        SearchConnection s = new SearchConnection("http://localhost:8080/sdl/").setIndex("my_index");
 *        SearchQuery q = new SearchQuery("love").setDebug(true)
 *                             .highlight("title").summarize("description")  //by default, no highlight or summarize on any column
 *                             .setHighlightTag("&lt;span style=\"color:#666\">", "&lt;/span>")  //default is &lt;b> ;lt;/b>
 *                             .setLength(10)  //default is 30
 *                             .setFacetCountLimit(20);  //default is no limit
 *        long startTime = System.currentTimeMillis();
 *        Result sr = null;
 *        try {
 *            sr = s.search(q);
 *        } catch (IOException e) {
 *            // TODO Auto-generated catch block
 *            e.printStackTrace();
 *        }
 *        System.out.println("total:"+sr.getTotal());
 *        System.out.println("doc count:"+sr.getDocList().size());
 *        System.out.println("Search time:"+sr.getSearchTime());
 *        for(Document d : sr.getDocList()) {
 *            System.out.println("---------------------------");
 *            for(Field f : d.getFieldList()) {
 *                System.out.println(f.getName()+"("+f.getType()+")"+f.getValue());
 *            }
 *        }
 *        for(FacetChoice fChoice : sr.getFacetChoiceList()) {
 *            System.out.println("Narrow By " + fChoice.getColumn());
 *            for(FacetCount fc : fChoice.getFacetCountList()) {
 *                System.out.println("  " + fc.getValue() + (fc.getEndValue().length()==0? "" : ","+fc.getEndValue()) + " ~ " + fc.getCount());
 *            }
 *        }
 * </pre>
 */
public class SearchConnection {
    String baseUrl;
    List<String> indexNames = new ArrayList<String>();
    int length = 0;

    public SearchConnection() {
    }
    
    /**
     * This is the base url, which should look like
     *  http://you_server:8080/sdl/
     * @param url
     */
    public SearchConnection(String url) {
        this.baseUrl = url;
        if(this.baseUrl!=null && !this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
    }
    
    /**
     * One or several index names
     * @param indexNames
     */
    public SearchConnection setIndex(String... indexNames) {
        this.indexNames.clear();
        if(indexNames!=null) {
            for(String n : indexNames) {
                if(n!=null) {
                    this.indexNames.add(n);
                }
            }
        }
        return this;
    }
    
    public void setURL(String URL) {
        this.baseUrl = URL;
        if(this.baseUrl!=null && !this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
    }   
    
    public void setLength(int length) {
    	this.length = length;
    }      
    
    public SearchConnection setIndex(String indexName) {
        this.indexNames.clear();
        if(indexName != null) {
           this.indexNames.add(indexName);
        }
        return this;
    }    
    
    public Result search(SearchQuery query) throws IOException {
        URL url = new URL(this.baseUrl+"searchProtocolBuffer.do");
        java.net.URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();

        //build the search request
        SearchProtocol.SearchRequest.Builder request = SearchProtocol.SearchRequest.newBuilder();
        if(indexNames!=null) {
            for(String n : indexNames) {
                request.addIndex(n);
            }
        }
        request.setQuery(query.q);
        request.setLuceneQuery(query.lq);
        request.setStart(query.start);
        if (this.length != 0) {
        	request.setResultPerPage(this.length);	
        } else {
        	request.setResultPerPage(query.length);
        }
        
        for(Sort s : query.sortBys) {
            SearchProtocol.Sort.Builder sb = SearchProtocol.Sort.newBuilder();
            sb.setColumn(s.columnName).setDescending(s.desc);
            request.addSort(sb);
        }
        
        request.setDebug(query.debug);

        for(StringColumnFormat scf : query.stringColumnFormats) {
            request.addStringColumnFormat(scf.toBuilder());
        }
        request.setBeginHighlightTag(query.beginHighlightTag);
        request.setEndHighlightTag(query.endHighlightTag);
        
        request.setBooleanOperator(query.booleanOperator);
        request.setFacetCountLimit(query.facetCountLimit);
        
        request.setSourceLocation(query.sourceLocation);
        request.setSearchable(query.searchableColumns);
        
        request.setEnalbeFacetSearch(query.enableFacetSearch);

        //write it
        request.build().writeTo(out);
        out.close();

        //reading the response
        SearchProtocol.SearchResponse response = SearchProtocol.SearchResponse.parseFrom(connection.getInputStream());
        return new Result(response);
    }
    
}
