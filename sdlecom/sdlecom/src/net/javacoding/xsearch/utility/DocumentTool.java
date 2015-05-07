package net.javacoding.xsearch.utility;

import java.util.ArrayList;

import net.javacoding.xsearch.search.HitDocument;

public class DocumentTool {

    /**
     * Connect docs that have the same value by doc.get(p)
     * So that on the velocity page, the docs can be easily merged
     * @param docs
     * @param p
     * @return docs of the original length
     */
    public ArrayList<HitDocument> groupBy(ArrayList<HitDocument> docs, String p) {
        if(docs==null) return null;
        ArrayList<String> pList = new ArrayList<String>();
        for(HitDocument d : docs) {
            if(!pList.contains(d.get(p))) {
                pList.add(d.get(p));
            }
        }
        ArrayList<HitDocument> ret = new ArrayList<HitDocument>();
        for(String pValue : pList) {
            for(HitDocument d : docs) {
                if(d==null) continue;
                if(pValue.equals(d.get(p))) {
                    ret.add(d);
                }
            }
        }
        return ret;
    }

}
