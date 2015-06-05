package net.javacoding.xsearch.search.spellcheck;

import java.util.Set;

public class PhraseExtractor {
    public static final String PHRASE_DELIMITER = "PRHASE_DELIMITER";

    private String text;
    private int textLength;
    private int    wordIndex;
    private int    wordStart;
    private Set<String> stopWords;
    
    public PhraseExtractor(Set<String> stopwords) {
        this.stopWords = stopwords;
    }

    public void setString(String t) {
        text = t;
        textLength = text==null? 0 : text.length();
        wordIndex = 0;
        wordStart = -1;
    }
    private boolean isPartOfWord(char x) {
        return Character.isLetterOrDigit(x)||x=='-'||x=='_';
    }
    private boolean isPhraseEnd(char x) {
        return !isPartOfWord(x)&&!Character.isWhitespace(x);
    }
    public boolean hasNext() {
        return wordIndex < textLength;
    }
    public String next() {
        String word = nextWord();
        if(word.length()<2) return "";
        if(word==PHRASE_DELIMITER) return "";
        StringBuilder sb = new StringBuilder(word);
        while((word=nextWord())!=PHRASE_DELIMITER) {
            if(word.length()<2) break;
            sb.append(" ").append(word);
        }
        return sb.toString();
    }
    public String nextWord() {
        char x = 0;
        while(wordIndex<textLength&&!isPartOfWord(x=text.charAt(wordIndex))) {
            wordIndex++;
            if(!Character.isWhitespace(x)) {
                return PHRASE_DELIMITER;
            }
        }
        if(wordIndex>=textLength-1) {
            wordIndex = textLength;
            return PHRASE_DELIMITER;
        }
        wordStart = wordIndex++;
        while(wordIndex<textLength&&isPartOfWord(text.charAt(wordIndex))) {
            wordIndex++;
        }
        String actualTerm = text.substring(wordStart, wordIndex);
        if(stopWords!=null&&stopWords.contains(actualTerm.toLowerCase())) {
            wordIndex++;
            return PHRASE_DELIMITER;
        }else {
            return actualTerm;
        }
    }
}
