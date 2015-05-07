package net.javacoding.xsearch.api;

import net.javacoding.xsearch.api.protocol.SearchProtocol;

public class StringColumnFormat {
    public static final int DIRECT = 0;
    public static final int HTML = 1;
    public static final int HIGHLIGHTED = 2;
    public static final int HIGHLIGHTED_HTML = 3;
    public static final int SUMMARIZED = 4;
    public static final int SUMMARIZED_HTML = 5;

    public String column;
    public int stringFormat;
    
    public StringColumnFormat(String column, int stringFormat) {
        this.column = column;
        this.stringFormat = stringFormat;
    }
    public SearchProtocol.StringColumnFormat.Builder toBuilder(){
        SearchProtocol.StringColumnFormat.Builder b = SearchProtocol.StringColumnFormat.newBuilder();
        b.setColumn(column);
        switch(stringFormat) {
        case DIRECT : b.setStringFormat(SearchProtocol.StringColumnFormat.StringFormat.DIRECT);break;
        case HTML : b.setStringFormat(SearchProtocol.StringColumnFormat.StringFormat.HTML);break;
        case HIGHLIGHTED : b.setStringFormat(SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED);break;
        case HIGHLIGHTED_HTML : b.setStringFormat(SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED_HTML);break;
        case SUMMARIZED : b.setStringFormat(SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED);break;
        case SUMMARIZED_HTML : b.setStringFormat(SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED_HTML);break;
        }
        return b;
    }
}
