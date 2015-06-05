package net.javacoding.xsearch.indexer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.javacoding.xsearch.search.synonym.SynonymManager;
import net.javacoding.xsearch.utility.EscapeChars;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.index.ReusableStringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservedWordsFilter extends TokenFilter {

    private static final Logger logger = LoggerFactory.getLogger(ReservedWordsFilter.class);
    //if p is null, means no reserved words file or no reserved words
    public static Pattern p = null;
    Matcher m = null;
    Reader reader = null;
    private Queue<Token> reservedTokenQueue;
    private Token lastToken = null;

    static{
    	Set<String> reservedWords = null;
    	File f = SynonymManager.getReservedWordsListFile();
        try {
        	if(f.exists()){
            	reservedWords = WordlistLoader.getWordSet(SynonymManager.getReservedWordsListFile());
            	if(reservedWords.size()>0){
                    setReservedWords(reservedWords);
            	}
        	}
        } catch (IOException e) {
        	logger.warn("Failed to load stopwords file:"+SynonymManager.getReservedWordsListFile());
        }
    }
    public ReservedWordsFilter(TokenStream input, Reader reader) {
        super(input);
        if(p!=null){
        	if(reader instanceof ReusableStringReader){
        		this.reader = reader;
        	}else if(reader instanceof StringReader){
        		this.reader = reader;
        	}
            reservedTokenQueue = new LinkedList<Token>();
        }
    }

    public static void setReservedWords(Set<String> wordSet){
        String[] words = new String[wordSet.size()];
        words = wordSet.toArray(words);
        
        //longest string as first pattern
        Arrays.sort(words,new Comparator<String>(){
            public int compare(String a, String b) {
                if(a==null) return 1;
                if(b==null) return -1;
                if(a.length()>b.length()) return -1;
                if(a.length()<b.length()) return 1;
                return a.compareTo(b);
            }
        });
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(int i=0;i<words.length;i++){
            if(i!=0){
                sb.append("|");
            }
            sb.append(EscapeChars.forRegex(words[i]));
        }
        sb.append(")\\W?");
        
        p = Pattern.compile(sb.toString(),Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Return tokens as normal, but at the end, use the pattern to scan the whole string for reserved words.
     */
    public Token next(Token result) throws IOException {
    	//initialize reservedTokenQueue for the first next() call
        if(m == null && p!=null && reader!=null){
        	if(reader instanceof ReusableStringReader){
                m = p.matcher(((ReusableStringReader) reader).s);
                while(m.find()){
            		Token token = new Token(m.group(1).toLowerCase().intern(), m.start(1), m.end(1));
            		token.setPositionIncrement(0);
            		reservedTokenQueue.add(token);
                }
        	}else if(reader instanceof StringReader){
        		char[] buf = new char[512];
        		try {
        			int base = 0;
        			int length_read = 0;
					while((length_read=((StringReader) reader).read(buf, 0, 512))>0){
			            m = p.matcher(new String(buf,0,length_read));
			            while(m.find()){
			        		Token token = new Token(m.group(1).toLowerCase().intern(), base+m.start(1), base+m.end(1));
			        		token.setPositionIncrement(0);
			        		reservedTokenQueue.add(token);
			            }
			            base+=length_read;
					}
	        		((StringReader) reader).reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }

        //get actual token either from stream or from last actual token(that's replaced by a reservedToken)
        Token t = null;
        if(lastToken != null){
        	t = lastToken;
        	lastToken = null;
        }else{
            t = input.next(result);
        }

        //compare reservedToken with actual token
        if (reservedTokenQueue!=null && reservedTokenQueue.size() > 0) {
            if(t==null){
                return reservedTokenQueue.remove();
                }
        	Token reservedToken = reservedTokenQueue.peek();
        	if(reservedToken.startOffset()<t.startOffset()){
        		//use the reservedToken
        		lastToken = t;
        		return reservedTokenQueue.remove();
        	}else if(reservedToken.startOffset()==t.startOffset()){
        		return reservedTokenQueue.remove();
            }else{
        		//use the actual token
            	return t;
            }
        }else{
        	return t;
        }
        
    }

}
