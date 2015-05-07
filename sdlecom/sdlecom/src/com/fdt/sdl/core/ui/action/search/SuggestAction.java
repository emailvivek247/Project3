package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.SpellCheckManager;
import net.javacoding.xsearch.search.spellcheck.DbsIndexPhraseDictionary;
import net.javacoding.xsearch.search.spellcheck.PhraseSuggester;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.common.collect.Lists;

/**
 * Implementation of <strong>Action </strong> that performs suggestion.
 */

public class SuggestAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.action.SuggestAction");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        try {
            String indexName = request.getParameter("indexName");
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            int length = U.getInt(request.getParameter("length"), 20);
            
            String input = request.getParameter("q");
            List<String> suggestions = new ArrayList<String>(length);
            if(input!=null) {
                input = input.toLowerCase();
                if(input.indexOf(' ')>0) {
                    List<String>words = Lists.newArrayList(input.split(" "));
                    boolean hasTrailingSpace = input.lastIndexOf(' ')==input.length()-1;
                    PhraseSuggester s = SpellCheckManager.getPhraseSuggester(dc, null);
                    if(!hasTrailingSpace) {
                        addSuggestions(suggestions, s, words.subList(0, words.size()-1),words.get(words.size()-1),length);
                    }else {
                        addSuggestions(suggestions, s, words,null,length);
                    }
                }else {
                    input = input.trim();//removing leading empty spaces
                    if(input.length()>0) {
                        PhraseSuggester s = SpellCheckManager.getPhraseSuggester(dc, null);
                        if(s==null||s.searcher==null) return null;
                        TermEnum termEnum = s.reader.terms(new Term(PhraseSuggester.FIELD_WORD,input));
                        try {
                            do {
                                Term t = termEnum.term();
                                if (t!=null && !t.text().startsWith(input)) break;
                                if (t != null) {
                                    suggestions.add(t.text());
                                }
                            } while (termEnum.next()&&suggestions.size()<length);
                        } finally {
                            if(termEnum!=null)termEnum.close();
                        }
                    }
                }
                request.setAttribute("suggestions", suggestions);
            }else {
                return null;
            }

            return (mapping.findForward("continue"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } catch (NullPointerException se) {
            se.printStackTrace();
            return null;
        } catch (Throwable t) {
            t.printStackTrace();
            request.setAttribute("errors",t);
            return null;
        }
    }
    
    private List<String> addSuggestions(List<String> suggestions, PhraseSuggester s, List<String> words, String partialWord, int length) throws IOException{
        if(s==null || s.searcher==null) return suggestions;
        Set<Integer> docs = new HashSet<Integer>();
        if(words!=null) {
            BooleanQuery query = new BooleanQuery();
            for(String word : words) {
                if(word!=null&&word!=""&&!DbsIndexPhraseDictionary.isStopword(word)) {
                    query.add(new TermQuery(new Term(PhraseSuggester.FIELD_WORD,word)), Occur.MUST);
                }
            }
            if(query.clauses().size()>0) {
                TopDocs topDocs = s.searcher.search(query,3*length);
                for(ScoreDoc d:topDocs.scoreDocs) {
                    docs.add(d.doc);
                }
            }
        }
        if(partialWord!=null) {
            partialWord = partialWord.toLowerCase();
            Set<Integer> filteredDocs = new HashSet<Integer>();
            TermDocs termDocs = s.reader.termDocs();
            TermEnum termEnum = s.reader.terms (new Term(PhraseSuggester.FIELD_WORD,partialWord));
            try {
                do {
                    Term term = termEnum.term();
                    if (term == null || term.field() != PhraseSuggester.FIELD_WORD || !term.text().startsWith(partialWord) ) break;
                    termDocs.seek(termEnum);
                    while (termDocs.next()) {
                        if (docs.contains(termDocs.doc())) {
                            filteredDocs.add(termDocs.doc());
                        }
                    }
                } while (termEnum.next());
            } finally {
                termDocs.close();
                termEnum.close();
            }
            docs = filteredDocs;
        }

        for(int d: docs) {
            suggestions.add(s.searcher.doc(d).get(PhraseSuggester.FIELD_PHRASE));
        }
        return suggestions;
    }
}
