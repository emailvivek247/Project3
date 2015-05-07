package net.javacoding.xsearch.search.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.work.util.DocumentSQLTaskHelper;
import com.fdt.sdl.core.ui.action.indexing.memory.SubmitAction;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.search.searcher.SearcherProvider;
import net.javacoding.xsearch.utility.DBTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class BufferIndexManager {
    public static class SubmissionBuffer{
        boolean isPausing;
        List<TextDocument> submissions;
        public boolean isPausing() {
            return isPausing;
        }
        public void setPausing(boolean enabled) {
            this.isPausing = enabled;
        }
        public List<TextDocument> getSubmissions() {
            return submissions;
        }
        public void setSubmissions(List<TextDocument> submissions) {
            this.submissions = submissions;
        }
        public SubmissionBuffer() {
            this.isPausing = false;
            this.submissions = new ArrayList<TextDocument>(4);
        }
        public void add(TextDocument td) {
            this.submissions.add(td);
        }
    }
    private static Logger     logger       = LoggerFactory.getLogger("net.javacoding.xsearch.search.memory.BufferIndexManager");
    private static Map<String,BufferIndex> indexMap = new ConcurrentHashMap<String, BufferIndex>();
    private static Map<String,SubmissionBuffer> submissionBufferMap = new ConcurrentHashMap<String, SubmissionBuffer>();
    public static synchronized BufferIndex getIndex(String name, boolean create) {
        if(indexMap.get(name)==null) {
            if(create) {
                indexMap.put(name, new BufferIndex());
            }else {
                return null;
            }
        }
        return indexMap.get(name);
    }
    public static BufferIndex clearIndex(DatasetConfiguration dc) {
        return clearIndex(dc.getName());
    }
    public static synchronized BufferIndex clearIndex(String name) {
        if(indexMap.get(name)!=null) {
            return indexMap.put(name, new BufferIndex());
        }
        return null;
    }
    
    public static synchronized void submit(DatasetConfiguration dc, TextDocument td) {
        SubmissionBuffer submissionBuffer;
        if((submissionBuffer=submissionBufferMap.get(dc.getName()))==null) {
            submissionBuffer = new SubmissionBuffer();
            submissionBufferMap.put(dc.getName(), submissionBuffer);
        }
        submissionBuffer.add(td);
        maybeProcessSubmissions(dc, submissionBuffer);
    }
    public static void pause(DatasetConfiguration dc) throws Exception {
        SubmissionBuffer submissionBuffer;
        if((submissionBuffer=submissionBufferMap.get(dc.getName()))==null) {
            submissionBuffer = new SubmissionBuffer();
            submissionBuffer.setPausing(true);
            submissionBufferMap.put(dc.getName(), submissionBuffer);
        }
    }
    public static void unPause(DatasetConfiguration dc) throws Exception {
        SubmissionBuffer submissionBuffer;
        if((submissionBuffer=submissionBufferMap.get(dc.getName()))==null) {
            submissionBuffer = new SubmissionBuffer();
            submissionBufferMap.put(dc.getName(), submissionBuffer);
        }
        submissionBuffer.setPausing(false);
        maybeProcessSubmissions(dc, submissionBuffer);
    }
    private static void maybeProcessSubmissions(DatasetConfiguration dc, SubmissionBuffer submissionBuffer) {
        SearcherProvider sp;
        IndexReaderSearcher irs = null;
        if(!submissionBuffer.isPausing() && (sp=SearcherManager.getSearcherProvider(dc.getName()))!=null && !sp.isInterim()) {
            TextDocument td = null;
            try {
                irs = sp.getIndexReaderSearcher();

                Document d = null;
                Column pkColumn = dc.getPrimaryKeyColumn();
                td = submissionBuffer.getSubmissions().remove(0);
                String pkValue = td.get(pkColumn.getName());
                Query query = new TermQuery(new Term(pkColumn.getName(),pkValue));

                if (irs != null) {
                    d = SubmitAction.deleteExisting(irs.getIndexReader(), query, true);
                }

                BufferIndex bi = BufferIndexManager.getIndex(dc.getName(), true);
                if(d==null) {
                    //maybe it's already updated, check the buffer index also
                    d = SubmitAction.deleteExisting(bi.getReader(), query, true);
                }
                
                if(d==null) {
                    d = new Document();
                }

                //move from text document to lucene document
                for (Column col : dc.getColumns()) {
                    String[] values = td.getValues(col.getColumnName());
                    if(values!=null) {
                        DocumentSQLTaskHelper.removeFromDocument(d,col);
                        for(String v : values) {
                            DocumentSQLTaskHelper.addToDocument(d, DBTool.getStringInDocument(v,col), col);
                        }
                    }
                }

                bi.addDocuemnt(dc, d);

            } catch(Exception sre) {
                if(td!=null) {
                    //add back if processing has error
                    submissionBuffer.add(td);
                }
            } finally {
                if (irs != null) {
                    try {
                        irs.release();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
