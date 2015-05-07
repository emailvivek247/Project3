package net.javacoding.xsearch.config;

/**
 * This interface defines constants used in the package.
 *
 */
public interface ConfigConstants {
    /** The XML declaration. */
    public final static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public final static int ERROR_INDEXNAME_REQUIRED = 1;
    public final static int ERROR_INDEXNAME_PATTERN = 2;
    public final static int ERROR_INDEXNAME_LENGTH = 3;
    public final static int ERROR_INDEXNAME_DUPLICATE = 4;
    
    public final static int FREE_SIZE_LIMIT = 100000;
}
