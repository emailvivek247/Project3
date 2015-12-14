package com.fdt.elasticsearch.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import net.javacoding.xsearch.foundation.WebserverStatic;

public class StopwordLoader {

    private static final String STOP_WORDS_LIST = "stopwords.txt";

    public static List<String> getStopwords() {
        File stopwordsFile = getStopWordsListFile();
        List<String> result = null;
        try {
            result = Files.lines(stopwordsFile.toPath()).map(l -> l.trim()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static File getStopWordsListFile() {
        return new File(WebserverStatic.getDictionaryDirectoryFile(), STOP_WORDS_LIST);
    }
}
