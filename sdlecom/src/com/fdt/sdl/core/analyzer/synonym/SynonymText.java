package com.fdt.sdl.core.analyzer.synonym;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SynonymText {

    public String[] getSynonyms(String key) {
        String[] str = PageStyleUtil.getSystemValues("system", key);
        return str;
    }

    public static List<String> getSynonyms() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("system");
        return resourceBundle.keySet().stream().map((key) -> {
            List<String> keyAndValues = Lists.asList(key, resourceBundle.getString(key).split(","));
            return Joiner.on(", ").join(keyAndValues);
        }).collect(Collectors.toList());
    }
}