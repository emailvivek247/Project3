package net.javacoding.xsearch.core.task.work.list;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.work.util.TaskUtil;
import net.javacoding.xsearch.sort.DiskMergeSortedArray;
import net.javacoding.xsearch.utility.DBTool;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class FetchFullDocumentListBySQLTask extends BaseFetchPrimaryKeysBySingleQueryTask {

    protected DiskMergeSortedArray fullPrimaryKeyList = null;
    public FetchFullDocumentListBySQLTask(IndexerContext ic) {
        super(ic);
    }
    
    public void prepare() {
        super.prepare();
        if(indexReader!=null) {
            // 1M objects
        	File dir = ic.getDatasetConfiguration().getFullListIndexDirectoryFile();
        	dir.mkdirs();
            fullPrimaryKeyList = new DiskMergeSortedArray(1024*1024,dir, "new");
        }
    }
    /*
     * Collect toBeDeleted list and use super.tearDown() to do the real delete work
     * @see net.javacoding.xsearch.core.task.work.BaseFetchPrimaryKeysBySingleQueryTask#tearDown()
     */
    public void stop() {
        try {
            if(indexReader!=null&&this.isListComplete) {
                logger.info("Getting what's current in the list ... ");
            	DiskMergeSortedArray existingPrimaryKeyList = getExistingKeys(indexReader, primaryKeyColumn.getColumnName());
                logger.info("Comparing to find what's deleted ... ");
                ArrayList<String> deleted = TaskUtil.minor(existingPrimaryKeyList, fullPrimaryKeyList);
                logger.info("Deleted size="+deleted.size());
                this.toBeDeleted = new ArrayList<Integer>(deleted.size());
                for(int i=0;i<deleted.size();i++) {
                    Hits hits = null;
                    try {
                        Term pkTerm = new Term(primaryKeyColumn.getColumnName(), (String) deleted.get(i));
                        Query pkQuery = new TermQuery(pkTerm);
                        hits = searcher.search(pkQuery);
                        for (int j = 0; hits!=null && j < hits.length(); j++) {
                            toBeDeleted.add(hits.id(j));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                existingPrimaryKeyList.close();
                fullPrimaryKeyList.close();
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally {
            //delete and commit is done in overwrited tearDown()
            super.stop();
        }
    }

    protected int processOneResult(ResultSet rs) throws java.sql.SQLException {
        String pkValue = DBTool.getString(rs, 1, primaryKeyColumn.getColumnType());
        fullPrimaryKeyList.add(pkValue);
        return 1;
    }
    
    private DiskMergeSortedArray getExistingKeys(IndexReader reader, String field) throws IOException{
    	DiskMergeSortedArray existing = new DiskMergeSortedArray(1024*1024,ic.getDatasetConfiguration().getFullListIndexDirectoryFile(), "old");
    	
        field = field.intern();

        TermDocs termDocs = reader.termDocs();
        TermEnum termEnum = reader.terms (new Term (field, ""));
        try {
			do {
				Term term = termEnum.term();
				if (term == null || term.field() != field)
					break;
				String termval = term.text();
				termDocs.seek(termEnum);
				while (termDocs.next()) {
				    if(!reader.isDeleted(termDocs.doc())) {
	                    existing.add(termval);
				    }
				}
			} while (termEnum.next());
		} finally {
			termDocs.close();
			termEnum.close();
		}
    	
    	return existing;
    }

}
