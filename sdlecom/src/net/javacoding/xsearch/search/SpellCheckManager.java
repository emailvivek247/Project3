/*
 * Created on Apr 6, 2005
 */
package net.javacoding.xsearch.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.search.analysis.QueryAnalysis;
import net.javacoding.xsearch.search.query.DbsClause;
import net.javacoding.xsearch.search.query.DbsQuery;
import net.javacoding.xsearch.search.spellcheck.DbsIndexPhraseDictionary;
import net.javacoding.xsearch.search.spellcheck.DbsIndexWordDictionary;
import net.javacoding.xsearch.search.spellcheck.PhraseSuggester;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.status.KeyWord;
import net.javacoding.xsearch.status.QueryLogAnalyzer;
import net.javacoding.xsearch.status.QueryReport;
import net.javacoding.xsearch.utility.AnalyzerUtil;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;

import com.fdt.sdl.admin.ui.action.constants.IndexType;

/**
 * Use lucene spell checker to check query
 */
public class SpellCheckManager {
    public static final String                     READY_FILE                     = "ready";
    private static Logger                          logger                         = LoggerFactory.getLogger(SpellCheckManager.class.getName());
    private static Hashtable<String, SpellChecker> spellCheckers                  = new Hashtable<String, SpellChecker>();
    private static Hashtable<String, PhraseSuggester> phraseSuggesters            = new Hashtable<String, PhraseSuggester>();

    static String                                  sep                            = WebserverStatic.sep;

    static String                                  DEFAULT_SPELL_CHECK_DICTIONARY = "spell_check";

    /**
     * check spelling error and return the suggestion for a query. Used in
     * rendering templates.
     * 
     * @param dc
     *            dataset config
     * @param query
     *            parsed query object
     * @param queryText
     *            the query string
     * @return String[] result of spellchecking if there is some spelling error
     *         result[0]: regular format result[1]: html format
     */
    public static ArrayList<String> checkSpellHtml(DatasetConfiguration dc, DbsQuery query, String queryText, String prefix, String suffix) {
        // logger.info("input="+query);
        ArrayList<String> suggestions = getQuerySuggestions(dc, query);
        if (suggestions.size() <= 0)
            return null;
        ArrayList<String> list = new ArrayList<String>();
        String sug1 = formatSuggestion(queryText, suggestions, false, prefix, suffix);
        list.add(sug1);
        list.add(formatSuggestion(queryText, suggestions, true, prefix, suffix));
        // logger.debug("spellchecker suggest=" + sug1);
        return list;
    }

    public static SpellChecker getSpellChecker(DatasetConfiguration dc, String fieldName) {
        //for now, do not handle field specific searches
        if(fieldName!=null) return null;
        SpellChecker s = spellCheckers.get(dc.getName());
        return s;
    }
    public static PhraseSuggester getPhraseSuggester(DatasetConfiguration dc, String fieldName) {
        //for now, do not handle field specific searches
        if(fieldName!=null) return null;
        PhraseSuggester s = phraseSuggesters.get(dc.getName());
        return s;
    }

