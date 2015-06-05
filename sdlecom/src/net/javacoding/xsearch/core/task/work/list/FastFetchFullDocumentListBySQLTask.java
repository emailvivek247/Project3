package net.javacoding.xsearch.core.task.work.list;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.BloomFilter;
import net.javacoding.xsearch.utility.DBTool;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

public class FastFetchFullDocumentListBySQLTask extends BaseFetchPrimaryKeysBySingleQueryTask {

    protected BloomFilter fullPrimaryKeyList = null;
    public FastFetchFullDocumentListBySQLTask(IndexerContext ic) {
        super(ic);
    }
    
    public void prepare() {
        super.prepare();
        if(indexReader!=null) {
            fullPrimaryKeyList = new BloomFilter(indexReader.maxDoc()*32+32);
        }
    }
    /*
     * Collect toBeDeleted list and use super.tearDown() to do the real delete work
     * @see net.javacoding.xsearch.core.task.work.BaseFetchPrimaryKeysBySingleQueryTask#tearDown()
     */
    public void stop() {
        try {
            if(indexReader!=null&&this.isListComplete) {
                logger.info("Comparing to find what's deleted ... ");
                this.toBeDeleted = new ArrayList<Integer>();
                checkToBeDeletedAmongExistingKeys(indexReader,primaryKeyColumn.getColumnName());
                logger.info("Deleted size="+this.toBeDeleted.size());
                fullPrimaryKeyList = null;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
		}finally {
            //delete and commit is done in overwrited tearDown()
            super.stop();
        }
    }

    protected int processOneResult(ResultSet rs) throws java.sql.SQLException {
        String pkValue = DBTool.getString(rs, 1, primaryKeyColumn.getColumnType());
        fullPrimaryKeyList.put(pkValue);
        return 1;
    }
    
    private void checkToBeDeletedAmongExistingKeys(IndexReader reader, String field) throws IOException{
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
                if(!fullPrimaryKeyList.hasKey(termval)) {
                    while (termDocs.next()) {
                        if(!reader.isDeleted(termDocs.doc())) {
                            this.toBeDeleted.add(termDocs.doc());
                        }
                    }
                }
			} while (termEnum.next());
		} finally {
			termDocs.close();
			termEnum.close();
		}
    }

}
