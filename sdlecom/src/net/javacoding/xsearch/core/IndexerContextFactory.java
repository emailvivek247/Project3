package net.javacoding.xsearch.core;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.impl.IndexerContextImpl;

public class IndexerContextFactory {

    public static IndexerContext createContext(DatasetConfiguration dc) throws Exception {
        IndexerContext context = new IndexerContextImpl(dc);
        return context;
    }
}