    public static void start(DatasetConfiguration dc) {
        startSpellCheker(dc);
        startPhraseSuggester(dc);
    }
    public static void startSpellCheker(DatasetConfiguration dc) {
        try {
            SpellChecker s = spellCheckers.get(dc.getName());
            if (s == null) {
                if (dc.getIsSpellChecking()) {
                    s = new SpellChecker(FSDirectory.getDirectory(dc.getDictionaryIndexDirectoryFile()));
                    spellCheckers.put(dc.getName(), s);
                } else {
                    if (isDefaultDictionaryReady()) {
                        File dicDir = new File(WebserverStatic.getDictionaryDirectoryFile(), DEFAULT_SPELL_CHECK_DICTIONARY);
                        FSDirectory fs = FSDirectory.getDirectory(dicDir);
                        s = new SpellChecker(fs);
                        spellCheckers.put(dc.getName(), s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void startPhraseSuggester(DatasetConfiguration dc) {
        try {
            PhraseSuggester s = phraseSuggesters.get(dc.getName());
            if (s == null) {
                if (dc.getIsSpellChecking()) {
                    s = new PhraseSuggester(FSDirectory.getDirectory(dc.getPhraseIndexDirectoryFile()));
                    phraseSuggesters.put(dc.getName(), s);
                }
            }else {
                s.close();
                s.open();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopSpellChecker(DatasetConfiguration dc) {
        spellCheckers.remove(dc.getName());
    }
    public static void stopPhraseSuggester(DatasetConfiguration dc) {
        PhraseSuggester s = phraseSuggesters.remove(dc.getName());
        if(s!=null) {
            s.close();
        }
    }

    /**
     * a simple version of checkSpellHtml, only return regular format
     * 
     * @param dc
     * @param query
     * @param queryText
     * @return
     */
    public static String checkSpell(DatasetConfiguration dc, DbsQuery query, String queryText) {
        // logger.info("input="+query);
        ArrayList<String> errors = getQuerySuggestions(dc, query);
        if (errors.size() <= 0)
            return null;
        String sug = formatSuggestion(queryText, errors, false, null, null);
        // logger.info("suggest="+sug);
        return sug;
    }

    private static ArrayList<String> getQuerySuggestions(DatasetConfiguration dc, DbsQuery query) {
        ArrayList<String> suggestions = new ArrayList<String>();
        if (query == null)
            return suggestions;
        DbsClause[] clauses = query.getClauses();
        if (clauses != null)
            //short circuit the suggestion if there is field included. This may need further improvements
            for (DbsClause clause : clauses) {
                if (clause.getField()!=DbsClause.DEFAULT_FIELD) {
                    return suggestions;
                }
            }
            for (DbsClause clause : clauses) {
                if (!clause.isPhrase()) {
                    String term = clause.getTerm().toString();
                    String check = errorCheck(dc, clause);
                    if (check != null) {
                        try {
                            org.apache.lucene.analysis.Token[] t = AnalyzerUtil.tokensFromAnalysis(dc.getAnalyzer(), term);
                            org.apache.lucene.analysis.Token[] c = AnalyzerUtil.tokensFromAnalysis(dc.getAnalyzer(), check);
                            if (t == null || c == null || t.length < 1 || c.length < 1)
                                continue;
                            if (t[0].termText().equals(c[0].termText())) {
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        suggestions.add(term);
                        suggestions.add(check);
                    }
                }
            }
        return suggestions;
    }

    static private String errorCheck(DatasetConfiguration dc, DbsClause clause) {
        String input = clause.getTerm().toString();
        // we need some rules for spell checking
        // 1. if string to short return
        // 2. if not all a-z, return
        if (input == null || input.length() < 4)
            return null;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) return null;
            if (!(c >= 'a' && c <= 'z'))
                return null;
        }
        String fieldName = clause.getField()==DbsClause.DEFAULT_FIELD ? null : clause.getField();
        return suggest(dc, fieldName, input);
    }

    /**
     * format the suggestion string to display, highlight the error part
     * 
     * @param inputQuery
     *            HTML escaped
     * @param suggestions
     *            arraylist of Strings, like {"error1","correct1","error2",
     *            "correct2" ...} *
     * @return display String for suggestion
     */
    static private String formatSuggestion(String inputQuery, ArrayList<String> suggestions, boolean isHtml, String prefix, String suffix) {
        if (inputQuery == null || suggestions == null || suggestions.size() == 0)
            return null;
        String output = inputQuery;
        for (int i = 0; i < suggestions.size(); i++) {
            String error = suggestions.get(i);
            String correct = suggestions.get(i + 1);
            i++;
            output = replaceError(output, error, correct, isHtml, prefix, suffix);
        }
        return output;
    }

    static private String replaceError(String input, String error, String correct, boolean isHtml, String prefix, String suffix) {
        String inputLow = input.toLowerCase();
        int index = inputLow.indexOf(error.toLowerCase());
        if (index == -1)
            return input;
        // todo:check exact match
        StringBuffer sb = new StringBuffer();
        if (index != 0)
            sb.append(input.substring(0, index));
        if (isHtml)
            sb.append(prefix);
        sb.append(correct);
        if (isHtml)
            sb.append(suffix);
        sb.append(replaceError(input.substring(index + error.length()), error, correct, isHtml, prefix, suffix));
        return sb.toString();
    }

    public static void maybeBuildIndex(DatasetConfiguration dc) throws Throwable {
        ArrayList<Column> columns = dc.getColumns(true);
        boolean needSpellChecking = false;
        for (Column column : columns) {
            if (column.getIsSpellChecking()) {
                needSpellChecking = true;
                break;
            }
        }
        if (!needSpellChecking)
            return;
        logger.debug("Need Spell Checking");
        File dictionaryDirFile = dc.getDictionaryIndexDirectoryFile();
        File phraseDirFile = dc.getPhraseIndexDirectoryFile();
        if (!dictionaryDirFile.exists()||!phraseDirFile.exists()) {
            dictionaryDirFile.mkdirs();
            phraseDirFile.mkdirs();
            reBuildIndex(dc);
        }
    }

    public static void reBuildIndex(DatasetConfiguration dc) throws Exception {
        if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
            FileUtil.deleteAllFiles(dc.getDictionaryIndexDirectoryFile());
            FileUtil.deleteAllFiles(dc.getPhraseIndexDirectoryFile());
            doBuildIndex(dc);
        }
    }

    private static void doBuildIndex(DatasetConfiguration dc) throws Exception {
        IndexReader indexReader = IndexStatus.openIndexReader(dc);
        logger.info("Opened index " + dc.getName());

        List<Column> columns = dc.getColumns(true);
        List<Column> spellCheckingColumns = new ArrayList<Column>();
        for (Column c : columns) {
            if (c.getIsSpellChecking()) {
                logger.info("Building Directory For Column " + c.getColumnName() + "...");
                spellCheckingColumns.add(c);
            }
        }

        /** This Has been Commented Right Now to be ReVisited Later **/
        //SpellChecker spellChecker = new SpellChecker(FSDirectory.getDirectory(dc.getDictionaryIndexDirectoryFile()));
        //spellChecker.indexDictionary(new DbsIndexWordDictionary(dc, indexReader, spellCheckingColumns));

        PhraseSuggester phraseSuggester = new PhraseSuggester(FSDirectory.getDirectory(dc.getPhraseIndexDirectoryFile()));
        phraseSuggester.indexDictionary(new DbsIndexPhraseDictionary(dc, indexReader, spellCheckingColumns));

        IndexStatus.setIndexReady(dc.getDictionaryIndexDirectoryFile());
        logger.info("Complete Directory For " + dc.getName());
    }

    /**
     * build standard default dictionary from DEFAULT_SPELL_CHECK_DICTIONARY.txt
     * file
     */
    public static void buildDefaultIndex() {
        try {
            File dicFile = new File(WebserverStatic.getDictionaryDirectoryFile(), DEFAULT_SPELL_CHECK_DICTIONARY + ".txt");
            if (!dicFile.exists()) {
                logger.info("ERROR: Dictionary doesn't exist -" + WebserverStatic.getDictionaryDirectoryFile() + sep + DEFAULT_SPELL_CHECK_DICTIONARY + ".txt");
                return;
            }
            if (!isDefaultDictionaryReady() || isDefaultDictionaryOld()) {
                File dicDir = new File(WebserverStatic.getDictionaryDirectoryFile(), DEFAULT_SPELL_CHECK_DICTIONARY);
                FSDirectory fs = FSDirectory.getDirectory(dicDir);
                SpellChecker s = new SpellChecker(fs);
                PlainTextDictionary dic = new PlainTextDictionary(dicFile);
                s.indexDictionary(dic);
                IndexStatus.setIndexReady(FileUtil.resolveFile(WebserverStatic.getDictionaryDirectoryFile(), DEFAULT_SPELL_CHECK_DICTIONARY));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public static boolean isDefaultDictionaryReady() {
        File readyFile = FileUtil.resolveFile(WebserverStatic.getDictionaryDirectoryFile(), DEFAULT_SPELL_CHECK_DICTIONARY,READY_FILE);
        return readyFile.exists();
    }

    private static boolean isDefaultDictionaryOld() {
        File readyFile = FileUtil.resolveFile(WebserverStatic.getDictionaryDirectoryFile(), DEFAULT_SPELL_CHECK_DICTIONARY,READY_FILE);
        File dicFile = new File(WebserverStatic.getDictionaryDirectoryFile(), DEFAULT_SPELL_CHECK_DICTIONARY + ".txt");
        long dictionaryTime = readyFile.lastModified();
        long dicFileTime = dicFile.lastModified();
        return (dicFileTime > dictionaryTime);
    }

    private static float DEFAULT_MIN = 0.5f;

    private static String suggest(DatasetConfiguration dc, String fieldName, String input) {
        try {
            SpellChecker checker = getSpellChecker(dc, fieldName);
            if (checker == null) {
                // buildIndex(dicName);
                return null;
            }
            checker.setAccuracy(DEFAULT_MIN);
            String[] suggestion = checker.suggestSimilar(input, 1);
            if (suggestion == null || suggestion.length < 1)
                return null;
            String s = suggestion[0];
            if (s.equalsIgnoreCase(input))
                return null;
            return suggestion[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void warmup(DatasetConfiguration dc) {
        try {
            DbsQuery query = null;
            QueryReport report = QueryLogAnalyzer.getReport(dc.getName(), 0);
            KeyWord[] keywords = report.getTopKeyWords();
            String q = null;
            for (int k = 0; keywords != null && k < keywords.length; k++) {
                q = keywords[k].getKey();
                if (!U.isEmpty(q)) {
                    query = QueryAnalysis.parseQuery(q, dc);
                    checkSpell(dc, query, q);
                    break;
                }
            }
        } catch (Exception e) {
            logger.info("Failed to add searcher pools: " + e.toString());
            e.printStackTrace();
        }
    }
}
