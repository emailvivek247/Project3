package net.javacoding.xsearch.search.spellcheck2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Spelling {

    private final Map<String, Integer> nWords = new HashMap<String, Integer>();

    public Spelling(String file) throws IOException {
    }

    private final List<String> edits1(String word) {
        List<String> result = new ArrayList<String>();
        //deletion: delete one character
        for(int i=0; i < word.length(); ++i) {
            result.add(word.substring(0, i) + word.substring(i+1));
        }
        //transposition: switch 2 adjacent characters
        for(int i=0; i < word.length()-1; ++i) {
            result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
        }
        //alteration: change one character
        for(int i=0; i < word.length(); ++i) {
            for(char c='a'; c <= 'z'; ++c) {
                result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
            }
        }
        //insertion: add one character
        for(int i=0; i <= word.length(); ++i) {
            for(char c='a'; c <= 'z'; ++c) {
                result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
            }
        }
        return result;
    }

    public final String correct(String word) {
        if(word==null) return null;
        word = word.toLowerCase().intern();
        if(nWords.containsKey(word)) return word;

        int c,c_count=0;
        String c_value = word;
        List<String> list = edits1(word);
        for(String w : list) {
            if(nWords.containsKey(w)) {
                if((c = nWords.get(w))>c_count) {
                    c_count = c;
                    c_value = w;
                }
            }
        }
        if(c_count>0) return c_value;
        for(String s : list) {
            for(String w : edits1(s)) {
                if(nWords.containsKey(w)) {
                    if((c = nWords.get(w))>c_count) {
                        c_count = c;
                        c_value = w;
                    }
                }
            }
        }
        return c_value;
    }

    public static void main(String args[]) throws IOException {
    }

}