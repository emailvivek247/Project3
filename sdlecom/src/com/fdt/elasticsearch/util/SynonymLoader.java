package com.fdt.elasticsearch.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import net.javacoding.xsearch.foundation.WebserverStatic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SynonymLoader {

    private static final String SYNONYM_WORDS_LIST = "synonyms.txt";

    public static List<String> getSynonymsFromDictionaryFile() {
        File synonymDictionaryFile = getSynonymWordsListFile();
        List<String> result = null;
        try {
            result = Files.lines(synonymDictionaryFile.toPath())
                    .map(l -> l.trim())
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .map(l -> Joiner.on(", ").join(l.indexOf(",") > 0 ? l.split(",") : l.split("[, ]")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static List<String> getSynonymsFromSystemProperties() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("system");
        return resourceBundle.keySet().stream().map((key) -> {
            List<String> keyAndValues = Lists.asList(key, resourceBundle.getString(key).split(","));
            return Joiner.on(", ").join(keyAndValues);
        }).collect(Collectors.toList());
    }

    private static File getSynonymWordsListFile() {
        return new File(WebserverStatic.getDictionaryDirectoryFile(), SYNONYM_WORDS_LIST);
    }

}
