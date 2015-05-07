package net.javacoding.xsearch.core.task.work.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import net.javacoding.xsearch.sort.DiskMergeSortedArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Searcher;

public class TaskUtil {
    protected static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.core.task.work.TaskUtil");

	//sql related
	public static void close(Statement call){
		try {
			if (call != null) {
				call.close();
			}
		} catch (Exception ex) {
		}
	}

	public static void close(ResultSet rs){
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (Exception ex) {
		}
	}
	public static void close(Connection conn){
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception ex) {
		}
	}
	public static void close(ResultSet rs, Statement call, Connection conn){
		close(rs);
		close(call);
		close(conn);
	}
	
	//index related
	public static void close(Searcher searcher){
        if (searcher != null) {
            try {
                searcher.close();
            } catch (Exception e) {}
        }
	}
	public static void close(IndexReader indexReader){
        if (indexReader != null) {
            try {
                indexReader.close();
            } catch (Exception e) {}
        }
	}

    public static String[] minor(String[] s1, String[]s2) {
        if(s1==null||s2==null)return null;
        for(int i=0;i<s1.length;i++) {if(s1[i]==null) s1[i]="";}
        for(int i=0;i<s2.length;i++) {if(s2[i]==null) s2[i]="";}
        Arrays.sort(s1);
        Arrays.sort(s2);
        logger.debug("old index size="+s1.length);
        logger.debug("new size="+s2.length);
        int deletedSize = 0;
        for(int i=0,j=0;i<s1.length&&j<s2.length;) {
            if(s1[i]==null) {i++;continue;}
            if(s2[j]==null) {j++;continue;}
            int ret = s1[i].compareTo(s2[j]);
            if(ret==0) {
                s1[i++]=null;
                j++;
            } else if(ret<0){
                i++;
                deletedSize++;
            } else {
                j++;
            }
        }
        String[] deletedKeys = new String[deletedSize];
        for(int i=0,j=0;i<s1.length&&j<deletedKeys.length;i++) {
            if(s1[i]!=null) {
                deletedKeys[j++]=s1[i];
            }
        }
        logger.debug("deleted size="+deletedKeys.length);
        return deletedKeys;
    }
    public static String[] minor(IndexReader ir1, String f1, IndexReader ir2, String f2) {
        if(ir1==null || ir2 ==null || f1==null || f2 == null) {
            return null;
        }
        try {
            String[] existingKeys = FieldCache.DEFAULT.getStrings(ir1, f1);
            String[] newKeys = FieldCache.DEFAULT.getStrings(ir2, f2);
            return minor(existingKeys, newKeys);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList minor(DiskMergeSortedArray s1, DiskMergeSortedArray s2) throws IOException, ClassNotFoundException {
        if(s1==null||s2==null)return null;
        ArrayList<Object> ret = new ArrayList<Object>();
        Comparable i= s1.pop();
        Comparable j= s2.pop();
        while(i!=null&&j!=null){
        	int compare = i.compareTo(j);
        	if(compare==0){
        		i = s1.pop();
        		j = s2.pop();
        	} else if (compare< 0){
        		//old i is not included in new j
        		ret.add(i);
        		i = s1.pop();
        	} else {
        		// new j is not in i, but that's ok
        		j = s2.pop();
        	}
        }
        //only add what's remained in the old list to the return results
        while(i!=null){
        	ret.add(i);
        	i = s1.pop();
        }
        return ret;
    }

}
