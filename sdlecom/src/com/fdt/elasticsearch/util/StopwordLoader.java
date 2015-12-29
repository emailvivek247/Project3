package com.fdt.elasticsearch.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.javacoding.xsearch.foundation.WebserverStatic;

public class StopwordLoader {

    private static final String STOP_WORDS_LIST = "stopwords.txt";
    
    private static List<String> stopwordsList;
    private static Set<String> stopwordsSet;

    public static List<String> getStopwords() {
        if (stopwordsList == null) {
            loadStopwords();
        }
        return stopwordsList;
    }

    public static Set<String> getStopwordSet() {
        if (stopwordsSet == null) {
            loadStopwords();
        }
        return stopwordsSet;
    }

    private static void loadStopwords() {
        File stopwordsFile = getStopWordsListFile();
        try {
            stopwordsList = Files.lines(stopwordsFile.toPath())
                    .map(l -> l.trim())
                    .collect(Collectors.toList());
            stopwordsSet = new HashSet<>(stopwordsList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getStopWordsListFile() {
        return new File(WebserverStatic.getDictionaryDirectoryFile(), STOP_WORDS_LIST);
    }
}
