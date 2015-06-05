package net.javacoding.xsearch.config;

public class IndexFieldType {
    
    public static final String KEYWORD = "Keyword";
    public static final String KEYWORD_BOOST = "KeywordBoost";
    public static final String KEYWORDS = "Keywords";
    public static final String KEYWORD_CASE_INSENSITIVE = "KeywordCaseInsensitive";
    public static final String KEYWORD_DATE_HIERARCHICAL = "KeywordDateHierarchical";
    public static final String TEXT = "Text";
    public static final String TEXT_HTML = "TextHtml";
    public static final String TEXT_COMPRESSED = "TextCompressed";
    public static final String UN_STORED = "UnStored";
    public static final String UN_INDEXED = "UnIndexed";
    public static final String UN_INDEXED_COMPRESSED = "UnIndexedCompressed";
    public static final String BOOST = "Boost";
    public static final String ZIP_CODE = "ZipCode";
    
    public static boolean isKeyword(String indexFieldType) {
        return indexFieldType!=null && indexFieldType.startsWith(KEYWORD);
    }
    public static boolean belongsTo(String type, String family) {
        return type!=null && type.startsWith(family);
    }

}
